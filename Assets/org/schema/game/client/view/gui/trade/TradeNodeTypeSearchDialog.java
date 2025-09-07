package org.schema.game.client.view.gui.trade;

import org.schema.game.client.controller.PlayerInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.ShopInterface;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.sound.controller.AudioController;

public class TradeNodeTypeSearchDialog extends PlayerInput implements GUICallback {

	private final GUITradeNodeSearchInputPanel orderDialog;

	public TradeNodeTypeSearchDialog(GameClientState state, ShopInterface currentClosestShop, ElementInformation info) throws ShopNotFoundException {
		super(state);
		this.orderDialog = new GUITradeNodeSearchInputPanel(state, this, info, currentClosestShop, this);
		this.orderDialog.setOkButton(false);
		this.orderDialog.setCancelButtonText(Lng.str("CLOSE"));
		this.orderDialog.onInit();
	}

	@Override
	public GUIElement getInputPanel() {
		return orderDialog;
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (!isOccluded()) {
			if (event.pressedLeftMouse()) {
				if (callingGuiElement.getUserPointer().equals("OK")) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(716);
					pressedOK();
				}
				if (callingGuiElement.getUserPointer().equals("CANCEL") || callingGuiElement.getUserPointer().equals("X")) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
					AudioController.fireAudioEventID(717);
					cancel();
				}
			}
		}
	}

	private void pressedOK() {
		deactivate();
	}

	@Override
	public void onDeactivate() {
	}
}
