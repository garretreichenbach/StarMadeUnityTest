package org.schema.game.server.data.simulation.groups;

import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;

public class RavegingSimulationGroup extends ShipSimulationGroup {

	

	public RavegingSimulationGroup(GameServerState state) {
		super(state);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.server.data.simulation.SimulationGroup#getType()
	 */
	@Override
	public GroupType getType() {
		return GroupType.RAVEGING;
	}

	@Override
	public void returnHomeMessage(PlayerState s) {
		s.sendServerMessage(new ServerMessage(
				Lng.astr("#### Transmission Start\nTarget has fled...\nReturn to base...\n#### Transmission End\n"), ServerMessage.MESSAGE_TYPE_WARNING, s.getId()));
	}

	/* (non-Javadoc)
	 * @see org.schema.game.server.data.simulation.SimulationGroup#sendInvestigationMessage(org.schema.game.common.data.player.PlayerState)
	 */
	@Override
	public void sendInvestigationMessage(PlayerState s) {
		s.sendServerMessage(new ServerMessage(
				Lng.astr("#### Transmission Start\nBase %s:\nHostile signs in %s...\nSending raiding party...\n#### Transmission End\n",  getStartSector(), s.getCurrentSector()), ServerMessage.MESSAGE_TYPE_WARNING, s.getId()));
	}

}
