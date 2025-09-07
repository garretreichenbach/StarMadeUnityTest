package org.schema.game.client.controller.manager.ingame;


import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.gui.chat.ChatPanel;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.Keyboard;

public class ChatControlManager extends AbstractControlManager {

	//	private static CharsetDecoder decoder = Charset.forName("US-ASCII").newDecoder();

	public ChatControlManager(GameClientState state) {
		super(state);

	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
//		getState().getChat().handleKeyEvent();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.manager.AbstractControlManager#setActive(boolean)
	 */
	@Override
	public void setActive(boolean active) {
		super.setActive(active);
		Keyboard.enableRepeatEvents(active);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.manager.AbstractControlManager#onSwitch(boolean)
	 */
	@Override
	public void onSwitch(boolean active) {
//		try{
//			throw new NullPointerException();
//		}catch(Exception e){
//			e.printStackTrace();
//		}
		if (!active) {
//			System.err.println("################SETTING LAST SELECTED: "+getState().currentActiveField);
			getState().getController().getInputController().setLastSelectedInput(getState().getController().getInputController().getCurrentActiveField());
			getState().getController().getInputController().setCurrentActiveField(null);
		}

		if (active) {
			ChatPanel.flagActivate = true;
		}

		getState().getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().suspend(active);
		getState().getGlobalGameControlManager().getIngameControlManager().getFreeRoamController().suspend(active);
		getState().getGlobalGameControlManager().getIngameControlManager().getAutoRoamController().suspend(active);
		super.onSwitch(active);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.manager.AbstractControlManager#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer) {
		super.update(timer);
		CameraMouseState.setGrabbed(false);
	}

}
