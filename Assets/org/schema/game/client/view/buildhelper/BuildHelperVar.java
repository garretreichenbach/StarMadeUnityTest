package org.schema.game.client.view.buildhelper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BuildHelperVar {

	String type();

	BuildHelperVarName name();

	int max();

	int min();

}
