package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.schine.network.objects.NetworkObject;

public class RemoteFloat extends RemoteComparable<Float> {

	public RemoteFloat(boolean synchOn) {
		this(0f, synchOn);
	}

	public RemoteFloat(float e, boolean onServer) {
		super(e, onServer);
	}

	public RemoteFloat(Float e, NetworkObject synchOn) {
		super(e, synchOn);
	}

	public RemoteFloat(NetworkObject synchOn) {
		this(0f, synchOn);
	}

	@Override
	public int byteLength() {
				return ByteUtil.SIZEOF_FLOAT;
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {
		set(buffer.readFloat());
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		buffer.writeFloat(get());
		return ByteUtil.SIZEOF_FLOAT;
	}

	@Override
	public String toString() {
		return get().toString();
	}
}
