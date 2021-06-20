package routingx.security;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import routingx.model.Token;
import routingx.model.User;
import routingx.utils.RandomStringUtils;

@Component
@Slf4j
public class UserPasswordEncoder implements PasswordEncoder {

	public static boolean isMD5(String msg) {
		int cnt = 0;
		for (int i = 0; i < msg.length(); ++i) {
			switch (msg.charAt(i)) {
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
			case 'a':
			case 'b':
			case 'c':
			case 'd':
			case 'e':
			case 'f':
			case 'A':
			case 'B':
			case 'C':
			case 'D':
			case 'E':
			case 'F':
				++cnt;
				if (32 <= cnt)
					return true;
				break;
			case '/':
				if ((i + 10) < msg.length()) {// "/storage/"
					char ch1 = msg.charAt(i + 1);
					char ch2 = msg.charAt(i + 8);
					if ('/' == ch2 && ('s' == ch1 || 'S' == ch1))
						return true;
				}
			default:
				cnt = 0;
				break;
			}
		}
		return false;
	}

	/**
	 * 兼容之前平台的加密方式
	 * 
	 * @param password
	 * @return
	 */
	public static String md5(String data) {
		return DigestUtils.md5Hex(data);
	}

	/**
	 * 兼容之前平台的加密方式
	 * 
	 * @param password
	 * @return
	 */
	public static String md5(byte[] data) {
		return DigestUtils.md5Hex(data);
	}

	@SuppressWarnings("deprecation")
	static PasswordEncoder create(String encodingId) {
		Map<String, PasswordEncoder> encoders = new HashMap<>();
		encoders.put("bcrypt", new BCryptPasswordEncoder());
		encoders.put("ldap", new org.springframework.security.crypto.password.LdapShaPasswordEncoder());
		encoders.put("MD4", new org.springframework.security.crypto.password.Md4PasswordEncoder());
		encoders.put("MD5", new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("MD5"));
		encoders.put("noop", org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance());
		encoders.put("pbkdf2", new Pbkdf2PasswordEncoder());
		encoders.put("scrypt", new SCryptPasswordEncoder());
		encoders.put("SHA-1", new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("SHA-1"));
		encoders.put("SHA-256",
				new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("SHA-256"));
		encoders.put("sha256", new org.springframework.security.crypto.password.StandardPasswordEncoder());
		encoders.put("argon2", new Argon2PasswordEncoder());
		if (!encoders.containsKey(encodingId)) {
			encodingId = "MD5";
		}
		return new DelegatingPasswordEncoder(encodingId, encoders);
	}

	private final PasswordEncoder passwordEncoder;

	public UserPasswordEncoder() {
		passwordEncoder = create("MD5");
	}

	/**
	 * 生成随机盐
	 * 
	 * @return
	 */
	public String nextSalt() {
		return RandomStringUtils.randomNumeric(8);
	}

	public boolean matches(Token user, String rawPassword) {
		try {
			if (StringUtils.isNotBlank(user.getSalt())) {
				String password = user.getAccount() + user.getSalt() + rawPassword;
				if (matches(password, user.getPassword())) {
					return true;
				} else {
					password = user.getAccount() + user.getSalt() + md5(rawPassword);
					return matches(password, user.getPassword());
				}
			} else {
				// 兼容老的加密方式
				if (user.getPassword().equals(rawPassword) //
						|| user.getPassword().equals(md5(rawPassword))) {
					return true;
				} else {
					return false;
				}
			}
		} catch (Exception ex) {
			log.error(ex.getMessage());
			return false;
		}
	}

	public String encode(Token user) {
		if (StringUtils.isBlank(user.getPassword())) {
			user.setPassword(User.DEFAULT_PASSWORD);
		}
		if (!isMD5(user.getPassword())) {
			user.setPassword(md5(user.getPassword()));
		}
		if (StringUtils.isBlank(user.getSalt())) {
			// 老的加密方式
			return user.getPassword();
		}
		final String password = user.getAccount() + user.getSalt() + user.getPassword();
		final String enpassword = encode(password);
		user.setPassword(enpassword);
		return enpassword;
	}

	@Override
	public String encode(CharSequence rawPassword) {
		return passwordEncoder.encode(rawPassword);
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		return passwordEncoder.matches(rawPassword, encodedPassword);
	}
}
