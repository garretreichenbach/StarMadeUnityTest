package org.schema.game.client.controller.manager.ingame.shop;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.ShopInterface;
import org.schema.game.common.controller.ShoppingAddOn;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.common.InputChecker;
import org.schema.schine.common.OnInputChangedCallback;
import org.schema.schine.common.TextCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.sound.controller.AudioController;

public class SellQuantityDialog extends QuantityDialog {

	public SellQuantityDialog(final GameClientState state, String title, final short element, int lastQuantity, ShopInterface shop) {
		super(state, element, title, new SellDesc(ElementKeyMap.getInfo(element), shop, title.contains("Put")), lastQuantity);
		((SellDesc) SellQuantityDialog.this.descriptionObject).wantedQuantity = -lastQuantity;
		setInputChecker((entry, callback) -> {
			try {
				if (entry.length() > 0) {
					int wantedToSell = Integer.parseInt(entry);
					int playerOverallQuantity = getState().getPlayer().getInventory(null).getOverallQuantity(element);
					ShopInterface currentClosestShop = getState().getCurrentClosestShop();
					if (currentClosestShop == null) {
						getState().getController().popupAlertTextMessage(Lng.str("No shop available!"), 0);
						return false;
					}
					if (wantedToSell <= 0) {
						getState().getController().popupAlertTextMessage(Lng.str("Invalid quantity!"), 0);
						return false;
					}
					if (!currentClosestShop.getShopInventory().canPutIn(element, wantedToSell)) {
						getState().getController().popupAlertTextMessage(Lng.str("Shop cannot stock any\nmore of that item!"), 0);
						getTextInput().clear();
						int howMuch = currentClosestShop.getShopInventory().canPutInHowMuch(element, wantedToSell, -1);
						getTextInput().append(String.valueOf(howMuch));
						getTextInput().selectAll();
						return false;
					}
					ElementInformation info = ElementKeyMap.getInfo(element);
					int canAfford = currentClosestShop.getShoppingAddOn().canShopAfford(element, wantedToSell);
					if (ShoppingAddOn.isSelfOwnedShop(state, currentClosestShop)) {
						// owner gets all
						canAfford = wantedToSell;
					}
					if (canAfford < wantedToSell) {
						getState().getController().popupAlertTextMessage(Lng.str("ERROR\nLimited shop demand!\nYou can only sell %s...", canAfford), 0);
						getTextInput().clear();
						getTextInput().append(String.valueOf(canAfford));
						getTextInput().selectAll();
						getTextInput().update();
					} else {
						if (wantedToSell <= playerOverallQuantity) {
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.SHOP, AudioTags.SELL)*/
							AudioController.fireAudioEventID(189);
							return true;
						} else {
							getState().getController().popupAlertTextMessage(Lng.str("ERROR\nYou don't have that many!\nYou can only sell %s...", playerOverallQuantity), 0);
							getTextInput().clear();
							getTextInput().append(String.valueOf(playerOverallQuantity));
							getTextInput().selectAll();
							getTextInput().update();
						}
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
				((SellDesc) SellQuantityDialog.this.descriptionObject).wantedQuantity = -Integer.parseInt(t);
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
		getState().getPlayer().getInventoryController().sell(element, quantity);
		return true;
	}
}
