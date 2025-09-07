package org.schema.game.common.controller.elements;

import org.schema.game.common.controller.FloatingRockManaged;
import org.schema.game.common.data.player.inventory.InventoryHolder;
import org.schema.schine.network.StateInterface;

public class FloatingRockManagerContainer extends StationaryManagerContainer<FloatingRockManaged> implements ShieldContainerInterface, LiftContainerInterface, InventoryHolder, DoorContainerInterface {
	public FloatingRockManagerContainer(StateInterface state, FloatingRockManaged asteroid) {
		super(state, asteroid);
	}
}