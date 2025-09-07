package org.schema.game.common.controller.rules.rules.conditions;

import org.schema.schine.network.TopLevelType;

public interface ConditionFactory<A, D extends Condition<A>> {
	public D instantiateCondition();
	public TopLevelType getType();
}
