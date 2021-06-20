package routingx.data;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import routingx.model.Sorted;
import routingx.utils.TextUtils;

@Setter(value = AccessLevel.PRIVATE)
@Getter
public class EntityProperties {
	public enum FieldType {
		BOOLEAN, NUMERIC, DATE, STRING, ENUM
	}

	@JsonIgnore
	private final PropertyDescriptor propertyDescriptor;
	@JsonIgnore
	private final Field field;
	private final String column;
	private final String name;
	private final FieldType fieldType;
	private final boolean persistable;
	private final boolean updatable;
	private final boolean temporal;

	private final boolean id;
	private final boolean version;
	private final boolean created;
	private final boolean creater;
	private final boolean deleted;
	private final boolean updated;
	private final boolean updater;
	private final boolean tenantId;
	private final boolean userId;

	public EntityProperties(Field field, PropertyDescriptor propertyDescriptor) {
		this.propertyDescriptor = propertyDescriptor;
		this.field = field;
		this.name = field.getName();
		String column = name;
		boolean updatable = true;
		Column c = getAnnotation(Column.class);
		if (c != null) {
			if (!c.name().isEmpty()) {
				column = c.name();
			} else {
				column = TextUtils.camelToUnderline(column);
			}
			updatable = c.updatable();
		} else {
			column = TextUtils.camelToUnderline(column);
		}
		fieldType = fieldType();
		this.column = column;
		this.updatable = updatable;
		this.persistable = persistable();
		this.id = idPersistable();
		this.temporal = hasAnnotation(Temporal.class);
		this.version = hasAnnotation(Version.class);
		this.created = hasAnnotation(Created.class);
		this.creater = hasAnnotation(Creater.class);
		this.deleted = hasAnnotation(Deleted.class);
		this.updated = hasAnnotation(Updated.class);
		this.updater = hasAnnotation(Updater.class);
		this.tenantId = hasAnnotation(TenantId.class);
		this.userId = hasAnnotation(UserId.class);
	}

	public Object getDefaultValue() {
		switch (fieldType) {
		case NUMERIC:
			return 0;
		case BOOLEAN:
			return Boolean.FALSE;
		default:
			if (LocalDateTime.class.isAssignableFrom(field.getType())) {
				return LocalDateTime.now();
			}
			if (LocalDate.class.isAssignableFrom(field.getType())) {
				return LocalDate.now();
			}
			if (Duration.class.isAssignableFrom(field.getType())) {
				return Duration.ZERO;
			}
			return null;
		}

	}

	public Object getDeletedValue() {
		switch (fieldType) {
		case NUMERIC:
			return -1;
		case BOOLEAN:
			return Boolean.TRUE;
		default:
			return Boolean.TRUE.toString();
		}
	}

	protected FieldType fieldType() {
		if (Number.class.isAssignableFrom(field.getType())) {
			return FieldType.NUMERIC;
		}
		if (field.getType().equals(java.lang.Boolean.class)) {
			return FieldType.BOOLEAN;
		}
		if (java.util.Date.class.isAssignableFrom(field.getType())
				|| java.time.temporal.Temporal.class.isAssignableFrom(field.getType())) {
			return FieldType.DATE;
		}
		return FieldType.STRING;
	}

	private static boolean isPersistable(Class<?> klass) {
		return null != klass.getAnnotation(Table.class)//
				|| null != klass.getAnnotation(Entity.class)//
				|| null != klass.getAnnotation(org.springframework.data.relational.core.mapping.Table.class) //
				|| null != klass.getAnnotation(Embeddable.class) //
				|| null != klass.getAnnotation(MappedSuperclass.class);
	}

	private boolean idPersistable() {
		boolean anno = (hasAnnotation(javax.persistence.Id.class)//
				|| hasAnnotation(org.springframework.data.annotation.Id.class)//
				|| hasAnnotation(EmbeddedId.class));
		if (!anno) {
			return anno;
		}
		if (!persistable()) {
			return false;
		}
		if (!isDbType(field.getType())) {
			return false;
		}
		return true;
	}

	private static boolean isDbType(Class<?> clazz) {
		if (clazz.isPrimitive()//
				|| Boolean.class.isAssignableFrom(clazz)//
				|| Number.class.isAssignableFrom(clazz)//
				|| String.class.isAssignableFrom(clazz)//
				|| java.util.Date.class.isAssignableFrom(clazz)//
				|| java.time.Duration.class.isAssignableFrom(clazz)//
				|| java.time.temporal.Temporal.class.isAssignableFrom(clazz)) {
			return true;
		}
		return false;
	}

	private boolean persistable() {
		if (!isPersistable(field.getDeclaringClass())) {
			return false;
		}
		int mod = field.getModifiers();
		if (hasAnnotation(Transient.class) || Modifier.isTransient(mod) || Modifier.isFinal(mod)
				|| Modifier.isStatic(mod)) {
			return false;
		}
		return true;
	}

	public <T extends Annotation> boolean hasAnnotation(Class<T> annotationClass) {
		return getAnnotation(annotationClass) != null;
	}

	public Sorted.Order getOrder() {
		OrderBy annot = getAnnotation(OrderBy.class);
		if (annot == null) {
			return null;
		} else {
			return new Sorted.Order(annot.value(), column, annot.order());
		}
	}

	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		T annot = null;
		if (annot == null && propertyDescriptor != null) {
			Method read = propertyDescriptor.getReadMethod();
			annot = read != null ? read.getAnnotation(annotationClass) : null;
		}
		if (annot == null && propertyDescriptor != null) {
			Method method = propertyDescriptor.getWriteMethod();
			annot = method != null ? method.getAnnotation(annotationClass) : null;
		}
		if (annot == null) {
			annot = field.getAnnotation(annotationClass);
		}
		return annot;
	}

}