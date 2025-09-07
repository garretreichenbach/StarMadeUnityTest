package org.schema.game.client.view.gui.ai;

import java.util.ArrayList;

import org.schema.game.common.controller.ai.AIConfiguationElements;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUIOverlay;
import org.schema.schine.graphicsengine.forms.gui.GUISettingsElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class GUIAISettingSelector extends GUISettingsElement implements GUICallback {

	private GUITextOverlay settingName;

	private GUIOverlay leftArrow;

	private GUIOverlay rightArrow;

	private AIConfiguationElements setting;

	private boolean init;

	public GUIAISettingSelector(InputState state, AIConfiguationElements settings) {
		super(state);
		this.setMouseUpdateEnabled(true);
		this.setCallback(this);
		this.setting = settings;
		leftArrow = new GUIOverlay(Controller.getResLoader().getSprite(getState().getGUIPath() + "tools-16x16-gui-"), getState());
		rightArrow = new GUIOverlay(Controller.getResLoader().getSprite(getState().getGUIPath() + "tools-16x16-gui-"), getState());
		settingName = new GUITextOverlay(FontSize.MEDIUM_18, getState());
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if(event.pressedLeftMouse()) {
			try {
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
				AudioController.fireAudioEventID(323);
				setting.switchSetting(true);
			} catch(StateParameterNotFoundException e) {
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
		if(!init) {
			onInit();
		}
		GlUtil.glPushMatrix();
		transform();
		settingName.draw();
		leftArrow.draw();
		rightArrow.draw();
		rightArrow.getPos().x = leftArrow.getWidth() + settingName.getWidth() + settingName.getWidthOfFont((String) settingName.getText().get(0));
		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		settingName.setText(new ArrayList());
		settingName.getText().add(setting.getCurrentState().toString());
		settingName.setPos(0, 5, 0);
		settingName.onInit();
		leftArrow.setMouseUpdateEnabled(true);
		rightArrow.setMouseUpdateEnabled(true);
		leftArrow.setCallback(new GUICallback() {

			private long startedL = -1;

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				boolean longPressed = false;
				if(event.pressedLeftMouse()) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(324);
					if(startedL == -1) {
						startedL = System.currentTimeMillis();
					}
					if(System.currentTimeMillis() - startedL > 1000) {
						longPressed = true;
					}
				} else {
					startedL = -1;
				}
				if(event.pressedLeftMouse() || longPressed) {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
					AudioController.fireAudioEventID(325);
					try {
						setting.switchSettingBack(true);
						updateText();
					} catch(StateParameterNotFoundException e) {
						e.printStackTrace();
						GLFrame.processErrorDialogException(e, getState());
					}
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
		rightArrow.setCallback(new GUICallback() {

			private long startedR = -1;

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				boolean longPressed = false;
				if(event.pressedLeftMouse()) {
					if(startedR == -1) {
						startedR = System.currentTimeMillis();
					}
					if(System.currentTimeMillis() - startedR > 1000) {
						longPressed = true;
					}
				} else {
					startedR = -1;
				}
				if(event.pressedLeftMouse() || longPressed) {
					try {
						setting.switchSetting(true, getState());
						updateText();
					} catch(StateParameterNotFoundException e) {
						e.printStackTrace();
						GLFrame.processErrorDialogException(e, getState());
					}
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
		leftArrow.setSpriteSubIndex(21);
		rightArrow.setSpriteSubIndex(20);
		settingName.getPos().x = leftArrow.getWidth();
		rightArrow.getPos().x = leftArrow.getWidth() + settingName.getWidth();
		init = true;
	}

	@Override
	protected void doOrientation() {
	}

	@Override
	public float getHeight() {
		return 30;
	}

	@Override
	public float getWidth() {
		return settingName.getWidth() + leftArrow.getWidth() + rightArrow.getWidth();
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	private void updateText() {
		settingName.getText().set(0, setting.getCurrentState().toString());
	}
}
