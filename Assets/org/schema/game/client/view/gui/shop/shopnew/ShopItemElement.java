package org.schema.game.client.view.gui.shop.shopnew;

import java.util.ArrayList;

import javax.vecmath.Vector4f;

import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.ShopInterface;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.network.objects.TradePriceInterface;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredUnderlayRectangle;
import org.schema.schine.graphicsengine.forms.gui.GUIListElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

public class ShopItemElement extends GUIAnchor {

	private final short buildIcon;
	private final ElementInformation info;
	private GUIListElement guiListElement;

	public ShopItemElement(InputState state, ElementInformation info) {
		super(state, UIScale.getUIScale().scale(500), UIScale.getUIScale().scale(44));
		this.buildIcon = (short) info.getBuildIconNum();
		this.setUserPointer(info.getId());
		this.info = info;
	}

	public GUIListElement getListElement() {
		if (guiListElement == null) {
			GUIColoredRectangle selected = new GUIColoredUnderlayRectangle(getState(), (int)getWidth(), (int)getHeight(), new Vector4f(1, 1, 1, 0.2f), this);
			guiListElement = new GUIListElement(this, selected, getState());
		}
		return guiListElement;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.GUIAncor#onInit()
	 */
	@Override
	public void onInit() {
		super.onInit();

		int layer = buildIcon / 256;

		
		
		
		final InventoryStatePriceCallback inventoryStatePriceCallback = 
				new InventoryStatePriceCallback(info);

		Sprite sprite = Controller.getResLoader().getSprite(UIScale.getUIScale().getGuiPath()+"build-icons-" + StringTools.formatTwoZero(layer) + "-16x16-gui-");

		GUIOverlay sp = new GUIOverlay(sprite, getState());

		GUITextOverlay textTitle = new GUITextOverlay(FontSize.SMALL_15, getState()) {

			/* (non-Javadoc)
			 * @see org.schema.schine.graphicsengine.forms.gui.GUITextOverlay#draw()
			 */
			@Override
			public void draw() {
//				if (inventoryStateStockCallback.inStock()) {
					setColor(0.5f, 0.8f, 1f, 1);
//				} else {
//					setColor(0.7f, 0.3f, 0.3f, 0.7f);
//				}
				super.draw();
			}

		};
		textTitle.setText(new ArrayList());
		textTitle.getText().add(info.getName());
		textTitle.getPos().x = UIScale.getUIScale().scale(32);
		textTitle.getPos().y = UIScale.getUIScale().inset;

		final GUITextOverlay textPrice = new GUITextOverlay(
				FontSize.SMALL_13, getState());
		textPrice.getPos().x = UIScale.getUIScale().inset;
		textPrice.getPos().y = UIScale.getUIScale().scale(28);
		

		int buildIconNum = buildIcon % 256;
		sp.setSpriteSubIndex(buildIconNum);
		sp.setScale(0.5f, 0.5f, 0.5f);

		textPrice.setTextSimple(inventoryStatePriceCallback);

		this.attach(textTitle);
		this.attach(sp);
		this.attach(textPrice);

		this.setMouseUpdateEnabled(true);
	}

	private class InventoryStateStockCallback {

		private ElementInformation info;
		private final boolean weWantToPurchase;

		public InventoryStateStockCallback(ElementInformation info, boolean weWantToPurchase) {
			this.info = info;
			this.weWantToPurchase = weWantToPurchase;
		}

		
		public int amount(){
			ShopInterface s = ((GameClientState) getState()).getCurrentClosestShop();
			if (s == null) {
				return -1;
			}
			int amount = s.getShopInventory().getOverallQuantity(info.getId());

			int buysOrSells = -1;
			
			//if we want to purchase, return the sell price of the shop
			TradePriceInterface price = s.getPrice(info.getId(), !this.weWantToPurchase);
			
			if(price != null){
				if(this.weWantToPurchase){ 
					//if we want to purchase, display stock subtracting the limit (if any limit)
					buysOrSells = price.getLimit() < 0 ? price.getAmount() :  price.getAmount() - price.getLimit();
					
				}else{
					//if we want to sell something to the shop, display the difference between shops stock and its limit
					
					//doesnt care about capacity. Else the number might be too high
					buysOrSells = price.getLimit() < 0 ? Integer.MAX_VALUE : price.getLimit() - price.getAmount();
				}
			}
			return buysOrSells;
		}
		@Override
		public String toString() {
			int amount = amount();
			if (amount <= 0) {
				return "err";
			}
			
			String str = getBStr(amount);
			
			return str;
		}

		private String getBStr(int val) {
			return val == Integer.MIN_VALUE ? Lng.str("all") : (val <= 0 ? Lng.str("none") : StringTools.massFormat(val));
		}
	}

	private class InventoryStatePriceCallback {

		private ElementInformation info;
		private InventoryStateStockCallback inventoryStateStockCallbackWePurchase;
		private InventoryStateStockCallback inventoryStateStockCallbackWeSell;

		private String getPriceString(ShopInterface currentClosestShop, boolean weWantToPurchase){
			int priceString = currentClosestShop.getPriceString(info, weWantToPurchase);
			if(priceString < 0){
				return weWantToPurchase ? Lng.str("Can't buy") : Lng.str("Can't sell"); 
			}
			
			if(weWantToPurchase){
				int amount = inventoryStateStockCallbackWePurchase.amount();
				if(amount <= 0){
					return Lng.str("Out of Stock");
				}else{
					String amountStr = "(x"+StringTools.massFormat(amount)+")";
					return Lng.str("Buy: %s %s", priceString, amountStr);
				}
			}else{
				int amount = inventoryStateStockCallbackWeSell.amount();
				if(amount <= 0){
					return Lng.str("No blocks to sell");
				}else{
					String amountStr = "(x"+StringTools.massFormat(amount)+")";
					if(amount == Integer.MAX_VALUE){
						amountStr = Lng.str("(All)");
					}
					return Lng.str("Sell: %s %s", priceString, amountStr);
				}
			}
			
		}
		
		public InventoryStatePriceCallback(ElementInformation info) {
			this.info = info;
			
			inventoryStateStockCallbackWePurchase = 
					new InventoryStateStockCallback(info, true);
			inventoryStateStockCallbackWeSell = 
					new InventoryStateStockCallback(info, false);
		}

		@Override
		public String toString() {
			ShopInterface currentClosestShop = ((GameClientState) getState()).getCurrentClosestShop();
			if (currentClosestShop == null) {
				return "err";
			}
			return String.format("%-20s %s", getPriceString(currentClosestShop, true), getPriceString(currentClosestShop, false));
		}
	}

}
