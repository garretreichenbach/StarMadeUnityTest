package org.schema.game.client.controller.tutorial.states.conditions;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;

public class TutorialConditionBlockExists extends TutorialCondition {

	private Vector3i pos;

	public TutorialConditionBlockExists(State toObserve, State establishing, Vector3i pos) {
		super(toObserve, establishing);
		this.pos = pos;
	}

	@Override
	public boolean isSatisfied(GameClientState state) {

		if (state.getController().getTutorialMode().currentContext == null) {
			System.err.println("[TUTORIALCONDITION] failed: no context: " + this);
			return false;
		}
		SegmentPiece cc = state.getController().getTutorialMode().currentContext.getSegmentBuffer().getPointUnsave(pos);
		if (cc != null) {
			return ElementKeyMap.isValidType(cc.getType());
		}
		//true if not yet available
		return true;

	}

	@Override
	public String getNotSactifiedText() {
		return "CRITICAL: block removed";
	}

	@Override
	protected Transition getTransition() {
		return Transition.TUTORIAL_CONDITION_BLOCK_EXISTS;
	}

}
