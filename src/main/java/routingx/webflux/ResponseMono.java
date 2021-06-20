package routingx.webflux;

import org.springframework.http.HttpStatus;

import reactor.core.publisher.Mono;
import routingx.CustomException;
import routingx.Note;
import routingx.Response;
import routingx.model.Page;

public class ResponseMono {
	public static <T> Mono<Response<T>> of(HttpStatus status, String msg, T data, Throwable ex) {
		return Mono.just(Response.of(status, msg, data, ex));
	}

	public static <T> Mono<Response<T>> ok(T data) {
		return Mono.just(Response.ok(data));
	}

	public static <T> Mono<Response<T>> ok(T data, String msg) {
		return Mono.just(Response.ok(data, msg));
	}

	public static <T> Mono<Response<T>> ok(Page page, T data) {
		return Mono.just(Response.ok(page, data));
	}

	public static <T> Mono<Response<T>> noContent() {
		return of(HttpStatus.NO_CONTENT, "找不到数据", null, null);
	}

	@Note("参数错误")
	public static <T> Mono<Response<T>> bq(String msg) {
		return of(HttpStatus.BAD_REQUEST, msg, null, null);
	}

	public static <T> Mono<Response<T>> er(CustomException ex) {
		return er(ex.getStatus(), ex.getMessage(), ex);
	}

	public static <T> Mono<Response<T>> er(String msg) {
		return er(HttpStatus.INTERNAL_SERVER_ERROR, msg, null);
	}

	public static <T> Mono<Response<T>> er(String msg, T data) {
		return of(HttpStatus.INTERNAL_SERVER_ERROR, msg, data, null);
	}

	public static <T> Mono<Response<T>> er(String msg, Throwable ex) {
		return of(HttpStatus.INTERNAL_SERVER_ERROR, msg, null, ex);
	}

	public static <T> Mono<Response<T>> er(HttpStatus status, String msg, Throwable ex) {
		return of(status, msg, null, null);
	}
}
