package routingx;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CustomException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final HttpStatus status;

	private CustomException() {
		super();
		this.status = HttpStatus.BAD_REQUEST;
	}

	private CustomException(HttpStatus status) {
		super(status.getReasonPhrase());
		this.status = status;
	}

	private CustomException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

	private CustomException(HttpStatus status, String message, Throwable cause) {
		super(message, cause);
		this.status = status;
	}

	/**
	 * 前端提交参数异常
	 * 
	 * @param message
	 * @return
	 */
	public static CustomException bq(String message) {
		return new CustomException(HttpStatus.BAD_REQUEST, message);
	}

	/**
	 * 服务内部错误异常
	 * 
	 * @param message
	 * @param cause
	 * @return
	 */
	public static CustomException er(Throwable cause) {
		return new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, cause.getMessage(), cause);
	}

	/**
	 * 服务内部错误异常
	 * 
	 * @param message
	 * @param cause
	 * @return
	 */
	public static CustomException er(String message, Throwable cause) {
		return new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
	}

	/**
	 * 服务内部错误异常
	 * 
	 * @param message
	 * @param cause
	 * @return
	 */
	public static CustomException er(String message) {
		return new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, message);
	}

	public static CustomException of(HttpStatus status) {
		return new CustomException(status);
	}

	public static CustomException of(HttpStatus status, Throwable cause) {
		return new CustomException(status, cause.getMessage(), cause);
	}

	public static CustomException of(HttpStatus status, String message, Throwable cause) {
		return new CustomException(status, message, cause);
	}

	public static CustomException of(HttpStatus status, String message) {
		return new CustomException(status, message);
	}

	public static void assertIsEmpty(Object value, String message) {
		if (value == null) {
			throw CustomException.bq(message);
		} else if (value.getClass().isArray()) {
			if (Array.getLength(value) == 0) {
				throw CustomException.bq(message);
			}
		} else if (value instanceof Collection<?>) {
			if ((long) ((Collection<?>) value).size() == 0) {
				throw CustomException.bq(message);
			}
		} else if (value instanceof Map<?, ?>) {
			if ((long) ((Map<?, ?>) value).size() == 0) {
				throw CustomException.bq(message);
			}
		}
	}

	public static void assertIsBank(String value, String message) {
		if (!StringUtils.hasText(value)) {
			throw CustomException.bq(message);
		}
	}

	public static void assertIsTrue(boolean expression, String message) {
		if (expression) {
			throw CustomException.bq(message);
		}
	}
}
