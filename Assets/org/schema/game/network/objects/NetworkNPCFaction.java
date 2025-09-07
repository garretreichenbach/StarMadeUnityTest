package org.schema.game.network.objects;

import org.schema.game.common.controller.ShopInterface;
import org.schema.game.common.controller.ShopNetworkInterface;
import org.schema.game.common.data.player.inventory.InventoryHolder;
import org.schema.game.common.data.player.inventory.NetworkInventoryInterface;
import org.schema.game.network.objects.remote.*;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteBooleanPrimitive;
import org.schema.schine.network.objects.remote.RemoteBuffer;
import org.schema.schine.network.objects.remote.RemoteByteBuffer;
import org.schema.schine.network.objects.remote.RemoteLongBuffer;
import org.schema.schine.network.objects.remote.RemoteLongIntPair;
import org.schema.schine.network.objects.remote.RemoteLongPrimitive;

public class NetworkNPCFaction extends NetworkObject implements NetworkInventoryInterface, ShopNetworkInterface {

	public RemoteInventoryMultModBuffer inventoryMultModBuffer = new RemoteInventoryMultModBuffer(this);
	public RemoteInventoryBuffer inventoryBuffer;
	public RemoteLongPrimitive shopCredits = new RemoteLongPrimitive(0L, this);
	public RemoteLongBuffer inventoryProductionBuffer = new RemoteLongBuffer(this);
	public RemoteBooleanPrimitive shopInfiniteSupply = new RemoteBooleanPrimitive(this);
	public RemoteBooleanPrimitive tradeNodeOn = new RemoteBooleanPrimitive(this);
	public RemoteByteBuffer tradeNodeOnRequests = new RemoteByteBuffer(this);
	public RemoteShortIntPairBuffer inventoryFilterBuffer = new RemoteShortIntPairBuffer(this);
	public RemoteInventoryClientActionBuffer inventoryClientActionBuffer = new RemoteInventoryClientActionBuffer(this);

	public RemoteCompressedShopPricesBuffer compressedPricesBuffer;

	public RemoteInventorySlotRemoveBuffer inventorySlotRemoveRequestBuffer = new RemoteInventorySlotRemoveBuffer(this);
	public RemoteTradePriceSingleBuffer pricesModifyBuffer = new RemoteTradePriceSingleBuffer(this);
	public RemoteShopOptionBuffer shopOptionBuffer = new RemoteShopOptionBuffer(this);
	public RemoteTradePriceBuffer pricesFullBuffer = new RemoteTradePriceBuffer(this);

	public RemoteLongPrimitive tradePermission = new RemoteLongPrimitive(0, this);
	public RemoteLongPrimitive localPermission = new RemoteLongPrimitive(0, this);
	public RemoteShortIntPairBuffer inventoryFillBuffer = new RemoteShortIntPairBuffer(this);
	public RemoteBuffer<RemoteLongIntPair> inventoryProductionLimitBuffer = new RemoteBuffer<RemoteLongIntPair>(RemoteLongIntPair.class, this);
	@Override
	public RemoteShortIntPairBuffer getInventoryFillBuffer() {
		return inventoryFillBuffer;
	}

	@Override
	public RemoteBuffer<RemoteLongIntPair> getInventoryProductionLimitBuffer() {
		return inventoryProductionLimitBuffer;
	}
	@Override
	public RemoteTradePriceSingleBuffer getPriceModifyBuffer() {
		return pricesModifyBuffer;
	}

	@Override
	public RemoteShopOptionBuffer getShopOptionBuffer() {
		return shopOptionBuffer;
	}

	@Override
	public RemoteTradePriceBuffer getPricesUpdateBuffer() {
		return pricesFullBuffer;
	}
	
	@Override
	public RemoteInventorySlotRemoveBuffer getInventorySlotRemoveRequestBuffer() {
		return inventorySlotRemoveRequestBuffer;
	}
	
	public NetworkNPCFaction(StateInterface state, NPCFaction spaceStation) {
		super(state);
		inventoryBuffer = new RemoteInventoryBuffer((InventoryHolder) spaceStation, this);
		compressedPricesBuffer = new RemoteCompressedShopPricesBuffer(((ShopInterface) spaceStation).getShoppingAddOn(), this);
	}

	/**
	 * @return the inventoriesChangeBuffer
	 */
	@Override
	public RemoteInventoryBuffer getInventoriesChangeBuffer() {
		return inventoryBuffer;
	}

	@Override
	public RemoteInventoryClientActionBuffer getInventoryClientActionBuffer() {
		return inventoryClientActionBuffer;
	}

	@Override
	public RemoteInventoryMultModBuffer getInventoryMultModBuffer() {
		return inventoryMultModBuffer;
	}

	@Override
	public RemoteLongBuffer getInventoryProductionBuffer() {
		return inventoryProductionBuffer;
	}

	@Override
	public RemoteShortIntPairBuffer getInventoryFilterBuffer() {
		return inventoryFilterBuffer;
	}

	@Override
	public RemoteLongStringBuffer getInventoryCustomNameModBuffer() {
		return null;
	}

	@Override
	public RemoteLongStringBuffer getInventoryPasswordModBuffer() {
		return null;
	}

	@Override
	public RemoteLongBooleanPairBuffer getInventoryAutoLockModBuffer() {
		return null;
	}


	@Override
	public RemoteCompressedShopPricesBuffer getCompressedPricesUpdateBuffer() {
		return compressedPricesBuffer;
	}

	/**
	 * @return the shopCredits
	 */
	@Override
	public RemoteLongPrimitive getShopCredits() {
		return shopCredits;
	}

	@Override
	public RemoteBooleanPrimitive getInfiniteSupply() {
		return shopInfiniteSupply;
	}

	@Override
	public RemoteBooleanPrimitive getTradeNodeOn() {
		return tradeNodeOn;
	}

	@Override
	public RemoteByteBuffer getTradeNodeOnRequest() {
		return tradeNodeOnRequests;
	}

	@Override
	public void onDelete(StateInterface stateI) {
	}

	@Override
	public void onInit(StateInterface stateI) {
	}

	@Override
	public RemoteLongPrimitive getTradePermission() {
		return tradePermission;
	}

	@Override
	public RemoteLongPrimitive getLocalPermission() {
		return localPermission;
	}


}
