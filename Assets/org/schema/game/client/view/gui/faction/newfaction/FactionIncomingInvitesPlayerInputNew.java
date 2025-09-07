package org.schema.game.client.view.gui.faction.newfaction;

import org.schema.game.client.controller.PlayerGameOkCancelInput;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.common.language.Lng;

public class FactionIncomingInvitesPlayerInputNew extends PlayerGameOkCancelInput {

	public FactionIncomingInvitesPlayerInputNew(GameClientState state) {
		super("FactionIncomingInvitesPlayerInputNew", state, Lng.str("Received Invitations"), "");

		getInputPanel().onInit();
		FactionIncomingInvitationsPanelNew factionIncomingInvitationsPanelNew = new FactionIncomingInvitationsPanelNew(getInputPanel().getContent(), state);
		factionIncomingInvitationsPanelNew.onInit();
		getInputPanel().getContent().attach(factionIncomingInvitationsPanelNew);
	}


	@Override
	public void onDeactivate() {

	}

	@Override
	public void pressedOK() {
		deactivate();
	}

}
