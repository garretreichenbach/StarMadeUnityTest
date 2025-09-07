package org.schema.game.client.controller.manager.ingame;

import org.schema.common.FastMath;
import org.schema.schine.graphicsengine.forms.gui.GUISettingsElement;

public abstract class AbstractSizeSetting {
	
	
	public GUISettingsElement guiCallBack;
	public int setting;

	
	public AbstractSizeSetting() {
		super();
		setting = getMin();
	}
	public abstract int getMin();
	public void dec() {
		setting = Math.max(getMin(), setting - 1);
		if (guiCallBack != null) {
			guiCallBack.settingChanged(setting);
		}
	}

	public void inc() {
		setting = Math.min(10, setting + 1);
		if (guiCallBack != null) {
			guiCallBack.settingChanged(setting);
		}
	}

	public void reset() {

		setting = 1;
		if (guiCallBack != null) {
			guiCallBack.settingChanged(setting);
		}
	}

	@Override
	public String toString() {
		return String.valueOf(setting);
	}

	public abstract int getMax();

	public float get() {
		return setting;
	}

	public void set(float value) {
		setting = Math.min(Math.max(getMin(), FastMath.round(value)), getMax());
		if (guiCallBack != null) {
			guiCallBack.settingChanged(setting);
		}
	}
}
