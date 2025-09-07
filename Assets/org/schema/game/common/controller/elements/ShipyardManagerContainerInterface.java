package org.schema.game.common.controller.elements;

import org.schema.game.common.controller.elements.shipyard.ShipyardCollectionManager;
import org.schema.game.common.controller.elements.shipyard.ShipyardElementManager;
import org.schema.game.common.controller.elements.shipyard.ShipyardUnit;

public interface ShipyardManagerContainerInterface {
	public ManagerModuleCollection<ShipyardUnit, ShipyardCollectionManager, ShipyardElementManager> getShipyard();
}
