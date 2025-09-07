package org.schema.game.client.controller.manager.ingame.ship;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.manager.AbstractControlManager;
import org.schema.game.client.controller.manager.ingame.EditSegmentInterface;
import org.schema.game.client.controller.manager.ingame.SegmentBuildController;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.EditableSendableSegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.SegmentPiece;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.input.KeyEventInterface;
import org.schema.schine.input.KeyboardMappings;

public class ShipControllerManager extends AbstractControlManager implements EditSegmentInterface {

	private ShipExternalFlightController shipExternalFlightController;

	private SegmentBuildController shipBuildController;

	public ShipControllerManager(GameClientState state) {
		super(state);
		initialize();
	}

	@Override
	public Vector3i getCore() {
		return new Vector3i(Ship.core);
	}

	@Override
	public SegmentPiece getEntered() {
		return getState().getGlobalGameControlManager().getIngameControlManager()
				.getPlayerGameControlManager().getPlayerIntercationManager().getInShipControlManager().getEntered();
	}

	@Override
	public EditableSendableSegmentController getSegmentController() {
		return getShip();
	}

	/**
	 * @return the shipBuildController
	 */
	public SegmentBuildController getSegmentBuildController() {
		return shipBuildController;
	}

	/**
	 * @param shipBuildController the shipBuildController to set
	 */
	public void setSegmentBuildController(SegmentBuildController shipBuildController) {
		this.shipBuildController = shipBuildController;
	}

	/**
	 * @return the ship
	 */
	public Ship getShip() {
		return getState().getShip();
	}

	/**
	 * @return the shipExternalFlightController
	 */
	public ShipExternalFlightController getShipExternalFlightController() {
		return shipExternalFlightController;
	}

	/**
	 * @param shipExternalFlightController the shipExternalFlightController to set
	 */
	public void setShipExternalFlightController(
			ShipExternalFlightController shipExternalFlightController) {
		this.shipExternalFlightController = shipExternalFlightController;
	}

	@Override
	public void handleKeyEvent(KeyEventInterface e) {
		super.handleKeyEvent(e);

			if (e.isTriggered(KeyboardMappings.CHANGE_SHIP_MODE)) {
				switchModes();
			}
	}


	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.manager.AbstractControlManager#onSwitch()
	 */
	@Override
	public void onSwitch(boolean active) {

		if (active) {
			if (!shipBuildController.isActive() && !shipExternalFlightController.isActive()) {
				shipBuildController.setActive(true);
			}
		} else {
		}
		super.onSwitch(active);
	}

	@Override
	public void update(Timer timer) {

		if (getShip() == null) {
			return;
		}

		super.update(timer);

	}

	public void initialize() {

		shipBuildController = new SegmentBuildController(getState(), this);
		getControlManagers().add(shipBuildController);

		shipExternalFlightController = new ShipExternalFlightController(this);
		getControlManagers().add(shipExternalFlightController);

	}

	public void switchModes() {
		boolean s = shipBuildController.isActive();
		//search for next available controller
		shipExternalFlightController.setActive(s);
		shipBuildController.setActive(!s);
		getState().getWorldDrawer().flagJustEntered(shipBuildController.getSegmentController());

	}

}
