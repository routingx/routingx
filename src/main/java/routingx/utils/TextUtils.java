package routingx.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;
import ognl.Ognl;
import routingx.CustomException;
import routingx.Note;

@Slf4j
public class TextUtils {

	public static final String LINE_SEPARATOR = System.lineSeparator();

	@Note("随机生成常见汉字")
	public static String getRandomChar() {
		String str = "";
		int highCode;
		int lowCode;

		Random random = new Random();

		highCode = (176 + Math.abs(random.nextInt(39))); // B0 + 0~39(16~55) 一级汉字所占区
		lowCode = (161 + Math.abs(random.nextInt(93))); // A1 + 0~93 每区有94个汉字

		byte[] b = new byte[2];
		b[0] = (Integer.valueOf(highCode)).byteValue();
		b[1] = (Integer.valueOf(lowCode)).byteValue();

		try {
			str = new String(b, "GBK");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return str;
	}

	public static List<String> split(String string) {
		List<String> list = new ArrayList<>();
		if (string != null && string.trim().length() > 0) {
			string = string.trim();
			string = string.replaceAll("，", ",");
			string = string.replaceAll(";", ",");
			string = string.replaceAll("；", ",");
			// string = string.replaceAll("|", ",");
			string = string.replaceAll(" ", ",");
			string = string.replaceAll("\n", ",");
			string = string.replaceAll("\n\t", ",");
			string = string.replaceAll("\t\n", ",");
			list.addAll(Arrays.asList(string.split(",")));
		}
		return list;
	}

	@Note("模板字符串解析")
	public static String analysisString(String model, Map<String, Object> map) {
		model = model.replace("}", "} ");
		List<String> objModel = getModelViewFile(model);
		for (String model_obj : objModel) {
			String patch = model_obj.replace("{", "").replace("}", "");
			String obj_string = getObjString(map.get(patch.split("\\.")[0]),
					StringUtils.join(".", removeListThis(patch.split("\\."))));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			try {
				model = model.replace("#$" + model_obj + " ",
						obj_string == null ? "" : sdf.format(new Date(Long.valueOf(obj_string) * 1000)));
			} catch (Exception ignored) {
			}
			model = model.replace("$" + model_obj + " ", obj_string == null ? "" : obj_string);
		}
		return model;
	}

	public static List<String> getModelViewFile(String s) {
		List<String> PString = new ArrayList<>();
		Pattern pattern = Pattern.compile("\\$(\\S)*");
		Matcher matcher = pattern.matcher(s);
		while (matcher.find()) {
			String ss = matcher.group();
			String[] res = ss.split("\\$");
			String a = res[1];
			String b = a.split("\"")[0];
			b = b.split(" ")[0];
			b = b.split(">")[0];
			b = b.split(",")[0];
			PString.add(b);
		}
		return PString;
	}

	private static String getObjString(Object obj, String objStr) {
		if (obj == null) {
			return null;
		}
		// 如果是字符串类型，则直接返回
		if (obj instanceof String) {
			return (String) obj;
		}
		if (obj instanceof Map<?, ?>) {
			Map<?, ?> map = (Map<?, ?>) obj;
			return "" + map.get(objStr);
		}
		String objs[] = objStr.split("\\.");
		Object object__ = null;
		try {
			object__ = Ognl.getValue(obj, objs[0]);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		String[] objs__ = removeListThis(objs);
		String objStr__ = String.join(".", objs__);
		if (objs.length == 1) {
			return object__.toString();
		}
		return getObjString(object__, objStr__);// 递归
	}

	@Note("丢掉 当前 也就是 第一个元素")
	private static String[] removeListThis(String[] objs) {
		if (objs.length == 1) {
			return objs;
		} else {
			String[] objs__ = new String[objs.length - 1];
			System.arraycopy(objs, 1, objs__, 0, objs.length - 1);
			return objs__;
		}
	}

	private static final String IDCARD_REGEX = "^d{15}|d{}18$";

	@Note("身份证号合法较验")
	public static boolean isIDCARD(String idcard) {
		if (idcard == null) {
			return false;
		}
		return idcard.matches(IDCARD_REGEX);
	}

	private static final String ACCOUNT_REGEX = "^[A-Za-z0-9_.-]+$";

	@Note("帐号合法较验^[A-Za-z0-9_.-]+$")
	public static boolean isAccount(String account) {
		if (account == null) {
			return false;
		}
		if (account.length() < 5 || account.length() > 100) {
			return false;
		}
		if (account.startsWith("_") || account.startsWith("-") || account.startsWith(".")) {
			return false;
		}
		if (account.indexOf("__") > 0 || account.indexOf("--") > 0 || account.indexOf("..") > 0) {
			return false;
		}
		return account.matches(ACCOUNT_REGEX);
	}

	private static final String EMAIL_REGEX = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

	private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

	@Note("邮件地址合法较验")
	public static boolean isMail(String email) {
		if (email == null) {
			return false;
		}
		Matcher matcher = EMAIL_PATTERN.matcher(email);
		return matcher.matches();
	}

	public static boolean innerIP(String ip) {
		if (ip == null) {
			ip = "127.0.0.1";
		}
		Pattern reg = Pattern.compile(
				"^(127\\.0\\.0\\.1)|(localhost)|(10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})|(172\\.((1[6-9])|(2\\d)|(3[01]))\\.\\d{1,3}\\.\\d{1,3})|(192\\.168\\.\\d{1,3}\\.\\d{1,3})$");
		Matcher match = reg.matcher(ip);
		return match.find();
	}

	@Note("文件转换为配置")
	public static Properties load(String text) {
		Properties prop = new Properties();
		try {
			prop.load(new StringReader(text));
		} catch (Throwable ex) {
			log.error(toStringWith(ex));
		}
		return prop;
	}

	public static String toString(final Throwable e) {
		if (e == null) {
			return null;
		}
		StringWriter stringWriter = new StringWriter();
		e.printStackTrace(new PrintWriter(stringWriter));
		return stringWriter.toString();
	}

	public static String toStringWith(final Throwable e) {
		return toStringWith(e, false);
	}

	private static String toStringWith(final Throwable e, boolean suppressed) {
		String startsWith = CustomException.class.getPackage().getName();
		if (e == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		if (!suppressed) {
			sb.append(e.getMessage());
		} else {
			append(e, startsWith, sb);
		}
		sb.append(LINE_SEPARATOR);
		StackTraceElement[] stackTraceElements = e.getStackTrace();
		if (stackTraceElements != null) {
			for (StackTraceElement stack : stackTraceElements) {
				if (stack.getClassName().startsWith(startsWith)) {
					if (suppressed) {
						sb.append("\t ");
					} else {
						sb.append("at ");
					}
					sb.append(stack.getClassName());
					sb.append(".");
					sb.append(stack.getMethodName());
					sb.append("(" + stack.getFileName() + ":" + stack.getLineNumber() + ")");
					sb.append(LINE_SEPARATOR);
				}
			}
		}
		Throwable[] errs = e.getSuppressed();
		for (Throwable err : errs) {
			sb.append("Suppressed: " + toStringWith(err, true));
		}
		return sb.toString();
	}

	private static void append(final Throwable e, String startsWith, StringBuilder sb) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new StringReader(e.getMessage()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.contains(startsWith)) {
					sb.append(LINE_SEPARATOR);
					sb.append(line);
				}
			}

		} catch (Exception ex) {
			log.error("", ex);
		} finally {
			try {
				reader.close();
			} catch (IOException e1) {
				;
			}
		}
	}

	public static final char UNDERLINE = '_';

	/**
	 * 驼峰格式字符串转换为下划线格式字符串
	 * 
	 * @param param-----------String
	 * @return String
	 */
	public static String camelToUnderline(String param) {
		if (param == null || "".equals(param.trim())) {
			return "";
		}
		int len = param.length();
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			char c = param.charAt(i);
			if (Character.isUpperCase(c)) {
				sb.append(UNDERLINE);
				sb.append(Character.toLowerCase(c));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	@Note("下划线格式字符串转换为驼峰格式字符串")
	public static String underlineToCamel(String param) {
		if (param == null || "".equals(param.trim())) {
			return "";
		}
		int len = param.length();
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			char c = param.charAt(i);
			if (c == UNDERLINE) {
				if (++i < len) {
					sb.append(Character.toUpperCase(param.charAt(i)));
				}
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static String toString(byte[] buffer) {
		return new String(buffer, StandardCharsets.UTF_8);
	}

	public static byte[] toBytes(String text) {
		return text.getBytes(StandardCharsets.UTF_8);
	}
}
