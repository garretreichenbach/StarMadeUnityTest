package org.schema.common;

import java.awt.event.KeyEvent;
import java.util.HashSet;

public class KeyStatus {
	private HashSet<Integer> keyMap;

	public KeyStatus() {
		super();
		this.keyMap = new HashSet<Integer>();
	}

	public boolean areAllKeysDown(int... k) {
		for (int i = 0; i < k.length; i++) {
			if (!keyMap.contains(k[i])) {
				return false;
			}
		}
		return true;
	}

	public void clearKeys() {
		keyMap.clear();
	}

	public boolean isKeyDown(int k) {
		return keyMap.contains(k);
	}

	public boolean isKeyDown(KeyEvent k) {
		return isKeyDown(k.getKeyCode());
	}

	public boolean isOneKeyDown(int... k) {
		for (int i = 0; i < k.length; i++) {
			if (keyMap.contains(k[i])) {
				return true;
			}
		}
		return false;
	}

	public void setKeyPressed(KeyEvent k) {
		keyMap.add(k.getKeyCode());
	}

	public void setKeyReleased(KeyEvent k) {
		keyMap.remove(k.getKeyCode());
	}

}
