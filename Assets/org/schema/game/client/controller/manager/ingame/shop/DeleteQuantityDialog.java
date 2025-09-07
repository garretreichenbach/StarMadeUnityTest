package org.schema.game.client.controller.manager.ingame.shop;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.InputChecker;
import org.schema.schine.common.OnInputChangedCallback;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.sound.controller.AudioController;

public class DeleteQuantityDialog extends QuantityDialog {

	private int slot;

	public DeleteQuantityDialog(final GameClientState state, int slot, String title, final short element, int lastQuantity) {
		super(state, element, title, new DeleteDesc(ElementKeyMap.getInfo(element)), lastQuantity);
		this.slot = slot;
		((DeleteDesc) DeleteQuantityDialog.this.descriptionObject).wantedQuantity = -lastQuantity;
		setInputChecker((entry, callback) -> {
			try {
				if (entry.length() > 0) {
					int quantity = Integer.parseInt(entry);
					int playerOverallQuantity = getState().getPlayer().getInventory(null).getOverallQuantity(element);
					if (quantity <= 0) {
						getState().getController().popupAlertTextMessage(Lng.str("Invalid quantity!"), 0);
						return false;
					}
					ElementInformation info = ElementKeyMap.getInfo(element);
					if (quantity <= playerOverallQuantity) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.SHOP, AudioTags.SELL)*/
						AudioController.fireAudioEventID(187);
						return true;
					} else {
						getState().getController().popupAlertTextMessage(Lng.str("ERROR\nYou can't delete that many!\nYou can only delete %s...", playerOverallQuantity), 0);
						getTextInput().clear();
						getTextInput().append(String.valueOf(playerOverallQuantity));
						getTextInput().selectAll();
						getTextInput().update();
					}
					return false;
				}
			} catch (NumberFormatException e) {
			}
			callback.onFailedTextCheck(Lng.str("Please only enter numbers!"));
			return false;
		});
		getTextInput().setOnInputChangedCallback(t -> {
			try {
				((SellDesc) DeleteQuantityDialog.this.descriptionObject).wantedQuantity = -Integer.parseInt(t);
			} catch (Exception e) {
			}
			return t;
		});
	}

	@Override
	public boolean onInput(String entry) {
		// System.err.println("Written "+entry);
		// ElementInformation elementInformation = ElementKeyMap.informationMap.get(element);
		int quantity = Integer.parseInt(entry);
		// quantity already validated
		getState().getPlayer().getInventoryController().delete(element, quantity, slot);
		return true;
	}
}
