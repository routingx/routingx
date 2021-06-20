package routingx.utils;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;
import routingx.json.JSON;

@Slf4j
public class HttpUtils {

	public enum RequestMethod {

		GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE

	}

	/**
	 * multipart/form-data 格式发送数据时各个部分分隔符的前缀,必须为 --
	 */
	private static final String BOUNDARY_PREFIX = "--";
	/**
	 * 回车换行,用于一行的结尾
	 */
	private static final String LINE_END = "\r\n";

	public static String get(String url) {
		return request(url, "", RequestMethod.GET, null);
	}

	public static String get(String url, Map<String, String> parameters) {
		try {
			String params = parameters(parameters);
			return request(url + "?" + params, "", RequestMethod.GET, null);
		} catch (Exception e) {
			log.error("", e);
		}
		return null;
	}

	public static String post(String url, String params) {
		return request(url, params, RequestMethod.POST, null);
	}

	public static String post(String url, Map<String, String> parameters) {
		return request(url, parameters, RequestMethod.POST, null);
	}

	public static String request(String url, Map<String, String> parameters, RequestMethod method, String contentType) {
		return request(url, parameters, method, contentType, null);
	}

	public static String request(String url, String parameters, RequestMethod method, String contentType) {
		return request(url, parameters, method, contentType, null);
	}

	public static String request(String url, String parameters, RequestMethod method, String contentType,
			String charset) {
		Map<String, String> map = new HashMap<>();
		if (StringUtils.isNotBlank(parameters)) {
			String[] keyValues = parameters.split("&");
			for (String kv : keyValues) {
				String[] kvs = kv.split("=");
				map.put(kvs[0], kvs[1]);
			}
		}
		return request(url, map, method, contentType, charset);
	}

	public static String request(String url, Map<String, String> parameters, RequestMethod method, String contentType,
			String charset) {
		PrintWriter out = null;
		HttpURLConnection httpConn = null;
		try {
			if (method.equals(RequestMethod.GET)) {
				if (parameters != null && parameters.size() > 0) {
					String params = parameters(parameters);
					if (url.indexOf("?") > 1) {
						url = url + "&" + params;
					} else {
						url = url + "?" + params;
					}
					if (log.isDebugEnabled()) {
						log.debug(url);
					}
				}
			}
			// charset = charset != null ? charset : "utf-8";
			URL connURL = new URL(url);
			if (url.startsWith("https://")) {
				httpConn = (HttpsURLConnection) connURL.openConnection();
			} else {
				httpConn = (HttpURLConnection) connURL.openConnection();
			}
			httpConn.setConnectTimeout(15000);
			httpConn.setReadTimeout(60000);
			httpConn.setRequestMethod(method.name());
			httpConn.setRequestProperty("Accept", "*/*");
			httpConn.setRequestProperty("Connection", "Keep-Alive");
			httpConn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Safari/537.36");
//			httpConn.setRequestProperty("Charset", charset);
//			httpConn.setRequestProperty("Accept-Charset", charset);
			String boundary = null;
			if (contentType != null) {
				if (contentType.startsWith("multipart/form-data")) {
					boundary = "boundary" + System.currentTimeMillis();
					httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
				} else {
					httpConn.setRequestProperty("Content-Type", contentType);
				}
			} else {
				httpConn.setRequestProperty("contentType", "application/x-www-form-urlencoded");
			}
			httpConn.setDoInput(true);
			httpConn.setDoOutput(true);
			httpConn.setUseCaches(false);
			httpConn.setInstanceFollowRedirects(true);
			if (url.startsWith("https://")) {
				SSLContext sc = SSLContext.getInstance("SSL");
				sc.init(null, new TrustManager[] { new TrustAnyTrustManager() }, new java.security.SecureRandom());
				((HttpsURLConnection) httpConn).setSSLSocketFactory(sc.getSocketFactory());
				((HttpsURLConnection) httpConn).setHostnameVerifier(new TrustAnyHostnameVerifier());
			}
			httpConn.connect();
			if (!method.equals(RequestMethod.GET) && parameters != null) {
				out = new PrintWriter(httpConn.getOutputStream());
				if (boundary != null) {
					for (String key : parameters.keySet()) {
						writeFormDataField(boundary, out, key, parameters.get(key));
					}
					out.write(BOUNDARY_PREFIX + boundary + BOUNDARY_PREFIX + LINE_END);
				} else if (contentType != null && contentType.indexOf("json") > 0) {
					out.write(JSON.format(parameters));
				} else {
					String params = parameters(parameters);
					out.write(params);
				}
				out.flush();
			}
			if (httpConn.getResponseCode() == 302) {
				String location = httpConn.getHeaderField("Location");
				httpConn.disconnect();
				url = location;
				log.debug(url);
				httpConn = (HttpURLConnection) new URL(url).openConnection();
				httpConn.setConnectTimeout(15000);
				httpConn.setReadTimeout(15000);
			}
			if (httpConn.getResponseCode() == 200) {
				return IOUtils.toString(httpConn.getInputStream(), charset);
			} else if (httpConn.getErrorStream() != null) {
				log.error(url);
				log.error(IOUtils.toString(httpConn.getErrorStream(),charset));
			} else {
				log.error("请求响应错误 ResponseCode = {} {}", httpConn.getResponseCode(), url);
			}
		} catch (Exception ex) {
			log.error("{} {} {}", method.name(), url, TextUtils.toStringWith(ex));
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				httpConn.disconnect();
			} catch (Exception ex) {
				log.error("", ex);
			}
		}
		return null;
	}

	public static String parameters(Map<String, String> parameters) throws UnsupportedEncodingException {
		StringBuffer sb = new StringBuffer();
		for (String name : parameters.keySet()) {
			String value = parameters.get(name);
			if (value == null) {
				log.warn(name + " field value is null");
			} else {
				sb.append(name).append("=").append(URLEncoder.encode(value, "UTF-8")).append("&");
			}
		}
		String params = sb.length() > 1 ? sb.substring(0, sb.length() - 1) : sb.toString();
		return params;
	}

	private static void writeFormDataField(String boundary, PrintWriter out, String key, String value) {
		out.write(BOUNDARY_PREFIX + boundary + LINE_END);
		out.write(String.format("Content-Disposition: form-data; name=\"%s\"", key));
		out.write(LINE_END);
		out.write(LINE_END);
		out.write(value);
		out.write(LINE_END);
	}

	private static class TrustAnyTrustManager implements X509TrustManager {
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[] {};
		}
	}

	private static class TrustAnyHostnameVerifier implements HostnameVerifier {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}

	public static String ip2Area(String ip) {
		if (TextUtils.innerIP(ip)) {
			return "内网";
		}
		String url = "http://whois.pconline.com.cn/?ip=" + ip;
		String text = request(url, "", RequestMethod.GET, null, "gb2312");
		try {
			if (text != null) {
				text = text.split("位置：")[1];
				text = text.split("</p>")[0];
				return text;
			}
		} catch (Throwable ex) {
			log.error("查询ip[{}]所在地区失败：{}", ip, TextUtils.toStringWith(ex));
		}
		return "";
	}
}
