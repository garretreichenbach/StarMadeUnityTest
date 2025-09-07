package org.schema.game.server.data.simulation.npc.resources;

import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.MachineProgram;
import org.schema.schine.ai.stateMachines.AiEntityState;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;
import org.schema.schine.ai.stateMachines.Message;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.server.ServerStateInterface;

public class NPCFactionFSM implements AiEntityStateInterface{

	protected final StateInterface state;
	private final boolean onServer;
	public String debugString = "";
	/**
	 * The name.
	 */
	protected String name;
	protected MachineProgram<? extends AiEntityState> program;

	public NPCFactionFSM(String name, StateInterface state) {
		this.name = name;
		this.state = state;
		onServer = this.state instanceof ServerStateInterface;
	}


	@Override
	public MachineProgram<? extends AiEntityState> getCurrentProgram() {
		return program;
	}

	@Override
	public void setCurrentProgram(MachineProgram<? extends AiEntityState> program) {
		this.program = program;
	}

	/**
	 * Gets the machine.
	 *
	 * @return the machine
	 */
	@Override
	public FiniteStateMachine<?> getMachine() {
		return program.getMachine();
	}

	/**
	 * @return the state
	 */
	@Override
	public StateInterface getState() {
		return state;
	}

	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	@Override
	public State getStateCurrent() {
		return program.getMachine().getFsm().getCurrentState();
	}

	@Override
	public boolean isActive() {
		return program != null && !program.isSuspended();
	}

	/**
	 * Process state machine.
	 *
	 * @param newState the new state
	 * @param message  the message
	 * @return true, if successful
	 */
	@Override
	public boolean processStateMachine(State newState, Message message) {
		if (program.getMachine() != null) {
			program.getMachine().onMsg(message);
			return true;
		}
		throw new RuntimeException(this.name + ": Message " + message.getContent() + " could not be sent from \"" + message.getSender() + "\" to \"" + message.getReceiver() + "\". REASON: machine null");
	}

	/**
	 * Update.
	 *
	 * @param timer
	 * @throws FSMException the fSM exception
	 * @throws Exception
	 * @
	 */
	@Override
	public void updateOnActive(Timer timer) throws FSMException {
		if (program != null && !program.isSuspended()) {
			program.getMachine().update();
			program.updateOtherMachines();
		}
	}
	public void afterUpdate(Timer timer)  {
		
	}
	public void updateGeneral(Timer timer)  {
		
	}
	/**
	 * @return the onServer
	 */
	public boolean isOnServer() {
		return onServer;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String r = name;
		if (program == null) {
			return r + "[NULL_PROGRAM]\n" + debugString;
		}
		if (program.getMachine().getFsm().getCurrentState() == null) {
			return r + "\n->[" + program.getClass().getSimpleName() + "->NULL_STATE]\n" + debugString;
		}
		return r + "\n->[" + program.getClass().getSimpleName() + "->" + program.getMachine().getFsm().getCurrentState().getClass().getSimpleName() + "]\n" + debugString;
	}
	
	public boolean isStateSet() {
		return program != null && program.getMachine() != null && program.getMachine().getFsm() != null
				&& program.getMachine().getFsm().getCurrentState() != null;
	}

}
