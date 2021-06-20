package routingx.security;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.web.server.WebFilter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class SecurityConfig {
	private final static String PAGE_INDEX = "/index.html";
	public final static String PAGE_LOGOUT = "/logout";
	public final static String PAGE_LOGIN = "/login";

	public final static String LOGIN = "login";
	public final static String LOGOUT = "logout";
	public final static String NONE = "/none";
	public final static String GUEST = "/guest";

	// security的鉴权排除列表
	private static final String[] excludedPages = { //
			"/static/**", "/", "/*.html", "**.js", "**.ico", //
			PAGE_INDEX, PAGE_LOGIN, PAGE_LOGOUT, //
			NONE + "/**", "/*" + NONE + "/**" };

	// 登录可访问
	private static final String[] defaultAuthorities = { //
			"/swagger-ui/**", "/swagger-resources/**", //
			"/v2/api-docs", "/v3/api-docs", "/*/v2/api-docs", "/*/v3/api-docs", //
			GUEST + "/**", "/*" + GUEST + "/**", //
			"/ems/**", //
	};

	public SecurityWebFilterChain securityWebFilterChain(@Autowired ServerHttpSecurity http,
			@Autowired SecurityContextRepository contextRepository,
			@Autowired AuthenticationManager authenticationManager,
			@Autowired AuthorizationManager authorizationManager) throws Exception {
		authorizationManager.defaultAuthorities(defaultAuthorities);
		final AuthenticationHandler handler = new AuthenticationHandler(authorizationManager);
		http.authorizeExchange()
				// 定义无需鉴权的请求路径
				.pathMatchers(excludedPages).permitAll()
				// option 请求默认放行
				.pathMatchers(HttpMethod.OPTIONS).permitAll()

				// 定义需要鉴权的请求路径
				.and().authorizeExchange().pathMatchers("/**")
				// 自定义的鉴权服务
				.access(authorizationManager).anyExchange().authenticated()
				// .and().httpBasic()
				// 登入
				.and().formLogin().loginPage(getLoginPage())
				// 定义授权对象
				.authenticationManager(authenticationManager)
				// 登入认证成功
				.authenticationSuccessHandler(handler)
				// 登入认证失败
				.authenticationFailureHandler(handler)
				// 为了支持jwt 自定义了这个类
				.and().securityContextRepository(contextRepository).exceptionHandling()
				// 未登录访问资源时的处理类，若无此处理类，前端页面会弹出登录窗口
				.authenticationEntryPoint(handler)
				// 访问被拒绝时自定义处理器
				.and().exceptionHandling().accessDeniedHandler(handler) //
				.and().logout().logoutUrl(getLogoutPage()).logoutSuccessHandler(handler)// 登出
				.and().csrf().disable()// 必须支持跨域
				.cors().and();// 跨域配置
		SecurityWebFilterChain chain = http.build();
		Iterator<WebFilter> weIterable = chain.getWebFilters().toIterable().iterator();
		final AuthenticationConverter converter = new AuthenticationConverter();
		while (weIterable.hasNext()) {
			WebFilter f = weIterable.next();
			if (log.isDebugEnabled()) {
				log.debug(f.toString());
			}
			if (f instanceof AuthenticationWebFilter) {
				AuthenticationWebFilter webFilter = (AuthenticationWebFilter) f;
				webFilter.setServerAuthenticationConverter(converter);
			}
		}
		return chain;
	}

	protected String getLoginPage() {
		return PAGE_LOGIN;
	}

	protected String getLogoutPage() {
		return PAGE_LOGOUT;
	}
}
