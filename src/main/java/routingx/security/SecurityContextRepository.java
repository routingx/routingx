package routingx.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import routingx.UserAgent;
import routingx.model.Token;
import routingx.webflux.SimpleExchange;
import routingx.webflux.SimpleExchangeContext;

@Component
@Slf4j
public class SecurityContextRepository implements ServerSecurityContextRepository {

	public static final String SECURITY_CONTEXT = "SECURITY_CONTEXT";

	@Autowired
	private AuthenticationManager authenticationManager;

	/**
	 * 
	 * @see 授权成功调用
	 * 
	 * @See 退出登录调用,退出时 context == null
	 */
	@Override
	public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
		return SimpleExchangeContext.context(ctx -> {
			if (context != null) {// 登录成功
				Token token = (Token) context.getAuthentication().getDetails();
				log.info("{} 登录成功", token.getUsername());
				SimpleExchange.setCookie(exchange, token);
				ctx.setToken(token);
			} else {// 退出登录
				SimpleExchange.clearCookie(exchange);
			}
			return Mono.empty();
		});
	}

	@Override
	public Mono<SecurityContext> load(ServerWebExchange exchange) {
		return SimpleExchangeContext.context(context -> loadContext(exchange, context));
	}

	private Mono<SecurityContext> loadContext(ServerWebExchange exchange, SimpleExchangeContext ctx) {
		Token form = ctx.getToken();
		if (form == null || form.isEmpty()) {
			log.info(exchange.getRequest().getPath().value());
			return Mono.empty();
		}
		return authenticationManager.check(form)//
				.doOnError(error -> SimpleExchange.clearCookie(exchange))//
				.flatMap(context -> authenticate(exchange, ctx, form));
	}

	private Mono<SecurityContext> authenticate(ServerWebExchange exchange, SimpleExchangeContext ctx, Token form) {
		if (!form.refresh()) {
			return Mono.just(new SecurityContextImpl(AuthenticationToken.authed(form)));
		}
		UserAgent ua = UserAgent.parse(exchange.getRequest().getHeaders().getFirst(UserAgent.NAME));
		form.setUserAgent(ua);
		form.setDeviceInfo(ua.getDeviceInfo());
		return authenticationManager.authenticate(AuthenticationToken.unauthed(form)).flatMap(auth -> {
			Token token = (Token) auth.getDetails();
			SimpleExchange.setCookie(exchange, token);
			ctx.setToken(token);
			return Mono.just(new SecurityContextImpl(auth));
		});
	}
}