package org.schema.game.common.facedit;

import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractListModel;

import com.bulletphysics.util.ObjectArrayList;

//#RM1958 remove JList generic argument
public abstract class ArrayModel<T> extends AbstractListModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final ObjectArrayList<T> arrayList;

	public ArrayModel(T[] list) {
		arrayList = new ObjectArrayList<T>();
		for (int i = 0; i < list.length; i++) {
			arrayList.add(list[i]);
		}
	}

	public abstract void changed(ObjectArrayList<T> l);

	public boolean add(T t) {
		boolean result = arrayList.add(t);
		if (result) {
			int index = getIndexOf(t);
			fireIntervalAdded(this, index, index + 1);
		}
		changed(arrayList);
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
		changed(arrayList);
		return result;
	}

}

