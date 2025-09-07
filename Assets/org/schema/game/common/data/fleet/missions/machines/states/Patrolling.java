package org.schema.game.common.data.fleet.missions.machines.states;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

public class Patrolling extends Attacking {

	private static final long SEARCH_FOR_TARGET_DELAY = 1000;

	public Patrolling(Fleet fleet) {
		super(fleet);
	}

	@Override
	public boolean onEnterFleetState() {
		try {
			restartAllLoaded();
		} catch(FSMException exception) {
			exception.printStackTrace();
		}
		return super.onEnterFleetState();
	}

	@Override
	public Transition getMoveTrasition(){
		return Transition.FLEET_PATROL;
	}

	@Override
	public boolean onUpdate() throws FSMException {
		boolean inCombat = false;
		for(FleetMember member : getEntityState().getMembers()) {
			if(member.getLoaded() instanceof Ship) {
				Ship ship = (Ship) member.getLoaded();
				if(isInAttackCycle(ship) || System.currentTimeMillis() - ship.getAiConfiguration().getAiEntityState().lastAttackStateSetByFleet > SEARCH_FOR_TARGET_DELAY) {
					inCombat = true;
					ship.getAiConfiguration().getAiEntityState().lastAttackStateSetByFleet = System.currentTimeMillis();
					try {
						ship.getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().stateTransition(Transition.SEARCH_FOR_TARGET);
					} catch(FSMException ignored) {}
					break;
				}
			}
		}
		if(!inCombat) {
			if(getEntityState().getCurrentMoveTarget() == null) getEntityState().setCurrentMoveTarget(getEntityState().getCurrentPatrolTarget());
			else if(getEntityState().getCurrentMoveTarget().equals(getCurrentSector())) getEntityState().setCurrentMoveTarget(getEntityState().goToNextPatrolTarget());
			return super.onUpdate();
		} else return true;
	}

	@Override
	public FleetStateType getType() {
		return FleetStateType.PATROLLING;
	}

	@Override
	public boolean isMovingAttacking() {
		return true;
	}

	@Override
	public boolean isMovingSentry() {
		return true;
	}

	private Vector3i getCurrentSector() {
		try {
			return Vector3i.parseVector3i(getEntityState().getFlagShipSector());
		} catch(NumberFormatException exception) {
			return new Vector3i();
		}
	}
}
