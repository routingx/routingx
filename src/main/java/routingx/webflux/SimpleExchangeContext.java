package routingx.webflux;

import java.util.function.Function;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import routingx.model.Token;

@Slf4j
public class SimpleExchangeContext {

	private static final String CONTEXT_KEY = SimpleExchangeContext.class.getName();

	private static final SimpleExchangeContext EMTITY = new SimpleExchangeContext();

	protected static Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain chain) {
		return chain.filter(serverWebExchange).contextWrite(of(serverWebExchange));
	}

	public static Context of(SimpleExchangeContext ctx) {
		return Context.of(CONTEXT_KEY, ctx);
	}

	public static Context of(ServerWebExchange serverWebExchange) {
		SimpleExchangeContext context = new SimpleExchangeContext(serverWebExchange);
		return Context.of(CONTEXT_KEY, context);
	}

	public static Context of(ServerWebExchange serverWebExchange, Token token) {
		SimpleExchangeContext context = new SimpleExchangeContext(serverWebExchange, token);
		return Context.of(CONTEXT_KEY, context);
	}

	public static Mono<SimpleExchangeContext> get() {
		return Mono.deferContextual(Mono::just).handle((context, sink) -> {
			if (context.hasKey(CONTEXT_KEY)) {
				SimpleExchangeContext ctx = context.get(CONTEXT_KEY);
				if (log.isDebugEnabled()) {
					log.debug("get " + ctx.toString());
				}
				sink.next(ctx);
			} else {
				sink.next(EMTITY);
			}
		});
	}

	public static final <R> Mono<R> context(Function<SimpleExchangeContext, Mono<R>> function) {
		return get().flatMap(context -> function.apply(context));
	}

	public static final <R> Mono<R> exchange(Function<ServerWebExchange, Mono<R>> function) {
		return get().flatMap(context -> function.apply(context.getExchange()));
	}

	public static final <R> Mono<R> token(Function<Token, Mono<R>> function) {
		return get().flatMap(context -> function.apply(context.getToken()));
	}

	public static Function<Context, Context> clear() {
		return context -> context.delete(CONTEXT_KEY);
	}

	private ServerWebExchange exchange;
	private Token token;

	private SimpleExchangeContext() {
	}

	private SimpleExchangeContext(ServerWebExchange serverWebExchange) {
		this.setExchange(serverWebExchange);
	}

	private SimpleExchangeContext(ServerWebExchange serverWebExchange, Token token) {
		this.setExchange(serverWebExchange);
		this.setToken(token);
	}

	public void setExchange(ServerWebExchange serverWebExchange) {
		this.exchange = serverWebExchange;
		if (exchange != null) {
			token = SimpleExchange.getCookie(exchange);
			if (log.isDebugEnabled()) {
				log.debug("set " + this.toString());
			}
		}
	}

	public ServerWebExchange getExchange() {
		return this.exchange;
	}

	public Token getToken() {
		return token != null ? token : Token.empty();
	}

	public void setToken(Token token) {
		this.token = token;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName());
		sb.append("@" + this.hashCode());
		sb.append("[");
		sb.append(token);
		if (exchange != null) {
			sb.append(" ");
			sb.append(exchange.getRequest().getPath().value());
			sb.append(" ");
			sb.append(exchange);
		}
		sb.append("]");
		return sb.toString();
	}

}
