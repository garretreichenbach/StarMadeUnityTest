package org.schema.game.client.controller.manager.ingame.shop;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.inventory.FreeItem;
import org.schema.schine.common.InputChecker;
import org.schema.schine.common.OnInputChangedCallback;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.sound.controller.AudioController;

public class DropCreditsDialog extends QuantityDialog {

	public DropCreditsDialog(GameClientState state, int lastQuantity) {
		super(state, FreeItem.CREDITS_TYPE, Lng.str("Drop Credits"), Lng.str("How many credits do you want to drop"), lastQuantity, 10);
		setInputChecker((entry, callback) -> {
			try {
				if (entry.length() > 0) {
					int quantity = Integer.parseInt(entry);
					if (quantity <= 0) {
						getState().getController().popupAlertTextMessage(Lng.str("ERROR: Invalid quantity!"), 0);
						return false;
					}
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.SHOP, AudioTags.DROP_CREDITS)*/
					AudioController.fireAudioEventID(188);
					return true;
				}
			} catch (NumberFormatException e) {
			}
			callback.onFailedTextCheck(Lng.str("Please only enter numbers!"));
			return false;
		});
		getTextInput().setOnInputChangedCallback(t -> {
			try {
				((BuyDesc) DropCreditsDialog.this.descriptionObject).wantedQuantity = Integer.parseInt(t);
			} catch (Exception e) {
			}
			return t;
		});
	}

	@Override
	public boolean onInput(String entry) {
		int quantity = Integer.parseInt(entry);
		getState().getPlayer().getNetworkObject().creditsDropBuffer.add(quantity);
		return true;
	}
}
