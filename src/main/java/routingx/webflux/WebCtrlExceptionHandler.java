package routingx.webflux;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebInputException;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import routingx.CustomException;
import routingx.Response;

@RestControllerAdvice
@ControllerAdvice
@Slf4j
public class WebCtrlExceptionHandler extends WebAbsContext {

	@ExceptionHandler(Exception.class)
	public Mono<Response<Object>> exception(Exception e) {
		log.error("未知错误" + e.toString(), e);
		return exchange(exchange -> {
			Response<Object> resp = Response.er("未知错误", e);
			exchange.getResponse().setRawStatusCode(resp.getStatus());
			resp.setPath(exchange.getRequest().getPath().value());
			return Mono.just(resp);
		});
	}

	@ExceptionHandler(RuntimeException.class)
	public Mono<Response<Object>> runtime(RuntimeException e) {
		return exchange(exchange -> {
			final Response<Object> resp;
			if (e instanceof ServerWebInputException) {
				ServerWebInputException ie = ((ServerWebInputException) e);
				resp = Response.er(ie.getStatus(), e.getMessage(), e);
			} else if (e instanceof CustomException) {
				resp = Response.er((CustomException) e);
			} else {
				log.error("运行时错误" + e.toString(), e);
				resp = Response.er("运行时错误", e);
			}
			exchange.getResponse().setRawStatusCode(resp.getStatus());
			resp.setPath(exchange.getRequest().getPath().value());
			return Mono.just(resp);
		});
	}
}
