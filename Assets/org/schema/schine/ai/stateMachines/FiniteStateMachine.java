package org.schema.schine.ai.stateMachines;

import api.listener.events.state.FSMStateEnterEvent;
import api.mod.StarLoader;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.MachineProgram;


/**
 * The Class FiniteStateMachine.
 */
public abstract class FiniteStateMachine<E> {


	/**
	 * The current state.
	 */
	State currentState;

	/**
	 * The fsm.
	 */
	private FSMclass fsm;

	/**
	 * The obj.
	 */
	private AiEntityStateInterface obj;

	private MachineProgram<?> robotProgram;

	private State startState;
	private Object2ObjectOpenHashMap<String, State> directStates;
	public FiniteStateMachine(AiEntityStateInterface obj, MachineProgram<?> program, E parameter, State startState) {
		this.obj = obj;

		this.robotProgram = program;

		createFSM(parameter);
		setStartingState(startState);
	}
	/**
	 * Instantiates a new finite state machine.
	 *
	 * @param obj     the obj
	 * @param program the program
	 */
	public FiniteStateMachine(AiEntityStateInterface obj, MachineProgram<?> program, E parameter) {
		assert (obj != null);
		// Logger.println(this,"~~ StartState initialized");
		this.obj = obj;

		this.robotProgram = program;

		createFSM(parameter);
		assert(startState != null);
		//		obj.getPlayer().addMachine(getFsm());
	}

	/**
	 * the main function to create a Final State machine It creates all the
	 * States and Transitions of states Wait t_wait = new Wait(); // transition
	 * <p/>
	 * Waiting wait = new Waiting(getObj()); // abstract state MovingToAttack
	 * mota = new MovingToAttack(getObj()); //abstract state
	 * <p/>
	 * FSMstate s_wait = new FSMstate(1, wait); //a state of this machine with
	 * //one transition and the abstract state //"wait" as starting state
	 * <p/>
	 * s_wait.addTransition(t_wait, mota); //a Transition with t_wait to the
	 * mota state
	 * <p/>
	 * getFsm().addState(s_wait); // adding the state to this machine
	 *
	 * @param parameter
	 */
	public abstract void createFSM(E parameter);

	/**
	 * returns the FSM-class of this machine;.
	 *
	 * @return the fsm
	 */
	public FSMclass getFsm() {
		return fsm;
	}

	/**
	 * Gets the robot program.
	 *
	 * @return the robot program
	 */
	public MachineProgram<?> getMachineProgram() {
		return robotProgram;
	}

	/**
	 * Sets the robot program.
	 *
	 * @param robotProgram the new robot program
	 */
	public void setMachineProgram(MachineProgram<?> robotProgram) {
		this.robotProgram = robotProgram;
	}

	/**
	 * Gets the obj.
	 *
	 * @return the game object this machine is working with
	 */
	public AiEntityStateInterface getObj() {
		return obj;
	}

	/**
	 * executes a message, that has been sent to this machine. state machines
	 * usually override this method to react to messages individually.
	 *
	 * @param message the message
	 */
	public abstract void onMsg(Message message);

	/**
	 * Sets the starting state.
	 *
	 * @param startState the new starting state
	 */
	public void setStartingState(State startState) {
		startState.initRecusively(this);
		startState.setMachineRecusively(this);
		setState(startState);
		this.startState = startState;
		fsm = new FSMclass(startState, this);
	}

	/**
	 * sets the current state of this machine.
	 *
	 * @param state of State
	 */
	public void setState(State state) {
		assert (obj != null);
		this.currentState = state;
		//		System.err.println("the current state "+obj.getRobot().toStringID()+" of the finite state machine is now "+this.currentState);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	/**
	 * calls the onUpdate() function of the current state of this FSM.
	 *
	 * @throws FSMException the fSM exception
	 */
	public void update() throws FSMException {
		if (currentState == null) {
			throw new FSMException("[CRITICAL] no state set! please set the FiniteStateMachine.setStartState(State state) Method in createFSM()");
		}
		State s = currentState;
		// put currentState in temp var, so newState = false is set on this
		// if the state changes in onEnter()
		if (s.isNewState()) {
			//			System.err.println("[MACHINE] state "+currentState.toString()+" is a new state -> onEnter()");
			s.onEnter();
			s.setNewState(false);
			//INSERTED CODE @172
			FSMStateEnterEvent event = new FSMStateEnterEvent(this, s);
			StarLoader.fireEvent(event, true);
			///
		} else {
			//			System.err.println("updateting current state of "+obj.getRobot().toStringID()+" "+currentState);
			s.onUpdate();
		}

	}

	public void init(State state) {
		state.init(this);
	}

	public void reset() {
		setState(startState);
		startState.setNewState(true);
	}

	/**
	 * @return the directStates
	 */
	public Object2ObjectOpenHashMap<String, State> getDirectStates() {
		return directStates;
	}

	/**
	 * @param directStates the directStates to set
	 */
	public void setDirectStates(Object2ObjectOpenHashMap<String, State> directStates) {
		this.directStates = directStates;
	}

	/**
	 * @return the startState
	 */
	public State getStartState() {
		return startState;
	}

	

}
