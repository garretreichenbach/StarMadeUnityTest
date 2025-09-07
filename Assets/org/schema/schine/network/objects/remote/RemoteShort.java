package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.schine.network.objects.NetworkObject;

public class RemoteShort extends RemoteComparable<Short> {

	public RemoteShort(boolean synchOn) {
		this((short) 0, synchOn);
	}

	public RemoteShort(NetworkObject synchOn) {
		this((short) 0, synchOn);
	}

	public RemoteShort(short e, boolean synchOn) {
		super(e, synchOn);
	}

	public RemoteShort(Short e, NetworkObject synchOn) {
		super(e, synchOn);
	}

	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_SHORT;
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {
		short v = buffer.readShort();
		set(v);
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		buffer.writeShort(get());
		return ByteUtil.SIZEOF_SHORT;
	}
}
