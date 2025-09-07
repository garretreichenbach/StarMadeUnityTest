package org.schema.game.common.data.player;

import org.schema.game.common.data.player.inventory.NetworkInventoryInterface;
import org.schema.schine.network.objects.remote.RemoteBytePrimitive;
import org.schema.schine.network.objects.remote.RemoteLongBuffer;

public interface NetworkPlayerInterface extends NetworkInventoryInterface {
	public RemoteBytePrimitive getBuildSlot();

	public RemoteLongBuffer getLagAnnouncement();
}
