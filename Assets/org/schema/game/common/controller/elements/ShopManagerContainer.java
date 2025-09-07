package org.schema.game.common.controller.elements;

import org.schema.game.common.controller.ShopSpaceStation;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.schine.network.StateInterface;

public class ShopManagerContainer extends StationaryManagerContainer<ShopSpaceStation> {

	public ShopManagerContainer(StateInterface state, ShopSpaceStation station) {
		super(state, station);
	}

	@Override
	public double getCapacityFor(Inventory inventory) {
		return getSegmentController().getCapacityFor(inventory);
	}
}
