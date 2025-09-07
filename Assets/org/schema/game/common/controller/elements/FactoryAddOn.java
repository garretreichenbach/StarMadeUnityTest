package org.schema.game.common.controller.elements;

import api.listener.fastevents.FactoryManufactureListener;
import api.listener.fastevents.FastListenerCommon;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.factory.FactoryCollectionManager;
import org.schema.game.common.controller.elements.factory.FactoryElementManager;
import org.schema.game.common.controller.elements.factory.FactoryProducerInterface;
import org.schema.game.common.controller.elements.factory.FactoryUnit;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.FactoryResource;
import org.schema.game.common.data.element.meta.RecipeInterface;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.Timer;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class FactoryAddOn {

	//	public static final long TIME_STEP = 5000;
	public final HashMap<Short, ManagerModuleCollection<FactoryUnit, FactoryCollectionManager, FactoryElementManager>> map = new HashMap<Short, ManagerModuleCollection<FactoryUnit, FactoryCollectionManager, FactoryElementManager>>();
	private final Object2ObjectOpenHashMap<Inventory, IntOpenHashSet> changedSet = new Object2ObjectOpenHashMap<Inventory, IntOpenHashSet>();
	private SegmentController segmentController;
	private boolean initialized;

	public static FactoryResource[] getInputType(RecipeInterface recipe, int i) {
		assert (recipe.getRecipeProduct() != null) : recipe;
		assert (recipe.getRecipeProduct()[i] != null) : recipe.getRecipeProduct();
		return recipe.getRecipeProduct()[i].getInputResource();
	}

	public static FactoryResource[] getOutputType(RecipeInterface recipe, int i) {
		return recipe.getRecipeProduct()[i].getOutputResource();
	}

	public static int getProductCount(RecipeInterface recipe) {
		return recipe.getRecipeProduct().length;
	}

	public static int getCount(FactoryResource r) {
		return r.count;
	}

	public static void produce(RecipeInterface recipe, int productChainIndex, Inventory ownInventory, FactoryProducerInterface iface, IntCollection changedOwnSet, GameServerState state) {
		
		final int prodLimit = ownInventory.getProductionLimit() > 0 ? ownInventory.getProductionLimit() : Integer.MAX_VALUE;
		boolean satisfied = true;
		int capability = iface.getFactoryCapability();
		
		for (FactoryResource s : getInputType(recipe, productChainIndex)) {
			int overallQuantity = ownInventory
					.getOverallQuantity(s.type);

			if (overallQuantity < getCount(s) * capability) {
				if (overallQuantity == 0 || overallQuantity < getCount(s)) {
					//nothing there. no need to go on
					satisfied = false;
					break;
				}
				//at least one production possible

				//how many of the original capability are possible
				capability = overallQuantity / getCount(s);

				assert (overallQuantity >= getCount(s) * capability);

			}
		}
		
		if (satisfied) {
			int maxAvail = 0;
			if(ownInventory.getProductionLimit() > 0){
				for (FactoryResource s : getOutputType(recipe, productChainIndex)) {
					maxAvail = Math.max(maxAvail, ownInventory.getOverallQuantity(s.type));
				}
				int diff = capability;
				
				//Lower capability to prodLimit 		
				if(diff > prodLimit){
					diff = prodLimit;
				}
				
				//Lower capability to remaining amount till prodLimit
				if(maxAvail + diff > prodLimit){
					diff = Math.max(0, prodLimit - maxAvail);
				}
				capability = diff;
			}
			
			if(capability > 0){
				/*
				 * Consume all necessary ingridients
				 */
				for (FactoryResource s : getInputType(recipe, productChainIndex)) {
					int overallQuantity = ownInventory.getOverallQuantity(s.type);
					ownInventory.deleteAllSlotsWithType(s.type, changedOwnSet);
					int slot;
					int count = overallQuantity - getCount(s) * capability;
	//					System.err.println("PUTTING BACK "+ElementKeyMap.toString(s.type)+": "+count);
	
					slot = ownInventory.incExistingOrNextFreeSlot(s.type, count);
					changedOwnSet.add(slot);

					//INSERTED CODE
					for (FactoryManufactureListener listener : FastListenerCommon.factoryManufactureListeners) {
						listener.onProduceItem(recipe, ownInventory, iface, s, count, changedOwnSet);
					}
					///
				}
	
				/*
				 * put all produced goods back into inventory
				 */
				for (FactoryResource s : getOutputType(recipe, productChainIndex)) {
					int count = getCount(s) * capability;
					int slot = ownInventory.incExistingOrNextFreeSlot(s.type, count);
					iface.getCurrentRecipe().producedGood(count, state);
					changedOwnSet.add(slot);
					//INSERTED CODE
					for (FactoryManufactureListener listener : FastListenerCommon.factoryManufactureListeners) {
						listener.onProduceItem(recipe, ownInventory, iface, s, count, changedOwnSet);
					}
					///
				}
			}
		}
	}

	public void initialize(final List<ManagerModule<?, ?, ?>> modules, final SegmentController s) {

		for (short fKey : ElementKeyMap.getFactorykeyset()) {
			ElementInformation info = ElementKeyMap.getInfo(fKey);
			assert (info.getFactory() != null);

			ManagerModuleCollection<FactoryUnit, FactoryCollectionManager, FactoryElementManager> managerModuleCollection =
					new ManagerModuleCollection<FactoryUnit, FactoryCollectionManager, FactoryElementManager>(
							new FactoryElementManager(s, info.getId(), info.getFactory().enhancer),
							info.getId(),
							info.getFactory().enhancer);

			modules.add(managerModuleCollection);
			map.put(info.getId(), managerModuleCollection);

		}
		//		System.err.println("[FACTORY] INITIALIZING FACTORY ADD ON "+ElementKeyMap.getFactorykeyset()+": "+map);

		this.segmentController = s;
		this.initialized = true;
	}

	public void update(Timer timer, boolean onServer) {
		assert (initialized);

		int steps = 0;
		int sentInventories = 0;
		int i = 0;
		long curTime = System.currentTimeMillis();

		for (ManagerModuleCollection<FactoryUnit, FactoryCollectionManager, FactoryElementManager> m : map.values()) {
			for (FactoryCollectionManager c : m.getCollectionManagers()) {
				long currentStep = segmentController.getState().getController().getServerRunningTime() / c.getBakeTime();
//					if(c.getControllerPos().equals(-24, 28, 11)){
//						System.err.println(segmentController.getState()+" BACK TIME: "+c.getBakeTime()+" : : : "+currentStep+" / "+c.lastStep+"; "+c);
//					}
				if (currentStep > c.lastStep) {

					c.manufractureStep(m.getElementManager(), changedSet);
					steps++;

					c.lastStep = currentStep;
				}

			}
			i++;

		}

		for (Entry<Inventory, IntOpenHashSet> a : changedSet.entrySet()) {
			if (!a.getValue().isEmpty()) {
				IntOpenHashSet copy = new IntOpenHashSet();
				copy.addAll(a.getValue());
				a.getKey().sendInventoryModification(copy);
				a.getValue().clear();
				i++;
				sentInventories++;
				//only do one update per frame to distribute load
				break;
			}
		}
		long took = System.currentTimeMillis() - curTime;
		if (took > 100) {
			System.err.println("[FACTORY] CALCULATION TOOK " + took + "MS with " + steps + " steps, and " + sentInventories + " inventories sent");
		}
	}

}
