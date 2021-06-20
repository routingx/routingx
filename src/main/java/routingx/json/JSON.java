package routingx.json;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import lombok.extern.slf4j.Slf4j;
import routingx.model.SuperEntity;
import routingx.utils.TextUtils;

@Slf4j
public class JSON {

	private static ObjectMapper om;

	private static ObjectMapper objectMapper() {
		if (om == null) {
			om = new ObjectMapper();
			om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
			om.configure(MapperFeature.USE_ANNOTATIONS, true);
			om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
			om.setDateFormat(new SimpleDateFormat(SuperEntity.DATE_TIME_FORMAT));
			om.setTimeZone(TimeZone.getTimeZone(SuperEntity.TIMEZONE));
//			om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);		
//			om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
//			om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

			// 解决LocalDateTime 时间格式问题
			JavaTimeModule javaTimeModule = new JavaTimeModule();
			DateTimeFormatter localDateTimeFormat = DateTimeFormatter.ofPattern(SuperEntity.DATE_TIME_FORMAT);
			javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(localDateTimeFormat));
			javaTimeModule.addDeserializer(LocalDateTime.class, new JacksonLocalDateTimeDeserializer());
			javaTimeModule.addDeserializer(Date.class, new JacksonDateDeserializer());
			om.registerModules(javaTimeModule);
			om.registerModules(new ParameterNamesModule());
			om.registerModules(ObjectMapper.findModules());
		}
		return om;
	}

	public static void setObjectMapper(ObjectMapper objectMapper) {
		JSON.om = objectMapper;
	}

	public static String format(String json) {
		try {
			if (StringUtils.isNotBlank(json)) {
				JsonNode node = objectMapper().readTree(json);
				return toJSONString(node, true);
			}
		} catch (Exception e) {
			log.error(json, e);
		}
		return json;
	}

	public static String format(byte[] buffer) {
		if (buffer == null) {
			return null;
		}
		try {
			if (buffer != null && buffer.length > 1) {
				JsonNode node = objectMapper().readTree(buffer);
				return toJSONString(node, true);
			}
		} catch (Exception e) {
			log.error(TextUtils.toString(buffer), e);
		}
		return TextUtils.toString(buffer);
	}

	/**
	 * 
	 * @param object
	 * @return
	 */
	public static String format(Object object) {
		if (object != null && object instanceof String) {
			return toJSONString(parseMap(object), true);
		}
		return toJSONString(object, true);
	}

	public static Map<String, Object> parseMap(Object object) {
		return parseMap(toJSONString(object));
	}

	/**
	 * 
	 * @param json
	 * @return
	 */
	public static Map<String, Object> parseMap(String json) {
		try {
			if (StringUtils.isNotBlank(json)) {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = objectMapper().readValue(json, HashMap.class);
				return map;
			}
		} catch (Exception e) {
			log.error(json, e);
		}
		return null;
	}

	public static Map<String, Object> parseMap(byte[] src) {
		try {
			if (src != null && src.length > 0) {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = objectMapper().readValue(src, HashMap.class);
				return map;
			}
		} catch (Exception e) {
			log.error("parseMap", e);
		}
		return null;
	}

	public static JSONObject parseObject(String json) {
		try {
			if (StringUtils.isNotBlank(json)) {
				return new JSONObject(objectMapper().readTree(json));
			}
		} catch (Exception e) {
			log.error(json, e);
		}
		return null;
	}

	/**
	 * 
	 * @param <T>
	 * @param json
	 * @param clazz
	 * @return
	 */
	public static <T> T parseObject(byte[] src, Class<T> clazz) {
		try {
			if (src != null && src.length > 0) {
				return objectMapper().readValue(src, clazz);
			}
		} catch (Exception e) {
			log.error("parseObject", e);
		}
		return null;
	}

	/**
	 * 
	 * @param <T>
	 * @param json
	 * @param clazz
	 * @return
	 */
	public static <T> T parseObject(String json, Class<T> clazz) {
		try {
			if (StringUtils.isNotBlank(json)) {
				return objectMapper().readValue(json, clazz);
			}
		} catch (Exception e) {
			log.error(json, e);
		}
		return null;
	}

	public static <T> T parseObject(Object object, Class<T> clazz) {
		try {
			if (object != null) {
				return objectMapper().readValue(objectMapper().writeValueAsString(object), clazz);
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> parseList(String json, Class<T> elementClasses) {
		if (StringUtils.isBlank(json)) {
			return null;
		}
		try {
			JavaType javaType = getCollectionType(ArrayList.class, elementClasses);
			List<T> list = (List<T>) objectMapper().readValue(json, javaType);
			return list;
		} catch (Throwable e) {
			try {
				List<T> list = objectMapper().readValue(json, new TypeReference<List<T>>() {
				});
				JavaType javaType = getCollectionType(ArrayList.class, elementClasses);
				list = (List<T>) objectMapper().readValue(JSON.format(list), javaType);
				return list;
			} catch (Throwable ex) {
				log.error(json, e);
			}
		}
		return null;
	}

	public static <T> List<T> parseList(Object array, Class<T> elementClasses) {
		String json = toJSONString(array);
		return parseList(json, elementClasses);
	}

	/**
	 * 
	 * @param object
	 * @return
	 */
	public static String toJSONString(Object object, boolean format) {
		try {
			if (object == null) {
				return null;
			}
			if (format) {
				return objectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);
			} else {
				return objectMapper().writeValueAsString(object);
			}
		} catch (Exception e) {
			log.error(object.toString(), e);
		}
		return null;
	}

	/**
	 * 
	 * @param object
	 * @return
	 */
	public static String toJSONString(Object object) {
		return toJSONString(object, false);
	}

	public static String toJSONString(JSONObject object) {
		return object.toString();
	}

	protected static List<JSONObject> toObject(List<JsonNode> nodeList) {
		List<JSONObject> jsons = new ArrayList<>();
		for (JsonNode n : nodeList) {
			jsons.add(new JSONObject(n));
		}
		return jsons;
	}

	protected static List<JsonNode> toNode(List<JSONObject> objectList) {
		List<JsonNode> jsons = new ArrayList<>();
		for (JSONObject o : objectList) {
			jsons.add(o.getNode());
		}
		return jsons;
	}

	private static JavaType getCollectionType(Class<?> collectionClass, Class<?> elementClasses) {
		return om.getTypeFactory().constructParametricType(collectionClass, elementClasses);
	}

	public static void writeValue(OutputStream outputStream, Object value) {
		try {
			om.writeValue(outputStream, value);
		} catch (Throwable e) {
			log.error("", e);
		}
	}
}
