package org.schema.game.network.objects;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.ShopInterface;
import org.schema.game.common.controller.ShopNetworkInterface;
import org.schema.game.common.controller.ai.AINetworkInterface;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.player.inventory.NetworkInventoryInterface;
import org.schema.game.network.objects.remote.*;
import org.schema.game.network.objects.valueUpdate.NTValueUpdateInterface;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.remote.RemoteArrayBuffer;
import org.schema.schine.network.objects.remote.RemoteBooleanPrimitive;
import org.schema.schine.network.objects.remote.RemoteBuffer;
import org.schema.schine.network.objects.remote.RemoteByteBuffer;
import org.schema.schine.network.objects.remote.RemoteFloatBuffer;
import org.schema.schine.network.objects.remote.RemoteIntArrayBuffer;
import org.schema.schine.network.objects.remote.RemoteLongBuffer;
import org.schema.schine.network.objects.remote.RemoteLongIntPair;
import org.schema.schine.network.objects.remote.RemoteLongPrimitive;
import org.schema.schine.network.objects.remote.RemoteShortBuffer;
import org.schema.schine.network.objects.remote.RemoteString;
import org.schema.schine.network.objects.remote.RemoteStringArray;
import org.schema.schine.network.objects.remote.RemoteVector4i;

public class NetworkSpaceStation extends NetworkSegmentController implements ShopNetworkInterface, NetworkInventoryInterface, AINetworkInterface, NetworkLiftInterface, NetworkDoorInterface, NTValueUpdateInterface, PowerInterfaceNetworkObject {

	public RemoteInventoryMultModBuffer inventoryMultModBuffer = new RemoteInventoryMultModBuffer(this);
	public RemoteBuffer<RemoteVector4i> liftActivate = new RemoteBuffer<RemoteVector4i>(RemoteVector4i.class, this);

	public RemoteInventoryBuffer inventoryBuffer;
	public RemoteInventoryClientActionBuffer inventoryClientActionBuffer = new RemoteInventoryClientActionBuffer(this);
	public RemoteBuffer<RemoteVector4i> doorActivate = new RemoteBuffer<RemoteVector4i>(RemoteVector4i.class, this);
	public RemoteString debugState = new RemoteString("", this);
	public RemoteArrayBuffer<RemoteStringArray> aiSettingsModification = new RemoteArrayBuffer<RemoteStringArray>(2, RemoteStringArray.class, this);
	public RemoteIntArrayBuffer inventoryActivateBuffer = new RemoteIntArrayBuffer(3, this);
	public RemoteBooleanPrimitive shopInfiniteSupply = new RemoteBooleanPrimitive(this);
	public RemoteBooleanPrimitive tradeNodeOn = new RemoteBooleanPrimitive(this);
	public RemoteByteBuffer tradeNodeOnRequests = new RemoteByteBuffer(this);
	public RemoteLongPrimitive shopCredits = new RemoteLongPrimitive(0L, this);
	public RemoteLongBuffer inventoryProductionBuffer = new RemoteLongBuffer(this);
	public RemoteValueUpdateBuffer valueUpdateBuffer;
	public RemoteShortIntPairBuffer inventoryFilterBuffer = new RemoteShortIntPairBuffer(this);
	public RemoteLongStringBuffer customNameModBuffer = new RemoteLongStringBuffer(this);
	public RemoteLongStringBuffer passwordModBuffer = new RemoteLongStringBuffer(this);
	public RemoteLongBooleanPairBuffer inventoryAutoLockModBuffer = new RemoteLongBooleanPairBuffer(this);
	public RemoteCompressedShopPricesBuffer compressedPricesBuffer;
	public RemoteInventorySlotRemoveBuffer inventorySlotRemoveRequestBuffer = new RemoteInventorySlotRemoveBuffer(this);
	public RemoteLongPrimitive tradePermission = new RemoteLongPrimitive(0, this);
	public RemoteLongPrimitive localPermission = new RemoteLongPrimitive(0, this);
	public RemoteTradePriceSingleBuffer pricesModifyBuffer = new RemoteTradePriceSingleBuffer(this);
	public RemoteShopOptionBuffer shopOptionBuffer = new RemoteShopOptionBuffer(this);
	public RemoteTradePriceBuffer pricesFullBuffer = new RemoteTradePriceBuffer(this);
	public RemoteReactorSetBuffer reactorSetBuffer;
	public RemoteReactorTreeBuffer reactorTreeBuffer;
	public RemoteShortBuffer reactorRecalibrateBuffer = new RemoteShortBuffer(this, 8);
	public RemoteBuffer<RemoteLongIntPair> reactorChangeBuffer = new RemoteBuffer<RemoteLongIntPair>(RemoteLongIntPair.class, this);
	public RemoteReactorPriorityQueueBuffer reactorPriorityQueueBuffer;
	public RemoteShortIntPairBuffer inventoryFillBuffer = new RemoteShortIntPairBuffer(this);
	public RemoteBuffer<RemoteLongIntPair> inventoryProductionLimitBuffer = new RemoteBuffer<RemoteLongIntPair>(RemoteLongIntPair.class, this);
	public RemoteFloatBuffer reactorCooldownBuffer = new RemoteFloatBuffer(this);
	public RemoteFloatBuffer energyStreamCooldownBuffer = new RemoteFloatBuffer(this);
	public RemoteReactorBonusUpdateBuffer reactorBonusMatrixUpdateBuffer = new RemoteReactorBonusUpdateBuffer(this);
	@Override
	public RemoteShortIntPairBuffer getInventoryFillBuffer() {
		return inventoryFillBuffer;
	}

	@Override
	public RemoteBuffer<RemoteLongIntPair> getInventoryProductionLimitBuffer() {
		return inventoryProductionLimitBuffer;
	}
	@Override
	public RemoteInventorySlotRemoveBuffer getInventorySlotRemoveRequestBuffer() {
		return inventorySlotRemoveRequestBuffer;
	}
	public NetworkSpaceStation(StateInterface state, SendableSegmentController spaceStation) {
		super(state, spaceStation);
		inventoryBuffer = new RemoteInventoryBuffer(((ManagedSegmentController<?>) spaceStation).getManagerContainer(), this);
		valueUpdateBuffer = new RemoteValueUpdateBuffer(this, ((ManagedSegmentController<?>) spaceStation).getManagerContainer());

		compressedPricesBuffer = new RemoteCompressedShopPricesBuffer(((ShopInterface) ((ManagedSegmentController<?>) spaceStation).getManagerContainer()).getShoppingAddOn(), this);
		reactorSetBuffer = new RemoteReactorSetBuffer(this, ((ManagedSegmentController<?>) spaceStation).getManagerContainer().getPowerInterface());
		reactorTreeBuffer = new RemoteReactorTreeBuffer(this, ((ManagedSegmentController<?>) spaceStation).getManagerContainer().getPowerInterface());
		reactorPriorityQueueBuffer = new RemoteReactorPriorityQueueBuffer(this, ((ManagedSegmentController<?>) spaceStation).getManagerContainer().getPowerInterface());
	}

	/**
	 * @return the aiSettingsModification
	 */
	@Override
	public RemoteArrayBuffer<RemoteStringArray> getAiSettingsModification() {
		return aiSettingsModification;
	}

	@Override
	public RemoteString getDebugState() {
		return debugState;
	}

	/**
	 * @return the doorActivate
	 */
	@Override
	public RemoteBuffer<RemoteVector4i> getDoorActivate() {
		return doorActivate;
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
		return customNameModBuffer;
	}

	@Override
	public RemoteLongStringBuffer getInventoryPasswordModBuffer() {
		return passwordModBuffer;
	}

	@Override
	public RemoteLongBooleanPairBuffer getInventoryAutoLockModBuffer() {
		return inventoryAutoLockModBuffer;
	}

	/**
	 * @return the liftActivate
	 */
	@Override
	public RemoteBuffer<RemoteVector4i> getLiftActivate() {
		return liftActivate;
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
	public RemoteValueUpdateBuffer getValueUpdateBuffer() {
		return valueUpdateBuffer;
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
	public RemoteLongPrimitive getTradePermission() {
		return tradePermission;
	}

	@Override
	public RemoteLongPrimitive getLocalPermission() {
		return localPermission;
	}
	
	@Override
	public RemoteReactorSetBuffer getReactorSetBuffer() {
		return reactorSetBuffer;
	}
	@Override
	public RemoteReactorTreeBuffer getReactorTreeBuffer() {
		return reactorTreeBuffer;
	}
	@Override
	public RemoteLongBuffer getConvertRequestBuffer() {
		return convertRequestBuffer;
	}
	@Override
	public RemoteLongBuffer getBootRequestBuffer() {
		return bootRequestBuffer;
	}
	@Override
	public RemoteLongPrimitive getActiveReactor() {
		return activeReactor;
	}
	@Override
	public RemoteShortBuffer getRecalibrateRequestBuffer() {
		return reactorRecalibrateBuffer;
	}
	@Override
	public RemoteBuffer<RemoteLongIntPair> getReactorChangeBuffer() {
		return reactorChangeBuffer;
	}
	@Override
	public RemoteReactorPriorityQueueBuffer getReactorPrioQueueBuffer() {
		return reactorPriorityQueueBuffer;
	}

	@Override
	public RemoteFloatBuffer getReactorCooldownBuffer() {
		return reactorCooldownBuffer;
	}
	@Override
	public RemoteFloatBuffer getEnergyStreamCooldownBuffer() {
		return energyStreamCooldownBuffer;
	}
	@Override
	public RemoteReactorBonusUpdateBuffer getReactorBonusMatrixUpdateBuffer() {
		return reactorBonusMatrixUpdateBuffer;
	}
}
