package org.schema.game.client.controller.tutorial.states;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.EditableSendableSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

public class ShipBuildModeSelectedOrConnectedTestState extends SatisfyingCondition {

	/**
	 *
	 */
	
	private short connectFrom;
	private short connectTo;

	public ShipBuildModeSelectedOrConnectedTestState(AiEntityStateInterface gObj, String message, GameClientState state,
	                                                 short connectFrom,
	                                                 short connectTo) {
		super(gObj, message, state);
		this.connectFrom = connectFrom;
		this.connectTo = connectTo;
		skipIfSatisfiedAtEnter = true;
	}

	@Override
	protected boolean checkSatisfyingCondition() throws FSMException {
		return isSelected(connectFrom) || isConnected(connectTo);
	}

	private boolean isConnected(short type) throws FSMException {
		EditableSendableSegmentController s = getGameState().getController().getTutorialMode().currentContext;
		if (s != null) {
			return !s.getControlElementMap().getAllControlledElements(type).isEmpty();
		} else {
			getEntityState().getMachine().getFsm().stateTransition(Transition.TUTORIAL_NO_SHIP_CREATED);
			return false;
		}
	}

	private boolean isSelected(short type) {
		SegmentPiece selectedBlock = getGameState().getGlobalGameControlManager().getIngameControlManager()
				.getPlayerGameControlManager().getPlayerIntercationManager().
						getInShipControlManager().getShipControlManager().getSegmentBuildController().getSelectedBlock();

		return selectedBlock != null && type == selectedBlock.getType();
	}

}
