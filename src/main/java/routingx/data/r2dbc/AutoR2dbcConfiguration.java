package routingx.data.r2dbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.DefaultReactiveDataAccessStrategy;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.util.Assert;

import io.r2dbc.spi.ConnectionFactory;
import routingx.dao.SimpleRepositoryFactory;
import routingx.dao.UniversalDao;
import routingx.dao.UniversalDaoImpl;
import routingx.manager.UniversalManager;
import routingx.manager.UniversalManargeImpl;
import routingx.service.UniversalService;
import routingx.service.UniversalServiceImpl;

/**
 * 
 * @author peishaoguo
 */
//@Import(value = { RoutingConnectionFactory.class })
@Configuration
public class AutoR2dbcConfiguration extends AbstractR2dbcConfiguration {

	@Bean(destroyMethod = "close")
	@ConditionalOnMissingBean(ConnectionFactory.class)
	public RoutingConnectionFactory connectionFactory() {
		return new RoutingConnectionFactory();
	}

	@Bean
	@Primary
	public ReactiveTransactionManager reactiveTransactionManager(
			@Autowired RoutingConnectionFactory connectionFactory) {
		return new R2dbcTransactionManager(connectionFactory);
	}

	@Bean
	public DefaultReactiveDataAccessStrategy reactiveDataAccessStrategy(ConnectionFactory connectionFactory,
			R2dbcConverter converter) {
		Assert.notNull(converter, "MappingContext must not be null!");
		return new DefaultReactiveDataAccessStrategy(getDialect(connectionFactory), converter);
	}

	@Bean
	@ConditionalOnMissingBean(SimpleRepositoryFactory.class)
	public SimpleRepositoryFactory simpleRepositoryFactory(R2dbcEntityTemplate entityTemplate) {
		return new SimpleRepositoryFactoryImpl(entityTemplate);
	}

	@Bean
	@ConditionalOnMissingBean(UniversalDao.class)
	public UniversalDao universalDao(@Autowired SimpleRepositoryFactory repositoryFactory) {
		return new UniversalDaoImpl(repositoryFactory);
	}

	@Bean
	@ConditionalOnMissingBean(UniversalService.class)
	public UniversalService universalService(@Autowired UniversalDao universalDao) {
		return new UniversalServiceImpl(universalDao);
	}

	@Bean
	@Primary
	@ConditionalOnMissingBean(UniversalManager.class)
	public UniversalManager universalManager(@Autowired UniversalService universalService) {
		return new UniversalManargeImpl(universalService);
	}
}
