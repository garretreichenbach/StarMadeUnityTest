package org.schema.game.client.controller.tutorial;

import org.schema.game.client.controller.tutorial.states.TutorialEnded;
import org.schema.schine.ai.MachineProgram;
import org.schema.schine.ai.stateMachines.AiEntityState;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;
import org.schema.schine.ai.stateMachines.Message;
import org.schema.schine.ai.stateMachines.State;
@Deprecated
public class TutorialStateMachine extends FiniteStateMachine<String> {

	/**
	 * The Constant serialVersionUID.
	 */
	
	private State tutorialStartState;
	private State tutorialEndState;
	private TutorialEnded tutorialEnded;

	/**
	 * Instantiates a new standart unit machine.
	 *
	 * @param obj the obj
	 */
	public TutorialStateMachine(AiEntityState obj, MachineProgram<?> p) {
		super(obj, p, "");
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.ai.stateMachines.FiniteStateMachine#createFSM()
	 */
	@Override
	public void createFSM(String parameter) {
		
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.ai.stateMachines.FiniteStateMachine#getMachineProgram()
	 */
	@Override
	public TutorialMode getMachineProgram() {
		return (TutorialMode) super.getMachineProgram();
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.ai.stateMachines.FiniteStateMachine#onMsg(org.schema.schine.ai.stateMachines.Message)
	 */
	@Override
	public void onMsg(Message message) {
		
	}

	/**
	 * @return the tutorialEndState
	 */
	public State getTutorialEndedState() {
		return tutorialEnded;
	}

	/**
	 * @return the tutorialEndState
	 */
	public State getTutorialEndState() {
		return tutorialEndState;
	}

	/**
	 * @return the tutorialStartState
	 */
	public State getTutorialStartState() {
		return tutorialStartState;
	}

}
