package org.schema.game.common.controller.observer;

import com.bulletphysics.util.ObjectArrayList;

/**
 * unsynchronized, memory efficient, and faster alternative to Observable
 * <p/>
 * HOWEVER: this class has to be used with the upmost caution, since
 * it's not thread safe
 *
 * @author Schema
 */
public class DrawerObservable {

	private final ObjectArrayList<DrawerObserver> observer = new ObjectArrayList<DrawerObserver>();

	public void addObserver(DrawerObserver o) {
		observer.add(o);
	}

	public void clearObservers() {
		observer.clear();
	}

	public int countObservers() {
		return observer.size();
	}

	public void deleteObserver(DrawerObserver o) {
		observer.remove(o);
	}

	public void deleteObserver(int o) {
		observer.remove(o);
	}

	public void notifyObservers() {
		notifyObservers(null, null);
	}

	public void notifyObservers(Object userdata) {
		notifyObservers(userdata, null);
	}

	public void notifyObservers(Object userdata, Object message) {
		for (int i = 0; i < observer.size(); i++) {
			observer.get(i).update(this, userdata, message);
		}
	}
}
