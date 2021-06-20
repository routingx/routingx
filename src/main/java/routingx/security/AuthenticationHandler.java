package routingx.security;

import java.net.URI;
import java.nio.charset.Charset;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import routingx.CustomException;
import routingx.model.Token;
import routingx.webflux.SimpleExchange;
import routingx.webflux.SimpleExchangeContext;

/**
 * 授权失败处理器
 * 
 * @author peixere
 *
 */
@Slf4j
public class AuthenticationHandler implements
		// 登录成功
		ServerAuthenticationSuccessHandler,
		// 登录失败
		ServerAuthenticationFailureHandler,
		// 退出成功
		ServerLogoutSuccessHandler,
		// 无权限访问
		ServerAccessDeniedHandler,
		// 未登录的请求
		ServerAuthenticationEntryPoint {

	private final URI location;

	private final AuthorizationManager authorizationManager;

	private ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

	public AuthenticationHandler(AuthorizationManager authorizationManager) {
		this.location = URI.create(SecurityConfig.PAGE_LOGIN);
		this.authorizationManager = authorizationManager;
	}

	public void setRedirectStrategy(ServerRedirectStrategy redirectStrategy) {
		Assert.notNull(redirectStrategy, "redirectStrategy cannot be null");
		this.redirectStrategy = redirectStrategy;
	}

	/**
	 * 授权成功
	 */
	@Override
	public Mono<Void> onAuthenticationSuccess(WebFilterExchange exchange, Authentication authentication) {
		return SimpleExchangeContext
				.context(context -> onAuthenticationSuccess(exchange.getExchange(), authentication, context));

	}

	private Mono<Void> onAuthenticationSuccess(ServerWebExchange exchange, Authentication authentication,
			SimpleExchangeContext context) {
		final Token token = (Token) authentication.getDetails();
		authorizationManager.cacheClear(token);
		context.setToken(token);
		if (SimpleExchange.xRequestedWith(exchange)) {
			return SimpleExchange.response(exchange, HttpStatus.OK, HttpStatus.OK.name(), authentication.getDetails());
		} else {
			return this.redirectStrategy.sendRedirect(exchange, location);
		}
	}

	/**
	 * 授权失败
	 */
	@Override
	public Mono<Void> onAuthenticationFailure(WebFilterExchange exchange, AuthenticationException e) {
		SimpleExchange.clearCookie(exchange.getExchange());
		if (SimpleExchange.xRequestedWith(exchange.getExchange())) {
			return SimpleExchange.response(exchange.getExchange(), HttpStatus.UNAUTHORIZED, e.getMessage());
		} else {
			log.error("授权失败: {}", e.getMessage());
			return this.redirectStrategy.sendRedirect(exchange.getExchange(), location);
		}
	}

	/**
	 * 登出成功
	 */
	@Override
	public Mono<Void> onLogoutSuccess(WebFilterExchange filterExchange, Authentication authentication) {
		final ServerWebExchange exchange = filterExchange.getExchange();
		final Token token = (Token) authentication.getDetails();
		if (token != null) {
			token.setTokenId(Token.OFF);
			authorizationManager.logLogin(exchange, token, "登出成功", null);
			authorizationManager.cacheClear(token);
			if (SimpleExchange.xRequestedWith(exchange)) {
				return SimpleExchange.response(exchange, HttpStatus.OK, "退出成功");
			} else {
				return redirectStrategy.sendRedirect(exchange, location);
			}
		} else {
			return SimpleExchange.response(exchange, HttpStatus.UNAUTHORIZED, "退出失败，登录信息不能为空");
		}
	}

	/**
	 * 无权限访问
	 */
	@Override
	public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException e) {
		if (SimpleExchange.xRequestedWith(exchange)) {
			HttpStatus status = HttpStatus.FORBIDDEN;
			if (e.getCause() instanceof CustomException) {
				CustomException cause = (CustomException) e.getCause();
				status = cause.getStatus();
			}
			return SimpleExchange.response(exchange, status, e.getMessage());
		} else {
			return Mono.defer(() -> Mono.just(exchange.getResponse())).flatMap(response -> {
				if (e.getCause() instanceof CustomException) {
					CustomException cause = (CustomException) e.getCause();
					response.setStatusCode(cause.getStatus());
				} else {
					response.setStatusCode(HttpStatus.FORBIDDEN);
				}
				response.getHeaders().setContentType(MediaType.TEXT_PLAIN);
				DataBufferFactory dataBufferFactory = response.bufferFactory();
				DataBuffer buffer = dataBufferFactory.wrap(e.getMessage().getBytes(Charset.defaultCharset()));
				return response.writeWith(Mono.just(buffer)).doOnError(error -> DataBufferUtils.release(buffer));
			});
		}
	}

	/**
	 * 未登录的请求
	 */
	@Override
	public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException e) {
		if (SimpleExchange.xRequestedWith(exchange)) {
			return SimpleExchange.response(exchange, HttpStatus.UNAUTHORIZED, e.getMessage());
		} else {
			return this.redirectStrategy.sendRedirect(exchange, location);
		}
	}
}
