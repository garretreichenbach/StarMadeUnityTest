package org.schema.game.common.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class FieldUtils{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
		List<Field> asList = Arrays.asList(type.getDeclaredFields());
	    fields.addAll(asList);

	    if (type.getSuperclass() != null) {
	       FieldUtils.getAllFields(fields, type.getSuperclass());
	    }

	    return fields;
	}
}
