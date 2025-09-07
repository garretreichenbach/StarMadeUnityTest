package org.schema.game.common.data.player.inventory;

import org.schema.game.common.controller.elements.InventoryMap;
import org.schema.schine.network.StateInterface;

import it.unimi.dsi.fastutil.ints.IntCollection;

public interface InventoryHolder {

	public InventoryMap getInventories();

	public Inventory getInventory(long pos);

	public NetworkInventoryInterface getInventoryNetworkObject();

	public String getName();

	public StateInterface getState();

	public String printInventories();

	public void sendInventoryModification(IntCollection  changedSlotsOthers,
			long parameter);

	public void sendInventoryModification(int slot, long parameter);

	public int getId();

	public void sendInventorySlotRemove(int slot, long parameter);

	public double getCapacityFor(Inventory inventory);

	public void volumeChanged(double volumeBefore, double volumeNow);

	public void sendInventoryErrorMessage(Object[] astr, Inventory inv);

}
