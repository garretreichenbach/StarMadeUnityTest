package org.schema.game.client.controller.manager.ingame.shop;

import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.ShopInterface;
import org.schema.game.common.data.element.ElementInformation;

public class BuyDesc {
	public int wantedQuantity;
	private ElementInformation info;
	private ShopInterface shop;
	private boolean shopOwner;

	public BuyDesc(ElementInformation info, ShopInterface shop, boolean shopOwner) {
		this.info = info;
		this.shop = shop;

		this.shopOwner = shopOwner;
	}

	@Override
	public String toString() {
		return "How many " + info.getName() + " do you want to " + (shopOwner ? "take" : "buy") + "?\n" +
				(shopOwner ? "" :
						"If you enter too many, the maximal amount you can affort\n" +
								"will be displayed.\n" +
								"Current Buying Value: " + shop.getShoppingAddOn().toStringForPurchase(info.getId(), wantedQuantity) + " (base " + wantedQuantity * info.getPrice(((GameStateInterface) shop.getState()).getMaterialPrice()) + "c)");
	}
}