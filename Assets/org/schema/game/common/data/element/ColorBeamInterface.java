package org.schema.game.common.data.element;

import javax.vecmath.Vector4f;

public interface ColorBeamInterface {
	public Vector4f getColor();

	public Vector4f getDefaultColor();

	public boolean hasCustomColor();
}
