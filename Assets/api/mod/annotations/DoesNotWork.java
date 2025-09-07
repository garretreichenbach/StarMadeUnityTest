package api.mod.annotations;

import java.lang.annotation.*;
/**
 * Annotates that something exists in StarMade/StarLoader, but does not actually work.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
public @interface DoesNotWork {
    String value() default "";
}
