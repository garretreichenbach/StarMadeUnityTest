package org.schema.game.common.data.player.inventory;

import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.objects.remote.RemoteIntegerArray;

public class InventoryController {

	private PlayerState player;

	public InventoryController(PlayerState player) {
		this.player = player;
	}

	public void buy(short selectedElementClass, int quantity) {
		RemoteIntegerArray a = new RemoteIntegerArray(2,
				player.getNetworkObject());
		a.set(0, quantity);
		a.set(1, (int) selectedElementClass);
		player.getNetworkObject().buyBuffer.add(a);
	}

	public void fillInventory() {
		((GameServerState) player.getState()).getGameConfig().fillInventory(player);
//		int i = 0;
//		assert (player.getInventory(null).inventoryEmpty());
//		player.getInventory(null).put(i++,
//				ElementKeyMap.HULL_ID,
//				30, -1);
//		player.getInventory(null).put(i++,
//				ElementKeyMap.POWER_ID, 10, -1);
//		player.getInventory(null).put(i++,
//				ElementKeyMap.THRUSTER_ID, 8, -1);
//		player.getInventory(null).put(i++,
//				ElementKeyMap.WEAPON_CONTROLLER_ID, 1, -1);
//		player.getInventory(null).put(i++,
//				ElementKeyMap.WEAPON_ID, 15, -1);
//
//
//
//		i = 10;
//		player.getInventory(null).put(i++, ElementKeyMap.CORE_ID,
//				1, -1);
//		player.getInventory(null).put(i++, ElementKeyMap.SALVAGE_CONTROLLER_ID,
//				1, -1);
//		player.getInventory(null).put(i++, ElementKeyMap.SALVAGE_ID,
//				5, -1);
//		MetaObject weapon = MetaObjectManager.instantiate(MetaObjectManager.WEAPON, Weapon.LASER);
//		player.getInventory(null).put(i++, weapon);
//		MetaObject heal = MetaObjectManager.instantiate(MetaObjectManager.WEAPON, Weapon.HEAL);
//		player.getInventory(null).put(i++, heal);
//		MetaObject powerSupply = MetaObjectManager.instantiate(MetaObjectManager.WEAPON, Weapon.POWER_SUPPLY);
//		player.getInventory(null).put(i++, powerSupply);
//		MetaObject marker = MetaObjectManager.instantiate(MetaObjectManager.WEAPON, Weapon.MARKER);
//		player.getInventory(null).put(i++, marker);
//		MetaObject helmet = MetaObjectManager.instantiate(MetaObjectManager.HELMET, (short) -1);
//		player.getInventory(null).put(i++, helmet);
	}

	public void resetInventory() {
		player.getInventory(null).clear();
		fillInventory();
	}

	public void sell(short selectedElementClass, int quantity) {
		RemoteIntegerArray a = new RemoteIntegerArray(2,
				player.getNetworkObject());

		a.set(0, quantity);
		a.set(1, (int) selectedElementClass);
		player.getNetworkObject().sellBuffer.add(a);
	}

	public void delete(short selectedElementClass, int quantity, int slot) {
		RemoteIntegerArray a = new RemoteIntegerArray(3,
				player.getNetworkObject());

		a.set(0, quantity);
		a.set(1, (int) selectedElementClass);
		a.set(2, slot);
		player.getNetworkObject().deleteBuffer.add(a);

	}

}
