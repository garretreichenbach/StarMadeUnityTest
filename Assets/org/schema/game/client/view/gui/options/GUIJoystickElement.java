package org.schema.game.client.view.gui.options;

import org.schema.schine.input.InputState;
import org.schema.schine.input.JoystickButtonMapping;
import org.schema.schine.input.JoystickEvent;
import org.schema.schine.input.JoystickMappingFile;
import org.schema.schine.input.KeyboardMappings;

public class GUIJoystickElement extends GUIAbstractJoystickElement {

	private JoystickMappingFile joystick;
	private KeyboardMappings mapping;

	public GUIJoystickElement(InputState state, KeyboardMappings s,
	                          JoystickMappingFile joystick) {
		super(state);
		this.mapping = s;
		this.joystick = joystick;
	}

	public boolean checkForDuplicates() {
		if (!joystick.getButtonFor(mapping).isSet()) {
			return false;
		}
		for (KeyboardMappings m : KeyboardMappings.values()) {
			if (m != mapping
					&& joystick.getButtonFor(mapping).equals(
					joystick.getButtonFor(m))) {

				// duplicate key
				if (checkRelated(mapping.getContext(), m.getContext())) {
					return true;
				}
			}
			if (joystick.getButtonFor(mapping).equals(joystick.getLeftMouse())) {

				// duplicate key
				if (checkRelated(mapping.getContext(), m.getContext())) {
					return true;
				}
			}
			if (joystick.getButtonFor(mapping).equals(joystick.getRightMouse())) {

				// duplicate key
				if (checkRelated(mapping.getContext(), m.getContext())) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean hasDuplicate() {
		return checkForDuplicates();
	}

	@Override
	public String getDesc() {
		return mapping.getDescription();
	}

	@Override
	public void mapJoystickPressed(JoystickEvent e) {
		joystick.getMappings().put(mapping, JoystickMappingFile.getPressedButton());
	}

	@Override
	public void mapJoystickPressedNothing() {
		joystick.getMappings().put(mapping, new JoystickButtonMapping());
	}

	@Override
	public String getCurrentSettingString() {
		return joystick.getButtonFor(mapping).toString();
	}

	@Override
	public boolean isHighlighted() {
		//TODO highlight on duplicates
		return false;
	}

}
