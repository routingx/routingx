package routingx.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AddressUtils {

	public static String getLocalIP() {
		List<String> ipList = new ArrayList<String>();
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			NetworkInterface networkInterface;
			Enumeration<InetAddress> inetAddresses;
			while (networkInterfaces.hasMoreElements()) {
				networkInterface = networkInterfaces.nextElement();
				inetAddresses = networkInterface.getInetAddresses();
				while (inetAddresses.hasMoreElements()) {
					InetAddress address = inetAddresses.nextElement();
					if (address != null && address instanceof Inet4Address) { // IPV4
						if (!"127.0.0.1".equals(address.getHostAddress())) {
							ipList.add(address.getHostAddress());
						}
					}
				}
			}
		} catch (Throwable e) {
			log.error("", e);
		}
		if (ipList.size() > 0) {
			return ipList.get(0);
		}
		return null;
	}

	public static List<String> getLocalIPList() {
		List<String> ipList = new ArrayList<String>();
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			NetworkInterface networkInterface;
			Enumeration<InetAddress> inetAddresses;
			while (networkInterfaces.hasMoreElements()) {
				networkInterface = networkInterfaces.nextElement();
				inetAddresses = networkInterface.getInetAddresses();
				while (inetAddresses.hasMoreElements()) {
					InetAddress address = inetAddresses.nextElement();
					if (address != null && address instanceof Inet4Address) { // IPV4
						if (!"127.0.0.1".equals(address.getHostAddress())) {
							ipList.add(address.getHostAddress());
						}
					}
				}
			}
		} catch (Throwable e) {
			log.error("", e);
		}
		return ipList;
	}

	public static boolean isInternal(byte[] addr) {
		final byte b0 = addr[0];
		final byte b1 = addr[1];
		// 10.x.x.x/8
		final byte SECTION_1 = 0x0A;
		// 172.16.x.x/12
		final byte SECTION_2 = (byte) 0xAC;
		final byte SECTION_3 = (byte) 0x10;
		final byte SECTION_4 = (byte) 0x1F;
		// 192.168.x.x/16
		final byte SECTION_5 = (byte) 0xC0;
		final byte SECTION_6 = (byte) 0xA8;
		switch (b0) {
		case SECTION_1:
			return true;
		case SECTION_2:
			if (b1 >= SECTION_3 && b1 <= SECTION_4) {
				return true;
			}
		case SECTION_5:
			switch (b1) {
			case SECTION_6:
				return true;
			}
		default:
			return false;
		}
	}
}
