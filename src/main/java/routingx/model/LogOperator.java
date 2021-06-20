package routingx.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@MappedSuperclass
@ApiModel(value = "操作日志", description = "使用切面记录controller层调用日志")
public class LogOperator extends TenantEntity {

	private static final long serialVersionUID = -225103893873243936L;

	@ApiModelProperty(value = "用户ID")
	@Column(length = 32)
	private String operatorId;

	@ApiModelProperty(value = "IP地址")
	@Column(length = 128)
	private String addressIp;

	@ApiModelProperty(value = "IP对应区域地址")
	@Column(length = 128)
	private String address;

	@ApiModelProperty(value = "是否正常")
	private Boolean success;

	@ApiModelProperty(value = "响应状态")
	private Integer statusCode;

	@ApiModelProperty(value = "是否超时")
	private Boolean timeout;

	@ApiModelProperty(value = "操作内容")
	private String content;

	@ApiModelProperty(value = "操作结果")
	private String result;

	@ApiModelProperty(value = "操作方法名")
	private String method;

	@ApiModelProperty(value = "请求参数")
	@Column(length = 1024)
	private String args;

	@ApiModelProperty(value = "URL")
	private String url;

	@ApiModelProperty(value = "消耗时长")
	private Long consuming;
}
