package org.schema.game.client.controller.tutorial.states.conditions;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.SegmentPiece;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;

public class TutorialConditionInShipUID extends TutorialCondition {

	private String inShipUID;

	public TutorialConditionInShipUID(State toObserve, State establishing, String inShipUID) {
		super(toObserve, establishing);
		this.inShipUID = inShipUID;
	}

	@Override
	public boolean isSatisfied(GameClientState state) {

		SegmentPiece entered = state.getGlobalGameControlManager().getIngameControlManager().getPlayerGameControlManager().
				getPlayerIntercationManager().getInShipControlManager().getEntered();

		if (entered != null && entered.getSegment().getSegmentController() != null) {
			return entered.getSegment().getSegmentController().getUniqueIdentifier().startsWith(inShipUID);
		}

		return false;
	}

	@Override
	public String getNotSactifiedText() {
		return "CRITICAL: not in ship";
	}
	@Override
	protected Transition getTransition() {
		return Transition.TUTORIAL_CONDITION_IN_SHIP_UID;
	}
}
