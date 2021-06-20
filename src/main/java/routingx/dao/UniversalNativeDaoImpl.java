package routingx.dao;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.data.relational.core.query.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import routingx.Response;
import routingx.model.Page;

@Transactional(readOnly = true)
class UniversalNativeDaoImpl extends NativeDaoImpl implements UniversalNativeDao {

	private <R> SimpleRepository<R, Serializable> getRepository(Class<R> clazz) {
		return factory().get(clazz);
	}

	private <R> SimpleRepository<R, Serializable> getRepository(R entity) {
		@SuppressWarnings("unchecked")
		Class<R> clazz = (Class<R>) ClassUtils.getUserClass(entity);
		return getRepository(clazz);
	}

	@Transactional
	@Override
	public Mono<List<Object>> removeThenSave(List<?> deleteList, List<?> entitysToSave) {
		if (deleteList.size() > 0) {
			return removeList(deleteList).flatMap(d -> saveList(entitysToSave));
		} else {
			return saveList(entitysToSave);
		}
	}

	@Transactional
	@Override
	public Mono<Integer> removeList(List<?> entitysToDelete) {
		Flux<Integer> flux = Flux.fromIterable(entitysToDelete).concatMap(this::remove);
		return flux.collectList().flatMap(intList -> {
			int count = 0;
			for (Integer num : intList) {
				count += num;
			}
			return Mono.just(count);
		});
	}

	@Transactional
	@Override
	public <R> Mono<Integer> remove(R entity) {
		return getRepository(entity).remove(entity);
	}

	@Transactional
	@Override
	public <R> Mono<Integer> removeAll(Class<R> clazz) {
		return getRepository(clazz).removeAll();
	}

	@Override
	public <R> Mono<Integer> removeAll(Class<R> clazz, List<? extends R> iterable) {
		return getRepository(clazz).removeAll(iterable);
	}

	@Transactional
	@Override
	public <R> Mono<Integer> removeById(Class<R> clazz, Serializable id) {
		return getRepository(clazz).removeById(id);
	}

	@Transactional
	@Override
	public <R> Mono<Integer> removeByIds(Class<R> clazz, List<Serializable> ids) {
		return getRepository(clazz).removeByIds(ids);
	}

	@Transactional
	@Override
	public <R> Mono<R> insert(R entity) {
		return getRepository(entity).insert(entity);
	}

	@Transactional
	@Override
	public <R> Mono<List<R>> insert(Class<R> clazz, List<? extends R> entitysToInsert) {
		Assert.isTrue(!CollectionUtils.isEmpty(entitysToInsert), "entitys to insert must not be null!");
		return getRepository(clazz).insert(entitysToInsert);
	}

	@Transactional
	@Override
	public Mono<List<Object>> insertList(List<?> entitysToInsert) {
		Assert.notNull(entitysToInsert, "entitys to insert must not be null!");
		Flux<Object> flux = Flux.fromIterable(entitysToInsert).concatMap(this::insert);
		return flux.collectList();
	}

	@Transactional
	@Override
	public <R> Mono<R> update(R entity) {
		return getRepository(entity).update(entity);
	}

	@Transactional
	@Override
	public <R> Mono<List<R>> update(Class<R> clazz, List<? extends R> entitysToUpdate) {
		Assert.isTrue(!CollectionUtils.isEmpty(entitysToUpdate), "entitysToUpdate to insert must not be null!");
		return getRepository(clazz).update(entitysToUpdate);
	}

	@Transactional
	@Override
	public Mono<List<Object>> updateList(List<?> entitysToUpdate) {
		Assert.notNull(entitysToUpdate, "entitysToUpdate to insert must not be null!");
		Flux<Object> flux = Flux.fromIterable(entitysToUpdate).concatMap(this::update);
		return flux.collectList();
	}

	@Transactional
	@Override
	public <R> Mono<R> save(R entity) {
		return getRepository(entity).save(entity);
	}

	@Transactional
	@Override
	public <R> Mono<List<R>> save(Class<R> clazz, List<? extends R> entitysToSave) {
		Assert.notNull(entitysToSave, "entitysToSave to insert must not be null!");
		if (entitysToSave.size() == 0) {
			return Mono.just(Collections.emptyList());
		}
		return getRepository(clazz).save(entitysToSave);
	}

	@Transactional
	@Override
	public Mono<List<Object>> saveList(List<?> entitysToSave) {
		Assert.notNull(entitysToSave, "entitysToSave to insert must not be null!");
		Flux<Object> flux = Flux.fromIterable(entitysToSave).concatMap(this::save);
		return flux.collectList();
	}

	@Override
	public <R> Mono<List<R>> find(R entity) {
		return getRepository(entity).find(entity);
	}

	@Override
	public <R> Mono<List<R>> find(Class<R> calzz, Query q) {
		return getRepository(calzz).find(q);
	}

	@Override
	public <R> Mono<List<R>> find(Class<R> clazz, String column, Object value) {
		return getRepository(clazz).find(column, value);
	}

	@Override
	public <R> Mono<List<R>> findAll(Class<R> clazz) {
		return getRepository(clazz).findAll();
	}

	@Override
	public <R> Mono<List<R>> find8Map(Class<R> clazz, Map<String, Object> whereMap) {
		return getRepository(clazz).find8Map(whereMap);
	}

	@Override
	public <R> Mono<R> findById(Class<R> clazz, Serializable id) {
		return getRepository(clazz).findById(id);
	}

	@Override
	public <R> Mono<R> findOne(R entity) {
		return getRepository(entity).findOne(entity);
	}

	@Override
	public <R> Mono<R> findOne(Class<R> clazz, String column, Object value) {
		return getRepository(clazz).findOne(column, value);
	}

	@Override
	public <R> Mono<Boolean> exists(R entity) {
		return getRepository(entity).exists(entity);
	}

	@Override
	public <R> Mono<List<R>> existsFind(R entity) {
		return getRepository(entity).existsFind(entity);
	}

	@Override
	public <R> Mono<Boolean> existsById(Class<R> clazz, Serializable id) {
		return getRepository(clazz).existsById(id);
	}

	@Override
	public <R> Mono<Long> count(R entity) {
		return getRepository(entity).count(entity);
	}

	@Override
	public <R> Mono<Long> countAll(Class<R> clazz) {
		return getRepository(clazz).countAll();
	}

	@Override
	public <R> Mono<Long> count(Class<R> calzz, Query q) {
		return getRepository(calzz).count(q);
	}

	@Override
	public <R> Mono<Response<List<R>>> page(R entity) {
		return getRepository(entity).page(entity);
	}

	@Override
	public <R> Mono<Response<List<R>>> page(Class<R> clazz, Page page) {
		return getRepository(clazz).page(page);
	}

}
