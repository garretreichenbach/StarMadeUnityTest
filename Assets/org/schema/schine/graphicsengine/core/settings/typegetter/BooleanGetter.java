package org.schema.schine.graphicsengine.core.settings.typegetter;

public class BooleanGetter extends TypeGetter<Boolean> {
	@Override
	public Boolean parseType(String s) throws NumberFormatException {
		return Boolean.parseBoolean(s);
	}
}
