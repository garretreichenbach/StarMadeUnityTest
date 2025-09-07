package org.schema.game.client.controller.tutorial.states;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.EditableSendableSegmentController;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

public class ShipBuildModeConnectedTestState extends SatisfyingCondition {

	/**
	 *
	 */
	
	private short clazz;

	public ShipBuildModeConnectedTestState(AiEntityStateInterface gObj, String message, GameClientState state, short clazz) {
		super(gObj, message, state);
		this.clazz = clazz;
		skipIfSatisfiedAtEnter = true;
	}

	@Override
	protected boolean checkSatisfyingCondition() throws FSMException {
		EditableSendableSegmentController s = getGameState().getController().getTutorialMode().currentContext;
		if (s != null) {
			return !s.getControlElementMap().getAllControlledElements(clazz).isEmpty();
		} else {
			getEntityState().getMachine().getFsm().stateTransition(Transition.TUTORIAL_NO_SHIP_CREATED);
			return false;
		}
	}

}
