package routingx.model;

import java.util.HashMap;
import java.util.Map;

public enum PlatformEnum {
	WEB("浏览器"), //
	@Deprecated
	web("浏览器"), //兼容
	APP("原生APP"), //
	APPLET("小程序"), //
	PUBLIC("公众号"),;

	private String memo;

	private PlatformEnum(String memo) {
		this.memo = memo;
	}

	public String memo() {
		return memo;
	}

	public String value() {
		return name();
	}

	@Override
	public String toString() {
		return name();
	}

	public static PlatformEnum of(String name) {
		for (PlatformEnum c : PlatformEnum.values()) {
			if (c.name().equalsIgnoreCase(name)) {
				return c;
			}
		}
		return null;
	}

	Map<String, PlatformEnum> enumConstantDirectory() {
		Map<String, PlatformEnum> directory = new HashMap<>();
		for (PlatformEnum c : PlatformEnum.values()) {
			directory.put(c.name(), c);
			directory.put(c.name().toLowerCase(), c);
		}
		return directory;
	}
}
