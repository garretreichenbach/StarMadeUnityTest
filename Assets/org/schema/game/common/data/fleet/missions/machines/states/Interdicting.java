package org.schema.game.common.data.fleet.missions.machines.states;

import org.schema.game.common.controller.PlayerUsableInterface;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.jumpprohibiter.InterdictionAddOn;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.server.ai.program.fleetcontrollable.FleetControllableProgram;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;

/**
 * [Description]
 *
 * @author TheDerpGamer (TheDerpGamer#0027)
 */
public class Interdicting extends FleetState {

	public Interdicting(Fleet fleet) {
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
			for(int i = 0; i < getEntityState().getMembers().size(); i++) {
				FleetMember fleetMember = getEntityState().getMembers().get(i);
				if(fleetMember.isLoaded()) {
					Ship s = (Ship) fleetMember.getLoaded();
					if(s.getAiConfiguration().getAiEntityState().getCurrentProgram() == null || !(s.getAiConfiguration().getAiEntityState().getCurrentProgram() instanceof FleetControllableProgram)) continue;
					if(!s.isCoreOverheating()) {
						if(s.isUsingPowerReactors()) {
							PlayerUsableInterface playerUsable = s.getManagerContainer().getPlayerUsable(PlayerUsableInterface.USABLE_ID_INTERDICTION);
							if(playerUsable instanceof InterdictionAddOn) {
								boolean executed = false;
								if(((InterdictionAddOn) playerUsable).canExecute()) executed = ((InterdictionAddOn) playerUsable).executeModule();
								if(!executed) getEntityState().sendOwnerMessageServer(new ServerMessage(Lng.astr("%s cannot interdict (not charged)", s.getName()), ServerMessage.MESSAGE_TYPE_ERROR));
							} else getEntityState().sendOwnerMessageServer(new ServerMessage(Lng.astr("%s cannot interdict (no interdiction capability)", s.getName()), ServerMessage.MESSAGE_TYPE_ERROR));
						}
					}
				}
			}
			stateTransition(Transition.FLEET_ACTION_DONE);
		}
		return false;
	}

	@Override
	public FleetStateType getType() {
		return FleetStateType.INTERDICTING;
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
