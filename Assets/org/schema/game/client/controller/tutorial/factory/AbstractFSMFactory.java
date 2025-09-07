package org.schema.game.client.controller.tutorial.factory;

import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.network.StateInterface;

public abstract class AbstractFSMFactory {

	protected static final Transition satisfyTrasition = Transition.CONDITION_SATISFIED;
	protected static final Transition restartTrasition = Transition.TUTORIAL_RESTART;
	protected static final Transition skipPartTrasition = Transition.TUTORIAL_SKIP_PART;
	protected static final Transition resetPartTrasition = Transition.TUTORIAL_RESET_PART;
	protected static final Transition endTutorialTransition = Transition.TUTORIAL_END;
	protected static final Transition backTransition = Transition.BACK;
	protected final State resetStepState;
	protected final StateInterface state;
	private final State fromState;
	private final State totalEndEndState;
	protected State lastStateBackTransitionState;
	private State startState;
	private State endState;

	public AbstractFSMFactory(State fromState, State resetStepState, State tutorialEndState,
	                          StateInterface state) {
		super();
		this.fromState = fromState;
		this.resetStepState = resetStepState;
		this.totalEndEndState = tutorialEndState;
		this.state = state;

	}

	public State create() {

		defineStartAndEnd();

		assert (startState != null);
		assert (endState != null);

		startState.addTransition(backTransition, fromState);
		fromState.addTransition(Transition.NEXT, startState);
		fromState.addTransition(satisfyTrasition, startState);
		fromState.addTransition(restartTrasition, resetStepState);
		fromState.addTransition(endTutorialTransition, totalEndEndState);

		State lastState = create(startState);
		if (lastStateBackTransitionState == null) {
			transition(lastState, endState);
		} else {
			transition(lastState, endState, lastStateBackTransitionState);
		}

		assert (startState.getStateData().getTransitionCount() > 0);
		return endState;
	}

	protected abstract State create(State startState);

	protected abstract void defineStartAndEnd();

	public AiEntityStateInterface getObj() {
		return fromState.getEntityState();
	}

	/**
	 * @return the startState
	 */
	public State getStartState() {
		return startState;
	}

	/**
	 * @param startState the startState to set
	 */
	public void setStartState(State startState) {
		this.startState = startState;
	}

	/**
	 * @param endState the endState to set
	 */
	public void setEndState(State endState) {
		this.endState = endState;
	}

	public void transition(State from, State to) {
		assert (to != null);
		assert (resetStepState != null);
		assert (fromState != null);
		assert (endState != null);
		assert (totalEndEndState != null);
		assert (from != null);
		from.addTransition(satisfyTrasition, to);
		from.addTransition(restartTrasition, resetStepState);
		from.addTransition(resetPartTrasition, fromState);
		from.addTransition(skipPartTrasition, endState);
		from.addTransition(endTutorialTransition, totalEndEndState);
		to.addTransition(backTransition, from);
	}

	public void transition(State from, State to, State backState) {
		from.addTransition(satisfyTrasition, to);
		from.addTransition(restartTrasition, resetStepState);
		from.addTransition(resetPartTrasition, fromState);
		from.addTransition(skipPartTrasition, endState);
		from.addTransition(endTutorialTransition, totalEndEndState);
		to.addTransition(backTransition, backState);
	}

}
