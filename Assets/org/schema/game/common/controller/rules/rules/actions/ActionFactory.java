package org.schema.game.common.controller.rules.rules.actions;

import org.schema.schine.network.TopLevelType;

public interface ActionFactory<A, D extends Action<A>> {
	public D instantiateAction();
	public TopLevelType getType();
	
}
