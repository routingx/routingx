package routingx.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import routingx.CustomException;
import routingx.utils.ObjectUtils;

class TokenEmpty extends Token {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8552567276329742802L;

	public TokenEmpty() {
		ObjectUtils.setDefaultValue(this);
		super.setAccount("-");
		super.setId("-");
		super.setTenantId("-");
	}

	@Override
	public void setAccount(String account) {
		throw CustomException.bq("Empty Token is readonly");
	}

	@Override
	public void setPassword(String id) {
		throw CustomException.bq("Empty Token is readonly");
	}

	@Override
	public void setId(String id) {
		throw CustomException.bq("Empty Token is readonly");
	}

	@Override
	public void setTenantId(String id) {
		throw CustomException.bq("Empty Token is readonly");
	}

	@Override
	@JsonIgnore
	public boolean isEmpty() {
		return true;
	}
}
