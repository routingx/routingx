
package routingx.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.data.relational.core.query.Query;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Mono;
import routingx.Response;
import routingx.dao.UniversalNativeDao;
import routingx.model.Page;
import routingx.webflux.WebAbsContext;

@Transactional(readOnly = true)
abstract class UniversalAbsServiceImpl extends WebAbsContext implements UniversalAbsService {

	protected abstract UniversalNativeDao access();

	@Transactional
	@Override
	public Mono<List<Object>> removeThenSave(List<?> deleteList, List<?> entitysToSave) {
		return access().removeThenSave(deleteList, entitysToSave);
	}

	@Transactional
	@Override
	public Mono<Integer> removeList(List<?> entitysToDelete) {
		return access().removeList(entitysToDelete);
	}

	@Transactional
	@Override
	public <R> Mono<Integer> remove(R entity) {
		return access().remove(entity);
	}

	@Transactional
	@Override
	public <R> Mono<Integer> removeAll(Class<R> clazz) {
		return access().removeAll(clazz);
	}

	@Override
	public <R> Mono<Integer> removeAll(Class<R> clazz, List<? extends R> iterable) {
		return access().removeAll(clazz, iterable);
	}

	@Transactional
	@Override
	public <R> Mono<Integer> removeById(Class<R> clazz, Serializable id) {
		return access().removeById(clazz, id);
	}

	@Transactional
	@Override
	public <R> Mono<Integer> removeByIds(Class<R> clazz, List<Serializable> ids) {
		return access().removeByIds(clazz, ids);
	}

	@Transactional
	@Override
	public <R> Mono<R> insert(R entity) {
		return access().insert(entity);
	}

	@Transactional
	@Override
	public <R> Mono<List<R>> insert(Class<R> clazz, List<? extends R> entitysToInsert) {
		return access().insert(clazz, entitysToInsert);
	}

	@Transactional
	@Override
	public Mono<List<Object>> insertList(List<?> entitysToInsert) {
		return access().insertList(entitysToInsert);
	}

	@Transactional
	@Override
	public <R> Mono<R> update(R entity) {
		return access().update(entity);
	}

	@Transactional
	@Override
	public <R> Mono<List<R>> update(Class<R> clazz, List<? extends R> entitysToUpdate) {
		return access().update(clazz, entitysToUpdate);
	}

	@Transactional
	@Override
	public Mono<List<Object>> updateList(List<?> entitysToUpdate) {
		return access().updateList(entitysToUpdate);
	}

	@Transactional
	@Override
	public <R> Mono<R> save(R entity) {
		return access().save(entity);
	}

	@Transactional
	@Override
	public <R> Mono<List<R>> save(Class<R> clazz, List<? extends R> entitysToSave) {
		return access().save(clazz, entitysToSave);
	}

	@Transactional
	@Override
	public Mono<List<Object>> saveList(List<?> entitysToSave) {
		return access().saveList(entitysToSave);
	}

	@Override
	public <R> Mono<List<R>> find(R entity) {
		return access().find(entity);
	}

	@Override
	public <R> Mono<List<R>> find(Class<R> calzz, Query q) {
		return access().find(calzz, q);
	}

	@Override
	public <R> Mono<List<R>> find(Class<R> clazz, String column, Object value) {
		return access().find(clazz, column, value);
	}

	@Override
	public <R> Mono<List<R>> findAll(Class<R> clazz) {
		return access().findAll(clazz);
	}

	@Override
	public <R> Mono<List<R>> find8Map(Class<R> clazz, Map<String, Object> whereMap) {
		return access().find8Map(clazz, whereMap);
	}

	@Override
	public <R> Mono<R> findById(Class<R> clazz, Serializable id) {
		return access().findById(clazz, id);
	}

	@Override
	public <R> Mono<R> findOne(R entity) {
		return access().findOne(entity);
	}

	@Override
	public <R> Mono<R> findOne(Class<R> clazz, String column, Object value) {
		return access().findOne(clazz, column, value);
	}

	@Override
	public <R> Mono<Boolean> exists(R entity) {
		return access().exists(entity);
	}

	@Override
	public <R> Mono<List<R>> existsFind(R entity) {
		return access().existsFind(entity);
	}

	@Override
	public <R> Mono<Boolean> existsById(Class<R> clazz, Serializable id) {
		return access().existsById(clazz, id);
	}

	@Override
	public <R> Mono<Long> count(R entity) {
		return access().count(entity);
	}

	@Override
	public <R> Mono<Long> countAll(Class<R> clazz) {
		return access().countAll(clazz);
	}

	@Override
	public <R> Mono<Long> count(Class<R> calzz, Query q) {
		return access().count(q);
	}

	@Override
	public <R> Mono<Response<List<R>>> page(R entity) {
		return access().page(entity);
	}

	@Override
	public <R> Mono<Response<List<R>>> page(Class<R> clazz, Page page) {
		return access().page(clazz, page);
	}

//	protected <R> Mono<List<R>> nativeList(String sql, Map<String, Object> params, Class<R> resultType) {
//		return access().nativeList(sql, params, resultType);
//	}
//
//	protected <R> Mono<R> nativeOne(String sql, Map<String, Object> params, Class<R> resultType) {
//		return access().nativeOne(sql, params, resultType);
//	}
//
//	protected Mono<Long> nativeCount(String sql, Map<String, Object> params) {
//		return access().nativeCount(sql, params);
//	}
//
//	protected <R> Mono<Response<List<R>>> nativePage(String sql, Page page, Class<R> resultType) {
//		return access().nativePage(sql, page, resultType);
//	}
//
//	protected Mono<Integer> nativeExe(String sql, Map<String, Object> params) {
//		return access().nativeExe(sql, params);
//	}
}
