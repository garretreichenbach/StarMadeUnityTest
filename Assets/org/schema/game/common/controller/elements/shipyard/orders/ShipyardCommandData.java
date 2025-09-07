package org.schema.game.common.controller.elements.shipyard.orders;

import org.schema.game.common.controller.elements.shipyard.ShipyardCollectionManager;

/**
 * <Description>
 *
 * @author Garret Reichenbach
 * @version 1.0 - [05/06/2022]
 */
public abstract class ShipyardCommandData {

	private final ShipyardCollectionManager.ShipyardCommandType type;
	private final Object[] args;

	public ShipyardCommandData(ShipyardCollectionManager.ShipyardCommandType type, Object... args) {
		this.type = type;
		this.args = args;
	}

	public ShipyardCollectionManager.ShipyardCommandType getType() {
		return type;
	}

	public Object[] getArgs() {
		return args;
	}

	public abstract boolean isStarted(ShipyardCollectionManager collectionManager);

	public abstract boolean isFinished(ShipyardCollectionManager collectionManager);

	public abstract void onStart(ShipyardCollectionManager collectionManager);

	public abstract void onFinish(ShipyardCollectionManager collectionManager);
}
