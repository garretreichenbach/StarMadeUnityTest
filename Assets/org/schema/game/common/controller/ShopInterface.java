package org.schema.game.common.controller;

import java.util.Set;

import org.schema.game.common.controller.trade.TradeNode;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.player.inventory.NoSlotFreeException;
import org.schema.game.common.data.player.inventory.ShopInventory;
import org.schema.game.network.objects.TradePriceInterface;
import org.schema.schine.network.StateInterface;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.ints.IntCollection;

public interface ShopInterface {

	public void fillInventory(boolean send, boolean full) throws NoSlotFreeException;

	public long getCredits();

	public ShopNetworkInterface getNetworkObject();

	public int getPriceString(ElementInformation info, boolean purchasePrice);
	/**
	 * @return the permissionToPurchase
	 */
	public long getPermissionToPurchase();

	public long getPermissionToTrade();

	public TradePriceInterface getPrice(short type, boolean buy);


	public int getSectorId();

	public SegmentBufferInterface getSegmentBuffer();

	public ShopInventory getShopInventory();

	public Set<String> getShopOwners();

	public ShoppingAddOn getShoppingAddOn();

	public StateInterface getState();

	public Transform getWorldTransform();

	public boolean isOnServer();

	public void modCredits(long i);

	public void sendInventoryModification(IntCollection slots, long parameter);

	public void sendInventoryModification(int slot, long param);

	public int getFactionId();

	public boolean isInfiniteSupply();

	public boolean isAiShop();

	public boolean isTradeNode();

	public TradeNode getTradeNode();

	public SegmentController getSegmentController();

	public boolean isValidShop();
	
	public boolean isNPCHomeBase();

	public boolean wasValidTradeNode();
}
