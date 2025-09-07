package org.schema.game.client.controller;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.inventory.GUIShipyardInfoBlocksFillScrollableList;
import org.schema.game.common.controller.elements.shipyard.ShipyardCollectionManager;
import org.schema.game.common.controller.elements.shipyard.ShipyardCollectionManager.ShipyardRequestType;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIDialogWindow;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;

public class PlayerShipyardInfoDialog extends PlayerGameOkCancelInput {
	private long lastUpdate;

	public PlayerShipyardInfoDialog(final GameClientState state, final ShipyardCollectionManager it) {
		super("PlayerShipyardInfo", state, 600, 400, Lng.str("Info"), "");
		getInputPanel().onInit();

		((GUIDialogWindow) getInputPanel().background).getMainContentPane().setTextBoxHeightLast(UIScale.getUIScale().scale(25));
		((GUIDialogWindow) getInputPanel().background).getMainContentPane().addNewTextBox(UIScale.getUIScale().scale(40));


		GUIShipyardInfoBlocksFillScrollableList l = new GUIShipyardInfoBlocksFillScrollableList(getState(),
				((GUIDialogWindow) getInputPanel().background).getMainContentPane().getContent(1), this, it){

					
					@Override
					public void draw() {
						if(System.currentTimeMillis() - lastUpdate > 2000){
							synchronized(getState()){
								it.sendStateRequestToServer(ShipyardRequestType.INFO);
							}
							lastUpdate = System.currentTimeMillis();
						}
						super.draw();
					}
			
		};
		l.onInit();

		((GUIDialogWindow) getInputPanel().background).getMainContentPane().getContent(1).attach(l);

		getInputPanel().setCancelButton(false);
		getInputPanel().setOkButtonText(Lng.str("DONE"));
		
		it.sendStateRequestToServer(ShipyardRequestType.INFO);
		lastUpdate = System.currentTimeMillis();
	}


	@Override
	public boolean allowChat() {
		return true;
	}	@Override
	public boolean isOccluded() {
		return false;
	}

	@Override
	public void onDeactivate() {

	}

	@Override
	public void pressedOK() {
		deactivate();
	}




}
