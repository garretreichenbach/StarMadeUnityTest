package org.schema.game.server.controller.gameConfig;

import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.server.controller.gameConfig.InventoryStarterGearFactory.SlotType;

public interface InventoryStarterGearExecuteInterface {
	public void execute(SlotType slotType, int hotbarSlot, int inevntorySlot, Inventory inventory, PlayerState player);
}
