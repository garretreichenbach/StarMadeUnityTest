package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.common.data.player.inventory.InventoryFilter;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteInventoryFilter extends RemoteField<InventoryFilter> {


	public RemoteInventoryFilter(InventoryFilter entry, boolean synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return 1;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {
		get().inventoryId = stream.readLong();
		get().filter.deserialize(stream, updateSenderStateId, onServer);
		get().fillUpTo.deserialize(stream, updateSenderStateId, onServer);
	}
	@Override
	public int toByteStream(final DataOutputStream buffer) throws IOException {
		buffer.writeLong(get().inventoryId);
		get().filter.serialize(buffer, onServer);
		get().fillUpTo.serialize(buffer, onServer);
		return 1;
	}


}
