package routingx.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ObjectUtils {

	public static Object readField(Object orig, String fieldName) {
		try {
			if (FieldUtils.getField(orig.getClass(), fieldName, true) != null) {
				return FieldUtils.readField(orig, fieldName, true);
			}
		} catch (Throwable e) {
			log.warn(e.getMessage(), e.toString());
		}
		return null;
	}

	public static Object readField(final Object orig, final Field field) {
		try {
			return FieldUtils.readField(field, orig, true);
		} catch (IllegalAccessException e) {
			log.warn(e.getMessage());
			return null;
		}
	}

	public static void writeFieldIfNull(Object entity, String field, Object value) {
		try {
			if (FieldUtils.getField(entity.getClass(), field, true) != null) {
				if (FieldUtils.readField(entity, field, true) == null) {
					writeField(entity, field, value);
				}
			}
		} catch (Throwable e) {
			log.debug(e.getMessage());
		}
	}

	public static void writeField(Object orig, String fieldName, Object value) {
		try {
			if (FieldUtils.getField(orig.getClass(), fieldName, true) != null) {
				FieldUtils.writeField(orig, fieldName, value, true);
			}
		} catch (Throwable e) {
			log.warn(e.getMessage(), e.toString());
		}
	}

	public static void writeField(final Object orig, final Field field, final Object value) {
		try {
			FieldUtils.writeField(field, orig, value, true);
		} catch (IllegalAccessException e) {
			log.warn(e.getMessage());
		}
	}

	public static void setDefaultValue(Object orig) {
		Field[] fields = ObjectUtils.getNonAccessModifierFields(orig.getClass());
		for (Field field : fields) {
			if (ObjectUtils.readField(orig, field) == null) {
				ObjectUtils.setDefaultValue(orig, field);
			}
		}
	}

	public static void setDefaultValue(Object orig, Field field) {
		if (field.getType().equals(java.lang.Boolean.class)) {
			writeField(orig, field, false);
		} else if (field.getType().equals(java.lang.Integer.class)) {
			writeField(orig, field, 0);
		} else if (field.getType().equals(java.lang.Long.class)) {
			writeField(orig, field, 0L);
		} else if (field.getType().equals(BigDecimal.class)) {
			writeField(orig, field, BigDecimal.ZERO);
		} else if (field.getType().equals(java.lang.Byte.class)) {
			writeField(orig, field, (byte) 0);
		} else if (field.getType().equals(java.lang.Short.class)) {
			writeField(orig, field, (short) 0);
		} else if (field.getType().equals(java.lang.Float.class)) {
			writeField(orig, field, 0F);
		} else if (field.getType().equals(java.lang.Double.class)) {
			writeField(orig, field, 0D);
		} else if (field.getType().equals(java.math.BigInteger.class)) {
			writeField(orig, field, BigInteger.ZERO);
		} else if (field.getType().equals(java.util.Date.class)) {
			writeField(orig, field, new Date());
		} else if (field.getType().equals(LocalDateTime.class)) {
			writeField(orig, field, LocalDateTime.now());
		} else if (field.getType().equals(LocalDate.class)) {
			writeField(orig, field, LocalDate.now());
		}
	}

	public static Field[] getAllFields(Class<?> clazz) {
		final List<Field> allFieldsList = FieldUtils.getAllFieldsList(clazz);
		return allFieldsList.toArray(ArrayUtils.EMPTY_FIELD_ARRAY);
		// return FieldUtils.getAllFields(clazz);
	}

	public static Field[] getNonAccessModifierFields(Class<?> clazz) {
		final List<Field> allFieldsList = FieldUtils.getAllFieldsList(clazz).stream()//
				.filter(field -> isNonAccessModifier(field)).collect(Collectors.toList());
		return allFieldsList.toArray(ArrayUtils.EMPTY_FIELD_ARRAY);
		// return FieldUtils.getAllFields(clazz);
	}

	public static boolean isNonAccessModifier(Field field) {
		int mod = field.getModifiers();
		if (Modifier.isTransient(mod) || Modifier.isFinal(mod) || Modifier.isStatic(mod)) {
			return false;
		}
		return true;
	}

	public static Field getField(Class<?> clazz, String fieldName) {
		return FieldUtils.getField(clazz, fieldName, true);
	}

	public static boolean hasField(Class<?> clazz, String fieldName) {
		return FieldUtils.getField(clazz, fieldName, true) != null;
	}

	public static String toStringTrueFalse(Boolean bool) {
		if (bool == null) {
			bool = Boolean.FALSE;
		}
		return BooleanUtils.toStringTrueFalse(bool);
	}
}
