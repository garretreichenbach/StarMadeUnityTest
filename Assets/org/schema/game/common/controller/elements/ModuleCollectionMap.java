package org.schema.game.common.controller.elements;

import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;

public class ModuleCollectionMap {

	private final ManagerModuleCollection<?, ?, ?>[] values = new ManagerModuleCollection<?, ?, ?>[ElementKeyMap.highestType + 1];

	ManagerModuleCollection<?, ?, ?> all;
	ManagerModuleCollection<?, ?, ?> signal;
	ManagerModuleCollection<?, ?, ?> railTrack;
	ManagerModuleCollection<?, ?, ?> railInv;

	public void put(short key, ManagerModuleCollection<?, ?, ?> value) {
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

	public ManagerModuleCollection<?, ?, ?> get(short key) {
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
		} else {
			return values[key] != null;
		}
	}

}
