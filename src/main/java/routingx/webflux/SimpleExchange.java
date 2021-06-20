package routingx.webflux;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import org.springframework.web.server.adapter.DefaultServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import routingx.Response;
import routingx.json.JSON;
import routingx.model.Token;

/**
 * {@link DefaultServerWebExchange}.
 * 
 * {@link SecurityContextServerWebExchange}.
 * 
 * {@link ServerRequest.create(this.getDelegate(), MESSAGE_READERS);}.
 * 
 * 
 * @author Administrator
 *
 */
@Slf4j
public class SimpleExchange extends ServerWebExchangeDecorator {

	private final Mono<MultiValueMap<String, String>> formDataMono;

	private final Mono<MultiValueMap<String, Part>> multipartDataMono;

	public SimpleExchange(ServerWebExchange delegate) {
		super(delegate);
		this.formDataMono = initFormData(delegate.getRequest(), ServerCodecConfigurer.create());
		this.multipartDataMono = initMultipartData(delegate.getRequest(), ServerCodecConfigurer.create());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Principal> Mono<T> getPrincipal() {
		return SimpleExchangeContext.token(user -> {
			if (user == null) {
				return getDelegate().getPrincipal();
			} else {
				return (Mono<T>) Mono.justOrEmpty(user.principal());
			}
		});
	}

	@Override
	public Mono<MultiValueMap<String, String>> getFormData() {
		return this.formDataMono;
	}

	@Override
	public Mono<MultiValueMap<String, Part>> getMultipartData() {
		return multipartDataMono;
	}

	private static final List<HttpMessageReader<?>> MESSAGE_READERS = HandlerStrategies.withDefaults().messageReaders();

	private static final ResolvableType MULTIPART_DATA_TYPE = ResolvableType.forClassWithGenerics(MultiValueMap.class,
			String.class, Part.class);

	private static final Mono<MultiValueMap<String, Part>> EMPTY_MULTIPART_DATA = Mono
			.just(CollectionUtils.unmodifiableMultiValueMap(new LinkedMultiValueMap<String, Part>(0))).cache();

	/**
	 * FormFieldPart
	 * 
	 * @param request
	 * @param configurer
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static Mono<MultiValueMap<String, Part>> initMultipartData(ServerHttpRequest request,
			ServerCodecConfigurer configurer) {
		try {
			MediaType contentType = request.getHeaders().getContentType();
			if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(contentType)) {
				return ((HttpMessageReader<MultiValueMap<String, Part>>) MESSAGE_READERS.stream()
						.filter(reader -> reader.canRead(MULTIPART_DATA_TYPE, MediaType.MULTIPART_FORM_DATA))
						.findFirst()//
						.orElseThrow(() -> new IllegalStateException("No multipart HttpMessageReader.")))//
								.readMono(MULTIPART_DATA_TYPE, request, Collections.emptyMap())
								.switchIfEmpty(EMPTY_MULTIPART_DATA).cache();
			}
		} catch (Throwable ex) {
			log.warn(ex.getMessage(), ex);
		}
		return EMPTY_MULTIPART_DATA;
	}

	private static final ResolvableType FORM_DATA_TYPE = ResolvableType.forClassWithGenerics(MultiValueMap.class,
			String.class, String.class);

	private static final Mono<MultiValueMap<String, String>> EMPTY_FORM_DATA = Mono
			.just(CollectionUtils.unmodifiableMultiValueMap(new LinkedMultiValueMap<String, String>(0))).cache();

	@SuppressWarnings("unchecked")
	private static Mono<MultiValueMap<String, String>> initFormData(ServerHttpRequest request,
			ServerCodecConfigurer configurer) {
		try {
			MediaType contentType = request.getHeaders().getContentType();
			if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(contentType)) {
				return ((HttpMessageReader<MultiValueMap<String, String>>) MESSAGE_READERS.stream()
						.filter(reader -> reader.canRead(FORM_DATA_TYPE, MediaType.APPLICATION_FORM_URLENCODED))
						.findFirst()//
						.orElseThrow(() -> new IllegalStateException("No form data HttpMessageReader.")))
								.readMono(FORM_DATA_TYPE, request, Collections.emptyMap())
								.switchIfEmpty(EMPTY_FORM_DATA).cache();
			}
		} catch (InvalidMediaTypeException ex) {
			// Ignore
		}
		return EMPTY_FORM_DATA;
	}

	public static Mono<MultiValueMap<String, Object>> extractValuesToBind(ServerWebExchange exchange) {
		MultiValueMap<String, String> queryParams = exchange.getRequest().getQueryParams();
		Mono<MultiValueMap<String, String>> formData = exchange.getFormData();
		Mono<MultiValueMap<String, Part>> multipartData = exchange.getMultipartData();
		return Mono.zip(Mono.just(queryParams), formData, multipartData).map(tuple -> {
			MultiValueMap<String, Object> result = new LinkedMultiValueMap<>();
			tuple.getT1().forEach((key, values) -> addBindValue(result, key, values));
			tuple.getT2().forEach((key, values) -> addBindValue(result, key, values));
			tuple.getT3().forEach((key, values) -> addBindValue(result, key, values));
			return result;
		});
	}

	private static void addBindValue(MultiValueMap<String, Object> params, String key, List<?> values) {
		if (!CollectionUtils.isEmpty(values)) {
			if (!params.containsKey(key)) {
				params.put(key, new ArrayList<>());
			}
			List<Object> list = params.get(key);
			for (Object value : values) {
				if (value instanceof FormFieldPart) {
					list.add(((FormFieldPart) value).value());
				} else {
					list.add(value);
				}
			}
		}
	}

	/**
	 * 异步请求
	 * 
	 * @param exchange
	 * @return
	 */
	public static boolean xRequestedWith(ServerWebExchange exchange) {
		MediaType contentType = exchange.getRequest().getHeaders().getContentType();
		if (contentType != null && MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
			return true;
		}
		String requestType = exchange.getRequest().getHeaders().getFirst("X-Requested-With");
		if (requestType != null) {
			return true;
		}
		String uri = exchange.getRequest().getPath().value();
		if (uri.endsWith(".html")) {
			return false;
		}
		if (uri.endsWith(".htm")) {
			return false;
		}
		return true;
	}

	public static Mono<Void> response(ServerWebExchange exchange, HttpStatus status, String message) {
		return response(exchange, status, message, null);
	}

	public static Mono<Void> response(ServerWebExchange exchange, HttpStatus status, String message, Object data) {
		ServerHttpResponse response = exchange.getResponse();
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
		response.setStatusCode(status);
		try {
			Response<Object> resp = Response.of(status, message, data);
			resp.setPath(exchange.getRequest().getPath().value());
			String json = JSON.format(resp);
			return response.writeAndFlushWith(
					Flux.just(ByteBufFlux.just(response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8)))));
		} catch (Exception ex) {
			log.error("", ex);
		}
		return Mono.empty();
	}

	public static String getHeader(ServerWebExchange exchange, String name) {
		return exchange.getRequest().getHeaders().getFirst(name);
	}

	public static String getCookie(ServerWebExchange exchange, String name) {
		HttpCookie cookie = exchange.getRequest().getCookies().getFirst(name);
		if (cookie != null) {
			return cookie.getValue();
		}
		return null;
	}

	public static void clearCookie(ServerWebExchange exchange) {
		exchange.getResponse().getCookies().clear();
		setCookie(exchange, Token.ID, "", Duration.ofMillis(1));
		setCookie(exchange, Token.ACCESS, "", Duration.ofMillis(1));
		setCookie(exchange, Token.REFRESH, "", Duration.ofMillis(1));
	}

	public static void setCookie(ServerWebExchange exchange, String name, String value) {
		setCookie(exchange, name, value, Duration.ofMinutes(30));
	}

	public static void setCookie(ServerWebExchange exchange, String name, String value, Duration duration) {
//		exchange.getResponse().getHeaders().add(name, value);
		ResponseCookie cookie = ResponseCookie.from(name, value)//
				.path("/")//
				.maxAge(duration)//
				.build();
		exchange.getResponse().addCookie(cookie);
	}

	public static void setCookie(ServerWebExchange exchange, Token token) {
		token.sign();
		if (!BooleanUtils.isTrue(token.getRememberMe())) {
			setCookie(exchange, Token.ID, token.getTokenId(), Duration.ofMinutes(30));
			setCookie(exchange, Token.ACCESS, token.getAccess(), Duration.ofMinutes(30));
		} else {
			setCookie(exchange, Token.ID, token.getTokenId(), Duration.ofDays(7));
			setCookie(exchange, Token.ACCESS, token.getAccess(), Duration.ofDays(7));
			setCookie(exchange, Token.REFRESH, token.getRefresh(), Duration.ofDays(30));
		}
		exchange.getResponse().getHeaders().add(Token.ID, token.getTokenId());
		exchange.getResponse().getHeaders().add(Token.ACCESS, token.getAccess());
		exchange.getResponse().getHeaders().add(Token.REFRESH, token.getRefresh());
	}

	public static void setCookieId(ServerWebExchange exchange, String id) {
		setCookie(exchange, Token.ID, id, Duration.ofMinutes(30));
	}

	public static String getCookieId(ServerWebExchange exchange) {
		String id = getCookie(exchange, Token.ID);
		if (StringUtils.isBlank(id)) {
			id = Token.nextId();
			setCookieId(exchange, id);
		}
		return id;
	}

	public static Token getCookie(ServerWebExchange exchange) {
		String access = getHeader(exchange, Token.ACCESS);
		String refresh = getHeader(exchange, Token.REFRESH);
		if (StringUtils.isBlank(access)) {
			access = getCookie(exchange, Token.ACCESS);
		}
		if (StringUtils.isBlank(refresh)) {
			refresh = getCookie(exchange, Token.REFRESH);
		}
		Token token = null;
		try {
			if (StringUtils.isNotBlank(access)) {
				token = Token.verify(access);
				token.setRefreshed(false);
				if (StringUtils.isBlank(refresh)) {
					setCookie(exchange, token);
				}
			}
		} catch (RuntimeException e) {
			if (log.isDebugEnabled()) {
				log.debug(e.getMessage());
			}
		}
		if (token == null) {
			try {
				if (StringUtils.isNotBlank(refresh)) {
					token = Token.verify(refresh);
					token.setRefreshed(true);
					setCookie(exchange, token);
				}
			} catch (RuntimeException ex) {
				if (log.isDebugEnabled()) {
					log.debug(ex.getMessage());
				}
			}
		}
		if (token != null) {
			token.setRefresh(refresh);
		}
		return token;
	}

	public static String getOriginHost(ServerHttpRequest request) {
		String host = request.getHeaders().getFirst("x-forwarded-host");
		if (StringUtils.isNotEmpty(host)) {
			return host;
		}
		host = request.getHeaders().getFirst("host");
		return host;
	}

	public static String getHost(ServerHttpRequest request) {
		String requestURL = request.getPath().value();
		String host = requestURL.split("//")[1].split("/")[0];
		return host;
	}

	public static String getRemoteIP(ServerHttpRequest request) {
		String forwarded = getAddress(request);
		if (ipNotBlank(forwarded)) {
			// 多次反向代理后会有多个ip值，第一个ip才是真实ip
			int index = forwarded.indexOf(",");
			if (index != -1) {
				forwarded = forwarded.substring(0, index);
			}
		}
		if (forwarded != null && forwarded.indexOf("0:0:") >= 0) {
			forwarded = "127.0.0.1";
		}
		return forwarded;
	}

	public static String getForwardedIP(ServerHttpRequest request) {
		String forwarded = getAddress(request);
		if (forwarded != null && forwarded.indexOf("0:0:") >= 0) {
			forwarded = "127.0.0.1";
		}
		return forwarded;
	}

	private static boolean ipNotBlank(String forwarded) {
		return StringUtils.isNotBlank(forwarded) && !"unKnown".equalsIgnoreCase(forwarded);
	}

	private static String getAddress(ServerHttpRequest request) {
		HttpHeaders headers = request.getHeaders();
		String forwarded = headers.getFirst("X-Forwarded-For");
		if (ipNotBlank(forwarded)) {
			return forwarded;
		}
		forwarded = headers.getFirst("X-Real-IP");
		if (ipNotBlank(forwarded)) {
			return forwarded;
		}
		forwarded = headers.getFirst("Proxy-Client-IP");
		if (ipNotBlank(forwarded)) {
			return forwarded;
		}
		forwarded = headers.getFirst("WL-Proxy-Client-IP");
		if (ipNotBlank(forwarded)) {
			return forwarded;
		}
		forwarded = headers.getFirst("HTTP_CLIENT_IP");
		if (ipNotBlank(forwarded)) {
			return forwarded;
		}
		forwarded = headers.getFirst("HTTP_X_FORWARDED_FOR");
		if (ipNotBlank(forwarded)) {
			return forwarded;
		}
		forwarded = headers.getFirst("cf-connecting-ip");
		if (ipNotBlank(forwarded)) {
			return forwarded;
		}
		forwarded = request.getRemoteAddress().getAddress().getHostAddress();
		if (ipNotBlank(forwarded)) {
			return forwarded;
		}
		return forwarded;
	}

}
