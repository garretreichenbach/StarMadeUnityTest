package org.schema.game.common.controller.elements.shipyard.orders.states;

import org.schema.common.LogUtil;
import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.elements.shipyard.ShipyardElementManager;
import org.schema.game.common.controller.elements.shipyard.orders.ShipyardEntityState;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.element.meta.MetaObjectManager;
import org.schema.game.common.data.element.meta.MetaObjectManager.MetaObjectType;
import org.schema.game.common.data.element.meta.VirtualBlueprintMetaItem;
import org.schema.game.common.data.player.inventory.NoSlotFreeException;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.common.language.Lng;

public class Deconstructing extends ShipyardState{

	private SegmentController currentDocked;
	private boolean waitForUnload;
	private int deconstructTimeMs;
	private int removing;

	
	
	public Deconstructing(ShipyardEntityState gObj) {
		super(gObj);
	}

	@Override
	public boolean onEnterS() {
		currentDocked = getEntityState().getCurrentDocked();
		getEntityState().currentMapTo.resetAll();
		getEntityState().currentMapFrom.resetAll();
		waitForUnload = false;
		
		removing = 0;
		if(currentDocked != null){
			currentDocked.railController.fillElementCountMapRecursive(getEntityState().currentMapFrom);
			
			deconstructTimeMs = (ShipyardElementManager.DECONSTRUCTION_CONST_TIME_MS + (int)(ShipyardElementManager.DECONSTRUCTION_MS_PER_BLOCK * currentDocked.getTotalElements()));
		}

		if(ServerConfig.BLUEPRINTS_USE_COMPONENTS.isOn()) {
			ElementCountMap resources = getEntityState().currentMapFrom.calculateComponents();
			getEntityState().currentMapFrom.resetAll();
			getEntityState().currentMapFrom = resources;
		}
		return false;
	}
	@Override
	public boolean canCancel() {
		return true;
	}
	@Override
	public boolean onExit() {
		return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {
		
		if(removing != 0){
			LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Deconstruct: removing flag "+removing);
			if(!getEntityState().getState().getLocalAndRemoteObjectContainer().getLocalObjects().containsKey(removing)){
				LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Deconstruct: removing flag DONE NO DESIGN");
				stateTransition(Transition.SY_DECONSTRUCTION_DONE_NO_DESIGN);
			}
		}else if(waitForUnload ){
			LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Deconstruct: waiting for unload "+getEntityState().getCurrentDocked());
			if(getEntityState().getCurrentDocked() == null){
				currentDocked = null;
				stateTransition(Transition.SY_DECONSTRUCTION_DONE);
			}else{
				System.err.println("[SHIPYARD][DECONSTRUCTING] Waiting for unload "+currentDocked);
			}
		}else if(currentDocked == null || currentDocked != getEntityState().getCurrentDocked()){
			LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Deconstruct: FAILED NO LONGER DOCKED "+currentDocked);
			getEntityState().sendShipyardErrorToClient(Lng.str("Deconstruction interrupted!\nShip no longer docked!"));
			System.err.println("[SHIPYARD][DECONSTRUCTION] FAILED: DOCKED CHANGED: "+currentDocked+"; "+getEntityState().getCurrentDocked());
			stateTransition(Transition.SY_ERROR);
		}else{
			
			if(getTickCount()*1000 > deconstructTimeMs){
				LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Deconstruct: TICK");
				if(getEntityState().currentMapTo.getExistingTypeCount() == 0){
					
					getEntityState().currentMapTo.add(getEntityState().currentMapFrom);
					assert(getEntityState().currentMapTo.getExistingTypeCount() > 0);
					LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Adding all block types needed "+getEntityState().currentMapTo.getExistingTypeCount());
				}else{
					if(getEntityState().currentName != null && !getEntityState().isInRepair && !getEntityState().getInventory().hasFreeSlot()){
						LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Failed: No slot fr");
						getEntityState().sendShipyardErrorToClient(Lng.str("Cannot create design!\nNo slot free in shipyard computer."));
						stateTransition(Transition.SY_ERROR);
					}else{
						getEntityState().setCompletionOrderPercentAndSendIfChanged(1.0);
						
						if(getEntityState().currentName != null && !getEntityState().isInRepair){
							((GameServerState)getEntityState().getState()).getMetaObjectManager();
							//create design
							VirtualBlueprintMetaItem m = (VirtualBlueprintMetaItem) MetaObjectManager.instantiate(MetaObjectType.VIRTUAL_BLUEPRINT, (short) -1, true);
							
							LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": deconstruct: created item "+m);
							
							int slot = -1;
							try {
								slot = getEntityState().getInventory().getFreeSlot();
							
								m.UID = new String(currentDocked.getUniqueIdentifier());
								m.virtualName = new String(getEntityState().currentName);
								currentDocked.setRealName(getEntityState().currentName);
								
								LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": deconstruct: forcing current dock load "+currentDocked);
								currentDocked.getSegmentProvider().enqueueHightPrio(0, 0, 0, true);
								assert(currentDocked.getSegmentBuffer().getPointUnsave(Ship.core).getType() == ElementKeyMap.CORE_ID);//autorequest true previously
								
								getEntityState().getInventory().put(slot, m);
								getEntityState().getInventory().sendInventoryModification(slot);
							
								getEntityState().getShipyardCollectionManager().sendShipyardStateToClient();
								
								getEntityState().unloadCurrentDockedVolatile();
								waitForUnload = true;
								getEntityState().putInInventoryAndConnected(getEntityState().currentMapTo, currentDocked.isScrap());
								
								
								getEntityState().saveInventories(currentDocked);
								
								
								getEntityState().currentMapTo.resetAll();
								getEntityState().currentMapFrom.resetAll();
								
								System.err.println("[SERVER] CREATED DESIGN "+m);
								
								getEntityState().designToLoad = m.getId();
								
								getEntityState().currentName = null;
								
								LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": deconstruct: created design successful "+m);
							} catch (NoSlotFreeException e) {
								LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": NO SLOT BUT RESERVED");
								e.printStackTrace();
								getEntityState().sendShipyardErrorToClient(Lng.str("SHIPYARD_ERROR:\nCritical Server Error\nNo slot while space reserved"));
								stateTransition(Transition.SY_ERROR);
							}
							
						}else{
							
							LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Save inventories and destroy; CURRENT: "+getEntityState().currentName+"; REPAIRING "+getEntityState().isInRepair);
							getEntityState().lastDeconstructedBlockCount = (int) Math.min(Integer.MAX_VALUE, getEntityState().currentMapTo.getTotalAmount());
							
							getEntityState().putInInventoryAndConnected(getEntityState().currentMapTo, currentDocked.isScrap());
							
							getEntityState().saveInventories(currentDocked);
							
							currentDocked.railController.destroyDockedRecursive();
							
							currentDocked.markForPermanentDelete(true);
							currentDocked.setMarkedForDeleteVolatile(true);
							
							removing = currentDocked.getId();
							
							LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Destroying: "+currentDocked);
						}
						
						
					}
				}
				
			
			}else{
				
				LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Deconstruct: in progress "+getTickCount()+"; "+(deconstructTimeMs/1000));
				
				getEntityState().setCompletionOrderPercentAndSendIfChanged((getTickCount()*1000d) / (deconstructTimeMs));
				if(getEntityState().currentName != null && !getEntityState().isInRepair && !getEntityState().getInventory().hasFreeSlot()){
					LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Deconstruct: failed: no slot free");
					
					getEntityState().sendShipyardErrorToClient(Lng.str("Cannot create design!\nNo slot free in shipyard computer."));
					stateTransition(Transition.SY_ERROR);
				}
			}
		}
		
		return false;
	}
	
}
