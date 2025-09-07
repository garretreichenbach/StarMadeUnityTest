package org.schema.common;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A LinkedList that efficiently sorts itself with every add.
 *
 * @author schema
 */
public class SortedList<T extends Comparable<T>> extends ArrayList<T> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	

	private int lastIndexAdded = -1;

	public SortedList() {
		super();
	}

	public SortedList(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * Adds this element to the list at the proper sorting position. If the
	 * element already exists, don't do anything.
	 */
	@Override
	public boolean add(T e) {
		if (e == null) {
			throw new NullPointerException("tried to add null " + e);
		}
		if (size() == 0) {
			lastIndexAdded = 0;
			return super.add(e);
		} else {

			// find insertion index
			int idx = Collections.binarySearch(this, e);

			if (idx >= 0) {
				lastIndexAdded = idx + 1;
				super.add(idx + 1, e);
				return true;
			}

			// add at this position
			lastIndexAdded = (-idx - 1);
			super.add(-idx - 1, e);
			return true;
		}
	}

	public int getLastIndexAdded() {
		return lastIndexAdded;
	}

	public void setLastIndexAdded(int lastIndexAdded) {
		this.lastIndexAdded = lastIndexAdded;
	}
}