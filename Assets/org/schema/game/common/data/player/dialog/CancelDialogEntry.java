package org.schema.game.common.data.player.dialog;

import javax.script.Bindings;

import org.luaj.vm2.LuaFunction;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.network.StateInterface;

public class CancelDialogEntry extends DialogStateMachineFactoryEntry {

	private long timeShown;
	private String message;

	public CancelDialogEntry(String message, long timeShown) {
		super();
		this.message = message;
		this.timeShown = timeShown;
	}

	@Override
	public State getState(AiEntityStateInterface gObj, StateInterface state, Bindings bindings) {
		DialogTextEntryHook actHook = null;
		if (getHook() != null) {
			assert (bindings != null) : "TextEntry: Binding null";
			System.err.println("[LUA] (Cancel) Getting bindings for: " + getHook().getHandleFunction());
			LuaFunction f = (LuaFunction) bindings.get(getHook().getHandleFunction());
			assert (f != null) : "[LUA] (Cancel) No function found: " + getHook().getHandleFunction();

			assert (getHook().getArguments() != null) : "[LUA] (Cancel) Arguments null: " + getHook().getHandleFunction();
			actHook = new DialogTextEntryHook(f, getHook().getArguments(), getHook().getStartState(), getHook().getEndState(), getHook().getFollowUp(), getHook().getCondition(), bindings);
		}

		return new DialogCancelState(gObj, state, actHook, message, timeShown);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "(cancelState: " + message + ")";
	}

}
