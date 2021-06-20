package routingx.data.r2dbc;

import static io.r2dbc.spi.ConnectionFactoryOptions.CONNECT_TIMEOUT;
import static io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD;
import static io.r2dbc.spi.ConnectionFactoryOptions.USER;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import routingx.CustomException;
import routingx.config.ConfigManager;
import routingx.json.JSON;

/**
 * 分表分库，读写分离
 * 
 * @author Administrator
 *
 */
@Slf4j
public class RoutingConnectionFactory
		extends org.springframework.r2dbc.connection.lookup.AbstractRoutingConnectionFactory {

	private final static String LOOKUP_KEY = RoutingConnectionFactory.class.getName();
	private final static String SIGNATURE_KEY = RoutingConnectionFactory.class.getName() + "_SIGNATURE";
	private final static String DATAID = "datasource";
	private final Map<Object, ConnectionPool> factories = new LinkedHashMap<>();

	private PathMatcher pathMatcher = new AntPathMatcher();

	@Autowired
	private ConfigManager configManager;

	/**
	 * 数据源配置
	 */
	@Autowired
	private RoutingConfig routingConfig;

	private Object defaultLookupKey;

	public RoutingConnectionFactory() {
		log.info(this.toString());
	}

	public void setPathMatcher(PathMatcher pathMatcher) {
		this.pathMatcher = pathMatcher;
	}

	public RoutingProperties[] getProperties() {
		return routingConfig.getProperties();
	}

	public void setProperties(RoutingProperties[] properties) {
		routingConfig.setProperties(properties);
	}

	@PreDestroy
	public void close() {
		factories.forEach((key, value) -> {
			log.info("close {}", value.toString());
			value.close();
		});
		factories.clear();
	}

	/**
	 * 动态数据源
	 */
	@Override
	public void afterPropertiesSet() {
		configManager.register(DATAID, config -> afterPropertiesSet(config));
	}

	private void afterPropertiesSet(String config) {
		if (config != null) {
			List<RoutingProperties> pList = JSON.parseList(config, RoutingProperties.class);
			if (pList != null && pList.size() > 0) {
				setProperties(pList.toArray(new RoutingProperties[pList.size()]));
			}
		}
		final RoutingProperties[] properties = getProperties();
		if (properties == null || properties.length <= 0) {
			throw new RuntimeException("properties must not be empty");
		}
		List<ConnectionPool> connectionPools = new ArrayList<>();
		connectionPools.addAll(factories.values());
		for (RoutingProperties prop : properties) {
			factories.put(prop.lookupKey(), afterPropertiesSetConnection(prop));
		}
		if (factories.size() <= 0) {
			throw new IllegalArgumentException("factories must not be empty");
		}
		defaultLookupKey = properties[0].lookupKey();// 第一个配置必须可读写
		ConnectionFactory defaultTarget = factories.get(defaultLookupKey);
		setTargetConnectionFactories(factories);
		setDefaultTargetConnectionFactory(defaultTarget);
		super.afterPropertiesSet();
		connectionPools.forEach(pool -> pool.close());
		connectionPools.clear();
	}

	/**
	 * 初始化数据源
	 * 
	 * @param prop
	 * @return
	 */
	private ConnectionPool afterPropertiesSetConnection(RoutingProperties prop) {
		ConnectionFactoryOptions options = ConnectionFactoryOptions.parse(prop.getUrl())//
				.mutate()//
				.option(USER, prop.getUsername())//
				.option(PASSWORD, prop.getPassword())//
				.option(CONNECT_TIMEOUT, Duration.ofSeconds(5))//
				.build();
		ConnectionFactory connectionFactory = ConnectionFactories.get(options);
		if (prop.getPool() == null) {
			prop.setPool(new RoutingPool());
		}
		RoutingPool pool = prop.getPool();
		if (pool.getMaxSize() < 1) {
			pool.setMaxSize(50);
		}
		if (pool.getInitialSize() < 1) {
			pool.setInitialSize(10);
		}
		if (StringUtils.isNotBlank(pool.getValidationQuery())) {
			pool.setValidationQuery("SELECT 1");
		}
		if (pool.getMaxIdleTime() < 1) {
			pool.setMaxIdleTime(Duration.ofMinutes(30).toSeconds());
		}
		ConnectionPoolConfiguration.Builder builder = ConnectionPoolConfiguration.builder(connectionFactory);
		builder.initialSize(pool.getInitialSize());
		builder.maxSize(pool.getMaxSize());
		builder.maxIdleTime(Duration.ofSeconds(pool.getMaxIdleTime()));
		// builder.maxLifeTime(Duration.ofSeconds(300));
		builder.acquireRetry(pool.getAcquireRetry());
		builder.maxAcquireTime(Duration.ofSeconds(pool.getMaxAcquireTime()));
		builder.maxCreateConnectionTime(Duration.ofSeconds(pool.getMaxCreateConnectionTime()));
		builder.validationQuery(pool.getValidationQuery());
		builder.name(prop.getId() + "-" + prop.getGroup());
		try {
			builder.registerJmx(true);
			ConnectionPool connectionPool = new ConnectionPool(builder.build());
			return connectionPool;
		} catch (Exception ex) {
			log.error("", ex);
			builder.registerJmx(false);
			ConnectionPool connectionPool = new ConnectionPool(builder.build());
			return connectionPool;
		}
	}

	/**
	 * 多数据源读写分离路由处理
	 * 
	 * @param point
	 * @return
	 * @throws Throwable
	 */
	public Object around(ProceedingJoinPoint point) throws Throwable {
		MethodSignature signature = (MethodSignature) point.getSignature();
		final String methodName = signature.getName();
		Object currentLookupKey;
		try {
			currentLookupKey = aroundCurrentLookupKey(methodName);
			String signatureName = signature.toShortString();
			if (log.isDebugEnabled()) {
				log.debug("{} {} {}", currentLookupKey, signatureName, argsClx(point));
			}
			Method method = signature.getMethod();
			if (method.getReturnType().equals(Mono.class)) {
				return ((Mono<?>) point.proceed())
						.contextWrite(Context.of(LOOKUP_KEY, currentLookupKey).put(SIGNATURE_KEY, signatureName));
			} else if (method.getReturnType().equals(Flux.class)) {
				return ((Flux<?>) point.proceed())
						.contextWrite(Context.of(LOOKUP_KEY, currentLookupKey).put(SIGNATURE_KEY, signatureName));
			} else {
				throw CustomException.bq("must return Mono or Flux");
				// return point.proceed();
			}
		} catch (Throwable ex) {
			log.error("{}.{} {}", signature.getDeclaringType().getSimpleName(), methodName, argsClx(point));
			log.error("", ex);
			throw ex;
		}
	}

	private String argsClx(ProceedingJoinPoint point) {
		Object[] args = point.getArgs();
		String argsClx = "";
		if (args != null) {
			for (Object arg : args) {
				if (StringUtils.isNotBlank(argsClx)) {
					argsClx += " ,";
				}
				if (arg == null) {
					argsClx += "null";
				} else if (arg instanceof Class<?>) {
					argsClx += ((Class<?>) arg).getSimpleName();
				} else {
					argsClx += arg.getClass().getSimpleName();
				}
			}
		}
		return argsClx;
	}

	private Object aroundCurrentLookupKey(final String methodName) {
		List<RoutingProperties> list = Arrays.asList(routingConfig.getProperties());
		try {
			Collections.shuffle(list);
			for (RoutingProperties prop : list) {
				if (prop.getPatterns() != null) {
					for (String pattern : prop.getPatterns()) {
						if (pathMatcher.match(pattern, methodName)) {
							return prop.lookupKey();
						}
					}
				}
			}
			return defaultLookupKey;
		} finally {
			list = null;
		}
	}

	@Override
	protected Mono<Object> determineCurrentLookupKey() {
		return Mono.deferContextual(Mono::just).handle((context, sink) -> {
			if (context.hasKey(LOOKUP_KEY)) {
				Object currentLookupKey = context.get(LOOKUP_KEY);
				Object signatureName = context.get(SIGNATURE_KEY);
				if (log.isDebugEnabled()) {
					log.debug("{} {}", currentLookupKey, signatureName);
				}
				sink.next(currentLookupKey);
			} else {
				if (log.isDebugEnabled()) {
					log.warn("defaultLookupKey {}", defaultLookupKey);
				}
				sink.next(defaultLookupKey);
			}
		});
	}

}
