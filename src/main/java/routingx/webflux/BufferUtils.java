package routingx.webflux;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;

import reactor.core.publisher.Mono;

public abstract class BufferUtils {

	public static Mono<byte[]> join(ServerHttpRequest request) {
		if (request.getHeaders().getContentLength() > 0) {
			return DataBufferUtils.join(request.getBody()).flatMap(buffer -> {
				return Mono.just(read(buffer));
			});
		}
		return Mono.just(new byte[0]);
	}

	public static byte[] read(DataBuffer buffer) {
		byte[] body = new byte[buffer.readableByteCount()];
		buffer.read(body);
		DataBufferUtils.release(buffer);
		return body;
	}

	public static DataBuffer wrap(byte[] bytes) {
		DataBufferFactory bufferFactory = new DefaultDataBufferFactory();
		DataBuffer buffer = bufferFactory.wrap(bytes);
		bufferFactory = null;
		return buffer;
	}
}
