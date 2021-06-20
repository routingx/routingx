package routingx.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import routingx.json.JSON;

@Slf4j
public class TokenTest {

	@Test
	public void test() throws Exception {
		log.info("" + LocalDateTime.class.isAssignableFrom(LocalDateTime.class));
		Token.addSecret("100", "test");
		Token token = new Token();
		token.setId(Token.nextId());
		token.setTokenId(token.getId());
		token.setAccount("admin");
		token.setPassword("123456");
		token.setRememberMe(true);
		token.setUpdated(LocalDateTime.now());
		token.setPlatform(PlatformEnum.PUBLIC);
		token.setSuperAdmin(false);
		token = JSON.parseObject(JSON.format(token), Token.class);
		token.sign();
		String access = token.getAccess();
		log.info(access);
		log.info(token.getRefresh());
		Token verify = Token.verify(access);
		log.info(JSON.format(verify));
	}

	@Test
	public void testHashCode() throws Exception {
		Map<Token, Token> map = new HashMap<>();
		Token token1 = new Token();
		token1.setId("1382196088613769216");
		token1.setTenantId("1382230420493570048");
		token1.setAccount("admin");
		token1.setPassword("123456");
		map.put(token1, token1);
		Token token2 = new Token();
		token2.setId("1382196088613769216");
		token2.setTenantId("1382230420493570048");
		token2.setAccount("admin");
		token2.setPassword("123456");
		log.info(token2.toString());
		TestCase.assertEquals(token1, token2);
		TestCase.assertTrue(map.containsKey(token2));
	}
}
