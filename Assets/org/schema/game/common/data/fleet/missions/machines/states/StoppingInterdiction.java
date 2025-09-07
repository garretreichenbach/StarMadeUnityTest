package org.schema.game.common.data.fleet.missions.machines.states;

import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.jumpprohibiter.InterdictionAddOn;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.server.ai.program.fleetcontrollable.FleetControllableProgram;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

/**
 * [Description]
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class StoppingInterdiction extends FleetState {

	public StoppingInterdiction(Fleet fleet) {
		super(fleet);
	}

	/**
	 * On update.
	 *
	 * @return true, if successful
	 * @throws FSMException
	 */
	@Override
	public boolean onUpdate() throws FSMException {
		FleetMember flagShip = getEntityState().getFlagShip();
		if(flagShip == null) stateTransition(Transition.FLEET_EMPTY);
		else {
			for(int i = 0; i < getEntityState().getMembers().size(); i ++) {
				FleetMember fleetMember = getEntityState().getMembers().get(i);
				if(fleetMember.isLoaded()) {
					Ship s = (Ship) fleetMember.getLoaded();
					if(s.getAiConfiguration().getAiEntityState().getCurrentProgram() == null || !(s.getAiConfiguration().getAiEntityState().getCurrentProgram() instanceof FleetControllableProgram)) continue;
					if(!s.isCoreOverheating()) {
						PlayerUsableInterface playerUsable = s.getManagerContainer().getPlayerUsable(PlayerUsableInterface.USABLE_ID_INTERDICTION);
						if(playerUsable instanceof InterdictionAddOn) ((InterdictionAddOn) playerUsable).dischargeFully();
					}
				}
			}
			stateTransition(Transition.FLEET_ACTION_DONE);
		}
		return false;
	}

	@Override
	public FleetStateType getType() {
		return FleetStateType.STOPPING_INTERDICTION;
	}

	/**
	 * On exit.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean onExit() {
		return false;
	}
}
