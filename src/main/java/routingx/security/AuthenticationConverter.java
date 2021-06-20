package routingx.security;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import routingx.UserAgent;
import routingx.json.JSON;
import routingx.model.PlatformEnum;
import routingx.model.Token;
import routingx.webflux.BufferUtils;
import routingx.webflux.SimpleExchange;

@Setter
@Getter
@Slf4j
public class AuthenticationConverter implements ServerAuthenticationConverter {

	private String usernameParameter = "username";

	private String passwordParameter = "password";

	private String rememberMeParameter = "rememberMe";

	private String platformParameter = "platform";

	private String captchaParameter = "captcha";

	@Override
	public Mono<Authentication> convert(ServerWebExchange exchange) {
		if (SecurityConfig.LOGIN.equals(exchange.getRequest().getPath().value())) {
			SimpleExchange.clearCookie(exchange);
		}
		MediaType contentType = exchange.getRequest().getHeaders().getContentType();
		if (MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
			return convert8Body(exchange);
		} else {
			return SimpleExchange.extractValuesToBind(exchange).flatMap(data -> convert8Form(exchange, data));
		}
	}

	private Mono<Authentication> convert8Form(ServerWebExchange exchange, MultiValueMap<String, Object> data) {
		Map<String, Object> map = new HashMap<>();
		data.keySet().forEach(key -> {
			map.put(key, data.getFirst(key));
		});
		Token form = convertToken(map);
		if (StringUtils.isNotBlank(form.getAccount()) && StringUtils.isNotBlank(form.getPassword())) {
			return convert(exchange, form);
		}
		return convert8Body(exchange);
	}

	private Mono<Authentication> convert8Body(ServerWebExchange exchange) {
		long contentLength = exchange.getRequest().getHeaders().getContentLength();
		ServerHttpRequest request = exchange.getRequest();
		MediaType contentType = request.getHeaders().getContentType();
		if (contentLength <= 0 || MediaType.MULTIPART_FORM_DATA.isCompatibleWith(contentType)
				|| MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(contentType)) {
			return convert8Header(exchange);
		}
		return BufferUtils.join(exchange.getRequest()).flatMap(buffer -> {
			Map<String, Object> data = JSON.parseMap(buffer);
			Token form = convertToken(data);
			if (StringUtils.isNotBlank(form.getAccount()) && StringUtils.isNotBlank(form.getPassword())) {
				return convert(exchange, form);
			} else {
				return convert8Header(exchange);
			}
		});
	}

	private Token convertToken(Map<String, Object> data) {
		String rememberMe = (String) data.remove(rememberMeParameter);
		String json = JSON.format(data);
		if (log.isDebugEnabled()) {
			log.debug(json);
		}
		Token form = JSON.parseObject(json, Token.class);
		if (form == null) {
			form = new Token();
		}
		form.setAccount((String) data.get(usernameParameter));
		form.setPassword((String) data.get(passwordParameter));
		form.setCaptcha((String) data.get(captchaParameter));
		form.setPlatform(PlatformEnum.of((String) data.get(platformParameter)));
		form.setRememberMe(BooleanUtils.toBoolean(rememberMe));
		return form;
	}

	private Mono<Authentication> convert8Header(ServerWebExchange exchange) {
		Token form = null; // SimpleExchange.getCookie(exchange);
		return convert(exchange, (form == null ? Token.empty() : form));
	}

	private Mono<Authentication> convert(ServerWebExchange exchange, Token form) {
		UserAgent ua = UserAgent.parse(exchange.getRequest().getHeaders().getFirst(UserAgent.NAME));
		form.setUserAgent(ua);
		form.setDeviceInfo(ua.getDeviceInfo());
		form.setRefreshed(true);
		return Mono.just(AuthenticationToken.unauthed(form));
	}

}
