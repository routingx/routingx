package routingx.model;

import java.io.Serializable;
import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.data.annotation.Transient;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import routingx.CustomException;
import routingx.Note;
import routingx.Pair;
import routingx.UserAgent;
import routingx.data.UniqueIndex;
import routingx.json.JSON;
import routingx.utils.DateTimeUtils;
import routingx.utils.ObjectUtils;
import routingx.utils.ValidatorUtils;

@Setter
@Getter
@MappedSuperclass
@Slf4j
public class Token extends TenantEntity implements Serializable {

	private static final long serialVersionUID = 3461221971771880634L;

	private static final long REFRESH_TIME_INTERVAL = Duration.ofMinutes(5).toMillis();

	public final static String ID = "sessionid";

	public final static String ACCESS = "access";

	public final static String REFRESH = "refresh";

	public final static String SEPARATOR = ".";

	public final static String OFF = "OFF";

	private final static String SECRET_KEY = "9527";

	private final static Map<String, String> SECRETS = new HashMap<>();

	private static final Token EMPTY = new TokenEmpty();

	static {
		// SECRETS.put(SECRET_KEY, Token.class.getPackage().getName());
		SECRETS.put(SECRET_KEY, "+wnKpBmkTpOjQuVi5oubv9ZNsCw=}");
	}

	public static Token empty() {
		return EMPTY;
	}

	public static void addSecret(String key, String secret) {
		if (StringUtils.isBlank(key) || key.length() > 8) {
			log.warn("key length must <= 8");
			return;
		}
		// TODO 为兼容老平台，取消此功能
		// SECRETS.put(key, secret);
	}

	@ApiModelProperty(value = "用户名")
	@Column(length = 64, updatable = false)
	@NotBlank(message = "用户名不可以为空")
	@Size(min = 1, max = 20, message = "用户名长度需要在20个字以内")
	@Pattern(regexp = ValidatorUtils.USERNAME, message = "用户名必须是2-20位字母、数字、下划线")
	@UniqueIndex
	private String account;

	@JsonIgnore
	@Column(length = 128, updatable = false)
	// @Size(min = 1, max = 20, message = "用户名长度需要在20个字以内")
	private String password;

	@ApiModelProperty(value = "随机盐", notes = "为提高加密强度，对用户密码加密时使用")
	@JsonIgnore
	@Column(length = 16, updatable = false)
	private String salt;

	@ApiModelProperty(value = "过期时间")
	private LocalDateTime expiresAt;

	@Column(length = 32)
	private String tokenId;

	@ApiModelProperty(value = "设备信息")
	@Column(length = 128)
	private String deviceInfo;

	@ApiModelProperty(value = "超级管理员")
	@Column(updatable = false, nullable = false)
	@JsonIgnore
	private Boolean superAdmin;

	protected void setSuperAdmin(Boolean superAdmin) {
		this.superAdmin = superAdmin;
	}

	@ApiModelProperty(value = "对应平台(WEB,APP,APPLET,PUBLIC)")
	@Transient
	@Enumerated(EnumType.STRING)
	private transient PlatformEnum platform;

	@ApiModelProperty(value = "证书过期")
	@Transient
	private transient Boolean credentialsExpired;

	@Transient
	private transient Boolean rememberMe;

	@ApiModelProperty(value = "验证信息")
	@Transient
	private transient String captcha;

	@ApiModelProperty(value = "访问令牌")
	@Transient
	private transient String access;

	@ApiModelProperty(value = "刷新令牌")
	@Transient
	private transient String refresh;

	@ApiModelProperty(value = "已刷新")
	@Transient
	private transient Boolean refreshed;

	@ApiModelProperty(value = "已经验证")
	@Transient
	private transient Boolean authenticated;

	@ApiModelProperty(value = "客户端信息", hidden = true)
	@Transient
	private transient UserAgent userAgent;

	public Token() {
	}

	@JsonIgnore
	public String getUserId() {
		return this.getId();
	}

	@Note("帐户已过期")
	public boolean expired() {
		if (expiresAt == null) {
			return false;
		}
		return System.currentTimeMillis() >= DateTimeUtils.asDate(expiresAt).getTime();
	}

	@Override
	public int hashCode() {
		return (getUserId() + getTenantId()).hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName());
		sb.append("@" + this.hashCode());
		sb.append("[account=" + getAccount() + ";");
		sb.append("userId=" + getUserId() + ";");
		sb.append("tenantId=" + getTenantId() + "]");
		return sb.toString();
	}

	private Pair<String, String> findSecret() {
		Pair<String, String> pair = Pair.of(SECRET_KEY, SECRETS.get(SECRET_KEY));
		int index = RandomUtils.nextInt(0, SECRETS.size());
		int i = 0;
		for (Map.Entry<String, String> entity : SECRETS.entrySet()) {
			if (index == i) {
				pair = Pair.of(entity.getKey(), entity.getValue());
			}
			i++;
		}
		return pair;
	}

	public String sign() {
		try {
			final Token token = this;
			Pair<String, String> pair = findSecret();
			token.setAccess(null);
			token.setRefresh(null);
			Algorithm algorithm = Algorithm.HMAC256(pair.getSecond());
			JWTCreator.Builder builder = JWT.create();
			String[] array = new String[10];
			array[0] = token.getId();
			array[1] = token.getTenantId();
			array[2] = token.getTokenId();
			array[3] = token.getAccount();
			array[4] = token.getPassword();
			array[5] = DateTimeUtils.format(token.getUpdated());
			array[6] = token.getDeviceInfo();
			array[7] = ObjectUtils.toStringTrueFalse(token.getRememberMe());
			array[8] = token.getPlatform().name();
			array[9] = ObjectUtils.toStringTrueFalse(token.getSuperAdmin());
			builder.withAudience(array);
			builder.withIssuer("peixere@qq.com");// 签发者
			builder.withIssuedAt(new Date());// 生成时间
			builder.withExpiresAt(DateUtils.addDays(new Date(), 7));
			token.setAccess(builder.sign(algorithm) + SEPARATOR + pair.getFirst());
			builder.withExpiresAt(DateUtils.addDays(new Date(), 30));
			token.setRefresh(builder.sign(algorithm) + SEPARATOR + pair.getFirst());
			return token.getAccess();
		} catch (Exception e) {
			log.warn(e.toString());
			throw CustomException.er("token sign fail", e);
		}
	}

	public static Token verify(final String token) {
		try {
			String text = token;
			String secret = SECRETS.get(SECRET_KEY);
			int index = token.lastIndexOf(SEPARATOR);
			if (index > 0) {
				text = token.substring(0, index);
				final String key = token.substring(index + 1, token.length());
				secret = SECRETS.get(key);
			}
			Algorithm algorithm = Algorithm.HMAC256(secret);
			JWTVerifier verifier = JWT.require(algorithm).build();
			DecodedJWT jwt = verifier.verify(text);
			Token user = new Token();
			List<String> array = jwt.getAudience();
			user.setId(array.get(0));
			user.setTenantId(array.get(1));
			user.setTokenId(array.get(2));
			user.setAccount(array.get(3));
			user.setPassword(array.get(4));
			user.setUpdated(DateTimeUtils.asLocalDateTime(array.get(5)));
			user.setDeviceInfo(array.get(6));
			user.setRememberMe(BooleanUtils.toBoolean(array.get(7)));
			user.setPlatform(PlatformEnum.of(array.get(8)));
			user.setSuperAdmin(BooleanUtils.toBoolean(array.get(9)));
			user.setAccess(token);
			user.setExpiresAt(DateTimeUtils.asLocalDateTime(jwt.getExpiresAt()));
			user.setAuthenticated(true);
			return user;
		} catch (Exception e) {
			throw CustomException.er("token verify fail", e);
		}
	}

	public <T extends Token> Token authed(T user) {
		Token token = user;
		token.setAuthenticated(true);
		token.setTokenId(this.getTokenId());
		if (StringUtils.isBlank(token.getTokenId())) {
			token.setTokenId(Token.nextId());
		}
		token.setDeviceInfo(this.getDeviceInfo());
		token.setPassword(this.getPassword());
		token.setRememberMe(this.getRememberMe());
		token.setUpdated(LocalDateTime.now());
		token.setUserAgent(this.getUserAgent());
		token.setPlatform(this.getPlatform());
		if (log.isDebugEnabled()) {
			log.debug(JSON.format(token));
		}
		return token;
	}

	public boolean refresh() {
		if (getUpdated() == null) {
			return true;
		}
		long time = DateTimeUtils.asDate(getUpdated()).getTime();
		time = System.currentTimeMillis() - time;
		boolean timeout = time > REFRESH_TIME_INTERVAL;
		if (timeout && log.isDebugEnabled()) {
			log.debug(getUpdated().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
		}
		return timeout;
	}

	public String getUsername() {
		return this.account;
	}

	@JsonIgnore
	public boolean isEmpty() {
		return false;
	}

	public Principal principal() {
		return new Principal() {

			@Override
			public String getName() {
				return getUsername();
			}

		};
	}
}
