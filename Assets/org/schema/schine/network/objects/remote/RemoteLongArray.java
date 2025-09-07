package org.schema.schine.network.objects.remote;

import org.schema.common.util.ByteUtil;
import org.schema.schine.network.objects.NetworkObject;

public class RemoteLongArray extends RemoteArray<Long> {

	private long[] transientArray;

	public RemoteLongArray(int size, boolean synchOn) {
		super(new RemoteLong[size], synchOn);
	}

	public RemoteLongArray(int size, NetworkObject synchOn) {
		super(new RemoteLong[size], synchOn);
	}

	@Override
	public int byteLength() {
		return get().length * ByteUtil.SIZEOF_LONG;
	}

	/**
	 * @return the transientArray
	 */
	public long[] getTransientArray() {
		return transientArray;
	}

	@Override
	protected void init(RemoteField<Long>[] e) {
		set(e);
	}

	@Override
	public void set(int i, Long value) {
		transientArray[i] = value;
		super.get()[i].set(value, forcedClientSending);
	}

	@Override
	public void set(RemoteField<Long>[] value) {
		super.set(value);
		for (int i = 0; i < value.length; i++) {
			get()[i] = new RemoteLong(0L, onServer);
		}
		transientArray = new long[value.length];
		addObservers();
	}

	public void setArray(long[] value) {
		if (value.length != get().length) {
			throw new IllegalArgumentException("Cannot change array size of remote array");
		}

		for (int i = 0; i < transientArray.length; i++) {
			transientArray[i] = value[i];
			get(i).set(value[i], forcedClientSending);
		}
	}
}
