package routingx.json;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public class JSONObject extends JSON {
	private JsonNode node;

	protected JSONObject(JsonNode node) {
		this.node = node;
	}

	protected JsonNode getNode() {
		return node;
	}

	public int size() {
		return node.size();
	}

	public boolean isValueNode() {
		return node.isValueNode();
	}

	public boolean isContainerNode() {
		return node.isContainerNode();
	}

	public boolean isMissingNode() {
		return node.isMissingNode();
	}

	public boolean isArray() {
		return node.isArray();
	}

	public boolean isObject() {
		return node.isObject();
	}

	public final boolean isPojo() {
		return node.isPojo();
	}

	public final boolean isNumber() {
		return node.isNumber();
	}

	public boolean isIntegralNumber() {
		return node.isIntegralNumber();
	}

	public boolean isFloatingPointNumber() {
		return node.isFloatingPointNumber();
	}

	public boolean isShort() {
		return node.isShort();
	}

	public boolean isInt() {
		return node.isInt();
	}

	public boolean isLong() {
		return node.isLong();
	}

	public boolean isFloat() {
		return node.isFloat();
	}

	public boolean isDouble() {
		return node.isDouble();
	}

	public boolean isBigDecimal() {
		return node.isBigDecimal();
	}

	public boolean isBigInteger() {
		return node.isBigInteger();
	}

	public final boolean isTextual() {
		return node.isTextual();
	}

	public final boolean isBoolean() {
		return node.isBoolean();
	}

	public final boolean isNull() {
		return node.isNull();
	}

	public final boolean isBinary() {
		return node.isBinary();
	}

	public String textValue() {
		return node.textValue();
	}

	public byte[] binaryValue() throws IOException {
		return node.binaryValue();
	}

	public boolean booleanValue() {
		return node.booleanValue();
	}

	public Number numberValue() {
		return node.numberValue();
	}

	public short shortValue() {
		return node.shortValue();
	}

	public int intValue() {
		return node.intValue();
	}

	public long longValue() {
		return node.longValue();
	}

	public float floatValue() {
		return node.floatValue();
	}

	public double doubleValue() {
		return node.doubleValue();
	}

	public BigDecimal decimalValue() {
		return node.decimalValue();
	}

	public BigInteger bigIntegerValue() {
		return node.bigIntegerValue();
	}

	public String asText() {
		return node.asText();
	}

	public int asInt() {
		return node.asInt();
	}

	public long asLong() {
		return node.asLong();
	}

	public double asDouble() {
		return node.asDouble();
	}

	public boolean asBoolean() {
		return node.asBoolean();
	}

	public boolean getBoolean(String fieldName) {
		JsonNode jn = node.get(fieldName);
		return jn != null ? jn.asBoolean() : false;
	}

	public int getInt(String fieldName) {
		return node.get(fieldName).asInt();
	}

	public long getLong(String fieldName) {
		JsonNode jn = node.get(fieldName);
		return jn != null ? jn.asLong() : 0;
	}

	public double getDouble(String fieldName) {
		JsonNode jn = node.get(fieldName);
		return jn != null ? jn.asDouble() : 0;
	}

	public String getString(String fieldName) {
		JsonNode jn = node.get(fieldName);
		return jn != null ? jn.asText() : null;
	}

	public JSONObject get(String fieldName) {
		return new JSONObject(node.get(fieldName));
	}

	public JSONObject get(int index) {
		return new JSONObject(node.get(index));
	}

	public Iterator<String> fieldNames() {
		return node.fieldNames();
	}

	public JSONObject path(String fieldName) {
		return new JSONObject(node.path(fieldName));
	}

	public JSONObject path(int index) {
		return new JSONObject(node.path(index));
	}

	public JSONObject findValue(String fieldName) {
		return new JSONObject(node.findValue(fieldName));
	}

	public JSONObject findPath(String fieldName) {
		return new JSONObject(node.findPath(fieldName));
	}

	public JSONObject findParent(String fieldName) {
		return new JSONObject(node.findParent(fieldName));
	}

	public List<JSONObject> findValues(String fieldName, List<JSONObject> foundSoFar) {
		return JSON.toObject(node.findValues(fieldName, JSON.toNode(foundSoFar)));
	}

	public List<String> findValuesAsText(String fieldName, List<String> foundSoFar) {
		return node.findValuesAsText(fieldName, foundSoFar);
	}

	public List<JSONObject> findParents(String fieldName, List<JSONObject> foundSoFar) {
		return JSON.toObject(node.findParents(fieldName, JSON.toNode(foundSoFar)));
	}

	public JSONObject deepCopy() {
		return new JSONObject(node.deepCopy());
	}

	@Override
	public String toString() {
		return node.toString();
	}

	@Override
	public boolean equals(Object o) {
		return node.equals(o);
	}

//	public static String format(String json) {
//		return JSON.format(json);
//	}
//
//	public static String format(Object object) {
//		return JSON.format(object);
//	}
//
//	public static Map<String, String> parseMap(String json) {
//		return JSON.parseMap(json);
//	}
//
//	public static JSONObject parseObject(String json) {
//		return JSON.parseObject(json);
//	}
//
//	public static <T> T parseObject(String json, Class<T> clazz) {
//		return JSON.parseObject(json, clazz);
//	}
//
//	public static <T> T parseObject(Object object, Class<T> clazz) {
//		return JSON.parseObject(object, clazz);
//	}
//
//	public static <T> List<T> parseList(String json, Class<?>... elementClasses) {
//		return JSON.parseList(json, elementClasses);
//	}
//
//	public static <T> List<T> parseList(Object array, Class<?>... elementClasses) {
//		return JSON.parseList(array, elementClasses);
//	}
//
//	public static String toJSONString(Object object, boolean format) {
//		return JSON.toJSONString(object, true);
//	}
//
//	public static String toJSONString(Object object) {
//		return JSON.toJSONString(object);
//	}
//
//	public static String toJSONString(JSONObject object) {
//		return JSON.toJSONString(object);
//	}
}
