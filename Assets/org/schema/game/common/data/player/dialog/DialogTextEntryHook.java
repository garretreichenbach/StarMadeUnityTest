package org.schema.game.common.data.player.dialog;

import javax.script.Bindings;

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

public class DialogTextEntryHook {

	private final LuaFunction handleFunction;
	private final Varargs args;
	private String endState;
	private String startState;
	private DialogTextEntryHookLua followUp;
	private DialogTextEntryHookLua condition;
	private Bindings bindings;

	public DialogTextEntryHook(LuaFunction handleFunction, Object[] objects, String startState, String endState, DialogTextEntryHookLua followUp, DialogTextEntryHookLua condition, Bindings bindings) {
		super();
		this.handleFunction = handleFunction;
		this.startState = startState;
		this.endState = endState;
		this.followUp = followUp;
		this.condition = condition;
		this.bindings = bindings;

		LuaValue[] vals = new LuaValue[objects.length];

		for (int i = 0; i < objects.length; i++) {
			if (objects[i] instanceof Integer) {
				vals[i] = LuaValue.valueOf((Integer) objects[i]);
			} else if (objects[i] instanceof Boolean) {
				vals[i] = LuaValue.valueOf((Boolean) objects[i]);
			} else if (objects[i] instanceof Double) {
				vals[i] = LuaValue.valueOf((Double) objects[i]);
			} else if (objects[i] instanceof String) {
				vals[i] = LuaValue.valueOf((String) objects[i]);
			} else {
				System.err.println("[LUA][ERROR] argument is ivalid: " + objects[i] + "; " + objects[i].getClass());
				throw new IllegalArgumentException(objects[i] + "; " + objects[i].getClass());
			}
			System.err.println("[LUA] added hook argument: " + objects[i]);
		}
		this.args = LuaValue.varargsOf(vals);
	}

	public boolean handleAsCondition(AiEntityStateInterface gObj) {
		LuaValue luagObj = CoerceJavaToLua.coerce(gObj);
		assert (handleFunction != null) : "DialogTextEntryHook: handle function null";
		Varargs returnValue = handleFunction.invoke(luagObj, args);
//		System.err.println("[LUA] HANDLING CONDITION: "+handleFunction+": "+returnValue.arg1());
		return returnValue.checkboolean(1);

	}

	public void handle(AiEntityStateInterface gObj, boolean isFollowUpHandle) {
		try {
			System.err.println("[LUA] calling hook " + gObj);

			((AICreatureDialogAI) gObj).setConversationState(startState);

			assert (condition == null || followUp != null) : "DialogTextEntryHook: Follow up not null but condition null";
			assert (followUp == null || condition != null) : "DialogTextEntryHook: condition not null but followUp null";

			LuaValue luagObj = CoerceJavaToLua.coerce(gObj);
			assert (handleFunction != null) : "DialogTextEntryHook: handle function null";
			Varargs returnValue = handleFunction.invoke(luagObj, args);

			System.err.println("[LUA] return value with args " + args + " is " + returnValue + ": " + returnValue.checkint(1) + "; isFollowUp " + isFollowUpHandle);

			int ret = returnValue.checkint(1); //1 is the first :(

			try {
				if (!isFollowUpHandle && !(gObj.getStateCurrent() instanceof DialogCancelState)) {
					gObj.getStateCurrent().stateTransition(Transition.DIALOG_HOOK, ret);
				}
			} catch (FSMException e) {
				e.printStackTrace();
				try{throw new Exception("System.exit() called");}catch(Exception ex){ex.printStackTrace();}System.exit(0);
			}

			if (condition != null) {
				((AICreatureDialogAI) gObj).setConversationState(endState);
				((AICreatureDialogAI) gObj).addDelayedConditionFollowUpHook(this, condition, followUp, bindings);
			} else {
				((AICreatureDialogAI) gObj).setConversationState(endState);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
