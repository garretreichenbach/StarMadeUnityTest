package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.network.objects.ShortIntPair;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteShortIntPair extends RemoteField<ShortIntPair> {
	public RemoteShortIntPair(ShortIntPair entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteShortIntPair(ShortIntPair entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_INT;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {

		get().pos = stream.readLong();
		get().type = stream.readShort();
		get().count = stream.readInt();
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		buffer.writeLong(get().pos);
		buffer.writeShort(get().type);
		buffer.writeInt(get().count);

		return 1;
	}

}
