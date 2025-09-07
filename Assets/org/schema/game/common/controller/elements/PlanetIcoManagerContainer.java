package org.schema.game.common.controller.elements;

import org.schema.game.common.controller.PlanetIco;
import org.schema.game.common.data.player.inventory.InventoryHolder;
import org.schema.schine.network.StateInterface;

public class PlanetIcoManagerContainer extends StationaryManagerContainer<PlanetIco> implements ShieldContainerInterface, LiftContainerInterface, InventoryHolder, DoorContainerInterface {

	public PlanetIcoManagerContainer(StateInterface state, PlanetIco station) {
		super(state, station);
	}

	

}
