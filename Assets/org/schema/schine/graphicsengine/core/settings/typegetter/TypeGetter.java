package org.schema.schine.graphicsengine.core.settings.typegetter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class TypeGetter<E> {
	private static Map<Class<? extends Object>, TypeGetter<?>> cache = new HashMap<Class<? extends Object>, TypeGetter<?>>();

	public static TypeGetter<?> getTypeGetter(Class<? extends Object> clazz) throws TypeNotKnowException {
		if (cache.containsKey(clazz)) {
			return cache.get(clazz);
		}

		if (clazz.equals(Float.class)) {
			cache.put(clazz, new FloatGetter());
		}
		if (clazz.equals(String.class)) {
			cache.put(clazz, new StringGetter());
		}
		if (clazz.equals(Integer.class)) {
			cache.put(clazz, new IntegerGetter());
		}
		if (clazz.equals(Short.class)) {
			cache.put(clazz, new ShortGetter());
		}
        if (clazz.equals(Boolean.class)) {
            cache.put(clazz, new BooleanGetter());
        }
        if (clazz.equals(Long.class)) {
            cache.put(clazz, new LongGetter());
        }
		if (cache.containsKey(clazz)) {
			return cache.get(clazz);
		}

		throw new TypeNotKnowException(clazz.toString());
	}

	public abstract E parseType(String s) throws NumberFormatException;

	@Override
	public String toString() {
		return Arrays.toString(this.getClass().getTypeParameters());
	}
}
