package org.schema.game.common.controller.elements;

import org.schema.schine.graphicsengine.core.Timer;

public interface ManagerUpdatableInterface {
	public void update(Timer timer);
	public int updatePrio();
	public boolean canUpdate();
	public void onNoUpdate(Timer timer);
}
