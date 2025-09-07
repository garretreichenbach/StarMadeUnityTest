package org.schema.schine.ai;

import org.schema.schine.ai.stateMachines.AiEntityState;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;
import org.schema.schine.ai.stateMachines.Message;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.StateInterface;

public interface AiEntityStateInterface {

	public MachineProgram<? extends AiEntityState> getCurrentProgram();

	public void setCurrentProgram(MachineProgram<? extends AiEntityState> program);

	public FiniteStateMachine getMachine();

	/**
	 * @return the state
	 */
	public StateInterface getState();

	public State getStateCurrent();

	public boolean isActive();

	public boolean processStateMachine(State newState, Message message);

	void updateOnActive(Timer timer) throws FSMException, Exception;

}
