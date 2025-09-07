package org.schema.schine.graphicsengine.forms.gui;

import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.input.InputState;

public abstract class GUISettingsElement extends GUIElement implements TooltipProvider {
	private GUIToolTip toolTip;

	public GUISettingsElement(InputState state) {
		super(state);
		this.toolTip = initToolTip();
	}

	public GUIToolTip initToolTip() {
		return null;
	}

	public void settingChanged(Object setting) {
	}

	@Override
	public void drawToolTip() {
		if (toolTip != null) {
			GlUtil.glPushMatrix();

			toolTip.draw();

			GlUtil.glPopMatrix();
		}
	}
}
