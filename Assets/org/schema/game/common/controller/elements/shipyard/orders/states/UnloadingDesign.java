package org.schema.game.common.controller.elements.shipyard.orders.states;

import org.schema.common.LogUtil;
import org.schema.game.common.controller.elements.shipyard.orders.ShipyardEntityState;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.common.language.Lng;

public class UnloadingDesign extends ShipyardState{


	public UnloadingDesign(ShipyardEntityState gObj) {
		super(gObj);
	}

	@Override
	public boolean onEnterS() {
		getEntityState().getShipyardCollectionManager().setCompletionOrderPercentAndSendIfChanged(0.0f);
		
		
		return false;
	}

	@Override
	public boolean onExit() {
				return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {
		if(getEntityState().isLoadedDesignValid()){
			
			double l = getTickCount();
			
			if(l < 1.0){
				LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Unloading design waiting "+l);
				getEntityState().getShipyardCollectionManager().setCompletionOrderPercentAndSendIfChanged((float)l);
			}else{
				getEntityState().unloadCurrentDockedVolatile();
				LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Unloading design DONE "+l);
				getEntityState().getShipyardCollectionManager().setCompletionOrderPercentAndSendIfChanged(1.0f);
				getEntityState().getShipyardCollectionManager().setCurrentDesign(-1);
				getEntityState().sendShipyardStateToClient();
				stateTransition(Transition.SY_UNLOADING_DONE);
			}
		}else{
			LogUtil.sy().fine(getEntityState().getSegmentController()+" "+getEntityState()+" "+this.getClass().getSimpleName()+": Design no longer loaded");
			getEntityState().getShipyardCollectionManager().setCurrentDesign(-1);
			getEntityState().sendShipyardStateToClient();
			getEntityState().sendShipyardErrorToClient(Lng.str("Error: Cannot unload design!\nNo Design Loaded!"));
			stateTransition(Transition.SY_ERROR);
		}
		return false;
	}

}
