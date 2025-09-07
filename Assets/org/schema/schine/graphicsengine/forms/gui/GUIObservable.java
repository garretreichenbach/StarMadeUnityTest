package org.schema.schine.graphicsengine.forms.gui;

import com.bulletphysics.util.ObjectArrayList;

import java.util.List;

public class GUIObservable {
public final List<GUIChangeListener> listener = new ObjectArrayList<GUIChangeListener>(); 
	
	public void addObserver(GUIChangeListener s) {
		listener.add(s);
	}
	public void deleteObserver(GUIChangeListener s) {
		listener.remove(s);
	}
	public void deleteObservers() {
		listener.clear();
	}
	public void notifyObservers(boolean listDimUpdate) {
		int s = listener.size();
		for(int i = 0; i < s; i++) {
			listener.get(i).onChange(listDimUpdate);
		}
	}
	public void notifyObservers() {
		notifyObservers(false);
	}
}
