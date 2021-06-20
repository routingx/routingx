package routingx.webflux;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.WebProperties.Resources;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;

import lombok.extern.slf4j.Slf4j;
import routingx.CustomException;
import routingx.utils.TextUtils;

@Component
@Order(-2)
@Slf4j
public class WebExceptionHandler extends DefaultErrorWebExceptionHandler {

	private final ErrorAttributes errorAttributes;

	public WebExceptionHandler( //
			ErrorAttributes errorAttributes, //
			Resources resourceProperties, //
			ServerProperties serverProperties, //
			ObjectProvider<ViewResolver> viewResolvers, //
			ServerCodecConfigurer serverCodecConfigurer, //
			ApplicationContext applicationContext) {

		super(errorAttributes, resourceProperties, serverProperties.getError(), applicationContext);
		this.errorAttributes = errorAttributes;
		setViewResolvers(viewResolvers.orderedStream().collect(Collectors.toList()));
		setMessageWriters(serverCodecConfigurer.getWriters());
		setMessageReaders(serverCodecConfigurer.getReaders());
	}

	@Override
	protected Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
		Map<String, Object> map = this.errorAttributes.getErrorAttributes(request, options);
		Throwable throwable = getError(request);
		if (throwable instanceof CustomException) {
			map.put("message", throwable.getMessage());
		} else {
			map.put("message", formatError(throwable, request, null));
		}
		int errorStatus = getHttpStatus(map);
		if (errorStatus >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
			map.put("error", TextUtils.toStringWith(throwable));
		}
		map.put("path", request.exchange().getRequest().getPath().value());
		return map;
	}

	@Override
	protected RouterFunction<ServerResponse> getRoutingFunction(final ErrorAttributes errorAttributes) {
		return super.getRoutingFunction(errorAttributes);
	}

	@Override
	protected void logError(ServerRequest request, ServerResponse response, Throwable throwable) {
		if (log.isDebugEnabled()) {
			log.debug(formatError(throwable, request, response));
		}
		if (HttpStatus.resolve(response.rawStatusCode()) != null
				&& response.statusCode().value() >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
			log.error("{} for {}{}{}", response.statusCode(), //
					formatRequest(request), //
					System.lineSeparator(), //
					(log.isDebugEnabled() ? TextUtils.toString(throwable) : TextUtils.toStringWith(throwable)));
		}
	}

	private static String formatError(Throwable ex, ServerRequest request, ServerResponse response) {
		String reason = ex.getClass().getSimpleName() + ": " + ex.getMessage();
		return (response != null ? response.statusCode() : "") + " Resolved [" + reason + "] for HTTP "
				+ request.methodName() + " " + request.path();
	}

	public static String formatRequest(ServerRequest request) {
		String rawQuery = request.uri().getRawQuery();
		String query = StringUtils.hasText(rawQuery) ? "?" + rawQuery : "";
		return "HTTP " + request.methodName() + " \"" + request.path() + query + "\"";
	}

}
