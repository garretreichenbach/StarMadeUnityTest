package org.schema.game.common.controller.rules.rules.actions;

import org.schema.schine.graphicsengine.core.Timer;

public interface ActionUpdate {
	public void update(Timer timer);
	public boolean onClient();
	public boolean onServer();
	public Action<?> getAction();
	public void onAdd();
	public void onRemove();
}
