package org.schema.game.client.controller.tutorial.states;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.ai.AiEntityStateInterface;

public class WaitingTextState extends TextState {

	/**
	 *
	 */
	
	private boolean lastNext;

	public WaitingTextState(AiEntityStateInterface gObj, GameClientState state,
	                        String message, int durationInMs) {
		super(gObj, state, message, durationInMs);
	}

	@Override
	protected boolean checkSatisfyingCondition() {
		if (!getGameState().getController().getTutorialMode().isBackgroundMode()) {
			if (lastNext != next && next) {
				entered = System.currentTimeMillis();
			}
			lastNext = next;
		}
		if (next || getGameState().getController().getTutorialMode().isBackgroundMode()) {
			return !getGameState().getController().getTutorialMode().isSuspended() && (System.currentTimeMillis() - entered) > getDurationInMs();
		} else {
			return false;
		}

	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.tutorial.states.TimedState#onEnter()
	 */
	@Override
	public boolean onEnter() {
		lastNext = false;
		return super.onEnter();
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.tutorial.states.SatisfyingCondition#getMessage()
	 */
	@Override
	public String getMessage() {
		if (next && !getGameState().getController().getTutorialMode().isBackgroundMode()) {
			return super.getMessage() + "(Tutorial will resume in " + (getDurationInMs() - (System.currentTimeMillis() - entered)) / 1000 + " sec\nor press 'u')";
		} else {
			return super.getMessage();
		}
	}
}
