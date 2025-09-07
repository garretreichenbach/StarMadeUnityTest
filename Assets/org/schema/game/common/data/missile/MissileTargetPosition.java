package org.schema.game.common.data.missile;

import javax.vecmath.Vector3f;

public class MissileTargetPosition {
	public Vector3f targetPosition;
	public long time;
	public long executedTime;
	public boolean isExecuted(long lastReferenceTime) {
		long timeToExecute = time - lastReferenceTime;
		return executedTime >= timeToExecute;
	}
	
	
}
