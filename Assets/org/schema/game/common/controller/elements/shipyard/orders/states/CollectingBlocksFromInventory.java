package org.schema.game.common.controller.elements.shipyard.orders.states;

import org.schema.game.common.controller.elements.shipyard.orders.ShipyardEntityState;
import org.schema.schine.ai.stateMachines.FSMException;

public class CollectingBlocksFromInventory extends ShipyardState{

	public CollectingBlocksFromInventory(ShipyardEntityState gObj) {
		super(gObj);
	}

	@Override
	public boolean onEnterS() {
				return false;
	}

	@Override
	public boolean onExit() {
				return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {
				return false;
	}

}
