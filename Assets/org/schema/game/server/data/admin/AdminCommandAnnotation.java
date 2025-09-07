package org.schema.game.server.data.admin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AdminCommandAnnotation {

	String[] exampleValues() default {};

	String[] parameters() default {};

}