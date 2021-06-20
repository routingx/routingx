package routingx.webflux;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
final class SimpleRequest extends ServerHttpRequestDecorator {

	private final HttpHeaders headers = new HttpHeaders();

	private byte[] bodyBuffer;

	public byte[] getBodyBuffer() {
		return bodyBuffer;
	}

	public SimpleRequest(ServerHttpRequest delegate) {
		super(delegate);
		headers.addAll(delegate.getHeaders());
		// bodyBuffer = resolveBody(delegate);
	}

	@Override
	public HttpHeaders getHeaders() {
		return headers;
	}

	@Override
	public Flux<DataBuffer> getBody() {
		if (bodyBuffer != null) {
			return Flux.just(BufferUtils.wrap(bodyBuffer));
		}
		return BufferUtils.join(getDelegate()).flatMapMany(bytes -> {
			bodyBuffer = bytes;
			if (log.isDebugEnabled()) {
				log.debug(SimpleExchangeFilter.fromat(getHeaders().getContentType(), bodyBuffer));
			}
			return Flux.just(BufferUtils.wrap(bodyBuffer));
		});
	}
}