package org.schema.game.common.controller.elements;

import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.player.inventory.InventoryHolder;
import org.schema.schine.network.StateInterface;

public class SpaceStationManagerContainer extends StationaryManagerContainer<SpaceStation> implements ShieldContainerInterface, LiftContainerInterface, InventoryHolder, DoorContainerInterface {

	public SpaceStationManagerContainer(StateInterface state, SpaceStation station) {
		super(state, station);
	}

}
