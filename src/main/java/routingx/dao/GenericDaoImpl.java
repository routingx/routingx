package routingx.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import reactor.core.publisher.Mono;
import routingx.Response;
import routingx.model.Page;
import routingx.utils.GenericUtils;

@Transactional(readOnly = true)
public class GenericDaoImpl<T, ID extends Serializable> extends UniversalNativeDaoImpl implements GenericDao<T, ID> {

	private final Class<T> entityClass;
	private SimpleRepository<T, Serializable> repository;

	public GenericDaoImpl(Class<T> entityClass) {
		this.entityClass = entityClass;
	}

	@SuppressWarnings("unchecked")
	protected GenericDaoImpl() {
		this.entityClass = (Class<T>) GenericUtils.getParameterizedType(this.getClass());
	}

	protected GenericDaoImpl(SimpleRepositoryFactory factory) {
		this();
		this.setFactory(factory);
	}

	@Override
	protected SimpleRepository<T, Serializable> getRepository() {
		if (repository == null) {
			repository = factory().get(entityClass);
		}
		return repository;
	}

	@Override
	public Class<T> getEntityClass() {
		return entityClass;
	}

	@Transactional
	@Override
	public Mono<Integer> removeAll() {
		return getRepository().removeAll();
	}

	@Transactional
	@Override
	public Mono<Integer> removeAll(List<? extends T> iterable) {
		return getRepository().removeAll(iterable);
	}

	@Transactional
	@Override
	public Mono<Integer> removeById(Serializable id) {
		return getRepository().removeById(id);
	}

//	@Transactional
//	@Override
//	public Mono<Integer> removeById(ID id) {
//		EntityMetaData meta = EntityMetaData.get(entityClass);
//		meta.isSqlDeleted()
//		return findById(id).flatMap(e -> {
//			EntityMetaData meta = EntityMetaData.get(entityClass);
//			if (meta.getLinkDeletes() != null) {
//				for (LinkDelete linkDelete : meta.getLinkDeletes()) {
////					return find(linkDelete.value(), linkDelete.column(), id).flatMap(list -> {
////						return getRepository().removeById(id);
////					});
//				}
//			}
//			return getRepository().removeById(id);
//		}).defaultIfEmpty(0);
//	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Mono<Integer> removeByIds(List<ID> ids) {
		if (ids.isEmpty()) {
			return Mono.just(0);
		}
		return getRepository().removeByIds((List<Serializable>) ids);
	}

	@Transactional
	@Override
	public Mono<List<T>> insert(List<? extends T> entitysToInsert) {
		Assert.notNull(entitysToInsert, "entitys to insert must not be null!");
		return getRepository().insert(entitysToInsert);
	}

	@Transactional
	@Override
	public Mono<List<T>> update(List<? extends T> entitysToUpdate) {
		Assert.notNull(entitysToUpdate, "entitysToUpdate to insert must not be null!");
		return getRepository().update(entitysToUpdate);
	}

	@Override
	public Mono<Integer> updateIfNull(String column, Object value) {
		return getRepository().updateIfNull(column, value);
	}

	@Transactional
	@Override
	public Mono<List<T>> save(List<? extends T> entitysToSave) {
		return getRepository().save(entitysToSave);
	}

	@Override
	public Mono<List<T>> find(String column, Object value) {
		return getRepository().find(column, value);
	}

	@Override
	public Mono<List<T>> findAll() {
		return getRepository().findAll();
	}

	@Override
	public Mono<List<T>> findAll(Sort sort) {
		return getRepository().findAll(sort);
	}

	@Override
	public Mono<List<T>> find8Map(Map<String, Object> whereMap) {
		return getRepository().find8Map(whereMap);
	}

	@Override
	public Mono<T> findById(Serializable id) {
		return getRepository().findById(id);
	}

	@Override
	public Mono<T> findOne(Query query) {
		return getRepository().findOne(query);
	}

	@Override
	public Mono<T> findOne(String column, Object value) {
		return getRepository().findOne(column, value);
	}

	@Override
	public Mono<Boolean> existsById(Serializable id) {
		return getRepository().existsById(id);
	}

	@Override
	public Mono<Response<List<T>>> page(Page page) {
		return getRepository().page(page);
	}

	@Override
	public Mono<Long> countAll() {
		return getRepository().countAll();
	}

	@Override
	public Mono<Long> count(Query q) {
		return getRepository().count(q);
	}

	@Override
	public Mono<List<T>> find(Query q) {
		return getRepository().find(q);
	}
}
