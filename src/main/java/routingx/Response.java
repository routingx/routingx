package routingx;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import routingx.model.Page;
import routingx.utils.TextUtils;

@Setter
@Getter
@JsonInclude(Include.NON_NULL)
@Api(value = "响应数据结构", hidden = true)
public class Response<T> {

	@Note("请求响应正确")
	public final static int OK = HttpStatus.OK.value();

	@ApiModelProperty(value = "响应状态码(请参考HTTP状态码)", example = "200")
	private int status = 200;

	private String requestId;

	private String path;

	@ApiModelProperty(value = "响应信息(必填)")
	private String message = "成功";

	@ApiModelProperty(value = "响应异常")
	private String error;

	@ApiModelProperty(value = "响应数据")
	private T data;

	private Date timestamp = new Date();

	@ApiModelProperty(value = "分页参数(可选)，没有分页时，不显示此节点")
	private Page page;

	private Response(T data) {
		this.setData(data);
	}

	private Response(T data, String msg) {
		this(data, msg, HttpStatus.OK);
	}

	private Response(T data, String msg, HttpStatus status) {
		this.setStatus(status.value());
		this.setMessage(msg);
		this.setData(data);
	}

	public boolean ok() {
		return OK == getStatus();
	}

	private void throwable(Throwable ex) {
		if (ex != null) {
			error = TextUtils.toStringWith(ex);
		} else {
			if (error == null) {
				error = "";
			}
		}
	}

	public static <T> Response<T> of(HttpStatus status, String msg, T data) {
		return of(status, msg, data, null);
	}

	public static <T> Response<T> of(HttpStatus status, String msg, T data, Throwable ex) {
		Response<T> res = new Response<>(data);
		res.setStatus(status.value());
		if (StringUtils.isBlank(msg) && ex != null) {
			res.setMessage(ex.getMessage());
		} else {
			res.setMessage(msg);
		}
		res.setData(data);
		res.throwable(ex);
		return res;
	}

	public static <T> Response<T> ok(T data) {
		return ok(data, "成功");
	}

	public static <T> Response<T> ok(T data, String msg) {
		Response<T> res = new Response<>(data, msg);
		Page page = new Page();
		page.setNumber(null);
		page.setSize(null);
		page.setTotal(0L);
		if (data == null) {
			page.setTotal(0L);
		} else if (data.getClass().isArray()) {
			page.setTotal((long) Array.getLength(data));
		} else if (data instanceof Collection<?>) {
			page.setTotal((long) ((Collection<?>) data).size());
		} else if (data instanceof Map<?, ?>) {
			page.setTotal((long) ((Map<?, ?>) data).size());
		} else {
			page.setTotal(1L);
		}
		res.setPage(page);
		return res;
	}

	public static <T> Response<T> ok(Page page, T data) {
		String msg = "查询成功";
		if (data == null) {
			msg = "找不到数据";
		} else if (data.getClass().isArray()) {
			if (Array.getLength(data) == 0) {
				msg = "找不到数据";
			}
		} else if (data instanceof Collection<?>) {
			if (CollectionUtils.isEmpty((Collection<?>) data)) {
				msg = "找不到数据";
			}
		}
		Response<T> res = new Response<>(data, msg);
		res.setPage(page);
		return res;
	}

	public static <T> Response<T> noContent() {
		return of(HttpStatus.NO_CONTENT, "找不到数据", null, null);
	}

	@Note("参数错误")
	public static <T> Response<T> bq(String msg) {
		return of(HttpStatus.BAD_REQUEST, msg, null, null);
	}

	public static <T> Response<T> er(CustomException ex) {
		return er(ex.getStatus(), ex.getMessage(), ex);
	}

	public static <T> Response<T> er(String msg) {
		return er(HttpStatus.INTERNAL_SERVER_ERROR, msg, null);
	}

	public static <T> Response<T> er(String msg, Throwable ex) {
		return of(HttpStatus.INTERNAL_SERVER_ERROR, msg, null, ex);
	}

	public static <T> Response<T> er(HttpStatus status, String msg, Throwable ex) {
		return of(status, msg, null, ex);
	}

}
