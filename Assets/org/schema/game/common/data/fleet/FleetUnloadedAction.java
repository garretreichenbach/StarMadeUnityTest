package org.schema.game.common.data.fleet;

import org.schema.schine.graphicsengine.core.Timer;

public abstract class FleetUnloadedAction {
	protected final FleetMember member;
	protected Fleet fleet;
	protected long creationTime;
	public FleetUnloadedAction(FleetMember member, Fleet fleet) {
		super();
		this.member = member;
		this.fleet = fleet;
		this.creationTime = System.currentTimeMillis();
	}
	
	/**
	 * 
	 * @return true if goal is reached 
	 */
	public abstract boolean execute(Timer time);
	
	protected void reset(Timer time) {
		this.reset(time.currentTime);
	}
	protected void reset(long currentTime) {
		this.creationTime = currentTime;
	}
}
