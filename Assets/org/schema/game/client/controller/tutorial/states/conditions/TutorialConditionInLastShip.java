package org.schema.game.client.controller.tutorial.states.conditions;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.SegmentPiece;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;

public class TutorialConditionInLastShip extends TutorialCondition {

	public TutorialConditionInLastShip(State toObserve, State establishing) {
		super(toObserve, establishing);
	}

	@Override
	public boolean isSatisfied(GameClientState state) {

		SegmentPiece entered = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().
				getPlayerIntercationManager().getInShipControlManager().getEntered();

		if (state.getController().getTutorialMode() == null || state.getController().getTutorialMode().lastSpawnedShip == null) {
			return false;
		}

		if (entered != null && entered.getSegment().getSegmentController() != null) {
			return entered.getSegment().getSegmentController() == state.getController().getTutorialMode().lastSpawnedShip;
		}

		return false;
	}

	@Override
	public String getNotSactifiedText() {
		return "CRITICAL: not in ship";
	}
	@Override
	protected Transition getTransition() {
		return Transition.TUTORIAL_CONDITION_IN_LAST_SHIP;
	}
}
