package routingx.data;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.data.domain.Sort.Direction;

@Target({ METHOD, FIELD })
@Retention(RUNTIME)
public @interface OrderBy {
	Direction value() default Direction.ASC;

	int order() default 0;
}
