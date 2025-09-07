package org.schema.schine.network.objects.remote;

import org.schema.common.util.ByteUtil;
import org.schema.schine.network.objects.NetworkObject;

public class RemoteByteArray extends RemoteArray<Byte> {

	private byte[] transientArray;

	public RemoteByteArray(int size, boolean synchOn) {
		super(new RemoteByte[size], synchOn);
	}

	public RemoteByteArray(int size, NetworkObject synchOn) {
		super(new RemoteByte[size], synchOn);
	}

	@Override
	public int byteLength() {
				return get().length * ByteUtil.SIZEOF_BYTE;
	}

	/**
	 * @return the transientArray
	 */
	public byte[] getTransientArray() {
		return transientArray;
	}

	@Override
	protected void init(RemoteField<Byte>[] e) {
		set(e);

	}

	//	@Override
	//	public int toByteStream(OutputStream buffer) throws IOException {
	//		int s = 0;
	//		buffer.write(transientArray);
	//		return s;
	//	}

	@Override
	public void set(int i, Byte value) {
		transientArray[i] = value;
		super.get()[i].set(value, forcedClientSending);
	}

	@Override
	public void set(RemoteField<Byte>[] value) {
		super.set(value);
		for (int i = 0; i < value.length; i++) {
			get()[i] = new RemoteByte((byte) 0, onServer);
		}
		transientArray = new byte[value.length];
		addObservers();
	}

	public void setArray(byte[] value) {
		if (value.length != get().length) {
			throw new IllegalArgumentException("Cannot change array size of remote array");
		}

		for (int i = 0; i < transientArray.length; i++) {
			transientArray[i] = value[i];
			get(i).set(value[i], forcedClientSending);
		}
	}
}
