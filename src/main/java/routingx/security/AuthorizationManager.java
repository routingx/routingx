package routingx.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import routingx.Note;
import routingx.model.Token;

@Note("鉴权服务，通过鉴权的才能继续访问请求")
@Component
@Slf4j
public class AuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

	private PathMatcher pathMatcher = new AntPathMatcher();

	@Autowired
	private SecurityDetailsService securityService;

	public void defaultAuthorities(String... antPatterns) {
		securityService.defaultAuthorities(antPatterns);
	}

	public void defaultAuthoritiesClear() {
		securityService.defaultAuthoritiesClear();
	}

	public void cacheClear(Token token) {
		securityService.cacheClear(token);
	}

	public void setPathMatcher(PathMatcher pathMatcher) {
		this.pathMatcher = pathMatcher;
	}

	@Override
	public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext context) {
		return authentication.flatMap(auth -> {
			if (!auth.isAuthenticated()) {
				return Mono.just(new AuthorizationDecision(false));
			} else {
				final Token token = (Token) auth.getDetails();
				return securityService.getCache(token).flatMap(user -> check(context, token, user));
			}
		});
	}

	private Mono<AuthorizationDecision> check(AuthorizationContext context, Token token, SecurityUser user) {
		if (!user.getAuthenticated()) {
			return Mono.just(new AuthorizationDecision(false));
		}
//		if (!token.getTokenId().equals(user.getTokenId())) {
//			AccessDeniedException ex = new AccessDeniedException(HttpStatus.UNAUTHORIZED.getReasonPhrase(),
//					CustomException.of(HttpStatus.UNAUTHORIZED));
//			SimpleExchange.clearCookie(context.getExchange());
//			return Mono.error(ex);
//			// return Mono.just(new AuthorizationDecision(false));
//		}
		ServerHttpRequest request = context.getExchange().getRequest();
		final String uri = request.getPath().pathWithinApplication().value();
		for (GrantedAuthority ga : user.getAuthorities()) {
			String pattern = ga.getAuthority();
			if (pathMatcher.match(pattern, uri)) {
				if (log.isDebugEnabled()) {
					MediaType contentType = context.getExchange().getResponse().getHeaders().getContentType();
					log.debug(" check {} {} {}", true, uri, contentType == null ? "" : contentType);
				}
				return Mono.just(new AuthorizationDecision(true));
			}
		}
		if (log.isDebugEnabled()) {
			log.debug(" check {} {} ", false, uri);
		}
		return Mono.just(new AuthorizationDecision(false));
	}

	@Note("登录/退出/注册日志")
	public void logLogin(ServerWebExchange exchange, Token token, String memo, Throwable error) {
		if (token == null) {
			return;
		}
		securityService.logLogin(exchange, token, memo, error);
	}
}
