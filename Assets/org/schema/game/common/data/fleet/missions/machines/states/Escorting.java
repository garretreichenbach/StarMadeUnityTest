package org.schema.game.common.data.fleet.missions.machines.states;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.ai.program.fleetcontrollable.FleetControllableProgram;
import org.schema.game.server.ai.program.fleetcontrollable.states.FleetMovingToSector;
import org.schema.game.server.ai.program.fleetcontrollable.states.FleetSeachingForTarget;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

/**
 * Escorting fleet state.
 * <p>When assigned to this mode, all fleet ships will escort the flagship, attacking enemies in the vicinity,
 * but also disengaging when needed in order to stay near the flagship.</p>
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class Escorting extends Defending {

	public Escorting(Fleet fleet) {
		super(fleet);
	}

	@Override
	public boolean onUpdate() throws FSMException {
		FleetMember flagShip = getEntityState().getFlagShip();
		if(flagShip != null) {
			for(FleetMember member : getEntityState().getMembers()) {
				if(!member.equals(getEntityState().getFlagShip())) {
					if(!member.getSector().equals(getEntityState().getFlagShip().getSector())) {
						//It's probably best to deal with each fleet member on an individual basis rather than setting the target for the entire fleet as then the Flagship would attempt to move as well.
						if(member.isLoaded()) {
							try {
								((TargetProgram<?>) ((Ship) member.getLoaded()).getAiConfiguration().getAiEntityState().getCurrentProgram()).setSectorTarget(new Vector3i(getEntityState().getFlagShip().getSector()));
								member.moveTarget = getEntityState().getFlagShip().getSector();
								if(!(((Ship) member.getLoaded()).getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().getCurrentState() instanceof FleetMovingToSector)) ((Ship) member.getLoaded()).getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().stateTransition(Transition.MOVE_TO_SECTOR);
							} catch(FSMException ignored) {}
						} else member.moveRequestUnloaded(getEntityState(), getEntityState().getFlagShip().getSector());
					} else {
						Ship s = (Ship) member.getLoaded();
						if(s.getAiConfiguration().getAiEntityState().getCurrentProgram() == null || !(s.getAiConfiguration().getAiEntityState().getCurrentProgram() instanceof FleetControllableProgram)) continue;
						if(!s.isCoreOverheating()) {
							try {
								s.getAiConfiguration().getAiEntityState().lastAttackStateSetByFleet = System.currentTimeMillis();
								((Ship) member.getLoaded()).getAiConfiguration().getAiEntityState().stop();
								if(!(((Ship) member.getLoaded()).getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().getCurrentState() instanceof FleetSeachingForTarget)) ((Ship) member.getLoaded()).getAiConfiguration().getAiEntityState().getCurrentProgram().getMachine().getFsm().stateTransition(Transition.SEARCH_FOR_TARGET);
							} catch(FSMException ignored) {} //Note, it might not be a good idea to always ignore this exception, but I'm not sure what to do with it otherwise.
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public FleetStateType getType() {
		return FleetStateType.ESCORTING;
	}

	@Override
	public boolean isMovingAttacking() {
		return true;
	}

	@Override
	public Transition getMoveTrasition(){
		return Transition.FLEET_ESCORT;
	}
}
