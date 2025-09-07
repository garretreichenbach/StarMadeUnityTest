package org.schema.game.common.facedit;

import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractListModel;

public class ArrayListModel<T> extends AbstractListModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<T> arrayList;

	public ArrayListModel(List<T> list) {
		arrayList = list;
	}

	public boolean add(T t) {
		boolean result = arrayList.add(t);
		if (result) {
			int index = getIndexOf(t);
			fireIntervalAdded(this, index, index + 1);
		}
		return result;
	}

	public List<T> getCollection() {
		return arrayList;
	}

	public int getIndexOf(T t) {
		int index = 0;
		for (T treeItem : arrayList) {
			if (treeItem.equals(t)) {
				return index;
			}
			index++;
		}
		return -1;
	}

	@Override
	public int getSize() {
		if (arrayList == null) {
			return 0;
		}
		return arrayList.size();
	}

	@Override
	public T getElementAt(int index) {
		if (index < 0 || index >= getSize()) {
			String s = "index, " + index + ", is out of bounds for getSize() = "
					+ getSize();
			throw new IllegalArgumentException(s);
		}
		Iterator<T> iterator = arrayList.iterator();
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
		boolean result = arrayList.remove(t);
		fireIntervalRemoved(this, index, index + 1);
		return result;
	}

	public void dataChanged(T t) {
		int indexOf = arrayList.indexOf(t);
		fireContentsChanged(this, indexOf, indexOf + 1);
	}

	public T remove(int index) {
		if (index < 0) {
			return null;
		}
		T result = arrayList.remove(index);
		fireIntervalRemoved(this, index, index + 1);
		return result;
	}

	public void allChanged() {
		fireContentsChanged(this, 0, getSize());
	}

}

