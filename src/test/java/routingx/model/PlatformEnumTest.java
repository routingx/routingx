package routingx.model;

import org.junit.Test;

import junit.framework.TestCase;

public class PlatformEnumTest {
	@Test
	public void test() throws Exception {
		PlatformEnum platform = PlatformEnum.of("0");
		TestCase.assertTrue(platform == null);

		platform = PlatformEnum.of("web");
		TestCase.assertTrue(platform != null);
	}
}
