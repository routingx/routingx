package routingx.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import reactor.core.publisher.Mono;
import routingx.Note;
import routingx.Response;
import routingx.dao.GenericDao;
import routingx.dao.GenericDaoImpl;
import routingx.model.Page;
import routingx.utils.GenericUtils;

@Note("事务处理层，必须交由Spring管理")
@Transactional(readOnly = true)
public abstract class GenericServiceImpl<T, ID extends Serializable> extends UniversalAbsServiceImpl
		implements GenericService<T, ID> {

	private final GenericDao<T, ID> access;

	protected GenericServiceImpl() {
		@SuppressWarnings("unchecked")
		Class<T> entityClass = (Class<T>) GenericUtils.getParameterizedType(this.getClass());
		access = new GenericDaoImpl<>(entityClass);
	}

	public GenericServiceImpl(GenericDao<T, ID> access) {
		this.access = access;
	}

	@Override
	protected GenericDao<T, ID> access() {
		return access;
	}

	@Override
	public Class<T> getEntityClass() {
		return access.getEntityClass();
	}

	@Transactional
	@Override
	public Mono<Integer> removeAll() {
		return access().removeAll();
	}

	@Transactional
	@Override
	public Mono<Integer> removeAll(List<? extends T> iterable) {
		return access().removeAll(iterable);
	}

	@Transactional
	@Override
	public Mono<Integer> removeById(ID id) {
		return access().removeById(id);
	}

	@Transactional
	@Override
	public Mono<Integer> removeByIds(List<ID> ids) {
		return access().removeByIds(ids);
	}

	@Transactional
	@Override
	public Mono<List<T>> insert(List<? extends T> entitysToInsert) {
		Assert.notNull(entitysToInsert, "entitys to insert must not be null!");
		return access().insert(entitysToInsert);
	}

	@Transactional
	@Override
	public Mono<List<T>> update(List<? extends T> entitysToUpdate) {
		Assert.notNull(entitysToUpdate, "entitysToUpdate to insert must not be null!");
		return access().update(entitysToUpdate);
	}

	@Override
	public Mono<Integer> updateIfNull(String column, Object value) {
		return access().updateIfNull(column, value);
	}

	@Transactional
	@Override
	public Mono<List<T>> save(List<? extends T> entitysToSave) {
		return access().save(entitysToSave);
	}

	@Override
	public Mono<List<T>> find(String column, Object value) {
		return access().find(column, value);
	}

	@Override
	public Mono<List<T>> findAll() {
		return access().findAll();
	}

	@Override
	public Mono<List<T>> findAll(Sort sort) {
		return access().findAll(sort);
	}

	@Override
	public Mono<List<T>> find8Map(Map<String, Object> whereMap) {
		return access().find8Map(whereMap);
	}

	@Override
	public Mono<T> findById(ID id) {
		return access().findById(id);
	}

	@Override
	public Mono<T> findOne(Query query) {
		return access().findOne(query);
	}

	@Override
	public Mono<T> findOne(String column, Object value) {
		return access().findOne(column, value);
	}

	@Override
	public Mono<Boolean> existsById(ID id) {
		return access().existsById(id);
	}

	@Override
	public Mono<Response<List<T>>> page(Page page) {
		return access().page(page);
	}

	@Override
	public Mono<Long> countAll() {
		return access().countAll();
	}

	@Override
	public Mono<Long> count(Query q) {
		return access().count(q);
	}

	@Override
	public Mono<List<T>> find(Query q) {
		return access().find(q);
	}
}
