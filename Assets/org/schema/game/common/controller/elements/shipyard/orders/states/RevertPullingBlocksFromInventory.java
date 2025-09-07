package org.schema.game.common.controller.elements.shipyard.orders.states;

import org.schema.common.LogUtil;
import org.schema.game.common.controller.elements.shipyard.orders.ShipyardEntityState;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

public class RevertPullingBlocksFromInventory extends ShipyardState{

	public RevertPullingBlocksFromInventory(ShipyardEntityState gObj) {
		super(gObj);
	}

	@Override
	public boolean onEnterS() {
		return false;
	}

	@Override
	public boolean onExit() {
				return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {
		
		if(getEntityState().currentMapTo.getTotalAmount() > 0){
			LogUtil.sy().fine(this+" "+getEntityState()+"; "+getEntityState().getSegmentController()+" REVERT PULLING ");
			getEntityState().putInInventoryAndConnected(getEntityState().currentMapTo, false);
			getEntityState().currentMapTo.resetAll();
		}
		
		stateTransition(Transition.SY_BLOCK_TRANSACTION_FINISHED);
		return false;
	}

}
