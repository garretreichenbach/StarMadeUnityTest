package org.schema.game.common.data.player.inventory;

import api.config.BlockConfig;
import org.schema.game.network.objects.remote.*;
import org.schema.schine.network.objects.remote.RemoteBuffer;
import org.schema.schine.network.objects.remote.RemoteLongBuffer;
import org.schema.schine.network.objects.remote.RemoteLongIntPair;

public interface NetworkInventoryInterface {
	public RemoteInventoryBuffer getInventoriesChangeBuffer();

	/**
	 * @return the inventoryUpdateBuffer
	 */
	public RemoteInventoryClientActionBuffer getInventoryClientActionBuffer();

	public RemoteInventoryMultModBuffer getInventoryMultModBuffer();

	//	public RemoteIntArrayBuffer getInventoryUpdateBuffer();
	public RemoteLongBuffer getInventoryProductionBuffer();

	public RemoteInventorySlotRemoveBuffer getInventorySlotRemoveRequestBuffer();

	public RemoteShortIntPairBuffer getInventoryFilterBuffer();
	public RemoteShortIntPairBuffer getInventoryFillBuffer();
	public RemoteBuffer<RemoteLongIntPair> getInventoryProductionLimitBuffer();

	//	public RemoteIntArrayBuffer getInventoryActivateBuffer();
	public boolean isOnServer();

	public RemoteLongStringBuffer getInventoryCustomNameModBuffer();

	RemoteLongStringBuffer getInventoryPasswordModBuffer();

	RemoteLongBooleanPairBuffer getInventoryAutoLockModBuffer();
}
