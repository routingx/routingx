package routingx.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import lombok.extern.slf4j.Slf4j;
import routingx.CustomException;
import routingx.Note;

/**
 * @description: 校验工具类
 * @author: peixere@qq.com
 **/
//@Null 被注释的元素必须为 null
//@NotNull 被注释的元素必须不为 null
//@AssertTrue 被注释的元素必须为 true
//@AssertFalse 被注释的元素必须为 false
//@Min(value) 被注释的元素必须是一个数字，其值必须大于等于指定的最小值
//@Max(value) 被注释的元素必须是一个数字，其值必须小于等于指定的最大值
//@DecimalMin(value) 被注释的元素必须是一个数字，其值必须大于等于指定的最小值
//@DecimalMax(value) 被注释的元素必须是一个数字，其值必须小于等于指定的最大值
//@Size(max=, min=) 被注释的元素的大小必须在指定的范围内
//@Digits (integer, fraction) 被注释的元素必须是一个数字，其值必须在可接受的范围内
//@Past 被注释的元素必须是一个过去的日期
//@Future 被注释的元素必须是一个将来的日期
//@Pattern(regex=,flag=) 被注释的元素必须符合指定的正则表达式
//
//Hibernate Validator提供的校验注解：
//@NotBlank(message =) 验证字符串非null，且长度必须大于0
//@Email 被注释的元素必须是电子邮箱地址
//@Length(min=,max=) 被注释的字符串的大小必须在指定的范围内
//@NotEmpty 被注释的字符串的必须非空
//@Range(min=,max=,message=) 被注释的元素必须在合适的范围内
@Slf4j
public final class ValidatorUtils {

	@Note("正则表达式：验证金额")
	private static final String MONEY = "^\\d+(\\.\\d{1,2})?$";

	@Note("正则表达式：验证金额")
	public static boolean isMoney(String money) {
		return Pattern.matches(MONEY, money);
	}

	@Note(value = "正则表达式：验证用户名", memo = "必须是2-20位字母、数字、下划线")
	public static final String USERNAME = "^[a-zA-Z]\\w{2,20}$";

	@Note(value = "正则表达式：验证用户名", memo = "必须是2-30位字母、数字、下划线")
	public static boolean isUsername(String username) {
		return Pattern.matches(USERNAME, username);
	}

	@Note("正则表达式：强密码验证")
	public static final String PASSWORD = "^(?![A-Za-z0-9]+$)(?![a-z0-9\\W]+$)(?![A-Za-z\\W]+$)(?![A-Z0-9\\W]+$)[a-zA-Z0-9\\W]{6,}$";

	@Note("正则表达式：强密码验证")
	public static boolean isPassword(String password) {
		return Pattern.matches(PASSWORD, password);
	}

	/**
	 * @see 正则表达式：验证手机号
	 * @see ^((17[0-9])|(14[0-9])|(13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$
	 * @see (\\+\\d+)?1[34578]\\d{9}$"
	 */
	@Note("正则表达式：验证手机号")
	public static final String MOBILE = "(\\+\\d+)?1[34578]\\d{9}$";

	@Note("正则表达式：验证手机号")
	public static boolean isMobile(String mobile) {
		return Pattern.matches(MOBILE, mobile);
	}

	@Note("正则表达式：验证固定电话号码")
	public static final String PHONE = "(\\+\\d+)?(\\d{3,4}\\-?)?\\d{7,8}$";

	@Note("正则表达式：验证固定电话号码")
	public static boolean checkPhone(String phone) {
		return Pattern.matches(PHONE, phone);
	}

	@Note("正则表达式：验证邮箱")
	public static final String EMAIL = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";

	@Note("正则表达式：验证邮箱")
	public static boolean isEmail(String email) {
		return Pattern.matches(EMAIL, email);
	}

	@Note("正则表达式：验证汉字")
	public static final String CHINESE = "^[\u4e00-\u9fa5],{0,}$";

	@Note("正则表达式：验证汉字")
	public static boolean isChinese(String chinese) {
		return Pattern.matches(CHINESE, chinese);
	}

	@Note("正则表达式：验证身份证")
	public static final String ID_CARD = "(^\\d{18}$)|(^\\d{15}$)";

	@Note("正则表达式：验证身份证")
	public static boolean isIDCard(String idCard) {
		return Pattern.matches(ID_CARD, idCard);
	}

	@Note("正则表达式：验证URL")
	public static final String URL = "http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?";

	@Note("正则表达式：验证URL")
	public static boolean isUrl(String url) {
		return Pattern.matches(URL, url);
	}

	@Note("正则表达式：验证IP地址")
	public static final String IP_ADDR = "(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)";

	@Note("正则表达式：验证IP地址")
	public static boolean isIPv4(String ipAddr) {
		return Pattern.matches(IP_ADDR, ipAddr);
	}

	public static boolean isEmpty(Map<?, ?> map) {
		return (map == null || map.isEmpty());
	}

	private final static ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();

	@Note("验证对象字段注解是否合法")
	public static <T> Map<String, String> validator(T value, Class<?>... groups) {
		Validator validator = VALIDATOR_FACTORY.getValidator();
		Set<ConstraintViolation<T>> result = validator.validate(value, groups);
		if (result.isEmpty()) {
			return Collections.emptyMap();
		} else {
			Map<String, String> errors = new HashMap<>();
			result.forEach(violation -> {
				errors.put(violation.getPropertyPath().toString(), violation.getMessage());
			});
			return errors;
		}
	}

	@Note("验证集合中对象字段注解是否合法")
	public static List<Map<String, String>> validatorList(Collection<Object> collection) {
		List<Map<String, String>> errors = new ArrayList<>();
		collection.forEach(value -> {
			errors.add(validator(value, new Class[0]));
		});
		return errors;
	}

	@Note("验证对象字段注解是否合法")
	public static void validatorAssert(Object value) throws CustomException {
		Map<String, String> map = validator(value, new Class[0]);
		if (!isEmpty(map)) {
			log.warn("{} {}", value.getClass().getName(), map.toString());
			throw CustomException.bq(map.toString());
		}
	}
}