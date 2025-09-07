package org.schema.schine.graphicsengine.core.settings.typegetter;

public class ShortGetter extends TypeGetter<Integer> {
	@Override
	public Integer parseType(String s) throws NumberFormatException {
		return Integer.parseInt(s);
	}
}
