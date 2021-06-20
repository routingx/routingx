package routingx.utils;

import org.junit.Test;

import junit.framework.TestCase;

public class ZipUtilsTest {

	@Test
	public void test() throws Exception {
		final String text = "7000@eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOlsiMTM4MjE5NjA4ODYxMzc2OTIxNiIsIjEzODY1MDgxMDE4NzE2MDM3MTIiLCJhZG1pbiIsIm5hc3RlMTIzNDU2IiwiMjAyMS0wNC0yNiAxNDo1MDoyMCIsIldpbmRvd3MgTlQgMTAuMDsgV2luNjQ7IHg2NCIsInRydWUiXSwiaXNzIjoiemhuYXN0ZS5jb20iLCJleHAiOjE2MjAwMjQ3MTcsImlhdCI6MTYxOTQxOTkxN30.lHcbTWzLsbASK7s-Cv4BbD2JVh9PGu5tXpEm7P7fEnI";
		String com = ZipUtils.compress(text);
		System.out.println(text);
		System.out.println(com);
		TestCase.assertTrue(ZipUtils.unCompress(com).equals(text));
	}
}
