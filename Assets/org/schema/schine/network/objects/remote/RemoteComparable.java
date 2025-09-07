package org.schema.schine.network.objects.remote;

import org.schema.schine.network.objects.NetworkObject;

public abstract class RemoteComparable<E extends Comparable<E>> extends RemoteField<E> implements Comparable<E> {

	public RemoteComparable(E e, boolean onServer) {
		super(e, onServer);
	}

	public RemoteComparable(E e, NetworkObject synchOn) {
		super(e, synchOn);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(E o) {
		return this.get().compareTo(o);
	}

	@Override
	public void set(E value) {
		set(value, forcedClientSending);
	}

	@Override
	public void set(E value, boolean forced) {
		//		if(forced){
		//			System.err.println("forcing client to send "+value+" in "+this.getClass());
		//		}
		if (onServer || forced) {
			//set changed if value has changed. leave changed if already has been changed
			setChanged(hasChanged() || !value.equals(this.get()));
		}

		super.set(value);

		if (hasChanged() && observer != null) {
			//received buffer entries dont have buffers
			observer.update(this);
		}
	}
}
