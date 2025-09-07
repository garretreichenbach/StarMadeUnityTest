package org.schema.game.client.controller.manager.ingame.shop;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.ShopInterface;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.schine.common.InputChecker;
import org.schema.schine.common.OnInputChangedCallback;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.sound.controller.AudioController;

public class BuyQuantityDialog extends QuantityDialog {

	public BuyQuantityDialog(GameClientState state, String title, final short element, int lastQuantity, ShopInterface shop) {
		super(state, element, title, new BuyDesc(ElementKeyMap.getInfo(element), shop, title.contains("Take")), lastQuantity);
		((BuyDesc) BuyQuantityDialog.this.descriptionObject).wantedQuantity = lastQuantity;
		setInputChecker((entry, callback) -> {
			try {
				if (entry.length() > 0) {
					ElementInformation elementInformation = ElementKeyMap.getInfo(element);
					int quantity = Integer.parseInt(entry);
					if (quantity <= 0) {
						getState().getController().popupAlertTextMessage(Lng.str("Invalid quantity!"), 0);
						return false;
					}
					if (getState().getPlayer().getInventory().getFirstSlot(element, false) == Inventory.FULL_RET) {
						getState().getController().popupAlertTextMessage(Lng.str("Inventory is full!"), 0);
						return false;
					}
					ShopInterface currentClosestShop = getState().getCurrentClosestShop();
					if (currentClosestShop == null) {
						getState().getController().popupAlertTextMessage(Lng.str("No shop available!"), 0);
						return false;
					} else {
					// int stock = currentClosestShop.getShopInventory().getOverallQuantity(element);
					// if ((stock < quantity)
					// && !currentClosestShop.getShoppingAddOn().isInfiniteSupply()) {
					// getState().getController().popupAlertTextMessage(Lng.str("This item is out of stock!"), 0);
					// if (stock >= 0) {
					// getTextInput().clear();
					// getTextInput().append(String.valueOf(stock));
					// getTextInput().selectAll();
					// getTextInput().update();
					// }
					// return false;
					// }
					}
					int canAffordAmount = currentClosestShop.getShoppingAddOn().canAfford(getState().getPlayer(), element, quantity);
					if (canAffordAmount == quantity) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.SHOP, AudioTags.BUY)*/
						AudioController.fireAudioEventID(186);
						return true;
					} else {
						getState().getController().popupAlertTextMessage(Lng.str("ERROR\nThe shop has limited availability or you can't afford that many!\nYou can only buy %s...", canAffordAmount), 0);
						getTextInput().clear();
						getTextInput().append(String.valueOf(canAffordAmount));
						getTextInput().selectAll();
						getTextInput().update();
					}
					return false;
				}
			} catch (NumberFormatException e) {
			}
			callback.onFailedTextCheck("Please only enter numbers!");
			return false;
		});
		getTextInput().setOnInputChangedCallback(t -> {
			try {
				((BuyDesc) BuyQuantityDialog.this.descriptionObject).wantedQuantity = Integer.parseInt(t);
			} catch (Exception e) {
			}
			return t;
		});
	}

	@Override
	public boolean onInput(String entry) {
		int quantity = Integer.parseInt(entry);
		// credits already validated
		getState().getPlayer().getInventoryController().buy(element, quantity);
		return true;
	}
}
