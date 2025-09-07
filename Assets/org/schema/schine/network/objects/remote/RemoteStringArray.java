package org.schema.schine.network.objects.remote;

import java.util.Arrays;

import org.schema.schine.network.objects.NetworkObject;

public class RemoteStringArray extends RemoteArray<String> {

	public RemoteStringArray(int size, boolean synchOn) {
		super(new RemoteString[size], synchOn);
		for (int i = 0; i < size; i++) {
			get()[i] = new RemoteString(synchOn);
		}
		addObservers();
	}

	public RemoteStringArray(int size, NetworkObject synchOn) {
		super(new RemoteString[size], synchOn);
		for (int i = 0; i < size; i++) {
			get()[i] = new RemoteString(synchOn);
		}
		addObservers();
	}

	@Override
	public int byteLength() {
		int s = 0;
		for (int i = 0; i < get().length; i++) {
			s += get()[i].byteLength();
		}
		return s;
	}

	@Override
	protected void init(RemoteField<String>[] e) {
		set(e);

	}

	@Override
	public void set(int i, String value) {
		super.get()[i].set(value, forcedClientSending);
	}

	@Override
	public void set(RemoteField<String>[] value) {
		super.set(value);
		for (int i = 0; i < value.length; i++) {
			get()[i] = new RemoteString(onServer);
			get()[i].observer = this;
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.objects.remote.RemoteField#toString()
	 */
	@Override
	public String toString() {
		return "RemoteStringArray" + Arrays.toString(get());
	}

}
