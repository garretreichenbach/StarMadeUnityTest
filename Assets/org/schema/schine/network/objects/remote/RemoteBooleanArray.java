package org.schema.schine.network.objects.remote;

import org.schema.schine.network.objects.NetworkObject;

public class RemoteBooleanArray extends RemoteArray<Boolean> {

	private boolean[] transientArray;

	public RemoteBooleanArray(int size, boolean synchOn) {
		super(new RemoteBoolean[size], synchOn);

	}

	public RemoteBooleanArray(int size, NetworkObject synchOn) {
		super(new RemoteBoolean[size], synchOn);

	}

	@Override
	public int byteLength() {
		return get().length;
	}

	/**
	 * @return the transientArray
	 */
	public boolean[] getTransientArray() {
		return transientArray;
	}

	@Override
	protected void init(RemoteField<Boolean>[] e) {
		set(e);
	}

	//	@Override
	//	public int toByteStream( DataOutputStream buffer) throws Exception {
	//		int s = 0;
	//		for(int i = 0; i < get().length; i++){
	//			s += get()[i].toByteStream(buffer);
	//		}
	//		return s;
	//	}

	//	public void fromByteStream( InputStream b) throws Exception {
	//		for(int i = 0; i < get().length; i++){
	//			get()[i].fromByteStream(b);
	//			set(i, get(i).get());
	//		}
	//	}

	@Override
	public void set(int i, Boolean value) {
		transientArray[i] = value;
		super.get()[i].set(value, forcedClientSending);
	}

	@Override
	public void set(RemoteField<Boolean>[] value) {
		super.set(value);
		for (int i = 0; i < value.length; i++) {
			get()[i] = new RemoteBoolean(false, onServer);
		}
		transientArray = new boolean[value.length];
		addObservers();

	}

	public void setArray(boolean[] value) {
		if (value.length != get().length) {
			throw new IllegalArgumentException("Cannot change array size of remote array");
		}
		for (int i = 0; i < transientArray.length; i++) {

			transientArray[i] = value[i];
			get(i).set(value[i], forcedClientSending);
		}
	}

}
