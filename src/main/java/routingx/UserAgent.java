package routingx;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import eu.bitwalker.useragentutils.BrowserType;
import eu.bitwalker.useragentutils.DeviceType;
import lombok.Getter;
import routingx.json.JSON;

@Getter
public class UserAgent {

	public final static String NAME = "User-Agent";
	private static String pattern = "^Mozilla/\\d\\.\\d\\s+\\(+.+?\\)";
	private static String pattern2 = "\\(+.+?\\)";
	private static Pattern r = Pattern.compile(pattern);
	private static Pattern r2 = Pattern.compile(pattern2);

	private final eu.bitwalker.useragentutils.UserAgent userAgent;
	private Short osId;
	private String osName;
	private String osGroup;
	private String osVersion;
	private String osManufacturer;
	private String osType;
	private Boolean mobile;
	private Boolean mobileDevice;
	private Boolean mobileBrowser;
	private String browserName;
	private String browserVersion;
	private String browserType;
	private String browserGroup;
	private String browserManufacturer;
	private String browserRenderingEngine;
	private String deviceInfo;

	public static UserAgent parse(String userAgentString) {
		return new UserAgent(userAgentString);
	}

	public UserAgent(String userAgentString) {
		userAgent = eu.bitwalker.useragentutils.UserAgent.parseUserAgentString(userAgentString);
		osId = userAgent.getOperatingSystem().getId();
		osGroup = userAgent.getOperatingSystem().getName().split(" ")[0];
		osName = userAgent.getOperatingSystem().name().split("_")[0];
		osVersion = userAgent.getOperatingSystem().name();
		osType = userAgent.getOperatingSystem().getDeviceType().name();
		osManufacturer = userAgent.getOperatingSystem().getManufacturer().name();
		mobileDevice = DeviceType.MOBILE.equals(userAgent.getOperatingSystem().getDeviceType());
		browserName = userAgent.getBrowser().getName();
		browserType = userAgent.getBrowser().getBrowserType().name();
		browserGroup = userAgent.getBrowser().getGroup().getName();
		browserManufacturer = userAgent.getBrowser().getManufacturer().name();
		// browserRenderingEngine =
		// userAgent.getBrowser().getRenderingEngine().getName();
		if (userAgent.getBrowser().getName() != null) {
			browserVersion = userAgent.getBrowser().getName();
		}
		if (userAgent.getBrowserVersion() != null) {
			browserVersion = browserVersion + " " + userAgent.getBrowserVersion().getVersion();
		}
		mobileBrowser = BrowserType.MOBILE_BROWSER.equals(userAgent.getBrowser().getBrowserType());
		mobile = mobileBrowser || mobileDevice;
		deviceInfo = getDeviceInfo(userAgentString);
	}

	private static String getDeviceInfo(String userAgent) {
		Matcher m = r.matcher(userAgent);
		String result = null;
		if (m.find()) {
			result = m.group(0);
		}
		if (result == null) {
			return result;
		}
		Matcher m2 = r2.matcher(result);
		if (m2.find()) {
			result = m2.group(0);
		}
		result = result.replace("(", "");
		result = result.replace(")", "");
		return filterDeviceInfo(result);
	}

	private static String filterDeviceInfo(String result) {
		if (StringUtils.isBlank(result)) {
			return null;
		}
		result = result.replace(" U;", "");
		result = result.replace(" zh-cn;", "");
		return result;
	}

	@Override
	public String toString() {
		return this.hashCode() + System.lineSeparator() + JSON.format(this);
	}

}
