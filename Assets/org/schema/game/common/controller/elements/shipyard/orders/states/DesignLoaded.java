package org.schema.game.common.controller.elements.shipyard.orders.states;

import org.schema.common.LogUtil;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.shipyard.orders.ShipyardEntityState;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.objects.Sendable;

public class DesignLoaded extends ShipyardState{

	private long lastCheckForCollision;
	private boolean checkSectorOverlap;

	public DesignLoaded(ShipyardEntityState gObj) {
		super(gObj);
	}

	@Override
	public boolean onEnterS() {
		getEntityState().getShipyardCollectionManager().setCompletionOrderPercentAndSendIfChanged(0.0f);
		checkSectorOverlap = true;
		lastCheckForCollision = System.currentTimeMillis();
		return false;
	}

	@Override
	public boolean onExit() {
		return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {
		if(!getEntityState().getShipyardCollectionManager().isLoadedDesignValid()){
			System.err.println("[SERVER] Currently loaded design not valid. UNLOADING "+getEntityState().getCurrentDesign()+" REASON: "+getEntityState().getShipyardCollectionManager().isLoadedDesignValidToSring());
			LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": CURRENT DESIGN NO LONGER VALID: "+getEntityState().getCurrentDesign()+"; "+getEntityState().getShipyardCollectionManager().isLoadedDesignValidToSring());
			//meta item has either been removed or something else happened
			stateTransition(Transition.SY_UNLOAD_DESIGN);
			return false;
		}else{
			if(System.currentTimeMillis() - lastCheckForCollision < 600){
				SegmentController currentDocked = getEntityState().getCurrentDocked();
				boolean col = currentDocked.railController.getCollisionChecker().checkPotentialCollisionWithRail(currentDocked, null, true);
				
				LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": loaded design checking for collision: "+col);
				if(!col){
					if(checkSectorOverlap){
						col = currentDocked.getCollisionChecker().checkSectorCollisionWithChildShapeExludingRails();
						if(currentDocked.railController.isFullyLoadedRecursive()){
							//after sector is fully loaded we can stop checking overlaps
							checkSectorOverlap = false;	
							for(Sendable s : currentDocked.getState().getLocalAndRemoteObjectContainer().getLocalObjects().values()){
								if(s instanceof SegmentController && 
									!((SegmentController) s).railController.isDockedAndExecuted() && 
									((SegmentController) s).getSectorId() == currentDocked.getSectorId() && 
									!((SegmentController) s).railController.isFullyLoadedRecursive()){
									//if there is any structure in the sector that has anything unloaded, we keep checking
									checkSectorOverlap = true;
									break;
								}
							}
							
						}
					}
				}
				
				if(col){
					LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": removing design sicne it overlaps with structure");
					getEntityState().sendShipyardErrorToClient(Lng.str("Removing Design because it overlaps with a structure"));
					stateTransition(Transition.SY_UNLOAD_DESIGN);
				}
				lastCheckForCollision = System.currentTimeMillis();
			}
		}
		return false;
	}
	@Override
	public String getClientShortDescription() {
		return Lng.str("Design Loaded");
	}
	
	@Override
	public boolean canEdit() {
		return true;
	}
}
