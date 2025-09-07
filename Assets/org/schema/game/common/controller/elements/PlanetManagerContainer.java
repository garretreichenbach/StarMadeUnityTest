package org.schema.game.common.controller.elements;

import org.schema.game.common.controller.Planet;
import org.schema.game.common.data.player.inventory.InventoryHolder;
import org.schema.schine.network.StateInterface;

public class PlanetManagerContainer extends StationaryManagerContainer<Planet> implements ShieldContainerInterface, LiftContainerInterface, InventoryHolder, DoorContainerInterface {

	public PlanetManagerContainer(StateInterface state, Planet station) {
		super(state, station);
	}

	

}
