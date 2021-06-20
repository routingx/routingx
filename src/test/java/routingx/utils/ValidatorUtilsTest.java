package routingx.utils;

import org.junit.Test;

public class ValidatorUtilsTest {

	@Test
	public void test() {
		ValidatorBean bean = new ValidatorBean();
		bean.setMail("aa@bb.com");
		bean.setMobile("+0813570665678");
		bean.setTelephone("+860756-9988776");
		bean.setUsername("a1_123456");
		bean.setPassword("Aabc012@");
		ValidatorUtils.validatorAssert(bean);
	}

}
