package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.schine.network.objects.NetworkObject;

public class RemoteBoolean extends RemoteComparable<Boolean> {

	public RemoteBoolean(boolean synchOn) {
		this(false, synchOn);
	}

	public RemoteBoolean(boolean e, boolean onServer) {
		super(e, onServer);
	}

	public RemoteBoolean(boolean e, NetworkObject synchOn) {
		super(e, synchOn);
	}

	public RemoteBoolean(NetworkObject synchOn) {
		this(false, synchOn);
	}

	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_BYTE;
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {
		set(buffer.readBoolean());
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		buffer.writeBoolean(get());
		return ByteUtil.SIZEOF_BYTE;
	}

}
