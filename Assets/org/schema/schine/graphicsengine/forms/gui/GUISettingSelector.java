package org.schema.schine.graphicsengine.forms.gui;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.UIScale;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public class GUISettingSelector extends GUISettingsElement implements GUICallback {

	private GUITextOverlay settingName;

	private GUIOverlay leftArrow;

	private GUIOverlay rightArrow;

	private boolean checked = false;

	private SettingsInterface setting;

	public GUIElement dependent;

	private boolean init;

	private GUIActiveInterface activeInterface;

	public GUISettingSelector(InputState state, int width, int heigth, GUIActiveInterface p, SettingsInterface settings) {
		super(state);
		this.setMouseUpdateEnabled(true);
		this.setCallback(this);
		this.setting = settings.getSettingsForGUI();
		leftArrow = new GUIOverlay(Controller.getResLoader().getSprite(getState().getGUIPath() + "tools-16x16-gui-"), getState());
		rightArrow = new GUIOverlay(Controller.getResLoader().getSprite(getState().getGUIPath() + "tools-16x16-gui-"), getState());
		settingName = new GUITextOverlay(FontSize.SMALL_14, getState());
		this.activeInterface = p;
		assert (setting != null);
	}

	public GUISettingSelector(InputState state, GUIActiveInterface p, SettingsInterface settings) {
		this(state, UIScale.getUIScale().scale(140), UIScale.getUIScale().scale(30), p, settings);
	}

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.pressedLeftMouse()) {
			checked = !checked;
			/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
			AudioController.fireAudioEventID(9);
			setting.next();
		}
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
		if (dependent != null) {
			rightArrow.getPos().x = dependent.getWidth() - rightArrow.getWidth() - UIScale.getUIScale().scale(12);
		} else {
			rightArrow.getPos().x = leftArrow.getWidth() + settingName.getWidth();
		}
		settingName.getPos().x = (int) (leftArrow.getWidth() + (rightArrow.getPos().x - leftArrow.getWidth()) / 2 - settingName.getMaxLineWidth() / 2);
		settingName.draw();
		leftArrow.draw();
		rightArrow.draw();
		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		settingName.setTextSimple(new Object() {

			@Override
			public String toString() {
				assert (setting != null);
				return setting.getAsString();
			}
		});
		settingName.onInit();
		settingName.getPos().y += UIScale.getUIScale().scale(13);
		leftArrow.setMouseUpdateEnabled(true);
		rightArrow.setMouseUpdateEnabled(true);
		leftArrow.getPos().y += UIScale.getUIScale().scale(3);
		rightArrow.getPos().y += UIScale.getUIScale().scale(3);
		leftArrow.setCallback(new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					checked = !checked;
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(10);
					setting.previous();
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
		rightArrow.setCallback(new GUICallback() {

			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if (event.pressedLeftMouse()) {
					checked = !checked;
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(11);
					setting.next();
				}
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		});
		leftArrow.setSpriteSubIndex(21);
		rightArrow.setSpriteSubIndex(20);
		init = true;
	}

	@Override
	protected void doOrientation() {
	}

	@Override
	public float getHeight() {
		return UIScale.getUIScale().scale(30);
	}

	@Override
	public float getWidth() {
		return settingName.getWidth() + leftArrow.getWidth() + rightArrow.getWidth();
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	@Override
	public boolean isOccluded() {
		return !(activeInterface != null && activeInterface.isActive());
	}
}
