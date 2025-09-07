package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.schine.network.objects.NetworkObject;

public class RemoteString extends RemoteComparable<String> {

	public RemoteString(boolean onServer) {
		super("", onServer);
	}

	public RemoteString(NetworkObject synchOn) {
		super("", synchOn);
	}

	//	public int getBytesSize() {
	//		return ByteUtil.stringToByteArray(get()).length;
	//	}

	public RemoteString(String e, NetworkObject synchOn) {
		super(e, synchOn);
	}

	public RemoteString(String entry, boolean onServer) {
		super(entry, onServer);
	}

	@Override
	public int byteLength() {
		return get().length() + ByteUtil.SIZEOF_INT; //string + stringlength
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {
		
		set(buffer.readUTF());
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		buffer.writeUTF(get());
		return 1;
	}

}
