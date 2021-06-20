package routingx.manager;

import java.io.Serializable;
import java.util.List;

import reactor.core.publisher.Mono;
import routingx.Response;

interface ResponseManager extends UniversalAccess {

//	Mono<Boolean> insertVerify(Object insertOrUpdate);
//
//	Mono<Boolean> updateVerify(Object insertOrUpdate);

	Mono<Response<Integer>> deleteById(Class<?> clazz, String id);

	<T> Mono<Response<Integer>> deleteByIds(Class<T> clazz, List<Serializable> ids);

	<T> Mono<Response<List<T>>> findAllResponse(Class<T> clazz);

	<T> Mono<Response<T>> findResponse(Class<T> clazz, String id);

	<T> Mono<Response<List<T>>> findResponse(T entity);

	<T> Mono<Response<T>> insertResponse(T entity);

	<T> Mono<Response<T>> updateResponse(T entity);

}
