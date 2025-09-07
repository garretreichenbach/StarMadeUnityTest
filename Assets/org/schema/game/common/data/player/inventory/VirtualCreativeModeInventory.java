package org.schema.game.common.data.player.inventory;

public class VirtualCreativeModeInventory extends CreativeModeInventory {


	public VirtualCreativeModeInventory(InventoryHolder state,
			long parameter) {
		super(state, parameter);
	}




	public static int getInventoryType() {
		return VIRTUEL_CREATIVE_MODE_INVENTORY;
	}

	
	@Override
	public boolean isLockedInventory() {
		return true;
	}

	@Override
	public int getLocalInventoryType() {
		return VIRTUEL_CREATIVE_MODE_INVENTORY;
	}
	
	

}
