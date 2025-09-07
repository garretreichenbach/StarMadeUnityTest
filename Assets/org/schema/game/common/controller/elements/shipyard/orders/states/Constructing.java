package org.schema.game.common.controller.elements.shipyard.orders.states;

import api.listener.fastevents.FastListenerCommon;
import api.listener.fastevents.ProductionItemPullListener;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.LogUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.shipyard.ShipyardCollectionManager;
import org.schema.game.common.controller.elements.shipyard.ShipyardElementManager;
import org.schema.game.common.controller.elements.shipyard.orders.ShipyardEntityState;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.VirtualBlueprintMetaItem;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.inventory.InventoryChangeMap;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.game.network.commands.gamerequests.EntityRequest;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.common.language.Lng;

import java.util.List;

public class Constructing extends ShipyardState {

	public boolean testDesign;
	int tickCounter;
	private VirtualBlueprintMetaItem currentDesign;
	private SegmentController currentDocked;
	private long currentFill;
	private Ship newShip;
	private final List<Ship> ships = new ObjectArrayList<Ship>();
	private long lastInventoryFilterStep;
	private long spawnedShip;

	private int lastSpawnedId;

	private boolean checkedName;

	private boolean forceRequest;

	public Constructing(ShipyardEntityState gObj) {
		super(gObj);
	}


	@Override
	public boolean onEnterS() {
		forceRequest = false;
		currentDesign = getEntityState().getCurrentDesign();
		currentDocked = getEntityState().getCurrentDocked();
		tickCounter = 0;
		spawnedShip = 0;
		if(!isLoadedFromTag()) {
			//if loaded we use the map of existing materials we got from tag
			getEntityState().currentMapTo.resetAll();
		} else {
//			System.err.println("LOADED MAP "+getEntityState().currentMapTo.getTotalAmount()+"; "+getEntityState().currentMapTo);
		}
		ships.clear();
		newShip = null;

		getEntityState().currentMapFrom.resetAll();

		if(currentDocked != null && currentDocked.isVirtualBlueprint()) {
			currentDocked.railController.fillElementCountMapRecursive(getEntityState().currentMapFrom);
		} else {
//			assert(false);
		}
		currentFill = getEntityState().currentMapTo.getTotalAmount();
		checkedName = false;
		if(ServerConfig.BLUEPRINTS_USE_COMPONENTS.isOn()) {
			ElementCountMap resources = getEntityState().currentMapFrom.calculateComponents();
			getEntityState().currentMapFrom.resetAll();
			getEntityState().currentMapFrom = resources;
			currentFill = getEntityState().currentMapTo.getTotalAmount();
		}
		return false;
	}

	@Override
	public boolean onExit() {
		return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {

		if(!checkedName) {


			checkedName = true;
			boolean existsEntity = ((GameServerState) getEntityState().getState()).existsEntity(EntityType.SHIP, getEntityState().currentName + (getEntityState().spawnDesignWithoutBlocks ? "_SY_TEST" : ""));

			LogUtil.sy().fine(getEntityState().getSegmentController() + " " + getEntityState() + " " + getClass().getSimpleName() + ": Checking name exists: " + existsEntity);
			if(existsEntity) {
				getEntityState().sendShipyardErrorToClient(Lng.str("Construction interrupted!\nOriginal Ship name already exists!"));
				stateTransition(Transition.SY_ERROR);
				return false;
			}


		}
//		if(!checkedOverlapping){
//			checkedOverlapping = true;
//			boolean checkSectorCollisionWithChildShape = currentDocked.getCollisionChecker().checkSectorCollisionWithChildShape();
//			
//			if(checkSectorCollisionWithChildShape){
//				getEntityState().sendShipyardErrorToClient(Lng.str("Cannot construct! Design is overlapping with another structure!"));
//				stateTransition(Transition.SY_ERROR);
//				return false;
//			}
//			
//		}
		if(spawnedShip > 0) {

			if(getEntityState().getState().getLocalAndRemoteObjectContainer().getLocalObjects().containsKey(lastSpawnedId) && (System.currentTimeMillis() - spawnedShip > 5000 || (getEntityState().getCurrentDocked() != null && getEntityState().getCurrentDocked().getId() == lastSpawnedId))) {


				SegmentController spawned = (SegmentController) getEntityState().getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(lastSpawnedId);
				SegmentPiece pointUnsave = spawned.getSegmentBuffer().getPointUnsave(Ship.core);//autorequest true previously

				System.err.println("[SERVER][SHIPYARD][CONSTRUCTING] construction done. Core: " + pointUnsave);
				LogUtil.sy().fine(getEntityState().getSegmentController() + " " + getEntityState() + " " + getClass().getSimpleName() + ": construction: waiting for construction DONE: CORE: " + pointUnsave);

				if(pointUnsave != null) {
					if(pointUnsave.getType() != ElementKeyMap.CORE_ID) {
						throw new IllegalArgumentException("No Core At Contructed Ship: " + pointUnsave);
					}

					stateTransition(Transition.SY_SPAWN_DONE);
				} else {
					if(!forceRequest) {
						LogUtil.sy().fine(getEntityState().getSegmentController() + " " + getEntityState() + " " + getClass().getSimpleName() + ": construction: waiting for construction Force request of 0 0 0: " + pointUnsave);

						spawned.getSegmentProvider().enqueueHightPrio(0, 0, 0, true);
						forceRequest = true;
					} else {
						LogUtil.sy().fine(getEntityState().getSegmentController() + " " + getEntityState() + " " + getClass().getSimpleName() + ": construction: waiting for construction WAITING FOR SPAWN: " + pointUnsave);
					}
				}
			} else {
				LogUtil.sy().fine(getEntityState().getSegmentController() + " " + getEntityState() + " " + getClass().getSimpleName() + ": construction: waiting for construction to be done");
				System.err.println("[SERVER][SHIPYARD][CONSTRUCTING] waiting for constructed ship to be loaded");
			}
		} else if(newShip != null && getEntityState().getCurrentDocked() == null) {

			boolean b = getEntityState().getShipyardCollectionManager().createDockingRelation(newShip, false);

			LogUtil.sy().fine(getEntityState().getSegmentController() + " " + getEntityState() + " " + getClass().getSimpleName() + ": construction: ship spawned " + newShip + ": creating docking relation: " + b);


			if(b) {

				getEntityState().getShipyardCollectionManager().setCurrentDesign(-1);

				for(Ship n : ships) {
					n.initialize();
					((GameServerState) getEntityState().getState()).getController().getSynchController().addNewSynchronizedObjectQueued(n);
				}


				getEntityState().getShipyardCollectionManager().sendShipyardStateToClient();

				getEntityState().currentMapTo.resetAll();
				getEntityState().currentMapFrom.resetAll();


				lastSpawnedId = newShip.getId();


//				if(newShip.getCreatorThread() != null){
//					newShip.getSegmentBuffer().getPointUnsave(Ship.core);//autorequest true previously
//				}

				newShip = null;


				ships.clear();
				spawnedShip = System.currentTimeMillis();

				LogUtil.sy().fine(getEntityState().getSegmentController() + " " + getEntityState() + " " + getClass().getSimpleName() + ": construction: ship successfully spawned and docked");

			} else {
				LogUtil.sy().fine(getEntityState().getSegmentController() + " " + getEntityState() + " " + getClass().getSimpleName() + ": construction: unable to dock");

				getEntityState().sendShipyardErrorToClient(Lng.str("Unable to dock ship to shipyard!"));
				stateTransition(Transition.SY_ERROR);
			}
			newShip = null;
			if(getEntityState().spawnDesignWithoutBlocks) {
				getEntityState().lastConstructedDesign = currentDesign;
			}
			currentDesign = null;


		} else if(newShip == null && currentDesign != null && currentDesign == getEntityState().getCurrentDesign() && currentDocked != null && currentDocked.isVirtualBlueprint() && currentDocked == getEntityState().getCurrentDocked()) {

			LogUtil.sy().fine(getEntityState().getSegmentController() + " " + getEntityState() + " " + getClass().getSimpleName() + ": construction: spawning new ship");

			if(getEntityState().spawnDesignWithoutBlocks || getEntityState().currentMapFrom.equals(getEntityState().currentMapTo) || getEntityState().getShipyardCollectionManager().isInstantBuild()) {

				LogUtil.sy().fine(getEntityState().getSegmentController() + " " + getEntityState() + " " + getClass().getSimpleName() + ": construction: spawning new ship: blocks all available");

				if(currentDocked.isFullyLoadedWithDock()) {
					assert (getEntityState().currentName != null);
					String fileName = EntityRequest.convertShipEntityName(getEntityState().currentName);

					if(EntityRequest.existsIdentifierWOExc(getEntityState().getState(), fileName)) {
						getEntityState().sendShipyardErrorToClient(Lng.str("A ship with this name already exists!"));
						stateTransition(Transition.SY_ERROR);
						assert (false);
					} else {

						SegmentPiece pointUnsave = getEntityState().getCurrentDocked().getSegmentBuffer().getPointUnsave(Ship.core);//autorequest true previously
						if(pointUnsave != null) {
							System.err.println("[SERVER][SHIPYARD][CONSTRUCING] core of design Design : " + pointUnsave);

							if(pointUnsave.getType() != ElementKeyMap.CORE_ID) {
								throw new IllegalArgumentException("No Core At Design: " + pointUnsave);
							}

							getEntityState().unloadCurrentDockedVolatile();

							Transform tr = new Transform();
							tr.setIdentity();

							System.err.println("[SERVER] CONSTRUCTION FINISHED: SPAWNING FILE: " + fileName);

							ships.clear();
							String name = getEntityState().currentName + (getEntityState().spawnDesignWithoutBlocks ? "_SY_TEST" : "");
							System.err.println("[SERVER] CONSTRUCTION FINISHED: SPAWNING NAME: " + name);


							int factionId = getEntityState().getShipyardCollectionManager().isPublicException(getEntityState().lastOrderFactionId) ? getEntityState().lastOrderFactionId : getEntityState().getSegmentController().getFactionId();


							newShip = ((Ship) currentDocked).copy(name, currentDocked.getSectorId(), tr, Lng.str("SHIPYARD_") + getEntityState().getSegmentController().getUniqueIdentifier(), factionId, ships, new EntityCopyData());
							System.err.println("[SERVER][SHIPYARD] copy finished!");

							//replace segment buffer, so it is not referenced twice
							currentDocked.setSegmentBuffer(new SegmentBufferManager(currentDocked));
						}
					}
				}

			} else if(newShip == null) {

				if(currentDocked.isFullyLoadedWithDock()) {
					LogUtil.sy().fine(getEntityState().getSegmentController() + " " + getEntityState() + " " + getClass().getSimpleName() + ": construction: spawning new ship: aquiring blocks start step: " + currentFill + " / " + currentDocked.getTotalElements() + " blocks");
					int ticksDone = getTicksDone();
					tickCounter += ticksDone;
					IntOpenHashSet mod = new IntOpenHashSet();
					Inventory inventory = getEntityState().getInventory();
					while(tickCounter > ShipyardElementManager.CONSTRUCTION_TICK_IN_SECONDS) {

						//					System.err.println("TICK_COUNTER "+tickCounter+" / "+ShipyardElementManager.CONSTRUCTION_TICK_IN_SECONDS);
						int toTake = Math.max(1, ShipyardElementManager.CONSTRUCTION_BLOCKS_TAKEN_PER_TICK);

						if(getEntityState().lastDeconstructedBlockCount > toTake && getEntityState().isInRepair) {
							toTake = getEntityState().lastDeconstructedBlockCount;
						}
						getEntityState().lastDeconstructedBlockCount = 0;

						for(short type : ElementKeyMap.keySet) {
							if(getEntityState().currentMapTo.get(type) < getEntityState().currentMapFrom.get(type)) {

								short invType = type;
								if(ElementKeyMap.isValidType(type) && ElementKeyMap.getInfoFast(type).getSourceReference() != 0) {
									invType = (short) ElementKeyMap.getInfoFast(type).getSourceReference();
								}

								int goalOrig = getEntityState().currentMapFrom.get(type) - getEntityState().currentMapTo.get(type);
								int goal = goalOrig;


								int count = ShipyardCollectionManager.DEBUG_MODE ? goal : inventory.getOverallQuantity(invType);
								int take = Math.min(toTake, Math.min(goal, count));

								if(!ShipyardCollectionManager.DEBUG_MODE) {
									inventory.decreaseBatch(invType, take, mod);
								}

								getEntityState().currentMapTo.inc(type, take);

								toTake -= take;

								currentFill += take;


							}

							if(toTake <= 0) {
								break;
							}
						}
						if(mod.size() > 0) {
							inventory.sendInventoryModification(mod);
						}
						tickCounter -= Math.max(1, ShipyardElementManager.CONSTRUCTION_TICK_IN_SECONDS);
					}
					if(mod.size() > 0) {
						getEntityState().getShipyardCollectionManager().sendShipyardGoalToClient();
					}
					getEntityState().setCompletionOrderPercentAndSendIfChanged((double) currentFill / currentDocked.getTotalElements());

					LogUtil.sy().fine(getEntityState().getSegmentController() + " " + getEntityState() + " " + getClass().getSimpleName() + ": construction: spawning new ship: aquiring blocks end step: " + currentFill + " / " + currentDocked.getTotalElements() + " blocks");
				} else {
					LogUtil.sy().fine(getEntityState().getSegmentController() + " " + getEntityState() + " " + getClass().getSimpleName() + ": construction: spawning new ship: aquiring blocks ERROR: Design not fully loaded");
				}
			} else {
				LogUtil.sy().fine(getEntityState().getSegmentController() + " " + getEntityState() + " " + getClass().getSimpleName() + ": new ship exists, but yard is idle: " + newShip);
			}
		} else if(newShip == null) {
			LogUtil.sy().fine(getEntityState().getSegmentController() + " " + getEntityState() + " " + getClass().getSimpleName() + ": construction interrupted. design no longer docked!");
			getEntityState().sendShipyardErrorToClient(Lng.str("Construction interrupted!\nDesign no longer docked!"));
			stateTransition(Transition.SY_ERROR);
		}

		return false;
	}

	@Override
	public boolean canCancel() {
		return true;
	}

	@Override
	public boolean hasBlockGoal() {
		return true;
	}


	@Override
	public boolean isPullingResources() {
		getEntityState().getShipyardCollectionManager().getControllerElement().refresh();
		return getEntityState().getShipyardCollectionManager().getControllerElement().isActive();
	}

	@Override
	public void pullResources() {
		ShipyardCollectionManager yard = getEntityState().getShipyardCollectionManager();
		long currentStep = yard.getSegmentController().getState().getController().getServerRunningTime() / ManagerContainer.TIME_STEP_STASH_PULL;
		if(currentStep > lastInventoryFilterStep) {

			Inventory inventory = getEntityState().getInventory();
			InventoryChangeMap changeMap = new InventoryChangeMap();

			//Inserted code @...
			for(ProductionItemPullListener listener : FastListenerCommon.productionItemPullListeners) {
				listener.onPrePull(null, this);
			}
			///
			for(short type : ElementKeyMap.keySet) {
				if(getEntityState().currentMapTo.get(type) < getEntityState().currentMapFrom.get(type)) {


					short invType = type;
					int additionalGoal = 0;
					if(ElementKeyMap.isValidType(type) && ElementKeyMap.getInfoFast(type).getSourceReference() != 0) {
						invType = (short) ElementKeyMap.getInfoFast(type).getSourceReference();
						additionalGoal = getEntityState().currentMapFrom.get(invType) - getEntityState().currentMapTo.get(invType);
					}

					int goal = getEntityState().currentMapFrom.get(type) - getEntityState().currentMapTo.get(type);
					goal += additionalGoal;
					int have = inventory.getOverallQuantity(invType);

					if(have < goal) {


						for(short s : ElementKeyMap.inventoryTypes) {
							LongOpenHashSet connectedInventoriesToThis = yard.getSegmentController().getControlElementMap().getControllingMap().get(yard.getControllerElement().getAbsoluteIndex()).get(s);
							if(connectedInventoriesToThis != null) {
								for(long l : connectedInventoriesToThis) {
									int toTake = Math.max(0, goal - have);
									if(toTake > 0 && have < goal) {
										Inventory pullFromInv = yard.getContainer().getInventory(ElementCollection.getPosIndexFrom4(l));

										if(pullFromInv != null) {
											int otherHas = pullFromInv.getOverallQuantity(invType);

											int take = Math.min(otherHas, toTake);

											if(take > 0 && (take = inventory.canPutInHowMuch(invType, take, -1)) > 0) {
												pullFromInv.decreaseBatch(invType, take, changeMap.getInv(pullFromInv));

												changeMap.getInv(inventory).add(inventory.putNextFreeSlotWithoutException(invType, take, -1));
												have += take;
											}
										}
									} else {
										break;
									}

								}
							}
						}
					}
				}
			}
			if(changeMap.size() > 0) {
				changeMap.sendAll();
			}
			lastInventoryFilterStep = currentStep;
			//Inserted code @...
			for(ProductionItemPullListener listener : FastListenerCommon.productionItemPullListeners) {
				listener.onPostPull(null, this);
			}
			///
		}
	}


	@Override
	public void onShipyardRemoved(Vector3i shipyardControllerPos) {
		super.onShipyardRemoved(shipyardControllerPos);
		getEntityState().currentMapTo.spawnInSpace(getEntityState().getSegmentController(), shipyardControllerPos);
	}


	@Override
	public String getClientShortDescription() {
		return Lng.str("Constructing (%d blocks every %d sec)", ShipyardElementManager.CONSTRUCTION_BLOCKS_TAKEN_PER_TICK, (long) ShipyardElementManager.CONSTRUCTION_TICK_IN_SECONDS);
	}


}
