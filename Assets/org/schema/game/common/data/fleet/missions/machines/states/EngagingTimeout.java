package org.schema.game.common.data.fleet.missions.machines.states;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.server.ai.program.common.states.SegmentControllerGameState;

public abstract class EngagingTimeout extends Timeout{

	public final Vector3i attackedSector;
	public final float dist;
	

	public EngagingTimeout(Vector3i attackedSector, float dist) {
		this.attackedSector = new Vector3i(attackedSector);
		this.dist = dist;
	}

	@Override
	public abstract void onTimeout(SegmentControllerGameState<?> currentState);
	@Override
	public abstract void onShot(
			SegmentControllerGameState<?> currentState) ;

	
	
	@Override
	public boolean checkTimeout(Vector3i sector){
		if(sector != null){
			boolean timed = Vector3i.getDisatance(sector, attackedSector) > dist;
			
			return timed;
		}
		return false;
	}

}
