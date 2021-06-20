package routingx.model;

import javax.persistence.MappedSuperclass;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import routingx.data.UniqueIndex;

@Setter
@Getter
@MappedSuperclass
@ApiModel(value = "系统参数", description = "系统参数")
public class Conf extends SuperEntity {

	private static final long serialVersionUID = -506237846404236775L;

	@ApiModelProperty(value = "参数名称")
	@UniqueIndex
	private String name;

	@ApiModelProperty(value = "参数键名")
	@UniqueIndex
	private String code;

	@ApiModelProperty(value = "参数键值")
	private String value;

	@ApiModelProperty(value = "排列顺序")
	private Integer sort;
}
