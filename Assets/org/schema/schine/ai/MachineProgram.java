package org.schema.schine.ai;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.schema.schine.ai.stateMachines.AIConfiguationElementsInterface;
import org.schema.schine.ai.stateMachines.AiEntityState;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.StateInterface;

public abstract class MachineProgram<E extends AiEntityState> {

	private final E entityState;

	protected HashMap<String, FiniteStateMachine<?>> machines = new HashMap<String, FiniteStateMachine<?>>();

	private FiniteStateMachine<?> currentMachine;

	private boolean suspended;

	public MachineProgram(E entityState, boolean startSuspended) {
		super();
		this.entityState = entityState;

		initializeMachines(machines);
		currentMachine = machines.get(getStartMachine());
		assert (currentMachine != null);
		this.suspended = startSuspended;
	}

	public MachineProgram(E entityState,
	                      boolean startSuspended,
	                      HashMap<String, FiniteStateMachine<?>> machines) {
		this.entityState = entityState;

		if (machines != null) {
			reinit(machines);
		}
		this.suspended = startSuspended;
	}

	public void reinit(HashMap<String, FiniteStateMachine<?>> machines) {
		this.machines = machines;
		currentMachine = machines.get(getStartMachine());
		assert (this.machines.isEmpty() || currentMachine != null) : machines + "; " + getStartMachine();
	}

	public void setCurrentMachine(String name) {
		currentMachine = machines.get(name);
	}

	public FiniteStateMachine<?> getOtherMachine(String string) {
		return machines.get(string);
	}

	public Set<String> getMachineNames() {
		return machines.keySet();
	}

	public abstract void onAISettingChanged(AIConfiguationElementsInterface setting) throws FSMException;

	/**
	 * @return the entityState
	 */
	public E getEntityState() {
		return entityState;
	}

	public FiniteStateMachine<?> getMachine() {
		return currentMachine;
	}

	protected abstract String getStartMachine();

	public StateInterface getState() {
		return entityState.getState();
	}

	protected abstract void initializeMachines(HashMap<String, FiniteStateMachine<?>> machines);

	public boolean isSuspended() {
		return suspended;
	}

	public void onStateChanged(State oldState, State currentState) {
	}

	public void onSuspended() {
	}

	public void onUnSuspended() {
	}

	public void suspend(boolean s) {
		if(isAlwaysOn()){
			this.suspended = false;
		}else{
			if (s != suspended) {
				if (s) {
					onSuspended();
				} else {
					onUnSuspended();
				}
				//			System.err.println("[MACHINEPROGRAM] "+this+" set suspension to "+s);
			}
			this.suspended = s;
		}
	}

	public boolean isAlwaysOn() {
		return false;
	}

	public void update(Timer timer) throws FSMException, Exception {
		entityState.updateOnActive(timer);
	}

	public void updateOtherMachines() throws FSMException {

	}

	public Collection<FiniteStateMachine<?>> getMachines() {
		return machines.values();
	}

	public boolean isInStartMachine() {
		return currentMachine == machines.get(getStartMachine());
	}

}
