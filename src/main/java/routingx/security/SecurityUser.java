package routingx.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import reactor.core.publisher.Mono;
import routingx.Note;
import routingx.model.User;
import routingx.model.UserState;

@Setter
@Getter
class SecurityUser extends User implements UserDetails {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1078819087252754855L;

	/**
	 * 用户有权限访问的地址
	 */
	@JsonIgnore
	private transient final List<GrantedAuthority> authorities = new ArrayList<>();

	public SecurityUser() {

	}

	public static Mono<SecurityUser> just(User user) {
		return Mono.just(clone(user));
	}

	public static SecurityUser clone(User user) {
		SecurityUser u = new SecurityUser();
		BeanUtils.copyProperties(user, u);
		u.setSuperAdmin(user.getSuperAdmin());
		if (user.getAuthorPatterns() != null) {
			for (String pattern : user.getAuthorPatterns()) {
				u.addAuthorities(pattern);
			}
			user.getAuthorPatterns().clear();
		}
		if (u.getSuperAdmin()) {
			u.getAuthorities().add(0, new SimpleGrantedAuthority("/**"));
		}
		u.setAuthenticated(!u.isEmpty());
		return u;
	}

	private void addAuthorities(String... authorities) {
		for (String role : authorities) {
			if (StringUtils.isNotBlank(role)) {
				this.authorities.add(new SimpleGrantedAuthority(role));
			}
		}
	}

	void addAuthorities(Collection<GrantedAuthority> authorities) {
		getAuthorities().addAll(authorities);
	}

	@Override
	public List<GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@JsonIgnore
	@Note("帐户未过期")
	@Override
	public boolean isAccountNonExpired() {
		return !expired();
	}

	@JsonIgnore
	@Note("帐户未锁定")
	@Override
	public boolean isAccountNonLocked() {
		return !UserState.LUCKED.equals(UserState.valueOf(this.getState()));
	}

	@JsonIgnore
	@Note("证书未过期")
	@Override
	public boolean isCredentialsNonExpired() {
		return !UserState.CERTIFICATE.equals(UserState.valueOf(this.getState()));
	}

	@JsonIgnore
	@Note("帐户已启用")
	@Override
	public boolean isEnabled() {
		return !getDisabled();
	}

	@JsonIgnore
	@Note("空帐户")
	public boolean isEmpty() {
		return StringUtils.isBlank(getUsername());
	}
}
