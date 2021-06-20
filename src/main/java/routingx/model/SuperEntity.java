package routingx.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.springframework.data.domain.Sort.Direction;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import routingx.Note;
import routingx.data.Created;
import routingx.data.Creater;
import routingx.data.OrderBy;
import routingx.data.Updated;
import routingx.data.Updater;

/**
 * 
 * 实体表基类
 * 
 * @author xxx
 * 
 * @version 2012-12-03
 * 
 * @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")。控制入参
 * 
 * @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")。
 * 
 */
@Getter
@Setter
@MappedSuperclass
public abstract class SuperEntity extends IDEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3374620984663200989L;
	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String TIME_FORMAT = "HH:mm:ss";
	public static final String TIMEZONE = "GMT+8";
	@Note("通配符")
	public static final String WILDCARD = "*";

	@ApiModelProperty(value = "创建时间", hidden = true)
	@Column(updatable = false)
	@JsonIgnore
	@OrderBy(value = Direction.DESC, order = -1)
	@Created
	private LocalDateTime createTime;

	@ApiModelProperty(value = "创建人", hidden = true)
	@Column(length = 50, updatable = false)
	@Creater
	private String creater;

	@JsonIgnore
	@ApiModelProperty(value = "更新时间", hidden = true)
	@Column(nullable = false)
	@Updated
	private LocalDateTime editTime;

	@ApiModelProperty(value = "更新人", hidden = true)
	@Column(length = 50, nullable = false)
	@Updater
	private String updater;

//	@ApiModelProperty(value = "软删除标志(false/0:未删除 true/1:已删除)")
//	@Column()
//	@Deleted
//	private Boolean deleted;

//	@ApiModelProperty(value = "停用标志(false/0:未停用 true/1:已停用)")
//	private Boolean disabled;

//	@ApiModelProperty(value = "版本号(乐观锁)", notes = "乐观锁")
//	@Column(updatable = false)
//	private Long versionNum;

	@Deprecated
	public LocalDateTime getCreateTime() {
		return createTime;
	}

	@Deprecated
	public void setCreateTime(LocalDateTime createTime) {
		this.createTime = createTime;
	}

	@Deprecated
	public LocalDateTime getEditTime() {
		return editTime;
	}

	@Deprecated
	public void setEditTime(LocalDateTime editTime) {
		this.editTime = editTime;
	}

	public LocalDateTime getUpdated() {
		return editTime;
	}

	public void setUpdated(LocalDateTime updated) {
		editTime = updated;
	}

	public LocalDateTime getCreated() {
		return createTime;
	}

	public void setCreated(LocalDateTime created) {
		createTime = created;
	}

}
