package routingx.utils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidatorBean {
	@NotBlank(message = "用户名不可以为空")
	@Size(min = 1, max = 20, message = "用户名长度需要在20个字以内")
	@Pattern(regexp = ValidatorUtils.USERNAME, message = "用户名必须是2-20位字母、数字、下划线")
	private String username;

	@NotBlank(message = "密码不可以为空")
	@Size(min = 6, max = 20, message = "密码长度需要在6-20个字符")
	@Pattern(regexp = ValidatorUtils.PASSWORD, message = "密码不合法")
	private String password;

	@NotBlank(message = "电话不可以为空")
	@Pattern(regexp = ValidatorUtils.PHONE, message = "电话不合法")
	private String telephone;

	@NotBlank(message = "手机不可以为空")
	@Pattern(regexp = ValidatorUtils.MOBILE, message = "手机不合法")
	private String mobile;

	@NotBlank(message = "邮箱不允许为空")
	@Size(min = 5, max = 50, message = "邮箱长度需要在50个字符以内")
	@Pattern(regexp = ValidatorUtils.EMAIL, message = "邮箱不合法")
	private String mail;
}
