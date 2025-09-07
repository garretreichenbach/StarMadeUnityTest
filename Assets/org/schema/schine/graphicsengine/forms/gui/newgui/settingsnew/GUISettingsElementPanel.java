package org.schema.schine.graphicsengine.forms.gui.newgui.settingsnew;

import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIAnchor;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIEngineSettingsCheckBox;
import org.schema.schine.graphicsengine.forms.gui.GUISettingsElement;
import org.schema.schine.graphicsengine.forms.gui.TooltipProvider;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

public class GUISettingsElementPanel extends GUIAnchor implements TooltipProvider {

	private GUIElement settingElement;
	private boolean init;
	private boolean backGroundShade;
	private GUIColoredRectangle rec;
	private boolean horizontal;

//	public GUISettingsElementPanel(InputState state, int width, int height, GUIElement settingsElement, boolean backGroundShade, boolean horizontal) {
//		this(state, width, height, settingsElement, backGroundShade, horizontal);
//	}

	public GUISettingsElementPanel(InputState state, int width, int height, GUIElement settingsElement, boolean backGroundShade, boolean horizontal) {
		super(state);
		this.horizontal = horizontal;
		this.settingElement = settingsElement;
		this.backGroundShade = backGroundShade;
	}

	public GUISettingsElementPanel(InputState state, GUIElement settingsElement, boolean backGroundShade, boolean horizontal) {
		this(state, 300, 60, settingsElement, backGroundShade, horizontal);
	}


	@Override
	public void draw() {
		if (!init) {
			onInit();
		}

		GlUtil.glPushMatrix();
		transform();

		if (backGroundShade) {
			rec.draw();
		}

		if (settingElement instanceof GUIEngineSettingsCheckBox) {
			settingElement.getPos().x = 10;
			if (isNewHud()) {
				settingElement.getPos().y = 6;
			}

		} else {
			settingElement.getPos().y = -2;

			if (horizontal) {
				settingElement.getPos().y = 30;
			} else {
			}
		}
		settingElement.draw();
		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		settingElement.onInit();

		if (backGroundShade) {
			if (settingElement instanceof GUIEngineSettingsCheckBox) {
				rec = new GUIColoredRectangle(getState(), UIScale.getUIScale().scale(486), (int)getHeight(), new Vector4f(0.068f, 0.068f, 0.068f, 0.3f));
			} else {
				rec = new GUIColoredRectangle(getState(), (int)getWidth(), (int)getHeight(), new Vector4f(0.068f, 0.068f, 0.068f, 0.3f));
			}

		}
		init = true;
	}

	@Override
	public float getHeight() {
		return settingElement.getHeight() + 5;
	}

	@Override
	public float getWidth() {
		return settingElement.getWidth();
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.AbstractSceneNode#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer) {
		settingElement.update(timer);
		super.update(timer);
	}

	@Override
	public void drawToolTip() {
		if (settingElement instanceof GUISettingsElement){
			((GUISettingsElementPanel) settingElement).drawToolTip();
		}
	}

}
