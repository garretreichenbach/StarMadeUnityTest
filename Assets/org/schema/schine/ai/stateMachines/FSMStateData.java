package org.schema.schine.ai.stateMachines;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;


/**
 * The Class FSMStateData.
 */
public class FSMStateData {

	/**
	 * The Constant serialVersionUID.
	 */
	

	// State[] states;
	/**
	 * The pi inputs.
	 */
	final Int2ObjectMap<State> inOut;
	private final Int2ObjectMap<Object> arguments;
	
	
	/**
	 * is used when the same transition is needed multiple times for different outcomes
	 */
	
	/**
	 * The state.
	 */
	private State state;

	/**
	 * A FSM State.
	 *
	 * @param state The Starting State
	 */
	public FSMStateData(State state) {
		this.state = state;
		inOut = new Int2ObjectOpenHashMap<State>();
		arguments = new Int2ObjectOpenHashMap<Object>();
	}

	/**
	 * Adds the transition.
	 *
	 * @param input  the input
	 * @param output the output
	 * @return true, if successful
	 */
	public boolean addTransition(Transition input, State output) {
		addTransition(input, output, 0, null);
		return true;
	}
	
	public static int getId(Transition t, int subId){
		return t.ordinal()*10000+subId;
	}
	public boolean addTransition(Transition input, State output, int subId, Object argument) {
		assert (output != null);
		
		int id = getId(input, subId);
		inOut.put(id, output);
		if(argument != null){
			arguments.put(id, argument);
		}
		if (output == null) {
			throw new NullPointerException("output null: " + output);
		}
		if (input == null) {
			throw new NullPointerException("input null: " + input);
		}
		return true;
	}


	/**
	 * Gets the output.
	 *
	 * @param input the input
	 * @return the output
	 * @throws FSMException the fSM exception
	 */
	public State getOutput(Transition input) throws FSMException {
		return getOutput(input, 0);
	}
	/**
	 * Gets the output.
	 *
	 * @param input the input
	 * @return the output
	 * @throws FSMException the fSM exception
	 */
	public State getOutput(Transition input, int subId) throws FSMException {
		
		State s = inOut.get(getId(input, subId));
		if (s == null) {
			throw new FSMException(state, input);
		}
		return s;
		
	}

	public boolean existsOutput(Transition input) {
		return inOut.containsKey(getId(input, 0));

	}
	public boolean existsOutput(Transition input, int subId) {
		return inOut.containsKey(getId(input, subId));
		
	}

	/**
	 * @return the state
	 */
	public State getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(State state) {
		this.state = state;
	}

	public int getTransitionCount() {
		return inOut.size();
	}

	public boolean removeTransition(Transition t) {
		State remove = inOut.remove(getId(t, 0));
		arguments.remove(getId(t, 0));
		return remove != null;
	}

	public void setRecusrively(FiniteStateMachine finiteStateMachine) {
		for (State o : inOut.values()) {
			o.setMachineRecusively(finiteStateMachine);
		}
	}

	public void initRecusrively(FiniteStateMachine finiteStateMachine) {
		for (State o : inOut.values()) {
			if (o == null) {
				throw new NullPointerException("this state is null: " + o);
			}
			o.initRecusively(finiteStateMachine);
		}
	}

	public Int2ObjectMap<State> getTransitions() {
		return inOut;
	}

	public Int2ObjectMap<Object> getArguments() {
		return arguments;
	}


}
