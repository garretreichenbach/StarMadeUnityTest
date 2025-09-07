package org.schema.game.common.data.player.inventory;

import org.schema.schine.graphicsengine.core.Timer;

public abstract class ActiveInventory extends Inventory {

	private boolean activate;

	public ActiveInventory(InventoryHolder state, long parameter) {
		super(state, parameter);
	}

	public void activate() {
		this.activate = true;
	}

	public void deactivate() {
		this.activate = false;
	}

	public boolean isActivated() {
		return activate;
	}

	@Override
	public String toString() {
		return "ActiveInventory: " + inventoryMap.toString();
	}

	public abstract void updateLocal(Timer timer);

}
