package routingx.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.Query;

import reactor.core.publisher.Mono;
import routingx.Response;
import routingx.model.Page;

public interface GenericService<T, ID extends Serializable> extends UniversalAbsService {

	Class<T> getEntityClass();

	Mono<Integer> removeAll();

	Mono<Integer> removeAll(List<? extends T> iterable);

	Mono<Integer> removeById(ID id);

	Mono<Integer> removeByIds(List<ID> ids);

	Mono<List<T>> insert(List<? extends T> entitysToInsert);

	Mono<List<T>> update(List<? extends T> entitysToUpdate);

	Mono<Integer> updateIfNull(String column, Object value);

	Mono<List<T>> save(List<? extends T> entitysToSave);

	Mono<Boolean> existsById(ID id);

	Mono<List<T>> find(Query q);

	Mono<List<T>> find(String column, Object value);

	Mono<List<T>> findAll();

	Mono<List<T>> findAll(Sort sort);

	Mono<T> findById(ID id);

	Mono<T> findOne(Query query);

	Mono<T> findOne(String column, Object value);

	Mono<List<T>> find8Map(Map<String, Object> whereMap);

	Mono<Long> count(Query q);

	Mono<Long> countAll();

	Mono<Response<List<T>>> page(Page page);
}
