package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.network.objects.StringLongLongPair;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteStringLongPair extends RemoteField<StringLongLongPair> {
	public RemoteStringLongPair(StringLongLongPair entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteStringLongPair(StringLongLongPair entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_INT;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {

		get().playerName = stream.readUTF();
		get().timeStamp = stream.readLong();
		get().size = stream.readLong();
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		buffer.writeUTF(get().playerName);
		buffer.writeLong(get().timeStamp);
		buffer.writeLong(get().size);

		return 1;
	}

}
