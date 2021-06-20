package routingx.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import routingx.SnowFlake;

@Setter
@Getter
@MappedSuperclass
public abstract class IDEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "主键(新增：忽略；更新：必填)", hidden = true)
	@Id()
	@javax.persistence.Id
	@Column(name = "id", unique = true, nullable = false, length = 32)
	private String id;

	@ApiModelProperty(value = "分页查询参数", hidden = true)
	@javax.persistence.Transient
	@Transient
	private transient Page page;

	public Page page() {
		if (getPage() == null) {
			this.setPage(new Page());
		}
		return getPage();
	}

	public static String nextId() {
		return SnowFlake.nextString();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		return (o != null && o.hashCode() == hashCode());
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		return (id != null ? id.hashCode() : super.hashCode());
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return getClass().getSimpleName() + ":" + hashCode() + "@id=" + this.id;
	}

}
