package routingx.utils;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CaptchaUtilsTest {

	@Test
	public void test() {
		log.info(CaptchaUtils.randomNumber(4));
	}

}
