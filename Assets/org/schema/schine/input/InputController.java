package org.schema.schine.input;

import java.util.List;

import org.schema.schine.common.JoystickAxisMapping;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.newgui.DialogInterface;

public interface InputController {

	
	public List<DialogInterface> getPlayerInputs();

	public boolean isChatActive();

	public void handleKeyEvent(KeyEventInterface e, Timer timer);


	public void handleLocalMouseInput();

	public boolean beforeInputUpdate();
	
	public BasicInputController getInputController();
	

	public boolean isJoystickOk();
	public double getJoystickAxis(JoystickAxisMapping map);


	public InputState getState();

	public void popupAlertTextMessage(String message);


	public void handleCharEvent(KeyEventInterface e);



}
