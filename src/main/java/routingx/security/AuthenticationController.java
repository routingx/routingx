package routingx.security;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import reactor.core.publisher.Mono;
import routingx.Response;
import routingx.model.Token;
import routingx.webflux.ResponseMono;
import routingx.webflux.SimpleExchange;
import routingx.webflux.WebAbsContext;

@Api(tags = "100.授权接口(登录、登出)")
@Controller
@RequestMapping(name = "授权接口")
public class AuthenticationController extends WebAbsContext {

	@Autowired
	private AuthenticationManager authenticationManage;

	@GetMapping(name = "首页")
	public Mono<String> index(final Model model) {
		return Mono.just("redirect:/index.html");
	}

	@ApiOperation(value = "登录页面")
	@GetMapping(name = "登录页面", value = SecurityConfig.PAGE_LOGIN)
	public Mono<String> login(final Model model) {
		return token(token -> {
			if (token != null) {
				return Mono.just("redirect:/index.html");
			}
			return Mono.create(monoSink -> monoSink.success(SecurityConfig.LOGIN));
		});
	}

	@ApiOperation(value = "登录接口")
	@PostMapping(name = "登录接口", value = SecurityConfig.PAGE_LOGIN)
	public Mono<Response<Token>> login(@RequestBody final LoginForm form) {
		return authenticationManage.authenticate(AuthenticationToken.unauthed(form.token())).flatMap(auth -> {
			return ResponseMono.ok(((AuthenticationToken) auth).getDetails());
		});
	}

	@ApiOperation(value = "确认退出")
	@PostMapping(name = "确认退出", value = SecurityConfig.PAGE_LOGOUT)
	public Mono<Response<String>> logout() {
		return ResponseMono.ok("退出成功");
	}

	@GetMapping("/context")
	@ResponseBody
	public Mono<Map<String, Object>> context() throws Exception {
		Map<String, Object> map = new HashMap<>();
		return context(context -> {
			map.put("id", SimpleExchange.getCookieId(context.getExchange()));
			map.put("token", context.getToken());
			map.put("webExchange", context.getExchange().toString());
			return Mono.just(map);
		});
	}

	@Autowired
	private ApplicationContext context;

	@Autowired
	private ScheduledExecutorService executor;

	@ApiOperation(value = "关闭服务")
	@GetMapping("/shutdown")
	public Mono<Response<String>> shutDownContext() {
		executor.execute(this::shutdown);
		return ResponseMono.ok("shutdowning");
	}

	private void shutdown() {
		ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) context;
		ctx.close();
	}
}
