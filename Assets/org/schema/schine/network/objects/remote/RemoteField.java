package org.schema.schine.network.objects.remote;

import org.schema.schine.network.objects.NetworkObject;

public abstract class RemoteField<E> implements Streamable<E> {

	public final boolean onServer;
	protected NetworkChangeObserver observer;
	protected boolean keepChanged;
	protected boolean forcedClientSending = false;
	private boolean changed;
	private E value;

	public RemoteField(E e, boolean onServer) {
		this(e, false, onServer);
	}

	public RemoteField(E e, boolean bool, boolean onServer) {
		value = e;
		//		this.synchObject = synchOn;
		this.onServer = onServer;
		changed = bool;
	}

	public RemoteField(E e, boolean bool, NetworkObject synchOn) {
		this(e, bool, synchOn.isOnServer());
		assert (synchOn != null);
	}

	public RemoteField(E e, NetworkObject synchOn) {
		this(e, false, synchOn);
	}

	@Override
	public void cleanAtRelease() {
		//		synchObject = null;
	}

	@Override
	public E get() {
		return value;
	}

	@Override
	public void set(E value) {
		assert (observer == null || observer.isSynched());
		if (NetworkObject.CHECKUNSAVE && observer != null && !observer.isSynched()) {
			throw new UnsaveNetworkOperationException();
		}
		this.value = value;
	}

	//	public int getBytesSize() {
	//		return bytes.length;
	//	}

	@Override
	public void set(E value, boolean forcedClientSending) {
		set(value);

	}

	public void forceClientUpdates() {
		forcedClientSending = true;
	}

	/**
	 * @return the changed
	 */
	@Override
	public final boolean hasChanged() {
		return changed;
	}

	@Override
	public boolean initialSynchUpdateOnly() {
		return false;
	}

	/**
	 * Advanced fields like remoteBuffer must
	 * not be reset until every element has been
	 * piped
	 *
	 * @return true if this object must not be reset to (changed = false)
	 */
	@Override
	public boolean keepChanged() {
		return keepChanged;
	}

	/**
	 * @param changed the changed to set
	 */
	@Override
	public void setChanged(boolean changed) {
		this.changed = changed;
	}
	
	public boolean isChanged(){
		return changed;
	}

	/* (non-Javadoc)
	 * @see java.util.Observable#addObserver(java.util.Observer)
	 */
	@Override
	public void setObserver(NetworkChangeObserver arg0) {
		this.observer = arg0;
	}

	@Override
	public String toString() {
		return "(" + getClass().getSimpleName() + "; val: " + value + ")";
	}

}
