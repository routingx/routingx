package routingx.utils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import routingx.json.JSON;

@Slf4j
public class UtilsTest {

	@Test
	public void addressUtils() {
		log.info(JSON.format(AddressUtils.getLocalIPList()));
	}

	@Test
	public void loaderUtils() {
		log.info(LoaderUtils.getDomainPath(java.util.Date.class));
	}

	@Test
	public void nickNameRandomUtils() {
		HashSet<String> set = new HashSet<String>();
		for (int i = 0; i < 100; i++) {
			set.add(RandomStringUtils.random());
		}
		log.info(JSON.format(set));
		set.clear();
		log.info(RandomStringUtils.randomEn(16));
	}

	@Test
	public void numericUtils() {
		long[] a = { 60, 2, 500, 10, 2000, 30, 40, 20, 100, 5, 6000 };
		System.out.println(NumericUtils.commonMultiple(a));
		long[] b = { 1200, 40, 10000, 200, 40000, 600, 800, 400, 2000, 100, 120000 };
		System.out.println(NumericUtils.commonMultiple(b));

		System.out.println(NumericUtils.equals(BigDecimal.ZERO, BigDecimal.ZERO));
		System.out.println(NumericUtils.equals(BigDecimal.valueOf(1), BigDecimal.valueOf(1)));
		System.out.println(RandomStringUtils.random(10, 32, 127, false, false));
		int betsMaxMultipleUser = 5 * 3 * 300;
		System.out.println(NumericUtils.divide2(BigDecimal.valueOf(12996), betsMaxMultipleUser).longValue());
		System.out
				.println(NumericUtils.divide2(NumericUtils.scale2(999989989994354.2D), betsMaxMultipleUser).intValue());
		System.out.println(
				(int) NumericUtils.divide2(NumericUtils.scale2(999989989994354.2D), betsMaxMultipleUser).longValue());
		System.out
				.println(NumericUtils.divide2(NumericUtils.scale2(999989989994354.2D), betsMaxMultipleUser).intValue());
		System.out.println(NumericUtils.scale2(0));
		System.out.println(NumericUtils.scale2(999989989994354.2D).doubleValue() - 100);
		System.out.println(BigDecimal.valueOf(100).setScale(2, RoundingMode.DOWN).divide(BigDecimal.valueOf(3), 2,
				RoundingMode.DOWN));
	}

	@Test
	public void textUtils() {
		List<String> list = TextUtils.split("1,2,3,4,5,6,7,8,9,10,13,26,39,52,11,14,15,16,17,18,19,1,8,2,0,2,1");
		System.out.println(list);
		TestCase.assertTrue(TextUtils.isAccount("aab2s_2.s-_9"));
		TestCase.assertTrue(TextUtils.isAccount("123567"));
		TestCase.assertFalse(TextUtils.isAccount("a--------"));
		TestCase.assertTrue(TextUtils.isMail("xx@cc.co"));
	}

	@Test
	public void verifyCodeUtils() throws IOException {
		File dir = new File("D:/upload/verifyCode");
		int w = 240, h = 80;
		for (int i = 0; i < 50; i++) {
			String verifyCode = CaptchaUtils.random(4);
			File file = new File(dir, i + verifyCode + ".jpg");
			CaptchaUtils.outputImage(w, h, file, verifyCode);
		}
	}

}
