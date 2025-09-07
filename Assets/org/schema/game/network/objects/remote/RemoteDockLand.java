package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.network.objects.DockingRequest;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteDockLand extends RemoteField<DockingRequest> {

	public RemoteDockLand(DockingRequest entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteDockLand(DockingRequest entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return 3 * ByteUtil.SIZEOF_INT            //pos
				+ get().id.length() + ByteUtil.SIZEOF_INT            //id string
				+ ByteUtil.SIZEOF_BYTE;    // dock
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {

		boolean dock = stream.readBoolean();
		String id = stream.readUTF();
		int x = stream.readInt();
		int y = stream.readInt();
		int z = stream.readInt();

		get().set(dock, id, new Vector3i(x, y, z));
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		buffer.writeBoolean(get().dock);
		buffer.writeUTF(get().id);
		buffer.writeInt(get().pos.x);
		buffer.writeInt(get().pos.y);
		buffer.writeInt(get().pos.z);

		return byteLength();
	}

}
