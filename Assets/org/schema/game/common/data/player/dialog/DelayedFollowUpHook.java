package org.schema.game.common.data.player.dialog;

import javax.script.Bindings;

import org.luaj.vm2.LuaFunction;

public class DelayedFollowUpHook {

	final DialogTextEntryHook condition;
	final DialogTextEntryHook followUp;
	DialogTextEntryHook dialogTextEntryHook;

	public DelayedFollowUpHook(DialogTextEntryHook dialogTextEntryHook,
	                           DialogTextEntryHookLua condition, DialogTextEntryHookLua followUp, Bindings bindings) {
		super();
		this.dialogTextEntryHook = dialogTextEntryHook;

		System.err.println("[LUA] FollowUp Getting bindings for: " + followUp.getHandleFunction());
		LuaFunction f = (LuaFunction) bindings.get(followUp.getHandleFunction());

		this.followUp = new DialogTextEntryHook(f, followUp.getArguments(), followUp.getStartState(), followUp.getEndState(), followUp.getFollowUp(), followUp.getCondition(), bindings);

		System.err.println("[LUA] Condition Getting bindings for: " + condition.getHandleFunction());
		LuaFunction con = (LuaFunction) bindings.get(condition.getHandleFunction());

		this.condition = new DialogTextEntryHook(con, condition.getArguments(), condition.getStartState(), condition.getEndState(), condition.getFollowUp(), condition.getCondition(), bindings);
	}

	public boolean isSatisfied(AICreatureDialogAI aiCreatureDialogAI) {
		return condition.handleAsCondition(aiCreatureDialogAI);
	}

	public void execute(AICreatureDialogAI aiCreatureDialogAI) {
		System.err.println("[LUA] executing follow up " + followUp);
		followUp.handle(aiCreatureDialogAI, true);
	}

}
