package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.DistMod;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteDistMod extends RemoteField<DistMod> {

	public RemoteDistMod(DistMod entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteDistMod(DistMod entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_INT;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {

		get().controllerPos = new Vector3i(stream.readInt(), stream.readInt(), stream.readInt());
		get().idPos = stream.readLong();
		get().effectId = stream.readByte();
		get().dist = stream.readInt();
		get().onServer = stream.readBoolean();
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {

		buffer.writeInt(get().controllerPos.x);
		buffer.writeInt(get().controllerPos.y);
		buffer.writeInt(get().controllerPos.z);

		buffer.writeLong(get().idPos);

		buffer.writeByte(get().effectId);

		buffer.writeInt(get().dist);

		buffer.writeBoolean(get().onServer);

		return 1;
	}

}
