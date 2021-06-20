package routingx.webflux;

import java.util.function.Function;

import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
import routingx.model.Token;

public abstract class WebAbsContext {

	protected final <R> Mono<R> context(Function<SimpleExchangeContext, Mono<R>> function) {
		return SimpleExchangeContext.get().flatMap(context -> function.apply(context));
	}

	protected final <R> Mono<R> exchange(Function<ServerWebExchange, Mono<R>> function) {
		return SimpleExchangeContext.get().flatMap(context -> function.apply(context.getExchange()));
	}

	protected final <R> Mono<R> token(Function<Token, Mono<R>> function) {
		return SimpleExchangeContext.get().flatMap(context -> function.apply(context.getToken()));
	}
}
