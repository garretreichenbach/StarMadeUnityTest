package org.schema.schine.graphicsengine.forms.gui;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActiveInterface;
import org.schema.schine.input.InputState;
import org.schema.schine.sound.controller.AudioController;

public abstract class GUICheckBox extends GUISettingsElement implements GUICallback {

	private GUIOverlay checkBox;

	private GUIOverlay check;

	private boolean init;

	public GUIActiveInterface activeInterface;

	public GUICheckBox(InputState state) {
		super(state);
		this.setMouseUpdateEnabled(true);
		super.setCallback(this);
		if (isNewHud()) {
			checkBox = new GUIOverlay(Controller.getResLoader().getSprite(getState().getGUIPath() + "UI 16px-8x8-gui-"), getState());
			check = new GUIOverlay(Controller.getResLoader().getSprite(getState().getGUIPath() + "UI 16px-8x8-gui-"), getState());
		} else {
			checkBox = new GUIOverlay(Controller.getResLoader().getSprite(getState().getGUIPath() + "tools-16x16-gui-"), getState());
			check = new GUIOverlay(Controller.getResLoader().getSprite(getState().getGUIPath() + "tools-16x16-gui-"), getState());
		}
	}

	protected abstract void activate() throws StateParameterNotFoundException;

	@Override
	public void callback(GUIElement callingGuiElement, MouseEvent event) {
		if (event.pressedLeftMouse()) {
			if (isActivated()) {
				try {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.CHECKBOX, AudioTags.SELECT)*/
					AudioController.fireAudioEventID(2);
					deactivate();
				} catch (StateParameterNotFoundException e) {
					e.printStackTrace();
				}
			} else {
				try {
					/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.CHECKBOX, AudioTags.DESELECT)*/
					AudioController.fireAudioEventID(1);
					activate();
				} catch (StateParameterNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public boolean isOccluded() {
		return activeInterface != null && !activeInterface.isActive();
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
		this.checkMouseInside();
		checkBox.draw();
		if (isActivated()) {
			check.draw();
		}
		GlUtil.glPopMatrix();
	}

	@Override
	public void onInit() {
		if (isNewHud()) {
			checkBox.setSpriteSubIndex(3);
			check.setSpriteSubIndex(2);
		} else {
			checkBox.setSpriteSubIndex(18);
			check.setSpriteSubIndex(19);
			checkBox.getPos().y += 2;
		}
		check.getPos().set(checkBox.getPos());
		init = true;
	}

	protected abstract void deactivate() throws StateParameterNotFoundException;

	@Override
	protected void doOrientation() {
	}

	/**
	 * @param callback the callback to set
	 */
	@Override
	public void setCallback(GUICallback callback) {
		assert (false) : "CANNOT SET CALLBACK BESIDES OWN";
	}

	@Override
	public float getHeight() {
		return checkBox.getHeight();
	}

	@Override
	public float getWidth() {
		return checkBox.getWidth();
	}

	@Override
	public boolean isPositionCenter() {
		return false;
	}

	protected abstract boolean isActivated();

	public boolean isActivatedCheckBox() {
		return isActivated();
	}
}
