package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.schine.network.NetworkGravity;
import org.schema.schine.network.objects.NetworkObject;

public class RemoteGravity extends RemoteField<NetworkGravity> {

	public RemoteGravity(NetworkGravity entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteGravity(NetworkGravity entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return 1;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {
		get().gravityIdReceive = stream.readInt();
		get().gravityReceive.set(stream.readFloat(), stream.readFloat(), stream.readFloat());
		get().central = stream.readBoolean();
		get().forcedFromServer = (stream.readBoolean());
		get().gravityReceived = true;
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {

		buffer.writeInt(get().gravityId);
		buffer.writeFloat(get().gravity.x);
		buffer.writeFloat(get().gravity.y);
		buffer.writeFloat(get().gravity.z);
		buffer.writeBoolean(get().central);
		buffer.writeBoolean(get().forcedFromServer);

		return 1;
	}

}
