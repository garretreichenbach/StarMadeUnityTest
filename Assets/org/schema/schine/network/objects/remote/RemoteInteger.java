package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.schine.network.objects.NetworkObject;

public class RemoteInteger extends RemoteComparable<Integer> {

	public RemoteInteger(boolean synchOn) {
		this(0, synchOn);
	}

	public RemoteInteger(Integer e, boolean onServer) {
		super(e, onServer);
	}

	public RemoteInteger(Integer e, NetworkObject synchOn) {
		super(e, synchOn);
	}

	public RemoteInteger(NetworkObject synchOn) {
		this(0, synchOn);
	}

	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_INT;
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {
		set(buffer.readInt());
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		buffer.writeInt(get());
		return 1;
	}
}
