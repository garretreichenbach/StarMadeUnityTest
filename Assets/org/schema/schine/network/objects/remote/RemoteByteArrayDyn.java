package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.schine.network.objects.NetworkObject;

public class RemoteByteArrayDyn extends RemoteField<byte[]> {
	public RemoteByteArrayDyn(byte[] entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteByteArrayDyn(byte[] entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_INT;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {
		//size read in RemoteByteArrayDynBuffer
		stream.readFully(get(), 0, get().length);
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		buffer.writeInt(get().length);
		buffer.write(get(), 0, get().length);
		return 1;
	}

}
