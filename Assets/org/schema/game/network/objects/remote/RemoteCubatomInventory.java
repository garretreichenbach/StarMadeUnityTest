package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.common.data.cubatoms.CubatomInventory;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteCubatomInventory extends RemoteField<CubatomInventory> {

	public RemoteCubatomInventory(CubatomInventory entry, boolean synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return 1; //segPos
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {

		get().deserialize(stream);

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {

		get().serialize(buffer);
		return 1;

	}

}
