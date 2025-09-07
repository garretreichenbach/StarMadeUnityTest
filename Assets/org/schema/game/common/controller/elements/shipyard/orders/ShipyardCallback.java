package org.schema.game.common.controller.elements.shipyard.orders;

import org.schema.game.common.controller.elements.shipyard.ShipyardCollectionManager;

/**
 * <Description>
 *
 * @author Garret Reichenbach
 * @version 1.0 - [05/06/2022]
 */
public interface ShipyardCallback {

	void onFinished(ShipyardEntityState entityState, ShipyardCollectionManager collectionManager);
}
