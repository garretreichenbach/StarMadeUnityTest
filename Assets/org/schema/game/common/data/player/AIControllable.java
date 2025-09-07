package org.schema.game.common.data.player;

import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.ai.AIControllerStateUnit;
import org.schema.schine.graphicsengine.core.Timer;

public interface AIControllable<E extends SimpleTransformableSendableObject> {
	public void handleControl(Timer timer, AIControllerStateUnit<E> unit);
}
