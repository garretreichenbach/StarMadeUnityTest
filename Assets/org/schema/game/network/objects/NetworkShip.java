package org.schema.game.network.objects;

import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.ShopInterface;
import org.schema.game.common.controller.ShopNetworkInterface;
import org.schema.game.common.controller.ai.AINetworkInterface;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.player.inventory.NetworkInventoryInterface;
import org.schema.game.network.objects.remote.*;
import org.schema.game.network.objects.valueUpdate.NTValueUpdateInterface;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.remote.*;

public class NetworkShip extends NetworkSegmentController implements ShopNetworkInterface, AINetworkInterface, NetworkDoorInterface, NetworkInventoryInterface, NTValueUpdateInterface, PowerInterfaceNetworkObject {

	public RemoteVector3f moveDir = new RemoteVector3f(this);
	public RemoteVector4f orientationDir = new RemoteVector4f(this);
	public RemoteVector3f targetPosition = new RemoteVector3f(this);
	public RemoteIntPrimitive targetId = new RemoteIntPrimitive(-1, this);
	public RemoteBytePrimitive targetType = new RemoteBytePrimitive((byte) -1, this);
	public RemoteVector3f targetVelocity = new RemoteVector3f(this);
	public RemoteInventoryClientActionBuffer inventoryClientActionBuffer = new RemoteInventoryClientActionBuffer(this);
	public RemoteBuffer<RemoteBoolean> onHitNotices = new RemoteBuffer<RemoteBoolean>(RemoteBoolean.class, this);
	public RemoteBuffer<RemoteVector4i> doorActivate = new RemoteBuffer<RemoteVector4i>(RemoteVector4i.class, this);
	public RemoteString debugState = new RemoteString("", this);
	public RemoteArrayBuffer<RemoteStringArray> aiSettingsModification = new RemoteArrayBuffer<RemoteStringArray>(2, RemoteStringArray.class, this);
	public RemoteBoolean stealthActive = new RemoteBoolean(this); //condensed version of old-power jam/cloak var based on new effects system. may be outdated
	public RemoteInventoryBuffer inventoryBuffer;
	public RemoteInventoryMultModBuffer inventoryMultModBuffer = new RemoteInventoryMultModBuffer(this);
	public RemoteLongBuffer inventoryProductionBuffer = new RemoteLongBuffer(this);
	public RemoteValueUpdateBuffer valueUpdateBuffer;
	public RemoteLongStringBuffer customNameModBuffer = new RemoteLongStringBuffer(this);
	public RemoteLongStringBuffer passwordModBuffer = new RemoteLongStringBuffer(this);
	public RemoteLongBooleanPairBuffer inventoryAutoLockModBuffer = new RemoteLongBooleanPairBuffer(this);
	public RemoteShortIntPairBuffer inventoryFilterBuffer = new RemoteShortIntPairBuffer(this);
	public RemoteInventorySlotRemoveBuffer inventorySlotRemoveRequestBuffer = new RemoteInventorySlotRemoveBuffer(this);
	public RemoteVector4f thrustBalanceAxis = new RemoteVector4f(this);
	public RemoteBuffer<RemoteVector4f> thrustBalanceAxisChangeBuffer = new RemoteBuffer<RemoteVector4f>(RemoteVector4f.class, this);
	public RemoteFloatPrimitive thrustRepulsorBalance = new RemoteFloatPrimitive(0.0f, this);
	public RemoteBuffer<RemoteFloat> thrustRepulsorBalanceBuffer = new RemoteBuffer<RemoteFloat>(RemoteFloat.class, this);
	public RemoteBooleanPrimitive automaticDampeners = new RemoteBooleanPrimitive(this);
	public RemoteBooleanPrimitive automaticDampenersReactivate = new RemoteBooleanPrimitive(this);
	public RemoteBuffer<RemoteBoolean> automaticDampenersReq = new RemoteBuffer<RemoteBoolean>(RemoteBoolean.class, this);
	public RemoteBuffer<RemoteBoolean> automaticDampenersReactivateReq = new RemoteBuffer<RemoteBoolean>(RemoteBoolean.class, this);
	public RemoteBooleanPrimitive thrustSharing = new RemoteBooleanPrimitive(this);
	public RemoteBuffer<RemoteBoolean> thrustSharingReq = new RemoteBuffer<RemoteBoolean>(RemoteBoolean.class, this);
	public RemoteLongBuffer lastPickupAreaUsed = new RemoteLongBuffer(this);
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
	public RemoteCockpitManagerBuffer cockpitManagerBuffer;
	public RemoteLongTransformationPairBuffer cockpitManagerUpdateBuffer = new RemoteLongTransformationPairBuffer(this);
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
	
	
	public NetworkShip(StateInterface state, Ship ship) {
		super(state, ship);
		inventoryBuffer = new RemoteInventoryBuffer(ship.getManagerContainer(), this);
		valueUpdateBuffer = new RemoteValueUpdateBuffer(this, ship.getManagerContainer());
		reactorSetBuffer = new RemoteReactorSetBuffer(this, ship.getManagerContainer().getPowerInterface());
		reactorTreeBuffer = new RemoteReactorTreeBuffer(this, ship.getManagerContainer().getPowerInterface());
		reactorPriorityQueueBuffer = new RemoteReactorPriorityQueueBuffer(this, ship.getManagerContainer().getPowerInterface());
		cockpitManagerBuffer = new RemoteCockpitManagerBuffer(this, ship.getManagerContainer().getCockpitManager());
		compressedPricesBuffer = new RemoteCompressedShopPricesBuffer(((ShopInterface) ((ManagedSegmentController<?>) ship).getManagerContainer()).getShoppingAddOn(), this);
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

	@Override
	public RemoteValueUpdateBuffer getValueUpdateBuffer() {
		return valueUpdateBuffer;
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

	public RemoteBooleanPrimitive shopInfiniteSupply = new RemoteBooleanPrimitive(this);
	public RemoteBooleanPrimitive tradeNodeOn = new RemoteBooleanPrimitive(this);
	public RemoteByteBuffer tradeNodeOnRequests = new RemoteByteBuffer(this);
	public RemoteLongPrimitive shopCredits = new RemoteLongPrimitive(0L, this);
	public RemoteCompressedShopPricesBuffer compressedPricesBuffer;
	public RemoteLongPrimitive tradePermission = new RemoteLongPrimitive(0, this);
	public RemoteLongPrimitive localPermission = new RemoteLongPrimitive(0, this);
	public RemoteTradePriceSingleBuffer pricesModifyBuffer = new RemoteTradePriceSingleBuffer(this);
	public RemoteShopOptionBuffer shopOptionBuffer = new RemoteShopOptionBuffer(this);
	public RemoteTradePriceBuffer pricesFullBuffer = new RemoteTradePriceBuffer(this);

	@Override
	public RemoteTradePriceSingleBuffer getPriceModifyBuffer() {
		return pricesModifyBuffer;
	}

	@Override
	public RemoteCompressedShopPricesBuffer getCompressedPricesUpdateBuffer() {
		return compressedPricesBuffer;
	}

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
	public RemoteShopOptionBuffer getShopOptionBuffer() {
		return shopOptionBuffer;
	}

	@Override
	public RemoteTradePriceBuffer getPricesUpdateBuffer() {
		return pricesFullBuffer;
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
