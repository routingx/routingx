package routingx.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Transient;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import routingx.Note;

/**
 * 分页类
 */
@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class Page implements Serializable {

	private static final long serialVersionUID = -6518359964486465431L;

	@ApiModelProperty(value = "每页行数最大值", example = "1")
	public static final int MAX_SIZE = 5000;

	@ApiModelProperty(value = "当前页码，从1开始", example = "1")
	private Integer number;

	@ApiModelProperty(value = "每页行数", example = "20")
	private Integer size;

	@ApiModelProperty(value = "排序(如：field1 ASC,field2 DESC)", hidden = true)
	private Sorted sort;

	@ApiModelProperty(value = "当前页起始行，从0开始", hidden = true, example = "0")
	private Integer start;

	@ApiModelProperty(value = "总共页数", hidden = true, example = "0")
	private Long count;

	@ApiModelProperty(value = "总记录数", hidden = true, example = "0")
	private Long total;

	@ApiModelProperty(value = "? >= 查询开始时间", hidden = true, example = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = SuperEntity.DATE_TIME_FORMAT, timezone = SuperEntity.TIMEZONE)
	@DateTimeFormat(pattern = SuperEntity.DATE_TIME_FORMAT)
	private Date startTime;

	@ApiModelProperty(value = "? <= 查询结束时间", hidden = true, example = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = SuperEntity.DATE_TIME_FORMAT, timezone = SuperEntity.TIMEZONE)
	@DateTimeFormat(pattern = SuperEntity.DATE_TIME_FORMAT)
	private Date endTime;

	public Page() {
		super();
		this.setNumber(1);
		this.setSize(20);
	}

	@ApiModelProperty(value = "查询条件参数", hidden = true)
	@javax.persistence.Transient
	@Transient
	private transient Map<String, Object> params;

	public final Map<String, Object> getParams() {
		if (params == null) {
			setParams(new HashMap<>());
		}
		return params;
	}

	@Note("put params ")
	public void put(String column, Object value) {
		if (params == null) {
			setParams(new HashMap<>());
		}
		getParams().put(column, value);
	}

	public boolean containsKey(String column) {
		if (params == null) {
			return false;
		}
		return params.containsKey(column);
	}

	public Integer limit() {
		if (size == null) {
			return size = 20;
		}
		if (size < 1) {
			size = 20;
		}
		if (size > MAX_SIZE) {
			size = MAX_SIZE;
		}
		return size;
	}

	public Integer offset() {
		if (number == null || number < 1) {
			number = 1;
		}
		setStart(limit() * (number - 1));
		return start;
	}

	public Long getCount() {
		if (total == null || size == null) {
			return null;
		}
		count = (total + size - 1) / size;
		return count;
	}

}
