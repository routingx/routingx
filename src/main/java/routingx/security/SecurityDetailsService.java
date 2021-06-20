package routingx.security;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import routingx.Note;
import routingx.ThreadExecutor;
import routingx.cache.Cache;
import routingx.json.JSON;
import routingx.model.Conf;
import routingx.model.Token;

@Component
@Slf4j
public final class SecurityDetailsService implements ReactiveUserDetailsService {

	public final static String MULTI_LOGIN = "MULTI_LOGIN";
	public final static String AUTHORITIES_CACHE_TIME = "AUTHORITIES_CACHE_TIME";

	@Note("登录可访问，不需要鉴权")
	private final List<GrantedAuthority> defaultAuthorities = new ArrayList<>();

	@Note("已登录用户数据缓存")
	private final Cache<Token, SecurityUser> authoritiesCache = new Cache<>(5000L);

	@Note("系统参数[conf]缓存：Cache<code, value>")
	private final Cache<String, String> confCache = new Cache<>(60000L);

	@Autowired
	private SecurityAuthenticationManager authenticationService;

	@Autowired
	private UserPasswordEncoder passwordEncoder;

	public SecurityDetailsService() {
		authoritiesCache.setLoading((token, df) -> load(token));
		confCache.setLoading((token, defaultValue) -> load(token, defaultValue));
	}

	@Note("加载用户权限")
	private Mono<SecurityUser> load(Token token) {
		if (log.isDebugEnabled()) {
			log.debug(token.toString());
		}
		return authenticationService.findAuthorities(token)//
				.flatMap(user -> loadAuthorities(token, SecurityUser.clone(user)));
	}

	@Note("登录可访问的权限，不需要鉴权")
	private Mono<SecurityUser> loadAuthorities(Token token, SecurityUser user) {
		return loadAuthorizedAuthorities().flatMap(authorizedAuthorities -> {
			if (CollectionUtils.isEmpty(authorizedAuthorities)) {
				authorizedAuthorities = defaultAuthorities;
			}
			user.addAuthorities(authorizedAuthorities);
			if (log.isDebugEnabled()) {
				log.debug("{} {}", token.getUserId(), JSON.format(user.getAuthorities()));
			}
			return Mono.just(user);
		});
	}

	@Note("登录可访问的权限，不需要鉴权")
	private Mono<List<GrantedAuthority>> loadAuthorizedAuthorities() {
		return findAuthorizedAuthorities().flatMap(authorities -> {
			List<GrantedAuthority> authoritieList = new ArrayList<>();
			for (String pattern : authorities) {
				authoritieList.add(new SimpleGrantedAuthority(pattern));
			}
			return Mono.just(authoritieList);
		});
	}

	public void defaultAuthorities(String... antPatterns) {
		for (String pattern : antPatterns) {
			defaultAuthorities.add(new SimpleGrantedAuthority(pattern));
		}
	}

	public void defaultAuthoritiesClear() {
		defaultAuthorities.clear();
	}

	public void cacheClear(Token token) {
		authoritiesCache.remove(token);
	}

	private Mono<String> load(String code, String defaultValue) {
		Conf form = new Conf();
		form.setCode(code);
		return authenticationService.findConf(form).flatMap(confList -> {
			if (confList.size() > 0) {
				return Mono.just(confList.get(0).getValue());
			} else {
				Conf conf = new Conf();
				conf.setCode(code);
				conf.setName(code);
				conf.setValue(defaultValue);
				ThreadExecutor.execute(() -> authenticationService.insert(conf).subscribe());
				return Mono.just(conf.getValue());
			}
		});
	}

	public Mono<String> getCache(String code) {
		return confCache.get(code);
	}

	public Mono<String> getCache(String code, String defaultValue) {
		return confCache.get(code, defaultValue);
	}

	@Note("同时只允许一个登录")
	public Mono<Boolean> singleLogin() {
		return getCache(MULTI_LOGIN, "Yes")//
				.flatMap(bool -> Mono.just(!BooleanUtils.toBoolean(bool)));
	}

	@Note("权限数据缓存时长(豪秒)")
	public Mono<Long> getGuthoritiesCacheTime() {
		return getCache(AUTHORITIES_CACHE_TIME, "600000")//
				.flatMap(number -> Mono.just(NumberUtils.toLong(number)));
	}

	@Note("登录可访问的权限，不需要鉴权")
	public Mono<List<String>> findAuthorizedAuthorities() {
		List<String> authorities = new ArrayList<>();
		return Mono.just(authorities);
	}

	public Mono<SecurityUser> getCache(Token token) {
		return getGuthoritiesCacheTime().flatMap(time -> {
			authoritiesCache.setExpireAfter(time);
			return authoritiesCache.get(token);
		});
	}

	@Override
	public Mono<UserDetails> findByUsername(String account) {
		return getByUsername(account).flatMap(user -> Mono.justOrEmpty(user));
	}

	public Mono<SecurityUser> getByUsername(String account) {
		return authenticationService.findByAccount(account).flatMap(u -> SecurityUser.just(u));
	}

	public boolean matches(SecurityUser user, String password) {
		return passwordEncoder.matches(user, password);
	}

	public void logLogin(ServerWebExchange exchange, Token token, String memo, Throwable error) {
		authenticationService.logLogin(exchange, token, memo, error);
	}

}
