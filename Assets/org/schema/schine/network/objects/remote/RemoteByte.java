package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.schine.network.objects.NetworkObject;

public class RemoteByte extends RemoteComparable<Byte> {

	public RemoteByte(boolean synchOn) {
		this((byte) 0, synchOn);
	}

	public RemoteByte(byte e, boolean onServer) {
		super(e, onServer);
	}

	public RemoteByte(Byte e, NetworkObject synchOn) {
		super(e, synchOn);
	}

	public RemoteByte(NetworkObject synchOn) {
		this((byte) 0, synchOn);
	}

	@Override
	public int byteLength() {
				return ByteUtil.SIZEOF_BYTE;
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {
		byte v = buffer.readByte();
		set(v);
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		buffer.writeByte(get());
		return ByteUtil.SIZEOF_BYTE;
	}
}
