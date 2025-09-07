package org.schema.game.client.controller;

import org.schema.game.common.data.world.SimpleTransformableSendableObject;

public interface EntitySelectionChangeChangeListener {
	public void onEntityChanged(SimpleTransformableSendableObject<?> old, SimpleTransformableSendableObject<?> selected);
}
