package routingx.webflux;

import org.reactivestreams.Publisher;
import org.springframework.boot.web.reactive.filter.OrderedWebFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import routingx.json.JSON;
import routingx.utils.TextUtils;
import routingx.utils.XmlUtils;

/**
 * ServerRequestCacheWebFilter
 * 
 * @author Administrator
 *
 */
@Slf4j
public class SimpleExchangeFilter implements OrderedWebFilter {

	private static final String lineSeparator = System.lineSeparator();

	public SimpleExchangeFilter() {
		log.info(getClass().getSimpleName());
	}

	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		ServerHttpRequest request = new SimpleRequest(exchange.getRequest());
		ServerHttpResponse response = new SimpleResponse(exchange.getResponse());
		exchange = new SimpleExchange(exchange.mutate().request(request).response(response).build());
		return chain.filter(exchange).contextWrite(SimpleExchangeContext.of(exchange));
	}

	class SimpleResponse extends ServerHttpResponseDecorator {

		public SimpleResponse(ServerHttpResponse delegate) {
			super(delegate);
		}

		@Override
		public Mono<Void> setComplete() {
			return getDelegate().setComplete().contextWrite(SimpleExchangeContext.clear());
		}

		@Override
		public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
			return super.writeWith(writeWithLog(body));
		}
	}

	private Publisher<? extends DataBuffer> writeWithLog(Publisher<? extends DataBuffer> body) {
		return DataBufferUtils.join(body)//
				.flatMap(buffer -> SimpleExchangeContext
						.exchange(exchange -> writeWith(exchange, BufferUtils.read(buffer))));
	}

	protected Mono<DataBuffer> writeWith(ServerWebExchange exchange, byte[] responseBody) {
		if (!log.isDebugEnabled()) {
			return toBuffer(exchange, responseBody);
		}
		return BufferUtils.join(exchange.getRequest()).flatMap(bytes -> {
			debug(exchange, bytes, responseBody);
			return toBuffer(exchange, responseBody);
		});
	}

	private Mono<DataBuffer> toBuffer(ServerWebExchange exchange, byte[] responseBody) {
		return Mono.justOrEmpty(BufferUtils.wrap(responseBody));
	}

	private void debug(ServerWebExchange exchange, byte[] requestBody, byte[] responseBody) {
		if (!log.isDebugEnabled()) {
			return;
		}
		MediaType mediaType = exchange.getResponse().getHeaders().getContentType();
		String request = fromat(exchange.getRequest().getHeaders().getContentType(), requestBody);
		String response = fromat(mediaType, responseBody);
		log.debug("{} {} {}", exchange.getRequest().getPath().value(),
				exchange.getRequest().getHeaders().getContentType(),
				exchange.getResponse().getHeaders().getContentType());
		if (MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)
				|| MediaType.APPLICATION_XML.isCompatibleWith(mediaType)) {
			log.debug("header：{}{}", lineSeparator, JSON.format(exchange.getResponse().getHeaders()));
			log.debug("request：{}{}", lineSeparator, request);
			log.debug("response：{}{}", lineSeparator, response);
		}
	}

	protected static String fromat(MediaType mediaType, byte[] buffer) {
		if (MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
			return JSON.format(buffer);
		} else if (MediaType.APPLICATION_XML.isCompatibleWith(mediaType)) {
			return XmlUtils.format(buffer);
		} else {
			return TextUtils.toString(buffer);
		}
	}
}
