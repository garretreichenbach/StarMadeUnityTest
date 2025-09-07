package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.schine.network.objects.NetworkObject;

public class RemoteLong extends RemoteComparable<Long> {
	public RemoteLong(boolean synchOn) {
		this(0L, synchOn);
	}

	public RemoteLong(long e, boolean onServer) {
		super(e, onServer);
	}

	public RemoteLong(Long e, NetworkObject synchOn) {
		super(e, synchOn);
	}

	public RemoteLong(NetworkObject synchOn) {
		this(0L, synchOn);
	}

	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_LONG;
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {
		set(buffer.readLong());
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		buffer.writeLong(get());
		return byteLength();
	}
}
