package org.schema.game.client.controller.manager.ingame.shop;

import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.ShopInterface;
import org.schema.game.common.data.element.ElementInformation;

public class SellDesc {
	public int wantedQuantity;
	private ElementInformation info;
	private ShopInterface shop;
	private boolean shopOwner;

	public SellDesc(ElementInformation info, ShopInterface shop, boolean shopOwner) {
		this.info = info;
		this.shop = shop;
		this.shopOwner = shopOwner;
	}

	@Override
	public String toString() {
		return "How many " + info.getName() + (shopOwner ? " " : " (base price: " + info.getPrice(((GameStateInterface) shop.getState()).getMaterialPrice()) + "c) ") + " do you want to " + (shopOwner ? "put in" : "sell") + "?\n" +
				(shopOwner ? "" : "If you enter too many, the maximal amount you can sell\n" +
						"will be automatically displayed.\n" +
						"Current Selling Value: " + 
						shop.getShoppingAddOn().toStringForPurchase(info.getId(), wantedQuantity) + 
						" (base " + -wantedQuantity * info.getPrice(((GameStateInterface) shop.getState()).getMaterialPrice()) + "c)");
	}
}