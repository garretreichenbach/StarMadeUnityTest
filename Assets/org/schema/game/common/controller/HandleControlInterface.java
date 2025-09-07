package org.schema.game.common.controller;

import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.ControllerStateUnit;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyboardMappings;

public interface HandleControlInterface {
	/**
	 * executedduring main update (after key event)
	 */
	public void handleKeyPress(ControllerStateInterface unit, Timer timer);
	/**
	 * executed during input update
	 */
	public void handleKeyEvent(ControllerStateUnit unit, KeyboardMappings mapping, Timer timer);
}
