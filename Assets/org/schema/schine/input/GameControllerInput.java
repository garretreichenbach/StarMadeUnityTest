package org.schema.schine.input;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GameControllerInput {
	public static final List<GameController> joysticks = new ObjectArrayList<>();
	public static GameController activeController;
	private boolean init;

	public void init() {
		
		joysticks.addAll(GLFWContollerUtil.getControllers());
		for(GameController c : joysticks) {
			if(activeController == null) {
				activeController = c;
			}
			System.err.println("[GAMECONTROLLER] Connected controller: "+c);
		}
		init = true;
	}

	public int getSize() {
			return joysticks.size();
	}

	public String getName(int i) {
		return joysticks.get(i).getName();
	}

	public void delesect() {
		activeController = null;
	}

	public void select(int i) {
		if (joysticks != null && i >= 0 && i < joysticks.size()) {
			activeController = joysticks.get(i);
		} else {
			System.err.println("[CONTROLLERINPUT] resetcontroller (tried " + i + ")");
			activeController = null;
		}
	}


	/**
	 * @return the activeController
	 */
	public GameController getActiveController() {
		return activeController;
	}

	/**
	 * @param activeController the activeController to set
	 */
	public static void setActiveController(GameController activeController) {
		GameControllerInput.activeController = activeController;
	}

	public boolean initialized() {
		return init;
	}
}
