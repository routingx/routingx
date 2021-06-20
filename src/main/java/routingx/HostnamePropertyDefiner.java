package routingx;

import java.net.InetAddress;
import java.net.UnknownHostException;

import ch.qos.logback.core.PropertyDefinerBase;

public class HostnamePropertyDefiner extends PropertyDefinerBase {

	@Override
	public String getPropertyValue() {
		InetAddress ia;
		try {
			ia = InetAddress.getLocalHost();
			String host = ia.getHostName();// 获取计算机主机名
			return host;
		} catch (UnknownHostException ignored) {
		}
		return "UNKNOW";
	}
}