package routingx.manager;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import reactor.core.publisher.Mono;
import routingx.Response;
import routingx.model.Page;

public interface GenericManager<T, ID extends Serializable> extends UniversalAccess {

	Mono<Integer> removeAll();

	Mono<Integer> removeAll(List<? extends T> iterable);

	Mono<Integer> removeById(ID id);

	Mono<Integer> removeByIds(List<Serializable> ids);

	Mono<List<T>> insert(List<? extends T> entitysToInsert);

	Mono<List<T>> update(List<? extends T> entitysToUpdate);

	Mono<List<T>> save(List<? extends T> entitysToSave);

	Mono<Boolean> existsById(ID id);

	Mono<List<T>> find(String column, Object value);

	Mono<List<T>> findAll();

	Mono<T> findById(ID id);

	Mono<T> findOne(String column, Object value);

	Mono<List<T>> find8Map(Map<String, Object> whereMap);

	Mono<Long> countAll();

	Mono<Response<List<T>>> page(Page page);

	Mono<Response<Integer>> deleteById(ID id);

	Mono<Response<Integer>> deleteByIds(List<Serializable> ids);

	Mono<Response<T>> findResponse(ID id);

	Mono<Response<List<T>>> findAllResponse();

}
