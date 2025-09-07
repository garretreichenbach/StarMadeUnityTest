package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.gamemap.requests.GameMapRequest;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteMapEntryRequest extends RemoteField<GameMapRequest> {

	public RemoteMapEntryRequest(GameMapRequest entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteMapEntryRequest(GameMapRequest entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return 1;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {
		get().pos = new Vector3i(stream.readInt(), stream.readInt(), stream.readInt());
		get().type = stream.readByte();
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {

		buffer.writeInt(get().pos.x);
		buffer.writeInt(get().pos.y);
		buffer.writeInt(get().pos.z);
		buffer.writeByte(get().type);

		return byteLength();
	}

}
