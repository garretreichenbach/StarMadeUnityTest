package org.schema.game.server.controller.gameConfig;

public class InventoryStarterGearFactory {
	public final SlotType slotType;
	public final InventoryStarterGearExecuteInterface executor;
	public InventoryStarterGearFactory(SlotType slotType,
	                                   InventoryStarterGearExecuteInterface executor) {
		super();
		this.slotType = slotType;
		this.executor = executor;
	}

	public enum SlotType {
		INVENTORY,
		HOTBAR
	}

}
