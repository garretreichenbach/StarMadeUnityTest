package org.schema.game.common.controller.elements.shipyard.orders.states;

import org.schema.common.LogUtil;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.shipyard.orders.ShipyardEntityState;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.common.language.Lng;

public class NormalShipLoaded extends ShipyardState{
	private SegmentController docked = null;
	public NormalShipLoaded(ShipyardEntityState gObj) {
		super(gObj);
	}

	@Override
	public boolean onEnterS() {
		docked = null;
		return false;
	}

	@Override
	public boolean onExit() {
		docked = null;
		return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {
		SegmentController currentDocked = getEntityState().getCurrentDocked();
		
		if(docked != currentDocked) {
			LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Normal Ship Docked: "+currentDocked);
		}
		
		docked = currentDocked;
		if(currentDocked == null || currentDocked.isVirtualBlueprint()){
			if(currentDocked != null && currentDocked.isVirtualBlueprint()){
				LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Blueprint undocked");
				getEntityState().unloadCurrentDockedVolatile();
			}
			if(getEntityState().wasUndockedManually){
				LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Normal Ship Undocked manually");
				getEntityState().wasUndockedManually = false;
			}else{
				LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Normal Ship Undocked for another reason");
				getEntityState().sendShipyardErrorToClient(Lng.str("No ship docked!"));
			}
			stateTransition(Transition.SY_ERROR);
		}
//		System.err.println("CURRENT DOCK "+currentDocked);
		return false;
	}
	@Override
	public String getClientShortDescription() {
		return Lng.str("Ship Docked");
	}
	
	@Override
	public boolean canEdit() {
		return true;
	}
	@Override
	public boolean canUndock() {
		return true;
	}
}
