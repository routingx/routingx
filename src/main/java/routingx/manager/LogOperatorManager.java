package routingx.manager;

import reactor.core.publisher.Mono;
import routingx.model.LogOperator;

public interface LogOperatorManager {
	Mono<LogOperator> insert(LogOperator entity);
}
