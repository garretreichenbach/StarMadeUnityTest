package org.schema.game.common.controller.rules.rules.actions;

public interface RecurringAction<E> {

	public long getLastActivate();
	public void setLastActivate(long time);
	public long getCheckInterval();
	public void onActive(E e);
}
