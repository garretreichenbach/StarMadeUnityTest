package org.schema.game.common.controller.rules.rules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RuleValue {
	String tag();

	int[] intMap() default {};
	String[] int2StringMap() default {};
}
