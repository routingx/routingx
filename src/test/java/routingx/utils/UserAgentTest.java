package routingx.utils;

import org.junit.Test;

import routingx.UserAgent;
import routingx.json.JSON;

public class UserAgentTest {

	@Test
	public void test() {
		UserAgent us = UserAgent.parse("");
		System.out.println(JSON.format(us));
		us = UserAgent.parse(
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.87 Safari/537.36");
		System.out.println(JSON.format(us));
	}
}
