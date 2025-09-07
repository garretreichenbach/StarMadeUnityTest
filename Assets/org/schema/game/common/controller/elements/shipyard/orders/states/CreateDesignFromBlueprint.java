package org.schema.game.common.controller.elements.shipyard.orders.states;

import com.bulletphysics.linearmath.Transform;
import org.schema.common.LogUtil;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.shipyard.orders.ShipyardEntityState;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.MetaObjectManager;
import org.schema.game.common.data.element.meta.MetaObjectManager.MetaObjectType;
import org.schema.game.common.data.element.meta.VirtualBlueprintMetaItem;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.player.inventory.NoSlotFreeException;
import org.schema.game.common.data.world.Sector;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.controller.EntityAlreadyExistsException;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.blueprint.ChildStats;
import org.schema.game.server.data.blueprint.SegmentControllerOutline;
import org.schema.game.server.data.blueprint.SegmentControllerSpawnCallbackDirect;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;

import java.io.IOException;

public class CreateDesignFromBlueprint extends ShipyardState{

	private SegmentController newShip;

	public CreateDesignFromBlueprint(ShipyardEntityState gObj) {
		super(gObj);
	}

	@Override
	public boolean onEnterS() {
		newShip = null;
		return false;
	}

	@Override
	public boolean onExit() {
		newShip = null;
		return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {
		SegmentController currentDocked = getEntityState().getCurrentDocked();
		
		
		if(newShip != null ){
			
			if(newShip.getSegmentBuffer().getPointUnsave(Ship.core) != null){
				if(getEntityState().getState().getLocalAndRemoteObjectContainer().getLocalObjects().containsKey(newShip.getId()) && 
						newShip.getSegmentBuffer().getPointUnsave(Ship.core).getType() == ElementKeyMap.CORE_ID){//autorequest true previously
					//autorequest true previously
					if(newShip.railController.isDockedAndExecuted()){
						LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Created design and docked from blueprint successfull "+newShip);
						stateTransition(Transition.SY_CONVERSION_DONE);
					}else if(!newShip.railController.isDockedOrDirty()){
						LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Created design and docked from blueprint failed "+newShip+"; Cant dock");
						newShip.railController.destroyDockedRecursive();
						newShip.markForPermanentDelete(true);
						newShip.setMarkedForDeleteVolatile(true);
						System.err.println("[SERVER][SHIPYARD] Create Design From blueprint failed. Ship "+newShip+" couldnt dock!");
						getEntityState().sendShipyardErrorToClient(Lng.str("SHIPYARD_ERROR:\nShip failed to dock!"));
						stateTransition(Transition.SY_ERROR);
					}
				}else {
					LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Created design and docked from blueprint WAITING "+newShip+"; No core or entity doesnt exist yet");
				}
			}else {
				LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Created design and docked from blueprint WAITING "+newShip+"; No core exists");
			}
			
		}else if(currentDocked != null){
			LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Created design from bp failed: occupied");
			getEntityState().sendShipyardErrorToClient(Lng.str("Cannot create design! Shipyard occupied!"));
			stateTransition(Transition.SY_ERROR);
		}else if(!getEntityState().getInventory().hasFreeSlot()){
			LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Created design from bp failed: no slot");
			getEntityState().sendShipyardErrorToClient(Lng.str("Cannot create design!\nNo slot free in shipyard computer."));
			stateTransition(Transition.SY_ERROR);
		}else{
			double l = getTickCount();
			if(l >= 1.0f){
				getEntityState().setCompletionOrderPercentAndSendIfChanged(1.0f);
				
				Transform tr = new Transform();
				tr.setIdentity();
				
				
				try {
					Sector sector = ((GameServerState)getEntityState().getState()).getUniverse().getSector(getEntityState().getSegmentController().getSectorId());
					
					if(sector != null){
						assert(getEntityState().currentBlueprintName != null);
						assert(getEntityState().currentName != null);
						
						SegmentPiece toDockOn = null; //this is for spawning turrets (designs will be docked to shipyard seperately)
						SegmentControllerOutline loadBluePrint = 
								BluePrintController.active.loadBluePrint((GameServerState) getEntityState().getState(), 
								getEntityState().currentBlueprintName,
								getEntityState().currentName, tr, -1, 
								FactionManager.ID_NEUTRAL, 
								sector.pos, 
								"SHIPYARD_"+getEntityState().getSegmentController().getUniqueIdentifier(), 
								PlayerState.buffer, 
								toDockOn,
								false, 
								
								new ChildStats(false));
						loadBluePrint.checkOkName();
	
						
						if (loadBluePrint != null && loadBluePrint.en.getType() == org.schema.game.server.data.blueprintnw.BlueprintType.SPACE_STATION) {
							LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Created design from bp failed: STTAION");
							getEntityState().sendShipyardErrorToClient(Lng.str("SHIPYARD ERROR:\nCan't use stations in shipyard!"));
							stateTransition(Transition.SY_ERROR);
						}if (loadBluePrint != null && loadBluePrint.hasOldDocking()) {
							LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Created design from bp failed: OLD DOCK");
							getEntityState().sendShipyardErrorToClient(Lng.str("SHIPYARD ERROR:\nOld Docking Systems not permitted in in shipyard!"));
							stateTransition(Transition.SY_ERROR);
						}else{

					
							newShip = loadBluePrint.spawn(sector.pos, true, new ChildStats(false), new SegmentControllerSpawnCallbackDirect(((GameServerState)getEntityState().getState()), sector.pos) {
								@Override
								public void onNoDocker() {
									getEntityState().sendShipyardErrorToClient(Lng.str("No docker blocks on blueprint!"));
								}
								
							});
							
							
							LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": creating and loading design -> FORCE REQUEST! "+newShip);
							newShip.getSegmentProvider().enqueueHightPrio(0, 0, 0, true);
							
							boolean b = getEntityState().getShipyardCollectionManager().createDockingRelation(newShip, true);
							
							if(b){
								
								((GameServerState)getEntityState().getState()).getMetaObjectManager();
								VirtualBlueprintMetaItem m = (VirtualBlueprintMetaItem) MetaObjectManager.instantiate(MetaObjectType.VIRTUAL_BLUEPRINT.type, (short) -1, true);
								
								int slot = -1;
								try {
									slot = getEntityState().getInventory().getFreeSlot();
								
									m.UID = newShip.getUniqueIdentifier();
									m.virtualName = new String(getEntityState().currentName);
									
									getEntityState().getInventory().put(slot, m);
									getEntityState().getInventory().sendInventoryModification(slot);
									
									getEntityState().getShipyardCollectionManager().setCurrentDesign(m.getId());
									
									newShip.initialize();
								
									getEntityState().getShipyardCollectionManager().sendShipyardStateToClient();
									
									
									LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Created design from bp successfull: "+newShip);
									
								
								} catch (NoSlotFreeException e) {
									e.printStackTrace();
									getEntityState().sendShipyardErrorToClient(Lng.str("SHIPYARD_ERROR:\nCritical Server Error\nNo slot while space reserved"));
									stateTransition(Transition.SY_ERROR);
									LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Created design from bp failed: no slot but reserved");
								}
							}else{
								LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Created design from bp failed (CANNOT DOCK) destroying: "+newShip);
								newShip.railController.destroyDockedRecursive();
								newShip.markForPermanentDelete(true);
								newShip.setMarkedForDeleteVolatile(true);
								getEntityState().sendShipyardErrorToClient(Lng.str("SHIPYARD_ERROR:\nCannot dock!"));
								stateTransition(Transition.SY_ERROR);
							}
						}
					}else {
						LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Created design from bp failed: Sector null");
					}
					
				} catch (EntityNotFountException ex) {
					ex.printStackTrace();
					getEntityState().sendShipyardErrorToClient(Lng.str("SHIPYARD_ERROR:\nEntity not found!"));
					LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Created design from bp failed: entity not found");
					stateTransition(Transition.SY_ERROR);
				} catch (IOException e) {
					e.printStackTrace();
					getEntityState().sendShipyardErrorToClient(Lng.str("SHIPYARD_ERROR:\nIOException!"));
					LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Created design from bp failed: IO Exception");
					stateTransition(Transition.SY_ERROR);
				} catch (EntityAlreadyExistsException e) {
					e.printStackTrace();
					getEntityState().sendShipyardErrorToClient(Lng.str("SHIPYARD_ERROR:\nEntity already exists!"));
					LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Created design from bp failed: Entity already exists");
					stateTransition(Transition.SY_ERROR);
				} catch (StateParameterNotFoundException e) {
					e.printStackTrace();
					getEntityState().sendShipyardErrorToClient(Lng.str("SHIPYARD_ERROR:\nState parameter not found!"));
					LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Created design from bp failed: state parameter not found");
					stateTransition(Transition.SY_ERROR);
				}

				
				
				
				
			}else{
				LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Created design from bp in progress: "+l);
				getEntityState().getShipyardCollectionManager().setCompletionOrderPercentAndSendIfChanged((float)l);
				//wait;
			}
				
			
		}

		return false;
	}

}
