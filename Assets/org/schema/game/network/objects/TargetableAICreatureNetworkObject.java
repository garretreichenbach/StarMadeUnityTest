package org.schema.game.network.objects;

import org.schema.game.common.controller.ai.AINetworkInterface;
import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.game.common.data.player.ForcedAnimation;
import org.schema.game.common.data.player.NetworkPlayerInterface;
import org.schema.game.common.data.player.inventory.NetworkInventoryInterface;
import org.schema.game.network.objects.remote.*;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.remote.RemoteArrayBuffer;
import org.schema.schine.network.objects.remote.RemoteBoolean;
import org.schema.schine.network.objects.remote.RemoteBuffer;
import org.schema.schine.network.objects.remote.RemoteBytePrimitive;
import org.schema.schine.network.objects.remote.RemoteFloatPrimitive;
import org.schema.schine.network.objects.remote.RemoteIntPrimitive;
import org.schema.schine.network.objects.remote.RemoteLongBuffer;
import org.schema.schine.network.objects.remote.RemoteLongIntPair;
import org.schema.schine.network.objects.remote.RemoteLongPrimitiveArray;
import org.schema.schine.network.objects.remote.RemoteString;
import org.schema.schine.network.objects.remote.RemoteStringArray;
import org.schema.schine.network.objects.remote.RemoteVector3f;

public class TargetableAICreatureNetworkObject extends NetworkPlayerCharacter implements NetworkInventoryInterface, AINetworkInterface, NetworkPlayerInterface {

	public RemoteVector3f target = new RemoteVector3f(this);
	public RemoteBoolean hasTarget = new RemoteBoolean(this);
	public RemoteString affinity = new RemoteString(this);
	public RemoteInventoryBuffer inventoryBuffer;

	public RemoteInventoryClientActionBuffer inventoryClientActionBuffer = new RemoteInventoryClientActionBuffer(this);
	public RemoteInventoryMultModBuffer inventoryMultModBuffer = new RemoteInventoryMultModBuffer(this);
	public RemoteString debugState = new RemoteString("", this);
	public RemoteArrayBuffer<RemoteStringArray> aiSettingsModification = new RemoteArrayBuffer<RemoteStringArray>(2, RemoteStringArray.class, this);
	public RemoteForcedAnimation forcedAnimation = new RemoteForcedAnimation(new ForcedAnimation(null, null, null, 0, false), this);
	public RemoteBytePrimitive buildSlot = new RemoteBytePrimitive((byte) 0, this);
	public RemoteVector3f targetPosition = new RemoteVector3f(this);
	public RemoteIntPrimitive targetId = new RemoteIntPrimitive(-1, this);
	public RemoteBytePrimitive targetType = new RemoteBytePrimitive((byte) -1, this);
	public RemoteVector3f targetVelocity = new RemoteVector3f(this);
	public RemoteString realName = new RemoteString(this);
	public RemoteLongBuffer inventoryProductionBuffer = new RemoteLongBuffer(this);
	public RemoteShortIntPairBuffer inventoryFilterBuffer = new RemoteShortIntPairBuffer(this);
	public RemoteFloatPrimitive health = new RemoteFloatPrimitive(4264, this);
	public RemoteLongPrimitiveArray sittingState = new RemoteLongPrimitiveArray(4, this);
	public RemoteLongPrimitiveArray climbingState = new RemoteLongPrimitiveArray(3, this);
	public RemoteInventorySlotRemoveBuffer inventorySlotRemoveRequestBuffer = new RemoteInventorySlotRemoveBuffer(this);
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
	public RemoteInventorySlotRemoveBuffer getInventorySlotRemoveRequestBuffer() {
		return inventorySlotRemoveRequestBuffer;
	}
	
	public TargetableAICreatureNetworkObject(StateInterface state, AbstractOwnerState owner) {
		super(state);
		inventoryBuffer = new RemoteInventoryBuffer(owner, this);
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
	 * @return the buildSlot
	 */
	@Override
	public RemoteBytePrimitive getBuildSlot() {
		return buildSlot;
	}

	

}
