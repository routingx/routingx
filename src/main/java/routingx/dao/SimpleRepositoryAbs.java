package routingx.dao;

import java.util.List;
import java.util.Map;

import org.springframework.data.relational.core.query.Query;

import reactor.core.publisher.Mono;
import routingx.Response;
import routingx.model.Page;

public interface SimpleRepositoryAbs {

	String getEntityName();
	
	String getEntityIdName();

	Mono<Long> count(Query q);
	
	<R> Mono<R> nativeOne(String sql, Map<String, Object> params, Class<R> resultType);

	Mono<Long> nativeCount(String sql, Map<String, Object> params);

	<R> Mono<List<R>> nativeList(String sql, Map<String, Object> params, Class<R> resultType);

	<R> Mono<Response<List<R>>> nativePage(String sql, Page page, Class<R> resultType);

	Mono<Integer> execute(String sql, Map<String, Object> params);
}