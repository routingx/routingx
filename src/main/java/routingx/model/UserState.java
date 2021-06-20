package routingx.model;

import routingx.Note;

@Note("0-已注册 1-已激活 2-已锁定 3-证书过期")
public enum UserState {

	REGED("已注册", 0), //
	ACTIVATED("已激活", 1), //
	LUCKED("已锁定", 2), //
	CERTIFICATE("证书过期", 3);

	private String memo;
	private Integer value;

	private UserState(String memo, int value) {
		this.memo = memo;
		this.value = value;
	}

	public String memo() {
		return memo;
	}

	public Integer value() {
		return value;
	}

	public static UserState valueOf(Integer value) {
		for (UserState c : UserState.values()) {
			if (c.value == value) {
				return c;
			}
		}
		return null;
	}

}
