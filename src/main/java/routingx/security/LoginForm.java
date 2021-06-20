package routingx.security;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.BooleanUtils;
import org.hibernate.validator.constraints.Length;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import routingx.model.PlatformEnum;
import routingx.model.Token;
import routingx.utils.ValidatorUtils;

@Setter
@Getter
@ApiModel("登录表单")
public class LoginForm {

	@ApiModelProperty(value = "用 户 名")
	@NotBlank(message = "用户名不可以为空")
	@Length(min = 1, max = 20, message = "用户名长度需要在20个字以内")
	@Pattern(regexp = ValidatorUtils.USERNAME, message = "用户名必须是2-20位字母、数字、下划线")
	private String account;

	@ApiModelProperty(value = "用户密码")
	@NotBlank(message = "密码不可以为空")
	@Length(min = 6, max = 20, message = "密码长度需要在6-20个字符")
	private String password;

	@ApiModelProperty(value = "记住密码")
	private String rememberMe;

	@ApiModelProperty(value = "验 证 码")
	private String captcha;

	@ApiModelProperty(value = "对应平台", notes = "WEB,APP,APPLET,PUBLIC")
	private PlatformEnum platform = PlatformEnum.WEB;

	public Token token() {
		Token token = new Token();
		token.setAccount(account);
		token.setPassword(password);
		token.setCaptcha(captcha);
		token.setPlatform(platform);
		token.setRememberMe(BooleanUtils.toBoolean(rememberMe));
		return token;

	}
}
