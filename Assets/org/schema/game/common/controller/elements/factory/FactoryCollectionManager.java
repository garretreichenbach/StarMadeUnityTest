package org.schema.game.common.controller.elements.factory;

import api.config.BlockConfig;
import api.listener.fastevents.FactoryManufactureListener;
import api.listener.fastevents.FastListenerCommon;
import api.listener.fastevents.ProductionItemPullListener;
import api.element.recipe.CustomModRefinery;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.effects.RaisingIndication;
import org.schema.game.client.view.gui.shiphud.HudIndicatorOverlay;
import org.schema.game.client.view.gui.structurecontrol.GUIKeyValueEntry;
import org.schema.game.client.view.gui.structurecontrol.ModuleValueEntry;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ControlBlockElementCollectionManager;
import org.schema.game.common.controller.elements.FactoryAddOn;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.power.PowerAddOn;
import org.schema.game.common.controller.elements.power.PowerManagerInterface;
import org.schema.game.common.controller.elements.power.reactor.PowerConsumer;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.MetaObjectState;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.StatusEffectType;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.FactoryResource;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.element.meta.Recipe;
import org.schema.game.common.data.element.meta.RecipeInterface;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.inventory.InventoryHolder;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.util.FastCopyLongOpenHashSet;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;

import java.util.Map;

public class FactoryCollectionManager extends ControlBlockElementCollectionManager<FactoryUnit, FactoryCollectionManager, FactoryElementManager> implements FactoryProducerInterface, PowerConsumer {

	public static final float POWER_MULT = 500;
	public static final long DEFAULT_BAKE_TIME = 10000L;
	public static final long MICRO_BAKE_TIME = 2500L;
	public static final long CAPSULE_BAKE_TIME = 2500L;
	public static final long EXTRACTOR_BAKE_TIME = 2500L; //TODO config
    private final Vector3i posTmp = new Vector3i();
	private final ShortOpenHashSet needed = new ShortOpenHashSet();
	public long lastStep;
	private Vector3i absPos = new Vector3i();
	private int capability = 1;
	private RecipeInterface currentRecipe;
	private int currentRecipeId = -1;
	private long lastCheck;
	private float powered;

	public FactoryCollectionManager(short enhancerId, SegmentPiece element,
	                                SegmentController segController, FactoryElementManager em) {
		super(element, enhancerId, segController, em);
	}

	public void addCapability(int capability) {
		this.capability += capability;
	}
	@Override
	public boolean isUsingIntegrity() {
		return false;
	}
	public boolean consumePower(FactoryElementManager elementManager) {
		if (getSegmentController().isUsingPowerReactors()) {
			boolean canConsume = powered >= 0.999999999f;
			
			if (!getSegmentController().isOnServer() && !canConsume) {
				Transform t = new Transform();
				t.setIdentity();
				Vector3i p = getControllerElement().getAbsolutePos(absPos);
				t.origin.set(p.x - SegmentData.SEG_HALF, p.y - SegmentData.SEG_HALF, p.z - SegmentData.SEG_HALF);
				getSegmentController().getWorldTransform().transform(t.origin);
				RaisingIndication raisingIndication = new RaisingIndication(t, Lng.str("Insufficient Energy: %s%%", StringTools.formatPointZero(powered *100d)), 1f, 0.1f, 0.1f, 1f);
				raisingIndication.speed = 0.2f;
				raisingIndication.lifetime = 2.0f;
				HudIndicatorOverlay.toDrawTexts.add(raisingIndication);
			}
			
			return canConsume;
		} else {
			PowerAddOn powerManager = ((PowerManagerInterface) elementManager.getManagerContainer()).getPowerAddOn();
			boolean consumePowerInstantly = powerManager.consumePowerInstantly(capability * POWER_MULT);

			if (!getSegmentController().isOnServer() && !consumePowerInstantly) {
				Transform t = new Transform();
				t.setIdentity();
				Vector3i p = getControllerElement().getAbsolutePos(absPos);
				t.origin.set(p.x - SegmentData.SEG_HALF, p.y - SegmentData.SEG_HALF, p.z - SegmentData.SEG_HALF);
				getSegmentController().getWorldTransform().transform(t.origin);
				RaisingIndication raisingIndication = new RaisingIndication(t, Lng.str("Insufficient Energy (%d / %d)", (int) powerManager.getPower(), (int) (capability * POWER_MULT)), 1f, 0.1f, 0.1f, 1f);
				raisingIndication.speed = 0.2f;
				raisingIndication.lifetime = 1.0f;
				HudIndicatorOverlay.toDrawTexts.add(raisingIndication);

			}
			return consumePowerInstantly;
		}
	}

//	public boolean canConsumePower(FactoryElementManager elementManager) {
//		PowerAddOn powerManager = ((PowerManagerInterface) elementManager.getManagerContainer()).getPowerAddOn();
//		boolean consumePowerInstantly = powerManager.canConsumePowerInstantly(capability * POWER_MULT);
//
//		if (!getSegmentController().isOnServer() && !consumePowerInstantly) {
//			Transform t = new Transform();
//			t.setIdentity();
//			Vector3i p = getControllerElement().getAbsolutePos(absPos);
//			t.origin.set(p.x - SegmentData.SEG_HALF, p.y - SegmentData.SEG_HALF, p.z - SegmentData.SEG_HALF);
//			getSegmentController().getWorldTransform().transform(t.origin);
//			RaisingIndication raisingIndication = new RaisingIndication(t, Lng.str("Insufficient Energy (%d / %d)", (int) powerManager.getPower(), (int) (capability * POWER_MULT)), 1f, 0.1f, 0.1f, 1f);
//			raisingIndication.speed = 0.2f;
//			raisingIndication.lifetime = 1.0f;
//			HudIndicatorOverlay.toDrawTexts.add(raisingIndication);
//
//		}
//		return consumePowerInstantly;
//	}

	@Override
	public int getMargin() {
		return 0;
	}

	@Override
	protected Class<FactoryUnit> getType() {
		return FactoryUnit.class;
	}

	@Override
	public boolean needsUpdate() {
		return false;
	}

	@Override
	public FactoryUnit getInstance() {
		return new FactoryUnit();
	}

	@Override
	protected void onChangedCollection() {
		refreshEnhancerCapabiities();
	}

	@Override
	public GUIKeyValueEntry[] getGUICollectionStats() {
		return new GUIKeyValueEntry[]{
				new ModuleValueEntry(Lng.str("Efficiency"), capability),
		};
	}

	@Override
	public String getModuleName() {
		return "Factory System";
	}

	private void handleProduct(RecipeInterface recipe, LongOpenHashSet[] connectedToThisInvs, FactoryElementManager elementManager, int productChainIndex, final Inventory ownInventory, Map<Inventory, IntOpenHashSet> changedSet, IntOpenHashSet changedOwnSet) {

		final int prodLimit = ownInventory.getProductionLimit() > 0 ? ownInventory.getProductionLimit() : Integer.MAX_VALUE;

		//Inserted Code @...
		for (ProductionItemPullListener listener : FastListenerCommon.productionItemPullListeners) {
			listener.onPrePull(this, null);
		}
		///
		for(int i = 0; i < connectedToThisInvs.length; i++){
			LongOpenHashSet connectedToThis = connectedToThisInvs[i];
			if (connectedToThis != null) {
				for (long v : connectedToThis) {
	
					ElementCollection.getPosFromIndex(v, posTmp);
					Inventory pullFrom = ((InventoryHolder) ((ManagedSegmentController<?>) getSegmentController()).getManagerContainer())
							.getInventory(ElementCollection.getIndex(posTmp));
	
					/*
					 * collect material from all
					 * factories that are connected to this one
					 */
					if (pullFrom != null) {
						boolean satisfied = true;
						IntOpenHashSet changedSlotsOthers = changedSet.get(pullFrom);
						if (changedSlotsOthers == null) {
							changedSlotsOthers = new IntOpenHashSet();
							changedSet.put(pullFrom, changedSlotsOthers);
						}
	
						for (FactoryResource s : FactoryAddOn.getInputType(recipe, productChainIndex)) {
							//					if(!needed.contains(s.type)){
							//						continue;
							//					}
							int ownCount = ownInventory.getOverallQuantity(s.type);
							int wantedCount = FactoryAddOn.getCount(s) * Math.min(prodLimit, this.capability);
							if(ownCount < wantedCount){
								//we need moar stuff
								
								int count = wantedCount - ownCount;
								
								int overallQuantity = pullFrom.getOverallQuantity(s.type);
		
								
								if (overallQuantity < count) {
									//take from other chests what we can get
									count = overallQuantity;
								}
								boolean canPutIn = ownInventory.canPutIn(s.type, count);
								if (canPutIn && overallQuantity > 0) {
		
									pullFrom.deleteAllSlotsWithType(s.type, changedSlotsOthers);
		
									int slot = pullFrom.incExistingOrNextFreeSlotWithoutException(s.type, overallQuantity - count);
									if (slot >= 0) {
		
										changedSlotsOthers.add(slot);
		
										int oSlot = ownInventory.incExistingOrNextFreeSlotWithoutException(s.type, count);
		
										changedOwnSet.add(oSlot);
									} else {
										System.err.println("[ERROR][EXCEPTION] invalid pull state: " + getSegmentController() + "; " + ElementKeyMap.toString(s.type));
									}
		
								}else if(!canPutIn){
//									System.err.println("[FACTORY] "+getControllerElement()+" cant produce because full");
									ownInventory.getInventoryHolder().sendInventoryErrorMessage(Lng.astr("Can't produce! Inventory capacity reached"), ownInventory);
									satisfied = false;
								}
							}
						}
						if(!satisfied){
							getContainer().sendConnected(ownInventory.getParameterIndex(), false, ElementKeyMap.ACTIVAION_BLOCK_ID);
						}else{
							getContainer().sendConnected(ownInventory.getParameterIndex(), true, ElementKeyMap.ACTIVAION_BLOCK_ID);
						}
					} else {
						if (System.currentTimeMillis() - lastCheck > 1000) {
							SegmentPiece pointUnsave = getSegmentController().getSegmentBuffer().getPointUnsave(posTmp);//autorequest true previously
							if (pointUnsave != null && pointUnsave.getType() == ElementKeyMap.FACTORY_INPUT_ENH_ID) {
								//expected: input enhancer
							} else {
								System.err.println("[FACTORY] " + getSegmentController() + ": no inventory found at " + v + "; ControllerPos: " + getControllerPos() + "; NOW loaded supposed inventory position (Unsave Point): " + pointUnsave);
							}
							lastCheck = System.currentTimeMillis();
						}
					}
				}
			}
		}

		//Inserted Code @...
		for (ProductionItemPullListener listener : FastListenerCommon.productionItemPullListeners) {
			listener.onPostPull(this, null);
		}
		///

		/*
		 * produce actual products
		 */
		FactoryAddOn.produce(recipe, productChainIndex, ownInventory, this, changedOwnSet, (GameServerState) getSegmentController().getState());
	}

	private long modBakeTime(long in){
		return (long)getConfigManager().apply(StatusEffectType.FACTORY_BAKE_TIME_MULT, in);
	}
	
	public long getBakeTime() {
		return modBakeTime(getBakeTimeRaw());
	}
	private long getBakeTimeRaw() {
		if (getControllerElement() != null) {
			ManagerContainer<?> managerContainer = ((ManagedSegmentController<?>) getSegmentController()).getManagerContainer();

			CustomModRefinery modRefinery = BlockConfig.customModRefineries.get(getControllerElement().getType());
			if(modRefinery != null){
				return modRefinery.getBakeTime();
			}

			if (getControllerElement().getType() == ElementKeyMap.FACTORY_MICRO_ASSEMBLER_ID) {
				return MICRO_BAKE_TIME;
			} else if (getControllerElement().getType() == ElementKeyMap.FACTORY_CAPSULE_REFINERY_ID || getControllerElement().getType() == ElementKeyMap.FACTORY_CAPSULE_REFINERY_ADV_ID) {
				return CAPSULE_BAKE_TIME;
			} else if (ElementKeyMap.isMacroFactory(getControllerElement().getType())) {

				Inventory inventory = managerContainer.getInventory(getControllerPos());
				if (inventory != null) {
					if (ElementKeyMap.isValidType(inventory.getProduction())) {
						return (long) (ElementKeyMap.getInfo(inventory.getProduction()).getFactoryBakeTime() * 1000f);
					} else {
						int metaId = inventory.getMeta(FactoryElementManager.RECIPE_SLOT);
						if (metaId != -1) {
							MetaObject object = ((MetaObjectState) getSegmentController().getState()).getMetaObjectManager().getObject(metaId);

							if (object != null && object instanceof Recipe) {
								return (long) (((Recipe) object).getBakeTime() * 1000f);
							} else {
								return 1000;
							}
						} else {
							return DEFAULT_BAKE_TIME;
						}

					}
				} else {
					return DEFAULT_BAKE_TIME;
				}

			} else {
				return DEFAULT_BAKE_TIME;
			}

		} else {
			return DEFAULT_BAKE_TIME;
		}
	}
	private LongOpenHashSet[] connectedInventories = new LongOpenHashSet[ElementKeyMap.inventoryTypes.size()];
	//	private final HashSet<Integer> changedSlotsSelf = new HashSet<Integer>();
	public void manufractureStep(FactoryElementManager elementManager, Map<Inventory, IntOpenHashSet> changedSet) {

		if (getControllerElement() != null) {
			getControllerElement().refresh();
			if (!getControllerElement().isActive()) {
//				System.err.println("NOT ACTIVE");
				return;
			}
		} else {
			return;
		}

		if (!consumePower(elementManager)) {
//			System.err.println("NO POWER");
			return;
		}

		if (getSegmentController().isOnServer()) {
			Vector3i absolutePos = getControllerElement().getAbsolutePos(absPos);
			Short2ObjectOpenHashMap<FastCopyLongOpenHashSet> mmap = elementManager.getControlElementMap().getControllingMap().get(getControllerElement().getAbsoluteIndex());
			for(int i = 0; i < ElementKeyMap.inventoryTypes.size(); i++){
				short invType = ElementKeyMap.inventoryTypes.getShort(i);
				
				connectedInventories[i] = mmap != null ? mmap.get(invType) : null;
			}

			Inventory ownInventory = ((InventoryHolder) ((ManagedSegmentController<?>) getSegmentController()).getManagerContainer())
					.getInventory(ElementCollection.getIndex(absolutePos));
			if (ownInventory == null) {
			} else {

				currentRecipe = getElementManager().getCurrentRecipe(ownInventory, this);

				if (currentRecipe != null) {

					int productCount = FactoryAddOn.getProductCount(currentRecipe);
					//INSERTED CODE
					if(!FastListenerCommon.factoryManufactureListeners.isEmpty()) {
						boolean cont = true;
						for (FactoryManufactureListener factoryManufactureListener : FastListenerCommon.factoryManufactureListeners) {
							if(!factoryManufactureListener.onPreManufacture(this, ownInventory, connectedInventories)){
								cont = false;
							}
						}
						if(!cont){
							return;
						}
					}
					///
					for (int productChainIndex = 0; productChainIndex < productCount; productChainIndex++) {
						IntOpenHashSet changedOwnSet = changedSet.get(ownInventory);
						if (changedOwnSet == null) {
							changedOwnSet = new IntOpenHashSet();
							changedSet.put(ownInventory, changedOwnSet);
						}

						handleProduct(currentRecipe, connectedInventories, elementManager, productChainIndex, ownInventory, changedSet, changedOwnSet);
					}
				} else {
					//					System.err.println("NO RECIPE: "+getElementManager().getCurrentRecipe(ownInventory, this));
				}
				//				}
			}
			int i = 0;

		}
	}

	public void refreshEnhancerCapabiities() {
		capability = 1; //default
		for (FactoryUnit w : getElementCollections()) {
			w.refreshFactoryCapabilities(this);
		}
	}

	/**
	 * @return the currentRecipe
	 */
	@Override
	public RecipeInterface getCurrentRecipe() {
		return currentRecipe;
	}

	/**
	 * @param currentRecipe the currentRecipe to set
	 */
	public void setCurrentRecipe(RecipeInterface currentRecipe) {
		this.currentRecipe = currentRecipe;
	}

	@Override
	public int getFactoryCapability() {
		return capability;
	}

	/**
	 * @return the currentRecipeId
	 */
	public int getCurrentRecipeId() {
		return currentRecipeId;
	}

	/**
	 * @param currentRecipeId the currentRecipeId to set
	 */
	public void setCurrentRecipeId(int currentRecipeId) {
		this.currentRecipeId = currentRecipeId;
	}

	@Override
	public double getPowerConsumedPerSecondResting() {
		double pw = capability * FactoryElementManager.REACTOR_POWER_CONSUMPTION_PER_BLOCK_RESTING;
		return getConfigManager().apply(StatusEffectType.FACTORIES_POWER_TOPOFF_RATE, pw);
	}

	@Override
	public double getPowerConsumedPerSecondCharging() {
		double pw = capability * FactoryElementManager.REACTOR_POWER_CONSUMPTION_PER_BLOCK_CHARGING;
		return getConfigManager().apply(StatusEffectType.FACTORIES_POWER_CHARGE_RATE, pw);
	}

	@Override
	public boolean isPowerCharging(long curTime) {
		return getControllerElement().isActive();
	}

	@Override
	public void setPowered(float powered) {
		this.powered = powered;
	}

	@Override
	public float getPowered() {
		return powered;
	}

	@Override
	public PowerConsumerCategory getPowerConsumerCategory() {
		return PowerConsumerCategory.FACTORIES;
	}

	@Override
	public void reloadFromReactor(double secTime, Timer timer, float tickTime, boolean powerCharging, float poweredResting) {
		
	}

	@Override
	public boolean isPowerConsumerActive() {
		return true;
	}
	@Override
	public void dischargeFully() {
	}
}
