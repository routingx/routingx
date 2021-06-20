package routingx.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class TenantEntity extends SuperEntity {

	private static final long serialVersionUID = 2245439601561837916L;
	public static final String TENANTID_COLUMN = "tenant_id";

	@Column(length = 32, updatable = false)
	@ApiModelProperty(value = "租户ID", hidden = true)
	private String tenantId;

}
