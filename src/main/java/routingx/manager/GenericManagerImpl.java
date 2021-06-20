package routingx.manager;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import routingx.CustomException;
import routingx.Response;
import routingx.ThreadExecutor;
import routingx.model.Page;
import routingx.service.GenericService;
import routingx.service.UniversalAbsService;
import routingx.service.UniversalService;
import routingx.utils.GenericUtils;
import routingx.webflux.ResponseMono;

@Slf4j
public class GenericManagerImpl<T, ID extends Serializable> extends UniversalAccessImpl
		implements GenericManager<T, ID>, ApplicationListener<ContextRefreshedEvent>, Ordered {

	private final Class<T> entityClass;
	private UniversalAbsService access;
	private ApplicationContext applicationContext;

	public GenericManagerImpl(Class<T> entityClass) {
		this.entityClass = entityClass;
	}

	@SuppressWarnings("unchecked")
	protected GenericManagerImpl() {
		entityClass = (Class<T>) GenericUtils.getParameterizedType(this.getClass());
	}

	@SuppressWarnings("unchecked")
	protected GenericManagerImpl(GenericService<T, ID> access) {
		entityClass = (Class<T>) GenericUtils.getParameterizedType(this.getClass());
		this.access = access;
	}

	@Override
	protected UniversalAbsService access() {
		if (access == null) {
			while (applicationContext == null) {
				ThreadExecutor.sleep(1000);
			}
			access = applicationContext.getBean(UniversalService.class);
		}
		return access;
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		this.applicationContext = event.getApplicationContext();
		access();
		ThreadExecutor.execute(this::run);
	}

	private void run() {
		try {
			init();
		} catch (Exception ex) {
			log.error("", ex);
		}
	}

	protected void init() {
		log.info(entityClass.getName());
	}

	@Override
	public Mono<Integer> removeAll() {
		return access().removeAll(entityClass);
	}

	@Override
	public Mono<Integer> removeAll(List<? extends T> iterable) {
		return access().removeAll(entityClass, iterable);
	}

	@Override
	public Mono<Integer> removeById(ID id) {
		return access().removeById(entityClass, id);
	}

	@Override
	public Mono<Integer> removeByIds(List<Serializable> ids) {
		return access().removeByIds(entityClass, ids);
	}

	@Override
	public Mono<List<T>> insert(List<? extends T> entitysToInsert) {
		return access().insert(entityClass, entitysToInsert);
	}

	@Override
	public Mono<List<T>> update(List<? extends T> entitysToUpdate) {
		return access().update(entityClass, entitysToUpdate);
	}

	@Override
	public Mono<List<T>> save(List<? extends T> entitysToSave) {
		return access().save(entityClass, entitysToSave);
	}

	@Override
	public Mono<List<T>> find(String column, Object value) {
		return access().find(entityClass, column, value);
	}

	@Override
	public Mono<List<T>> findAll() {
		return access().findAll(entityClass);
	}

	@Override
	public Mono<List<T>> find8Map(Map<String, Object> whereMap) {
		return access().find8Map(entityClass, whereMap);
	}

	@Override
	public Mono<T> findById(ID id) {
		return access().findById(entityClass, id);
	}

	@Override
	public Mono<T> findOne(String column, Object value) {
		return access().findOne(entityClass, column, value);
	}

	@Override
	public Mono<Boolean> existsById(ID id) {
		return access().existsById(entityClass, id);
	}

	@Override
	public Mono<Response<List<T>>> page(Page page) {
		return access().page(entityClass, page);
	}

	@Override
	public Mono<Long> countAll() {
		return access().countAll(entityClass);
	}

	@Override
	public Mono<Response<Integer>> deleteById(ID id) {
		return removeById(id).flatMap(r -> {
			if (r > 0) {
				return ResponseMono.ok(r, "删除成功");
			} else {
				return Mono.error(CustomException.er("删除失败"));
			}
		});
	}

	@Override
	public Mono<Response<Integer>> deleteByIds(List<Serializable> ids) {
		return removeByIds(ids).flatMap(size -> ResponseMono.ok(size));
	}

	@Override
	public Mono<Response<List<T>>> findAllResponse() {
		return findAll()//
				.flatMap(r -> ResponseMono.ok(r))//
				.defaultIfEmpty(Response.bq("找不到数据"));
	}

	@Override
	public Mono<Response<T>> findResponse(ID id) {
		return findById(id)//
				.flatMap(r -> ResponseMono.ok(r))//
				.defaultIfEmpty(Response.bq("找不到数据"));
	}
}
