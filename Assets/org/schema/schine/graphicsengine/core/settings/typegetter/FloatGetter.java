package org.schema.schine.graphicsengine.core.settings.typegetter;

public class FloatGetter extends TypeGetter<Float> {
	@Override
	public Float parseType(String s) throws NumberFormatException {
		return Float.parseFloat(s);
	}
}
