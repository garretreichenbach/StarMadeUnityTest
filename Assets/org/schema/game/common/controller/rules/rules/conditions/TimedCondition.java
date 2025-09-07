package org.schema.game.common.controller.rules.rules.conditions;

public interface TimedCondition {
	public boolean isTimeToFire(long time);

	public void flagTriggeredTimedCondition();

	public boolean isTriggeredTimedCondition();
	public boolean isRemoveOnTriggered();

	public boolean isTriggeredTimedEndCondition();

	public void flagTriggeredTimedEndCondition();
}
