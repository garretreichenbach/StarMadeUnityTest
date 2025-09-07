package org.schema.game.common.controller.elements.factory;

import api.config.BlockConfig;
import api.element.recipe.CustomModRefinery;
import org.schema.common.config.ConfigurationElement;
import org.schema.game.client.view.gui.structurecontrol.ControllerManagerGUI;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.TimedUpdateInterface;
import org.schema.game.common.controller.elements.UsableControllableElementManager;
import org.schema.game.common.data.MetaObjectState;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.MetaObjectManager.MetaObjectType;
import org.schema.game.common.data.element.meta.Recipe;
import org.schema.game.common.data.element.meta.RecipeInterface;
import org.schema.game.common.data.player.ControllerStateInterface;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;

import java.io.IOException;

public class FactoryElementManager extends UsableControllableElementManager<FactoryUnit, FactoryCollectionManager, FactoryElementManager> implements CargoCapacityElementManagerInterface, TimedUpdateInterface {

	@ConfigurationElement(name = "ReactorPowerConsumptionPerBlockResting")
	public static float REACTOR_POWER_CONSUMPTION_PER_BLOCK_RESTING = 10f;
	
	@ConfigurationElement(name = "ReactorPowerConsumptionPerBlockCharging")
	public static float REACTOR_POWER_CONSUMPTION_PER_BLOCK_CHARGING = 50f;

	@ConfigurationElement(name = "ExtractorsEnabledOnHomebase")
	public static boolean ENABLE_EXTRACTORS_ON_HOMEBASE = false;
	
	static final int RECIPE_SLOT = 0;
	private static final long TIME_STEP = 10000;
	private final short enhancerId;
	private final short facId;
	private long lastExecution;
	private long nextExecution;

	public FactoryElementManager(SegmentController segController, short facId, short enhancerId) {
		super(facId, enhancerId, segController);
		this.facId = facId;
		this.enhancerId = enhancerId;
	}

	/**
	 * @return the enhancerId
	 */
	public short getEnhancerId() {
		return enhancerId;
	}

	/**
	 * @return the facId
	 */
	public short getFacId() {
		return facId;
	}

	@Override
	public long getLastExecution() {
		return lastExecution;
	}

	@Override
	public long getNextExecution() {
		return nextExecution;
	}

	@Override
	public long getTimeStep() {
		return TIME_STEP;
	}

	@Override
	public void update(Timer timer) throws IOException, InterruptedException {
		//		for(ControlBlockElementCollectionManager<FactoryUnit> f : getCollectionManagers()){
		//			((FactoryCollectionManager)f).manufractureStep(this);
		//		}

		//		lastExecution = System.currentTimeMillis();
		//		nextExecution = lastExecution + getTimeStep();
	}

	@Override
	public ControllerManagerGUI getGUIUnitValues(FactoryUnit firingUnit,
	                                             FactoryCollectionManager col,
	                                             ControlBlockElementCollectionManager<?, ?, ?> supportCol,
	                                             ControlBlockElementCollectionManager<?, ?, ?> effectCol) {
				return null;
	}

	@Override
	protected String getTag() {
		return "factory";
	}

	@Override
	public FactoryCollectionManager getNewCollectionManager(
			SegmentPiece position, Class<FactoryCollectionManager> clazz) {
		return new FactoryCollectionManager(enhancerId, position, getSegmentController(), this);
	}

	@Override
	public String getManagerName() {
		return Lng.str("Factory System Collective");
	}


	@Override
	public void handle(ControllerStateInterface unit, Timer timer) {

	}

	public RecipeInterface getCurrentRecipe(Inventory ownInventory,
	                                        FactoryCollectionManager factoryCollectionManager) {

		if (controllerId == ElementKeyMap.FACTORY_MICRO_ASSEMBLER_ID) {
			return ElementKeyMap.microAssemblerRecipe;
		} else if (controllerId == ElementKeyMap.FACTORY_BLOCK_RECYCLER_ID) {
			return ElementKeyMap.recyclerRecipe;
		} else if (controllerId == ElementKeyMap.FACTORY_CAPSULE_REFINERY_ID) {
			return ElementKeyMap.capsuleRecipe;
		} else if (controllerId == ElementKeyMap.FACTORY_CAPSULE_REFINERY_ADV_ID){
			return ElementKeyMap.advCapsuleRecipe;
		}
		//INSERTED CODE
		// Use mod recipe for custom refineries
		CustomModRefinery modRefinery = BlockConfig.customModRefineries.get(controllerId);
		if(modRefinery != null){
			return modRefinery.getRecipe();
		}
		///

		short production = ownInventory.getProduction();

		if (ElementKeyMap.isValidType(production)) {
			return ElementKeyMap.getInfo(production).getProductionRecipe();
		} else {

			short type = ownInventory.getType(RECIPE_SLOT);

			if (type == MetaObjectType.RECIPE.type) {
				int metaId = ownInventory.getMeta(RECIPE_SLOT);
				if (factoryCollectionManager.getCurrentRecipe() == null || metaId != factoryCollectionManager.getCurrentRecipeId()) {
					factoryCollectionManager.setCurrentRecipe((Recipe) ((MetaObjectState) getSegmentController().getState()).getMetaObjectManager().getObject(metaId));
					factoryCollectionManager.setCurrentRecipeId(metaId);
				}
			} else {
				factoryCollectionManager.setCurrentRecipe(null);
			}
			return factoryCollectionManager.getCurrentRecipe();
		}
	}
	public boolean isUsingRegisteredActivation() {
		return false;
	}
}
