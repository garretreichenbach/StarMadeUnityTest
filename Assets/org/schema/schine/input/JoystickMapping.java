package org.schema.schine.input;

import org.schema.schine.graphicsengine.core.Controller;

public abstract class JoystickMapping {

	public abstract boolean isDown();

	public boolean ok() {
		return Controller.getControllerInput().getActiveController() != null;
	}

	public GameController get() {
		return Controller.getControllerInput().getActiveController();
	}

	public abstract boolean isSet();

}
