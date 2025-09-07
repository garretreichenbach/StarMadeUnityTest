package org.schema.schine.common;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class DelayedUpdateList<E> extends ObjectArrayList<E>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5415924539142318762L;
	private final ObjectArrayList<E> toAdd = new ObjectArrayList<E>();
	private final ObjectArrayList<E> toRemove = new ObjectArrayList<E>();
	private boolean changed;
	public void addDelayed(E e) {
		synchronized (toAdd) {
			toAdd.add(e);
			changed = true;
		}
	}
	public void removeDelayed(E e) {
		synchronized (toRemove) {
			toRemove.add(e);
			changed = true;
		}
	}
	
	
	public void synch() {
		if(changed) {
			changed = false;
			synchronized(toAdd) {
				addAll(toAdd);
				toAdd.clear();
			}
			synchronized(toRemove) {
				removeAll(toRemove);
				toRemove.clear();
			}
		}
	}
	
}
