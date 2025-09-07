package org.schema.schine.input;

public class JoystickButtonMapping extends JoystickMapping {

	public int buttonId = -1;
	public String buttonName = "none";

	@Override
	public boolean isDown() {
		if (ok() && buttonId >= 0 && buttonId < JoystickMappingFile.getButtonCount()) {
			return JoystickMappingFile.isButtonPressed(buttonId);
		}
		if (ok() && buttonId >= JoystickMappingFile.getButtonCount()) {
			System.err.println("[JOYSTICK] WARNING: button is mapped to a invalid value");
		}
		return false;

	}

	@Override
	public boolean isSet() {
		return buttonId != -1;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return buttonId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
				if (obj != null && obj instanceof JoystickButtonMapping) {
			return buttonId == ((JoystickButtonMapping) obj).buttonId;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return buttonName;
	}
}
