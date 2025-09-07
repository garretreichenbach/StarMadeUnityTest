package org.schema.game.client.view.gui.advanced.tools;

import org.schema.schine.graphicsengine.forms.gui.newgui.settingsnew.GUIScrollSettingSelector;

public abstract class SliderResult extends AdvResult<SliderCallback> {

	private float currentValue = Float.MIN_VALUE;

	public SliderResult() {
	}

	@Override
	protected void initDefault() {
		change(getDefault());
	}

	public float getCurrentValue() {
		return currentValue;
	}

	public boolean showLabel() {
		return true;
	}

	public void mod(float by) {
		change(currentValue + by);
	}

	public void change(float to) {
		float val = currentValue;

		currentValue = Math.min(getMax(), Math.max(getMin(), to));

		if(val != currentValue) {
			if(callback != null) {
				callback.onValueChanged(currentValue);
			}
		}
	}

	public abstract float getDefault();

	public abstract float getMax();

	public abstract float getMin();

	@Override
	public void refresh() {
		change(getDefault());
	}

	public void onInitializeScrollSetting(GUIScrollSettingSelector scrollSetting) {
	}

	public float getResetValue() {
		//Override for rigth click reset
		return getMin();
	}
}
