package org.schema.game.common.data.player.inventory;

import java.util.Collection;

public class InventoryMultMod {
	public Collection<Integer> slots;
	public Inventory inventory;
	public long parameter = Long.MIN_VALUE;
	public InventorySlot[] receivedMods;

	public InventoryMultMod() {
		super();
	}

	public InventoryMultMod(Collection<Integer> slots, Inventory inventory, long parameter) {
		super();
		this.slots = slots;
		this.inventory = inventory;
		this.parameter = parameter;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[INV_MOD: " + slots + " on " + parameter + "; " + (inventory != null ? inventory.getInventoryHolder() : "nullInventory")+ "]";
	}

}
