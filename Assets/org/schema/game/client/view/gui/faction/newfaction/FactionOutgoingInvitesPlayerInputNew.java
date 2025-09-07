package org.schema.game.client.view.gui.faction.newfaction;

import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.common.language.Lng;

public class FactionOutgoingInvitesPlayerInputNew extends PlayerGameOkCancelInput {

	public FactionOutgoingInvitesPlayerInputNew(GameClientState state) {
		super("FactionOutgoingInvitesPlayerInputNew", state, Lng.str("Sent Pending Invitations"), "");

		getInputPanel().onInit();
		FactionOutgoingInvitationsPanelNew factionOutgoingInvitationsPanelNew = new FactionOutgoingInvitationsPanelNew(getInputPanel().getContent(), state);
		factionOutgoingInvitationsPanelNew.onInit();
		getInputPanel().getContent().attach(factionOutgoingInvitationsPanelNew);
	}


	@Override
	public void onDeactivate() {

	}

	@Override
	public void pressedOK() {
		deactivate();
	}

}
