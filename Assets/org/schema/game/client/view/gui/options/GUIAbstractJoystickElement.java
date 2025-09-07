package org.schema.game.client.view.gui.options;

import java.util.ArrayList;

import org.schema.game.client.controller.PlayerJoystickInput;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.GUIInputPanel;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUICallback;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.input.InputState;
import org.schema.schine.input.JoystickEvent;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardContext;
import org.schema.schine.input.KeyboardMappings;
import org.schema.schine.sound.controller.AudioController;

public abstract class GUIAbstractJoystickElement extends GUIElement implements GUICallback, GUIControlConfigElementInterface {

	private static long deactive = 0;

	private static boolean active = false;

	int i = 0;

	private GUITextOverlay settingName;

	private boolean init;

	private GUIInputPanel input;

	public GUIAbstractJoystickElement(InputState state) {
		super(state);
		settingName = new GUITextOverlay(FontSize.SMALL_14, getState());
	}

	public static boolean active() {
		return active || System.currentTimeMillis() - deactive < 200;
	}

	public abstract boolean hasDuplicate();

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.pressedLeftMouse()) {
			GameClientState s = (GameClientState) getState();
			if (!active()) {
				active = true;
				(new PlayerJoystickInput(s) {

					@Override
					public void callback(GUIElement callingGuiElement, MouseEvent event) {
						if (event.pressedLeftMouse()) {
							if (callingGuiElement.getUserPointer().equals("CANCEL") || callingGuiElement.getUserPointer().equals("X")) {
								/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
								AudioController.fireAudioEventID(593);
								cancel();
							}
						}
					}

					@Override
					public void handleJoystickEvent(JoystickEvent e) {
						/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.OK)*/
						AudioController.fireAudioEventID(594);
						mapJoystickPressed(e);
						deactivate();
					}

					@Override
					public void handleKeyEvent(KeyEventInterface e) {
						if (e.isTriggered(KeyboardMappings.DIALOG_CLOSE)) {
							/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
							AudioController.fireAudioEventID(595);
							cancel();
						}
					}

					@Override
					public GUIElement getInputPanel() {
						return input;
					}

					@Override
					protected void initialize() {
						input = new GUIInputPanel("ASSIGN_JOYSTICK", getState(), this, "Assign New Button to " + getDesc(), "Press controller button to assign it to \n\n" + "<" + getDesc() + "> \n\nor press ESC to cancel.");
						input.setOkButtonText("NOTHING");
						input.setCallback(this);
						input.onInit();
						input.getButtonOK().setCallback(new GUICallback() {

							@Override
							public void callback(GUIElement callingGuiElement, MouseEvent event) {
								if (event.pressedLeftMouse()) {
									/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.BUTTON, AudioTags.PRESS, AudioTags.CANCEL)*/
									AudioController.fireAudioEventID(596);
									mapJoystickPressedNothing();
									deactivate();
								}
							}

							@Override
							public boolean isOccluded() {
								return false;
							}
						});
					}

					@Override
					public void onDeactivate() {
						active = false;
						deactive = System.currentTimeMillis();
					}
				}).activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				AudioController.fireAudioEventID(597);
			}
		}
	}

	public abstract String getDesc();

	public abstract void mapJoystickPressed(JoystickEvent e);

	public abstract void mapJoystickPressedNothing();

	public abstract String getCurrentSettingString();

	protected boolean checkRelated(KeyboardContext a, KeyboardContext b) {
		return isRelated(a, b) || isRelated(b, a);
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
		if (i % 20 == 0) {
			if (hasDuplicate()) {
				settingName.getColor().g = 0;
				settingName.getColor().b = 0;
			} else {
				settingName.getColor().g = 1;
				settingName.getColor().b = 1;
			}
		}
		i++;
		settingName.getText().set(0, getCurrentSettingString());
		settingName.draw();
		checkMouseInside();
		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		settingName.setText(new ArrayList());
		settingName.getText().add(getCurrentSettingString());
		settingName.onInit();
		// this.setCallback(this);
		this.setMouseUpdateEnabled(true);
		init = true;
	}

	@Override
	public float getHeight() {
		return 30;
	}

	@Override
	public float getWidth() {
		return 140;
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	private boolean isRelated(KeyboardContext a, KeyboardContext b) {
		if (a == b) {
			return true;
		}
		if (!a.isRoot()) {
			return isRelated(a.getParent(), b);
		} else {
			return false;
		}
	}
}
