package routingx.security;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import routingx.Note;
import routingx.model.Token;
import routingx.webflux.SimpleExchangeContext;

/**
 * 授权管理器
 */
@Component
@Slf4j
@Setter
@Getter
public class AuthenticationManager implements ReactiveAuthenticationManager {

	@Autowired
	private SecurityDetailsService securityService;

	@Override
	public Mono<Authentication> authenticate(Authentication authentication) {
		return SimpleExchangeContext.exchange(exchange -> {
			return doAuthenticate(authentication).doOnSuccess(auth -> {
				logLogin(exchange, auth, null);
			}).doOnError(error -> {
				logLogin(exchange, authentication, error);
			});
		});

	}

	private Mono<Authentication> doAuthenticate(Authentication authentication) {
		if (StringUtils.isBlank(authentication.getName())) {
			return Mono.error(new BadCredentialsException("用户不能为空"));
		}
		return securityService.getByUsername(authentication.getName())//
				.switchIfEmpty(Mono.error(new BadCredentialsException("请输入正确的用户和密码")))//
				.flatMap(user -> authenticate(user, authentication));
	}

	private void logLogin(ServerWebExchange exchange, Authentication authentication, Throwable error) {
		Token form = ((AuthenticationToken) authentication).getDetails();
		if (form.getRefreshed()) {
			securityService.logLogin(exchange, form, "登录成功", error);
		}
	}

	/**
	 * 授权
	 * 
	 * @param authentication
	 * @return
	 */
	private Mono<Authentication> authenticate(SecurityUser user, Authentication authentication) {
		try {
			AuthenticationToken authenticationToken = ((AuthenticationToken) authentication);
			Token form = authenticationToken.getDetails();
			user.setUserAgent(form.getUserAgent());
			user.setRefreshed(BooleanUtils.isTrue(form.getRefreshed()));
			authenticationToken.setDetails(user);
			if (!user.isEnabled()) {
				throw (new DisabledException("该账户已被禁用，请联系管理员"));
			} else if (!user.isAccountNonLocked()) {
				throw (new LockedException("该账号已被锁定"));
			} else if (!user.isAccountNonExpired()) {
				throw (new AccountExpiredException("该账号已过期，请联系管理员"));
			} else if (!user.isCredentialsNonExpired()) {
				throw (new CredentialsExpiredException("该账户的登录凭证已过期，请重新登录"));
			} else if (!securityService.matches(user, form.getPassword())) {
				throw (new BadCredentialsException("用户不存在或者密码错误"));
			}
			return Mono.just(AuthenticationToken.authed(form.authed(user)));
		} catch (AuthenticationException ex) {
			log.warn(" authenticate {} {}", user.getUsername(), ex.getMessage());
			return Mono.error(ex);
		} catch (Throwable ex) {
			log.error(" authenticate error " + user.getUsername() + " " + ex.getMessage(), ex);
			return Mono.error(new BadCredentialsException("授权验证失败，未知程序异常", ex));
		}
	}

	@Note("检查TOKEN是否与用户信息一致")
	public Mono<SecurityContext> check(Token token) {
		return securityService.getCache(token).flatMap(user -> check(token, SecurityUser.clone(user)));
	}

	private Mono<SecurityContext> check(Token token, SecurityUser user) {
		if (!user.getAuthenticated() //
				|| !token.getTenantId().equals(user.getTenantId())) {
			return Mono.error(new AccountExpiredException("用户信息已经变更，请重新登录"));
		}
		return securityService.singleLogin().flatMap(bool -> {
			if (bool && !token.getTokenId().equals(user.getTokenId())) {
				return Mono.error(new AccountExpiredException("用户已经在别的地方登录"));
			} else {
				return authenticate(user, AuthenticationToken.authed(token))//
						.flatMap(authed -> Mono.just(new SecurityContextImpl(authed)));
			}
		});
	}
}
