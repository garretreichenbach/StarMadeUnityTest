package org.schema.game.common.facedit;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.AbstractListModel;

public class TreeSetListModel<T extends Comparable<T>> extends AbstractListModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private TreeSet<T> treeSet;

	public TreeSetListModel() {
		treeSet = new TreeSet<T>();
	}

	public TreeSetListModel(Comparator<? super T> comparator) {
		treeSet = new TreeSet<T>(comparator);
	}

	public boolean add(T t) {
		boolean result = treeSet.add(t);
		if (result) {
			int index = getIndexOf(t);
			fireIntervalAdded(this, index, index + 1);
		}
		return result;
	}

	public TreeSet<T> getCollection() {
		return treeSet;
	}

	public int getIndexOf(T t) {
		int index = 0;
		for (T treeItem : treeSet) {
			if (treeItem.equals(t)) {
				return index;
			}
			index++;
		}
		return -1;
	}

	@Override
	public int getSize() {
		return treeSet.size();
	}

	@Override
	public T getElementAt(int index) {
		if (index < 0 || index >= getSize()) {
			String s = "index, " + index + ", is out of bounds for getSize() = "
					+ getSize();
			throw new IllegalArgumentException(s);
		}
		Iterator<T> iterator = treeSet.iterator();
		int count = 0;
		while (iterator.hasNext()) {
			T t = iterator.next();
			if (index == count) {
				return t;
			}
			count++;
		}
		// out of index. return null. will probably never reach this
		return null;
	}

	public boolean remove(T t) {
		int index = getIndexOf(t);
		if (index < 0) {
			return false;
		}
		boolean result = treeSet.remove(t);
		fireIntervalRemoved(this, index, index + 1);
		return result;
	}

}

