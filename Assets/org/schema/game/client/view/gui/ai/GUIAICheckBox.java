package org.schema.game.client.view.gui.ai;

import org.schema.game.common.controller.ai.AIConfiguationElements;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUISettingsElement;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class GUIAICheckBox extends GUISettingsElement implements GUICallback {

	private GUIOverlay checkBox;

	private GUIOverlay check;

	private AIConfiguationElements booleanSettings;

	public GUIAICheckBox(InputState state, AIConfiguationElements booleanSettings) {
		super(state);
		this.setMouseUpdateEnabled(true);
		this.setCallback(this);
		this.booleanSettings = booleanSettings;
		assert (booleanSettings.getCurrentState() instanceof Boolean);
		checkBox = new GUIOverlay(Controller.getResLoader().getSprite(getState().getGUIPath() + "tools-16x16-gui-"), getState());
		check = new GUIOverlay(Controller.getResLoader().getSprite(getState().getGUIPath() + "tools-16x16-gui-"), getState());
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.pressedLeftMouse()) {
			try {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
				AudioController.fireAudioEventID(322);
				booleanSettings.switchSetting(true);
			} catch (StateParameterNotFoundException e) {
				e.printStackTrace();
				GLFrame.processErrorDialogException(e, getState());
			}
		}
	}

	@Override
	public void cleanUp() {
	}

	@Override
	public void draw() {
		GlUtil.glPushMatrix();
		transform();
		this.checkMouseInside();
		checkBox.draw();
		if (booleanSettings.isOn()) {
			check.draw();
		}
		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		checkBox.setSpriteSubIndex(18);
		check.setSpriteSubIndex(19);
	}

	@Override
	protected void doOrientation() {
	}

	@Override
	public float getHeight() {
		return checkBox.getHeight() + 10;
	}

	@Override
	public float getWidth() {
		return checkBox.getWidth();
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}
}
