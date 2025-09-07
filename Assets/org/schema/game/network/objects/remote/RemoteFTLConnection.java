package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.common.data.world.FTLConnection;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteFTLConnection extends RemoteField<FTLConnection> {

	public RemoteFTLConnection(FTLConnection entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteFTLConnection(FTLConnection entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return 1;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {

		get().deserialize(stream, updateSenderStateId);

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {

		get().serialize(buffer);

		return byteLength();
	}

}
