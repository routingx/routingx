package routingx.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Mono;
import routingx.Response;
import routingx.ThreadExecutor;
import routingx.model.Page;
import routingx.webflux.WebAbsContext;

@Transactional(readOnly = true)
class NativeDaoImpl extends WebAbsContext implements NativeDao, ApplicationContextAware, Ordered {

	private SimpleRepositoryFactory factory;
	private ApplicationContext applicationContext;

	protected SimpleRepositoryFactory factory() {
		if (factory == null) {
			while (applicationContext == null) {
				ThreadExecutor.sleep(1000);
			}
			factory = applicationContext.getBean(SimpleRepositoryFactory.class);
		}
		return factory;
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		factory();
	}

	protected void setFactory(SimpleRepositoryFactory factory) {
		this.factory = factory;
	}

	protected SimpleRepository<?, Serializable> getRepository() {
		return factory().get();
	}

	protected <R> Mono<List<R>> nativeList(String sql, Map<String, Object> params, Class<R> resultType) {
		return getRepository().nativeList(sql, params, resultType);
	}

	protected <R> Mono<R> nativeOne(String sql, Map<String, Object> params, Class<R> resultType) {
		return getRepository().nativeOne(sql, params, resultType);
	}

	protected Mono<Long> nativeCount(String sql, Map<String, Object> params) {
		return getRepository().nativeCount(sql, params);
	}

	protected <R> Mono<Response<List<R>>> nativePage(String sql, Page page, Class<R> resultType) {
		return getRepository().nativePage(sql, page, resultType);
	}

	@Transactional
	protected Mono<Integer> nativeExe(String sql, Map<String, Object> params) {
		return getRepository().execute(sql, params);
	}

}
