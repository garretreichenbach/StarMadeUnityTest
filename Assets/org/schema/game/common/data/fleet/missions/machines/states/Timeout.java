package org.schema.game.common.data.fleet.missions.machines.states;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.server.ai.program.common.states.SegmentControllerGameState;

public abstract class Timeout{

	public boolean wasTimedOut;
	public Timeout() {
	}

	public abstract void onTimeout(SegmentControllerGameState<?> currentState);
	public abstract void onShot(
			SegmentControllerGameState<?> currentState) ;

	public boolean isTimeout(Vector3i sector) {
		boolean timed = checkTimeout(sector);
		if(timed){
			wasTimedOut = timed;
		}
		return timed;
	}
	public abstract boolean checkTimeout(Vector3i sector);

	public abstract void onNoTargetFound(SegmentControllerGameState<?> currentState);

}
