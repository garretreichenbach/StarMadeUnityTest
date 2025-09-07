package org.schema.game.client.controller.tutorial.states.conditions;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;

public abstract class TutorialCondition  {

	/**
	 *
	 */
	
	static int i;
	private State establishing;
	private State toObserve;
	private int id;

	public TutorialCondition(State toObserve, State establishing) {
		this.establishing = establishing;
		this.toObserve = toObserve;

		toObserve.addTransition(getTransition(), establishing);
		id = i++;
	}

	protected abstract Transition getTransition();

	public abstract boolean isSatisfied(GameClientState gameState);

	public abstract String getNotSactifiedText();

	public boolean checkAndStateTransitionIfMissed(GameClientState gameState) throws FSMException {
		if (!isSatisfied(gameState)) {
			toObserve.stateTransition(getTransition());
			return false;
		}
		return true;
	}

	/**
	 * state that establishes condition
	 *
	 * @return
	 */
	public State getEstablishingState() {
		return establishing;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.ai.stateMachines.Transition#hashCode()
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.ai.stateMachines.Transition#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof TutorialCondition) {
			return id == ((TutorialCondition) o).id;
		} else {
			return false;
		}
	}

}
