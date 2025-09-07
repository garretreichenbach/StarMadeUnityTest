package org.schema.game.common.data.player.dialog;

import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.network.StateInterface;

public abstract class DialogSatisfyingCondition extends State {

	/**
	 *
	 */
	
	private final StateInterface gameState;
	protected boolean next;
	protected boolean skipIfSatisfiedAtEnter;
	private Object[] message;
	private boolean forcedSatisfied;

	public DialogSatisfyingCondition(AiEntityStateInterface gObj, Object[] message, StateInterface state) {
		super(gObj);
		this.gameState = state;
		this.message = message;
	}

	private boolean checkBasicCondition() {
		return next;
	}

	protected abstract boolean checkSatisfyingCondition() throws FSMException;

	public void forceSatisfied() {
		System.err.println("Forcing satisfied on " + this);
		this.forcedSatisfied = true;
	}

	/**
	 * @return the gameState
	 */
	public StateInterface getGameState() {
		return gameState;
	}

	/**
	 * @return the message
	 */
	public Object[] getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(Object[] message) {
		this.message = message;
	}

	public void satisfy() {
		next = true;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.ai.stateMachines.State#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + ": " + getMessage()[0].toString().substring(0, Math.min(10, getMessage()[0].toString().length() - 1)) + "...";
	}

	@Override
	public boolean onEnter() {
//		System.err.println("TEST STATE: "+message);
		next = false;
		forcedSatisfied = false;
		try {
			if (skipIfSatisfiedAtEnter && checkSatisfyingCondition()) {
				System.err.println("AUTO SKIP STATE " + this);
				forcedSatisfied = true;
			}
		} catch (FSMException e) {
			e.printStackTrace();
		}
		if (!forcedSatisfied) {
		}
		return true;
	}

	@Override
	public boolean onExit() {
		return true;
	}

	@Override
	public boolean onUpdate() throws FSMException {

		if ((checkBasicCondition() && checkSatisfyingCondition()) || forcedSatisfied) {
			System.err.println("FORCED SATISFIED");
			forcedSatisfied = false;
			getEntityState().getMachine().getFsm().stateTransition(Transition.CONDITION_SATISFIED);
			return false;
		} else {
		}
		if (next) {
		}

		//		System.err.println("CONDITION: "+this+":  next "+next+"; cond "+checkSatisfyingCondition());
		return true;
	}

}
