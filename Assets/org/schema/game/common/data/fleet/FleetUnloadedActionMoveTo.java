package org.schema.game.common.data.fleet;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.graphicsengine.core.Timer;

public class FleetUnloadedActionMoveTo extends FleetUnloadedAction{

	
	
	public FleetUnloadedActionMoveTo(FleetMember member, Fleet fleet, Vector3i goal) {
		super(member, fleet);
		this.target = goal;
	}

	private final Vector3i target;

	@Override
	public boolean execute(Timer time) {
		if(time.currentTime - creationTime < getTimeTakenPerSector()){
			return false;
		}
		
		member.moveUnloadedTowardsGoal(fleet, target);
		reset(time);
		
		return true;
	}

	

	private long getTimeTakenPerSector() {
		return (ServerConfig.FLEET_OUT_OF_SECTOR_MOVEMENT.getInt());
	}


	public void setTarget(Vector3i to) {
		if(target != null && !target.equals(to)){
			target.set(to);
		}
		else{
			target.set(new Vector3i(to));
		}
	}
}
