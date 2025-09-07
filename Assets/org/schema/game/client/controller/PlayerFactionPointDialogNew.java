package org.schema.game.client.controller;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.faction.newfaction.FactionPointRevenueScrollableListNew;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;

public class PlayerFactionPointDialogNew extends PlayerGameOkCancelInput {

	public PlayerFactionPointDialogNew(GameClientState state) {
		super("PlayerFactionPointDialogNew", state, 640, 480, Lng.str("Faction Point Statistics"), "");
		getInputPanel().setOkButton(false);
		getInputPanel().setCancelButtonText(Lng.str("CLOSE"));
		getInputPanel().onInit();

		FactionPointRevenueScrollableListNew n = new FactionPointRevenueScrollableListNew(state, ((GUIDialogWindow) getInputPanel().getBackground()).getMainContentPane().getContent(0), state.getFaction());
		n.onInit();
		((GUIDialogWindow) getInputPanel().getBackground()).getMainContentPane().getContent(0).attach(n);
	}

	@Override
	public void onDeactivate() {
		
	}

	@Override
	public void pressedOK() {

	}

}
