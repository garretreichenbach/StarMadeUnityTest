package org.schema.game.client.controller.tutorial.states;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.ai.AiEntityStateInterface;

public abstract class TimedState extends SatisfyingCondition {

	/**
	 *
	 */
	

	protected long entered;

	public TimedState(AiEntityStateInterface gObj, String message, GameClientState state) {
		super(gObj, message, state);
	}

	@Override
	protected boolean checkSatisfyingCondition() {
		if (getGameState().getController().getTutorialMode().isBackgroundMode()) {
			return !getGameState().getController().getTutorialMode().isSuspended() && (System.currentTimeMillis() - entered) > getDurationInMs();
		} else {
			return next;
		}
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.tutorial.states.SatisfyingCondition#onEnter()
	 */
	@Override
	public boolean onEnter() {
		this.entered = System.currentTimeMillis();

		return super.onEnter();
	}

	public abstract int getDurationInMs();

}
