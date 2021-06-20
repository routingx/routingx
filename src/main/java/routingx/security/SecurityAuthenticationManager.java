package routingx.security;

import java.util.List;

import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
import routingx.Note;
import routingx.model.Conf;
import routingx.model.Token;
import routingx.model.User;

public interface SecurityAuthenticationManager {

	@Note("查询用户基本信息")
	Mono<User> findByAccount(String account);

	@Note("登录/退出/注册日志")
	void logLogin(ServerWebExchange exchange, Token token, String memo, Throwable error);

	@Note("取用户的权限数据")
	Mono<User> findAuthorities(Token token);

	Mono<List<Conf>> findConf(Conf form);

	Mono<Conf> insert(Conf conf);
}
