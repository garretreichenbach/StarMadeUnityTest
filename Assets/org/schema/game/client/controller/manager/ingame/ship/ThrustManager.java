package org.schema.game.client.controller.manager.ingame.ship;

import org.schema.game.client.controller.PlayerThrustManagerInput;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.Ship;
import org.schema.schine.graphicsengine.camera.CameraMouseState;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.newgui.DialogInterface;
import org.schema.schine.input.KeyEventInterface;

public class ThrustManager extends AbstractControlManager {

	private PlayerThrustManagerInput gameMenu;

	
	private Ship selectedShip;
	
	public ThrustManager(GameClientState state) {
		super(state);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.manager.AbstractControlManager#handleKeyEvent()
	 */
	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		super.handleKeyEvent(e);

	}


	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.manager.AbstractControlManager#onSwitch(boolean)
	 */
	@Override
	public void onSwitch(boolean active) {
		boolean contains = false;
		if (active) {
			synchronized (getState().getController().getPlayerInputs()) {
				for (DialogInterface p : getState().getController().getPlayerInputs()) {
					if (p instanceof PlayerThrustManagerInput) {
						contains = true;
					}
				}
			}
			if (!contains) {
				if(selectedShip != null){
					gameMenu = new PlayerThrustManagerInput(getState(), selectedShip);
					getState().getController().getPlayerInputs().add(gameMenu);
				}
			}
		} else {
			synchronized (getState().getController().getPlayerInputs()) {
				for (int i = 0; i < getState().getController().getPlayerInputs().size(); i++) {
					DialogInterface p = getState().getController().getPlayerInputs().get(i);
					if (p instanceof PlayerThrustManagerInput) {

						getState().getController().getPlayerInputs().get(i).deactivate();
						break;
					}
				}
			}
		}

		super.onSwitch(active);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.manager.AbstractControlManager#update(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void update(Timer timer) {
		CameraMouseState.setGrabbed(false);
		super.update(timer);
		if(gameMenu != null){
			gameMenu.update(timer);
		}
	}

	public Ship getSelectedShip() {
		return selectedShip;
	}

	public void setSelectedShip(Ship selectedShip) {
		this.selectedShip = selectedShip;
	}

}
