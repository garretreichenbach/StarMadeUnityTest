package org.schema.game.common.data.player.dialog;

import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.network.StateInterface;

public abstract class DialogTimedState extends DialogSatisfyingCondition {

	/**
	 *
	 */
	

	protected long entered;

	public DialogTimedState(AiEntityStateInterface gObj, Object[] message, StateInterface state) {
		super(gObj, message, state);
	}

	@Override
	protected boolean checkSatisfyingCondition() {
		return next;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.controller.tutorial.states.SatisfyingCondition#onEnter()
	 */
	@Override
	public boolean onEnter() {
		this.entered = System.currentTimeMillis();

		return super.onEnter();
	}

	public abstract long getDurationInMs();

}
