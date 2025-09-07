package org.schema.game.network.objects;

import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.ShopInterface;
import org.schema.game.common.controller.ShopNetworkInterface;
import org.schema.game.common.controller.ai.AINetworkInterface;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.player.inventory.NetworkInventoryInterface;
import org.schema.game.network.objects.remote.RemoteCompressedShopPricesBuffer;
import org.schema.game.network.objects.remote.RemoteInventoryBuffer;
import org.schema.game.network.objects.remote.RemoteLongStringBuffer;
import org.schema.game.network.objects.remote.RemoteReactorBonusUpdateBuffer;
import org.schema.game.network.objects.remote.RemoteReactorPriorityQueueBuffer;
import org.schema.game.network.objects.remote.RemoteReactorSetBuffer;
import org.schema.game.network.objects.remote.RemoteReactorTreeBuffer;
import org.schema.game.network.objects.remote.RemoteShortIntPairBuffer;
import org.schema.game.network.objects.remote.RemoteValueUpdateBuffer;
import org.schema.game.network.objects.valueUpdate.NTValueUpdateInterface;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.remote.RemoteArrayBuffer;
import org.schema.schine.network.objects.remote.RemoteBuffer;
import org.schema.schine.network.objects.remote.RemoteFloatBuffer;
import org.schema.schine.network.objects.remote.RemoteIntArrayBuffer;
import org.schema.schine.network.objects.remote.RemoteLongBuffer;
import org.schema.schine.network.objects.remote.RemoteLongIntPair;
import org.schema.schine.network.objects.remote.RemoteLongPrimitive;
import org.schema.schine.network.objects.remote.RemoteShortBuffer;
import org.schema.schine.network.objects.remote.RemoteString;
import org.schema.schine.network.objects.remote.RemoteStringArray;
import org.schema.schine.network.objects.remote.RemoteVector4i;

public class NetworkManagedShop extends NetworkShop implements ShopNetworkInterface, NetworkInventoryInterface, AINetworkInterface, NetworkLiftInterface, NetworkDoorInterface, NTValueUpdateInterface, PowerInterfaceNetworkObject {

    public RemoteBuffer<RemoteVector4i> liftActivate = new RemoteBuffer<RemoteVector4i>(RemoteVector4i.class, this);

    public RemoteBuffer<RemoteVector4i> doorActivate = new RemoteBuffer<RemoteVector4i>(RemoteVector4i.class, this);
    public RemoteString debugState = new RemoteString("", this);
    public RemoteArrayBuffer<RemoteStringArray> aiSettingsModification = new RemoteArrayBuffer<RemoteStringArray>(2, RemoteStringArray.class, this);
    public RemoteIntArrayBuffer inventoryActivateBuffer = new RemoteIntArrayBuffer(3, this);
    public RemoteValueUpdateBuffer valueUpdateBuffer;
    public RemoteLongStringBuffer customNameModBuffer = new RemoteLongStringBuffer(this);
    public RemoteReactorSetBuffer reactorSetBuffer;
    public RemoteReactorTreeBuffer reactorTreeBuffer;
    public RemoteShortBuffer reactorRecalibrateBuffer = new RemoteShortBuffer(this, 8);
    public RemoteBuffer<RemoteLongIntPair> reactorChangeBuffer = new RemoteBuffer<RemoteLongIntPair>(RemoteLongIntPair.class, this);
    public RemoteReactorPriorityQueueBuffer reactorPriorityQueueBuffer;
    public RemoteShortIntPairBuffer inventoryFillBuffer = new RemoteShortIntPairBuffer(this);
    public RemoteFloatBuffer reactorCooldownBuffer = new RemoteFloatBuffer(this);
    public RemoteFloatBuffer energyStreamCooldownBuffer = new RemoteFloatBuffer(this);
    public RemoteReactorBonusUpdateBuffer reactorBonusMatrixUpdateBuffer = new RemoteReactorBonusUpdateBuffer(this);
    @Override
    public RemoteShortIntPairBuffer getInventoryFillBuffer() {
        return inventoryFillBuffer;
    }

    public NetworkManagedShop(StateInterface state, SendableSegmentController spaceStation) {
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
    public RemoteLongStringBuffer getInventoryCustomNameModBuffer() {
        return customNameModBuffer;
    }

    /**
     * @return the liftActivate
     */
    @Override
    public RemoteBuffer<RemoteVector4i> getLiftActivate() {
        return liftActivate;
    }


    /**
     * @return the shopCredits
     */
    @Override
    public RemoteLongPrimitive getShopCredits() {
        return shopCredits;
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
}
