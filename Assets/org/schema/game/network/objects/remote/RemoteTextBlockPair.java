package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteTextBlockPair extends RemoteField<TextBlockPair> {
	public RemoteTextBlockPair(TextBlockPair entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteTextBlockPair(TextBlockPair entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_INT;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {

		get().text = stream.readUTF();
		get().block = stream.readLong();
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		buffer.writeUTF(get().text);
		buffer.writeLong(get().block);

		return 1;
	}

}
