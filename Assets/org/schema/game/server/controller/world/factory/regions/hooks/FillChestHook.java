package org.schema.game.server.controller.world.factory.regions.hooks;

import java.util.Iterator;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.Logbook;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.element.meta.MetaObjectManager;
import org.schema.game.common.data.element.meta.MetaObjectManager.MetaObjectType;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.inventory.NoSlotFreeException;
import org.schema.game.common.data.world.Universe;
import org.schema.game.server.controller.world.factory.regions.TresureRegion;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class FillChestHook extends RegionHook<TresureRegion> {

	@Override
	public void execute() {

		float countMul = ServerConfig.CHEST_LOOT_COUNT_MULTIPLIER.getFloat();
		float stackMul = ServerConfig.CHEST_LOOT_STACK_MULTIPLIER.getFloat();

		System.err.println("[SERVER] EXECUTING REGION HOOK: " + this + ": " + region.controllerPosition);

		SegmentController segmentController = onCreatedSegment.getSegmentController();
		GameServerState state = (GameServerState) segmentController.getState();

		ManagerContainer<?> managerContainer = ((ManagedSegmentController<?>) segmentController).getManagerContainer();

		Inventory inventory = managerContainer.getInventory(region.controllerPosition);
		if (inventory != null) {
			try {
				IntOpenHashSet l = new IntOpenHashSet();
				if (countMul <= 0 || stackMul <= 0) {

				} else {

					int slots = (int) (Universe.getRandom().nextInt(45) * countMul);
					slots = Math.min(slots, 45);

					for (int i = 0; i < slots; i++) {
						Iterator<Short> iterator = ElementKeyMap.keySet.iterator();
						int rInt = Universe.getRandom().nextInt(ElementKeyMap.typeList().length);

						short type = ElementKeyMap.typeList()[rInt];
						ElementInformation info = ElementKeyMap.getInfo(type);
						if (!info.isShoppable()) {
							continue;
						}
						

						int amount = (int) ((Universe.getRandom().nextInt(1000) + 100) * stackMul);
						System.err.println("putting into CHEST: " + ElementKeyMap.getInfo(type).getName() + " #" + amount);
						l.add(inventory.putNextFreeSlot(type, amount, -1));
					}
				}
				if (Universe.getRandom().nextInt(3) == 0) {
					MetaObject logbook = MetaObjectManager
							.instantiate(MetaObjectType.LOG_BOOK, (short) -1, true);
					((Logbook) logbook).setTxt((Logbook.getRandomEntry(state)));

					int slot = inventory.getFreeSlot();
					inventory.put(slot, logbook);
					l.add(slot);
				}
//				if(Universe.getRandom().nextInt(8) == 0){
//
//					int recipes = Universe.getRandom().nextInt(5)+1;
//
//					for(int i = 0; i < recipes; i++){
//
//						int rInt = Universe.getRandom().nextInt(ElementKeyMap.typeList().length);
//						short type = ElementKeyMap.typeList()[rInt];
//						ElementInformation info = ElementKeyMap.getInfo(type);
//						if(info.isShoppable()){
//							if(info.isLeveled() && info.getLevel().getLevel() > 1){
//								continue;
//							}
//							MetaObject recipe = RecipeCreator.getRecipeFor(type);
//
//							int slot = inventory.getFreeSlot();
//							inventory.put(slot, recipe);
//							l.add(slot);
//						}
//					}
//				}

				if (l.size() > 0) {
					inventory.sendInventoryModification(l);
				}

			} catch (NoSlotFreeException e) {
			}
//			catch (InvalidFactoryParameterException e) {
//				e.printStackTrace();
//			}
		} else {
			System.err.println("[REGIONHOOK] Exception: Inventory not found at: " + region.controllerPosition);
		}
	}

}
