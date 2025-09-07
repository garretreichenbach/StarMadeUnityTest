package org.schema.schine.graphicsengine.core.settings.typegetter;

public class LongGetter extends TypeGetter<Long> {
	@Override
	public Long parseType(String s) throws NumberFormatException {
		return Long.parseLong(s);
	}
}
