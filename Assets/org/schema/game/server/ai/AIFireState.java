package org.schema.game.server.ai;

public abstract class AIFireState {
	public float secondsToExecute;
	public long timeStarted;

	public final boolean isTimedOut(long updateTime){
		return (float)(((double)(updateTime - timeStarted))/1000d) > secondsToExecute;
	}

	/**
	 * @return if the fire state requires a mouse-button release at this time
	 */
	public abstract boolean needsShotReleased();

	/**
	 * @return whether the firing action can be ended after timeout
	 */
	protected boolean canFinish(){
		return true;
	}

	/**
	 * @return if the firing action is completed
	 */
	public final boolean isExecuted(long updateTime) {
		return isTimedOut(updateTime) && canFinish();
	}
}
