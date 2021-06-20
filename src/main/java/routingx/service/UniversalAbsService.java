package routingx.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.data.relational.core.query.Query;

import reactor.core.publisher.Mono;
import routingx.Response;
import routingx.model.Page;

public interface UniversalAbsService {

	Mono<List<Object>> insertList(List<?> entitysToInsert);

	Mono<List<Object>> updateList(List<?> entitysToUpdate);

	Mono<List<Object>> saveList(List<?> entitysToSave);

	Mono<List<Object>> removeThenSave(List<?> deleteList, List<?> entitysToSave);

	Mono<Integer> removeList(List<?> entitysToDelete);

	<R> Mono<Integer> remove(R entity);

	<R> Mono<Integer> removeAll(Class<R> clazz);

	<R> Mono<Integer> removeAll(Class<R> clazz, List<? extends R> iterable);

	<R> Mono<Integer> removeById(Class<R> clazz, Serializable id);

	<R> Mono<Integer> removeByIds(Class<R> clazz, List<Serializable> ids);

	<R> Mono<R> insert(R entity);

	<R> Mono<List<R>> insert(Class<R> clazz, List<? extends R> entitysToInsert);

	<R> Mono<R> update(R entity);

	<R> Mono<List<R>> update(Class<R> clazz, List<? extends R> entitysToUpdate);

	<R> Mono<R> save(R objectToSave);

	<R> Mono<List<R>> save(Class<R> clazz, List<? extends R> entitysToSave);

	<R> Mono<List<R>> find(R entity);

	<R> Mono<List<R>> find(Class<R> clazz, Query q);

	<R> Mono<List<R>> find(Class<R> clazz, String column, Object value);

	<R> Mono<List<R>> findAll(Class<R> clazz);

	<R> Mono<List<R>> find8Map(Class<R> clazz, Map<String, Object> whereMap);

	<R> Mono<R> findById(Class<R> clazz, Serializable id);

	<R> Mono<R> findOne(R entity);

	<R> Mono<R> findOne(Class<R> clazz, String column, Object value);

	<R> Mono<Boolean> existsById(Class<R> clazz, Serializable id);

	<R> Mono<Boolean> exists(R entity);
	
	<R> Mono<List<R>> existsFind(R entity);

	<R> Mono<Long> count(R entity);

	<R> Mono<Long> count(Class<R> clazz, Query q);

	<R> Mono<Long> countAll(Class<R> clazz);

	<R> Mono<Response<List<R>>> page(R entity);

	<R> Mono<Response<List<R>>> page(Class<R> clazz, Page page);

}
