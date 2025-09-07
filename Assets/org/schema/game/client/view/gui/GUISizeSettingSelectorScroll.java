package org.schema.game.client.view.gui;

import org.schema.game.client.controller.manager.ingame.AbstractSizeSetting;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollablePanel;
import org.schema.schine.graphicsengine.forms.gui.newgui.settingsnew.GUIScrollSettingSelector;
import org.schema.schine.input.InputState;

@Deprecated
public class GUISizeSettingSelectorScroll extends GUIScrollSettingSelector implements GUICallback {

	private final AbstractSizeSetting setting;
	

	public GUISizeSettingSelectorScroll(InputState state, AbstractSizeSetting setting) {
		super(state, GUIScrollablePanel.SCROLLABLE_HORIZONTAL, 200);
		this.setMouseUpdateEnabled(true);
		this.setCallback(this);
		this.setting = setting;
		setting.guiCallBack = this;
	}
	public GUISizeSettingSelectorScroll(InputState state, AbstractSizeSetting setting, GUIElement dep) {
		super(state, GUIScrollablePanel.SCROLLABLE_HORIZONTAL, 200);
		this.setMouseUpdateEnabled(true);
		this.setCallback(this);
		this.setting = setting;
		super.dep = dep;
		setting.guiCallBack = this;
		
	}
	
	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.dWheel > 0) {
			scrollHorizontal(getScroller().getScrollAmountClickOnArrow());
		}
		if (event.dWheel < 0) {
			scrollHorizontal(-getScroller().getScrollAmountClickOnArrow());
		}
	}

	@Override
	protected void decSetting() {
		setting.dec();
	}

	@Override
	protected void incSetting() {
		setting.inc();
	}

	@Override
	protected float getSettingX() {
		return setting.get();
	}

	@Override
	protected void setSettingX(float value) {
		setting.set(value);
	}

	@Override
	protected float getSettingY() {
		return setting.get();
	}

	@Override
	protected void setSettingY(float value) {
		setting.set(value);
	}

	@Override
	public void settingChanged(Object obj) {
		super.settingChanged(obj);
	}

	@Override
	public float getMaxX() {
		return setting.getMax();
	}

	@Override
	public float getMaxY() {
		return setting.getMax();
	}

	@Override
	public float getMinX() {
		return setting.getMin();
	}

	@Override
	public float getMinY() {
		return setting.getMin();
	}

	@Override
	public boolean isVerticalActive() {
		return false;
	}

}
