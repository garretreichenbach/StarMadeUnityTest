package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.schine.network.objects.NetworkObject;

public abstract class RemoteArray<E extends Comparable<E>> extends RemoteField<RemoteField<E>[]> implements NetworkChangeObserver, StreamableArray<E> {
	public RemoteArray(RemoteField<E>[] e, boolean synchOn) {
		super(e, synchOn);
		init(e);
	}

	public RemoteArray(RemoteField<E>[] e, NetworkObject synchOn) {
		super(e, synchOn);
		init(e);
	}

	protected void addObservers() {

		for (int i = 0; i < get().length; i++) {
			get()[i].observer = (this);
		}
	}

	@Override
	public int arrayLength() {
		return get().length;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.objects.remote.RemoteField#set(java.lang.Object)
	 */

	@Override
	public void fromByteStream(DataInputStream b, int updateSenderStateId) throws IOException {
		for (int i = 0; i < get().length; i++) {
			get()[i].fromByteStream(b, updateSenderStateId);
			set(i, get(i).get());
		}
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		for (int i = 0; i < get().length; i++) {
			get()[i].toByteStream(buffer);
		}
		return byteLength();
	}

	public RemoteField<E> get(int i) {
		return super.get()[i];
	}

	protected abstract void init(RemoteField<E>[] e);

	public abstract void set(int i, E value);

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Streamable<?> o) {
		setChanged(true);
		//immediately change single field back
		o.setChanged(false);
		if (observer != null) {
			observer.update(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.objects.remote.NetworkChangeObserver#isSynched()
	 */
	@Override
	public boolean isSynched() {
		return observer == null || observer.isSynched();
	}

}
