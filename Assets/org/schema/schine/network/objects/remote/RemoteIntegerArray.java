package org.schema.schine.network.objects.remote;

import org.schema.common.util.ByteUtil;
import org.schema.schine.network.objects.NetworkObject;

public class RemoteIntegerArray extends RemoteArray<Integer> {

	private int[] transientArray;

	public RemoteIntegerArray(int size, boolean synchOn) {
		super(new RemoteInteger[size], synchOn);

	}

	public RemoteIntegerArray(int size, NetworkObject synchOn) {
		super(new RemoteInteger[size], synchOn);

	}

	@Override
	public int byteLength() {
		return get().length * ByteUtil.SIZEOF_INT;
	}

	/**
	 * @return the transientArray
	 */
	public int[] getTransientArray() {
		return transientArray;
	}

	@Override
	protected void init(RemoteField<Integer>[] e) {
		set(e);

	}

	@Override
	public void set(int i, Integer value) {
		transientArray[i] = value;
		super.get()[i].set(value, forcedClientSending);
	}

	@Override
	public void set(RemoteField<Integer>[] value) {

		super.set(value);
		for (int i = 0; i < value.length; i++) {
			get()[i] = new RemoteInteger(0, onServer);
		}
		transientArray = new int[value.length];
		addObservers();
	}

	public void setArray(int[] value) {

		if (value.length != get().length) {
			throw new IllegalArgumentException("Cannot change array size of remote array");
		}

		for (int i = 0; i < transientArray.length; i++) {
			transientArray[i] = value[i];
			get(i).set(value[i], forcedClientSending);

		}
	}

}
