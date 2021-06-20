package routingx.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.util.Assert;

import routingx.model.Token;

/**
 * 
 * {@link UsernamePasswordAuthenticationToken}
 * 
 * {@link AbstractAuthenticationToken}
 * 
 * 
 * @author Administrator
 *
 */
public class AuthenticationToken implements Authentication {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4808163104927840059L;

	private final Collection<GrantedAuthority> authorities;
	private final Boolean authenticated;
	private Token details;

	public static AuthenticationToken unauthed(Token details) {
		return new AuthenticationToken(details, AuthorityUtils.NO_AUTHORITIES, false);
	}

	public static AuthenticationToken authed(Token details) {
		details.setAuthenticated(true);
		return new AuthenticationToken(details, AuthorityUtils.NO_AUTHORITIES, true);
	}

	private AuthenticationToken(Token details, Collection<GrantedAuthority> authorities, Boolean authenticated) {
		this.authenticated = authenticated;
		if (authenticated) {
			Assert.notNull(details, "details must not be null!");
		}
		this.details = details;
		ArrayList<GrantedAuthority> temp = new ArrayList<>();
		if (authorities != null) {
			temp.addAll(authorities);
		}
		this.authorities = Collections.unmodifiableList(temp);
	}

	@Override
	public String getName() {
		return getPrincipal();
	}

	@Override
	public Collection<GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public Object getCredentials() {
		return null;
	}

	protected void setDetails(Token details) {
		this.details = details;
	}

	@Override
	public Token getDetails() {
		return details;
	}

	@Override
	public String getPrincipal() {
		return details != null ? details.getAccount() : null;
	}

	@Override
	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
		if (isAuthenticated) {
			throw new IllegalArgumentException(
					"Cannot set this token to trusted - use constructor which takes a authenticated list instead");
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString()).append(": ");
		sb.append("Principal: ").append(this.getPrincipal()).append("; ");
		sb.append("Credentials: [PROTECTED]; ");
		sb.append("Authenticated: ").append(this.isAuthenticated()).append("; ");
		sb.append("Details: ").append(this.getDetails()).append("; ");

		if (!authorities.isEmpty()) {
			sb.append("Granted Authorities: ");

			int i = 0;
			for (GrantedAuthority authority : authorities) {
				if (i++ > 0) {
					sb.append(", ");
				}

				sb.append(authority);
			}
		} else {
			sb.append("Not granted any authorities");
		}

		return sb.toString();
	}
}
