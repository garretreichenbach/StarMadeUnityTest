package org.schema.game.common.controller.elements;

import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class ModuleMap {

	private final ManagerModule<?, ?, ?>[] values = new ManagerModule<?, ?, ?>[ElementKeyMap.highestType + 1];

	private ManagerModule<?, ?, ?> all;
	private ManagerModule<?, ?, ?> signal;
	private ManagerModule<?, ?, ?> railTrack;
	private ManagerModule<?, ?, ?> railInv;

	public void put(short key, ManagerModule<?, ?, ?> value) {
		if (key == Element.TYPE_ALL) {
			all = value;
		} else if (key == Element.TYPE_SIGNAL) {
			signal = value;
		} else if (key == Element.TYPE_RAIL_TRACK) {
			railTrack = value;
		} else if (key == Element.TYPE_RAIL_INV) {
			railInv = value;
		} else {
			values[key] = value;
		}
	}

	public ManagerModule<?, ?, ?> get(short key) {
		if (key == Element.TYPE_ALL) {
			return all;
		} else if (key == Element.TYPE_SIGNAL) {
			return signal;
		} else if (key == Element.TYPE_RAIL_TRACK) {
			return railTrack;
		} else if (key == Element.TYPE_RAIL_INV) {
			return railInv;
		} else {
			return values[key];
		}

	}

	public boolean containsKey(short key) {
		if (key == Element.TYPE_ALL) {
			return all != null;
		} else if (key == Element.TYPE_SIGNAL) {
			return signal != null;
		} else if (key == Element.TYPE_RAIL_TRACK) {
			return railTrack != null;
		} else if (key == Element.TYPE_RAIL_INV) {
			return railInv != null;
		}  else {
			return values[key] != null;
		}
	}

	public boolean checkIntegrity(){
		for(ManagerModule<?, ?, ?> m : values){
			if(m != null){
				boolean check = check(m);
				if(!check){
					return false;
				}
			}
		}
		return check(all) && 
				check(signal) && 
				check(railTrack);
	}

	private boolean check(ManagerModule<?, ?, ?> m) {
		
		ObjectOpenHashSet<ManagerModule<?, ?, ?>> set = new ObjectOpenHashSet<ManagerModule<?, ?, ?>>();
		set.add(m);
		while(m.getNext() != null){
			m = m.getNext();
			if(set.contains(m)){
				System.err.println("ERROR: Manager Module "+m+" has loop");
				try{
					throw new RuntimeException("Invalid Manager Module: "+m+"; "+ElementKeyMap.toString(m.getElementID())+"; "+set);
				}catch(Exception e){
					e.printStackTrace();
				}
				return false;
			}
			set.add(m);
		}
		return true;
	}

	@Override
	public String toString() {
		return "ModuleMap [all=" + all + ", signal=" + signal + ", railTrack="
				+ railTrack + "]";
	}

	
}
