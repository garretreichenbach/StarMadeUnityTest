package org.schema.game.common.data.world;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public interface GravityStateInterface {
	public ObjectArrayList<SimpleTransformableSendableObject> getCurrentGravitySources();
}
