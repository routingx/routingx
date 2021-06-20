package routingx.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.Query;

import reactor.core.publisher.Mono;
import routingx.Response;
import routingx.model.Page;

public interface SimpleRepository<T, ID extends Serializable> extends SimpleRepositoryAbs {

	Mono<Integer> remove(T entity);

	Mono<Integer> removeAll();

	Mono<Integer> removeAll(List<? extends T> iterable);

	Mono<Integer> removeById(ID id);

	Mono<Integer> removeByIds(List<ID> ids);

	Mono<T> insert(T entity);

	Mono<List<T>> insert(List<? extends T> entitysToInsert);

	Mono<T> update(T entity);

	Mono<List<T>> update(List<? extends T> entitysToUpdate);

	Mono<Integer> updateIfNull(String column, Object value);

	Mono<T> save(T objectToSave);

	Mono<List<T>> save(List<? extends T> entitysToSave);

	Mono<Boolean> existsById(ID id);

	Mono<Boolean> exists(T entity);
	
	Mono<List<T>> existsFind(T entity);

	Mono<List<T>> find(T entity);

	Mono<List<T>> find(Query q);

	Mono<List<T>> find(String column, Object value);

	Mono<List<T>> findIds(String column, Object value);

	Mono<List<T>> findAll();

	Mono<List<T>> findAll(Sort sort);

	Mono<T> findById(ID id);

	Mono<T> findOne(T entity);

	Mono<T> findOne(Query query);

	Mono<T> findOne(String column, Object value);

	Mono<List<T>> find8Map(Map<String, Object> whereMap);

	Mono<Long> count(T entity);

	Mono<Long> countAll();

	Mono<Response<List<T>>> page(T entity);

	Mono<Response<List<T>>> page(Page page);

}