package routingx.manager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import routingx.CustomException;
import routingx.Response;
import routingx.ThreadExecutor;
import routingx.json.JSON;
import routingx.model.LogOperator;
import routingx.model.Token;
import routingx.utils.TextUtils;
import routingx.webflux.SimpleExchange;
import routingx.webflux.SimpleExchangeContext;
import routingx.webflux.WebAbsContext;

@Slf4j
public abstract class AspectController extends WebAbsContext implements Ordered {

	@Autowired(required = false)
	private LogOperatorManager logOperatorManager;

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.METHOD, ElementType.TYPE })
	public @interface Ignore {
		String value() default "";
	}

	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}

	public abstract void pointcut();

	// GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE
	@Around("pointcut()")
	public Object doAround(ProceedingJoinPoint point) throws Throwable {
		if (logOperatorManager == null) {
			return point.proceed();
		}
		long exeStartTime = System.currentTimeMillis();
		try {
			MethodSignature signature = (MethodSignature) point.getSignature();
			Method method = signature.getMethod();
			Ignore ignore = method.getAnnotation(Ignore.class);
			if (ignore == null) {
				ignore = point.getTarget().getClass().getAnnotation(Ignore.class);
			}
			if (ignore != null) {
				return proceed(point, true, exeStartTime);
			}
			if (method.getReturnType().equals(Mono.class)) {
				return context(context -> mono(context, point, exeStartTime));
			} else if (method.getReturnType().equals(Flux.class)) {
				return flux(point, exeStartTime);
			} else {
				return proceed(point, false, exeStartTime);
			}
		} catch (Exception e) {
			log.warn(e.toString());
			throw e;
		}
	}

	private Object flux(ProceedingJoinPoint point, long exeStartTime) throws Throwable {
		return ((Flux<?>) point.proceed()).flatMap(res -> {
			context(context -> {
				log(context, point, exeStartTime);
				return Mono.empty();
			}).subscribe();
			return Flux.just(res);
		}).doOnError(ex -> {
			context(context -> {
				log(context, point, exeStartTime, ex);
				return Mono.error(ex);
			});
		});
	}

	private Object proceed(ProceedingJoinPoint point, boolean ignore, long exeStartTime) throws Throwable {
		Object res = point.proceed();
		boolean timeout = System.currentTimeMillis() - exeStartTime > 500;
		if (!ignore || timeout) {// 忽略的接口执行超时记录日志
			context(context -> {
				log(context, point, exeStartTime);
				return Mono.empty();
			}).subscribe();
		}
		return res;
	}

	private Mono<?> mono(SimpleExchangeContext context, ProceedingJoinPoint point, long exeStartTime) {
		try {
			Mono<?> mono = ((Mono<?>) point.proceed());
			return mono.doOnError(ex -> {
				log(context, point, exeStartTime, ex);
			}).flatMap(res -> {
				if (res instanceof Response<?>) {
					Response<?> resp = (Response<?>) res;
					if (!resp.ok()) {
						context.getExchange().getResponse().setRawStatusCode(resp.getStatus());
					}
					log(context, point, exeStartTime, resp);
				} else {
					log(context, point, exeStartTime);
				}
				return Mono.just(res);
			});
		} catch (Throwable ex) {
			log(context, point, exeStartTime, ex);
			return Mono.error(ex);
		}
	}

	private void log(SimpleExchangeContext context, ProceedingJoinPoint point, long exeStartTime) {
		int state = context.getExchange().getResponse().getRawStatusCode();
		this.log(context, point, exeStartTime, state, null);
	}

	private void log(SimpleExchangeContext context, ProceedingJoinPoint point, long exeStartTime, Response<?> resp) {
		this.log(context, point, exeStartTime, resp.getStatus(), resp.getError());
	}

	private void log(SimpleExchangeContext context, ProceedingJoinPoint point, long exeStartTime, Throwable ex) {
		String err = null;
		int state = context.getExchange().getResponse().getRawStatusCode();
		if (ex != null && ex instanceof CustomException) {
			state = ((CustomException) ex).getStatus().value();
			err = ex.getMessage();
		} else if (ex != null) {
			err = TextUtils.toStringWith(ex);
			state = 500;
		}
		this.log(context, point, exeStartTime, state, err);
	}

	private void log(SimpleExchangeContext context, //
			ProceedingJoinPoint point, //
			long exeStartTime, //
			int stateCode, //
			String err) {
		long consuming = System.currentTimeMillis() - exeStartTime;
		HttpMethod httpMethod = context.getExchange().getRequest().getMethod();
		MethodSignature signature = (MethodSignature) point.getSignature();
		Method method = signature.getMethod();
		try {
			Object[] args = point.getArgs();
			LogOperator logOperator = new LogOperator();
			String name = getMethodName(httpMethod, method);
			name = getName(point) + " - " + name;
			logOperator.setContent(name);
			logOperator.setMethod(signature.toShortString());
			logOperator.setStatusCode(stateCode);
			logOperator.setConsuming(consuming);
			logOperator.setMethod(context.getExchange().getRequest().getMethodValue());
			logOperator.setUrl(context.getExchange().getRequest().getPath().value());
			logOperator.setAddressIp(SimpleExchange.getRemoteIP(context.getExchange().getRequest()));

			logOperator.setSuccess(stateCode < 500);
			logOperator.setSuccess(consuming < 600);
			logOperator.setResult("操作正常");
			if (!StringUtils.isBlank(err)) {
				if (stateCode >= 500) {
					logOperator.setResult("服务异常:" + err);
				} else if (stateCode >= 400) {
					logOperator.setResult("操作异常:" + err);
				} else {
					logOperator.setResult("其它异常:" + err);
				}
				if (logOperator.getResult().length() > 255) {
					logOperator.setResult(logOperator.getResult().substring(0, 255));
				}
			}
			logOperator.setArgs(JSON.toJSONString(args));
			if (logOperator.getArgs().length() > 1024) {
				logOperator.setArgs(logOperator.getArgs().substring(0, 1024));
			}

			Token token = context.getToken();
			if (token != null) {
				logOperator.setUpdater(token.getAccount());
				logOperator.setCreater(token.getAccount());
				logOperator.setOperatorId(token.getUserId());
				logOperator.setTenantId(token.getTenantId());
			} else {
				logOperator.setUpdater(Token.empty().getUsername());
				logOperator.setCreater(Token.empty().getUsername());
				logOperator.setOperatorId(Token.empty().getUserId());
				logOperator.setTenantId(Token.empty().getTenantId());
			}
			ThreadExecutor.execute(() -> logOperatorManager.insert(logOperator).doOnError(error -> {
				log.error(error.getMessage());
			}).contextWrite(SimpleExchangeContext.of(context)).subscribe());
		} catch (Exception e) {
			log.error("保存日志异常 {}", TextUtils.toStringWith(e));
		}
	}

	/**
	 * 取Controller类的RequestMapping注解
	 * 
	 * @param point
	 * @return
	 */
	private String getName(ProceedingJoinPoint point) {
		RequestMapping requestMapping = point.getTarget().getClass().getAnnotation(RequestMapping.class);
		if (requestMapping == null) {
			Class<?> clazz = point.getTarget().getClass().getSuperclass();
			if (clazz != null) {
				requestMapping = clazz.getAnnotation(RequestMapping.class);
			}
		}
		String name = requestMapping != null ? requestMapping.name() : null;
		if (name == null) {
			name = point.getTarget().getClass().getSimpleName();
		}
		return name;
	}

	private String getMethodName(HttpMethod httpMethod, Method method) {
		switch (httpMethod) {
		case POST:
			PostMapping post = MethodUtils.getAnnotation(method, PostMapping.class, true, false);
			if (post != null) {
				return post.name();
			}
			break;
		case GET:
			GetMapping get = MethodUtils.getAnnotation(method, GetMapping.class, true, false);
			if (get != null) {
				return get.name();
			}
			break;
		case PUT:
			PutMapping put = MethodUtils.getAnnotation(method, PutMapping.class, true, false);
			if (put != null) {
				return put.name();
			}
			break;
		case PATCH:
			PatchMapping patch = MethodUtils.getAnnotation(method, PatchMapping.class, true, false);
			if (patch != null) {
				return patch.name();
			}
			break;
		case DELETE:
			DeleteMapping delete = MethodUtils.getAnnotation(method, DeleteMapping.class, true, false);
			if (delete != null) {
				return delete.name();
			}
			break;
		default:
			break;
		}
		RequestMapping request = MethodUtils.getAnnotation(method, RequestMapping.class, true, false);
		if (request != null) {
			return request.name();
		}
		return method.getName();
	}

}
