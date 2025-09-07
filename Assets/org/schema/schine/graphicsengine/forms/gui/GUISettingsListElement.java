package org.schema.schine.graphicsengine.forms.gui;

import javax.vecmath.Vector4f;

import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;

public class GUISettingsListElement extends GUIListElement implements TooltipProvider {

	private GUISettingsElement settingElement;
	private GUITextOverlay nameOverlay;
	private boolean init;
	private boolean backGroundShade;
	private GUIColoredRectangle rec;
	private boolean horizontal;

	public GUISettingsListElement(InputState state, int width, int height, String name, GUISettingsElement settingsElement, boolean backGroundShade, boolean horizontal) {
		this(state, width, height, FontSize.SMALL_14, name, settingsElement, backGroundShade, horizontal);
	}

	public GUISettingsListElement(InputState state, int width, int height, FontInterface font, String name, GUISettingsElement settingsElement, boolean backGroundShade, boolean horizontal) {
		super(state);
		this.setName(name);
		this.horizontal = horizontal;
		this.settingElement = settingsElement;
		this.nameOverlay = new GUITextOverlay(font, state);
		this.nameOverlay.setTextSimple(name);
		this.backGroundShade = backGroundShade;
		setContent(this);
	}

	public GUISettingsListElement(InputState state, String name, GUISettingsElement settingsElement, boolean backGroundShade, boolean horizontal) {
		this(state, UIScale.getUIScale().scale(300), UIScale.getUIScale().scale(60), name, settingsElement, backGroundShade, horizontal);
	}

	@Override
	public void cleanUp() {

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

		nameOverlay.draw();

		if (settingElement instanceof GUIEngineSettingsCheckBox) {
			settingElement.getPos().x = nameOverlay.getMaxLineWidth() + UIScale.getUIScale().scale(10);
		} else {
			if (horizontal) {
				settingElement.getPos().y = UIScale.getUIScale().scale(30);
			} else {
				settingElement.getPos().x = nameOverlay.getMaxLineWidth() + UIScale.getUIScale().scale(10);
			}
		}
		settingElement.getPos().y = UIScale.getUIScale().scale(8);
		settingElement.draw();
		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		nameOverlay.onInit();
		settingElement.onInit();
		nameOverlay.getPos().y += UIScale.getUIScale().scale(8);

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
		if (horizontal) {
			return nameOverlay.getTextHeight() + settingElement.getHeight() + UIScale.getUIScale().scale(14);
		} else {
//			System.err.println("EE "+Math.max(settingElement.getHeight(), nameOverlay.getTextHeight() + 14)+"; "+settingElement.getHeight());
			return Math.max(settingElement.getHeight() + 14, nameOverlay.getTextHeight() + 14);
		}
	}

	@Override
	public float getWidth() {
		if (horizontal) {
			return Math.max(settingElement.getWidth(), nameOverlay.getMaxLineWidth());
		} else {
			return nameOverlay.getMaxLineWidth() + settingElement.getWidth();
		}
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
		settingElement.drawToolTip();
	}

}
