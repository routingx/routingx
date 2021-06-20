package routingx.data;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import routingx.Note;

@Note("删除关联表注解")
@Target({ ElementType.TYPE })
@Retention(RUNTIME)
public @interface LinkDeletes {
	LinkDelete[] value();
}
