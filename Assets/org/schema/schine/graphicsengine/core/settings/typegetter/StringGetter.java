package org.schema.schine.graphicsengine.core.settings.typegetter;

public class StringGetter extends TypeGetter<String> {
	@Override
	public String parseType(String s) throws NumberFormatException {
		return s;
	}
}
