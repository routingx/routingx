package routingx.model;

import java.util.Collections;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import routingx.data.Deleted;
import routingx.utils.ValidatorUtils;

@Setter
@Getter
@ApiModel("用户信息")
@MappedSuperclass
public class User extends Token {

	private static final long serialVersionUID = -7066074229094495156L;
	private static final String ADMIN = "admin";
	public static final String DEFAULT_PASSWORD = "123456";
	private static final User SUPERADMIN = admin();

	private static final User EMPTY = empty();

	public static User admin() {
		if (SUPERADMIN != null) {
			return SUPERADMIN;
		}
		User user = new User();
		user.setSuperAdmin(true);
		user.setAccount(ADMIN);
		return user;
	}

	public static User empty() {
		if (EMPTY != null) {
			return EMPTY;
		}
		User user = new User();
		user.setSuperAdmin(false);
		user.setAccount("");
		user.setPassword("");
		user.setName("");
		user.setAuthenticated(false);
		user.setAuthorPatterns(Collections.emptyList());
		return user;
	}

	@ApiModelProperty(value = "手机号码", notes = "找回密码，重置密码")
	@Column(length = 32)
	@Pattern(regexp = ValidatorUtils.MOBILE, message = "手机不合法")
	private String mobile;

	@ApiModelProperty(value = "邮箱地址", notes = "找回密码，重置密码")
	@Size(min = 5, max = 50, message = "邮箱长度需要在50个字符以内")
	@Pattern(regexp = ValidatorUtils.EMAIL, message = "邮箱不合法")
	@Column(length = 128)
	private String email;

	@ApiModelProperty(value = "IC卡")
	@Column
	private String rfid;

	@ApiModelProperty(value = "帐户状态(0-已注册 1-已激活 2-已锁定)")
	private Integer state;

	@ApiModelProperty(value = "软删除标志(false/0:未删除 true/1:已删除)")
	@Column()
	@Deleted
	private Boolean deleted;

	@ApiModelProperty(value = "停用标志(false/0:未停用 true/1:已停用)")
	private Boolean disabled;

	@ApiModelProperty(value = "用户姓名")
	@Column(length = 64)
	@NotBlank(message = "用户姓名不可以为空")
	@Size(min = 2, max = 25, message = "用户姓名长度需要在25个字符以内")
	private String name;

	@ApiModelProperty(value = "头像地址")
	@Column(length = 100)
	private String icon;

	@ApiModelProperty(value = "用户类型(1-WEB端用户 2-APP用户)")
	private Integer userType;

	@ApiModelProperty(value = "身份：0-系统运维人员 1-用户/客户 10-预付费-户主")
	@Column
	private Integer identity;

	@ApiModelProperty(value = "备注")
	@Column()
	private String memo;

	@ApiModelProperty(value = "用户权限", hidden = true)
	@Transient
	@JsonIgnore
	private transient List<String> authorPatterns;

	public String getFullname() {
		return this.name;
	}
}
