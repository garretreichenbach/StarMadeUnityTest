package org.schema.game.common.data.player.inventory;

public class PlayerInventory extends Inventory {

	public PlayerInventory(InventoryHolder state, long parameter) {
		super(state, parameter);
	}

	public static int getInventoryType() {
		return PLAYER_INVENTORY;
	}

	@Override
	public int getActiveSlotsMax() {
		return 10;
	}

	@Override
	public int getLocalInventoryType() {
		return PLAYER_INVENTORY;
	}


	@Override
	public String getCustomName() {
		return "";
	}

}
