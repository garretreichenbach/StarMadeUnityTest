package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.schine.network.objects.NetworkObject;

public class RemoteLongIntPair extends RemoteComparable<LongIntPair> {

	public RemoteLongIntPair(boolean onServer) {
		super(new LongIntPair(), onServer);
	}

	public RemoteLongIntPair(NetworkObject synchOn) {
		super(new LongIntPair(), synchOn);
	}

	//	public int getBytesSize() {
	//		return ByteUtil.stringToByteArray(get()).length;
	//	}

	public RemoteLongIntPair(LongIntPair e, NetworkObject synchOn) {
		super(e, synchOn);
	}

	public RemoteLongIntPair(LongIntPair entry, boolean onServer) {
		super(entry, onServer);
	}



	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_LONG + ByteUtil.SIZEOF_INT; //string + stringlength
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {
		get().l = buffer.readLong();
		get().i = buffer.readInt();
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		buffer.writeLong(get().l);
		buffer.writeInt(get().i);
		return byteLength();
	}

}
