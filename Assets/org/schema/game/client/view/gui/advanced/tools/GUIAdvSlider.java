package org.schema.game.client.view.gui.advanced.tools;

import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.newgui.settingsnew.GUIScrollSettingSelector;
import org.schema.schine.input.InputState;

public class GUIAdvSlider extends GUIAdvTool<SliderResult> {

	public GUIScrollSettingSelector scrollSetting;

	public GUIAdvSlider(InputState state, GUIElement dependent, final SliderResult r) {
		super(state, dependent, r);
		assert (getRes() != null);

		scrollSetting = new GUIScrollSettingSelector(getState(), GUIScrollablePanel.SCROLLABLE_HORIZONTAL, 50, getRes().getFontSize()) {
			@Override
			public boolean isVerticalActive() {
				return false;
			}


			@Override
			public void settingChanged(Object setting) {
//				System.err.println("SETTINGS :"+getName()+": "+setting+"; "+getRes().getCurrentValue());
				if(setting instanceof Integer) {
					getRes().change((Integer) setting);
				}
				if(setting instanceof Float) {
					getRes().change(((Float) setting).intValue());
				}
				super.settingChanged(setting);
				getRes().change(getRes().getCurrentValue());
			}

			@Override
			public boolean showLabel() {
				return getRes().showLabel();
			}

			@Override
			public void resetScrollValue() {
				setSettingX(getRes().getResetValue());
			}

			@Override
			protected void incSetting() {
				getRes().mod(1);
				settingChanged(null);
			}

			@Override
			protected float getSettingY() {
				return 0;
			}

			@Override
			protected void setSettingY(float value) {
			}

			@Override
			protected float getSettingX() {
				return getRes().getCurrentValue();
			}

			@Override
			protected void setSettingX(float value) {
				getRes().change((int) value);
				settingChanged(null);
			}

			@Override
			public float getMaxY() {
				return 0;
			}

			@Override
			public float getMaxX() {
				return getRes().getMax();
			}

			@Override
			protected void decSetting() {
				getRes().mod(-1);
				settingChanged(null);
			}

			@Override
			public float getMinX() {
				return getRes().getMin();
			}

			@Override
			public float getMinY() {
				return 0;
			}
		};
		scrollSetting.setNameLabel(new Object() {
			@Override
			public String toString() {
				return getRes().getName();
			}

		});

		getRes().onInitializeScrollSetting(scrollSetting);
		scrollSetting.dep = this;
		scrollSetting.widthMod = -10;
		scrollSetting.posMoxX = 5;

		attach(scrollSetting);
	}

	@Override
	public void onInit() {
		super.onInit();

	}

	@Override
	public int getElementHeight() {
		return (int) scrollSetting.getHeight();
	}

	public void setValue(int i) {
		if(scrollSetting != null) {
			scrollSetting.set(i);
			scrollSetting.settingChanged(null);
		}
	}
}
