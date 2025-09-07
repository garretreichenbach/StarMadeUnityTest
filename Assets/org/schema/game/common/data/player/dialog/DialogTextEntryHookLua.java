package org.schema.game.common.data.player.dialog;

import java.util.Arrays;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class DialogTextEntryHookLua {

	private final String handleFunction;
	private final Int2ObjectOpenHashMap<DialogStateMachineFactoryEntry> hookChilds = new Int2ObjectOpenHashMap<DialogStateMachineFactoryEntry>();
	private final Object[] arguments;

	private String startState = "NONE";
	private String endState = "NONE";
	private DialogTextEntryHookLua followUp;
	private DialogTextEntryHookLua condition;

	public DialogTextEntryHookLua(String handleFunction, Object... arguments) {
		super();
		this.handleFunction = handleFunction;
		System.err.println("[LUA] ARGUMENTS for " + handleFunction + ": " + Arrays.toString(arguments));
		this.arguments = arguments;
	}

	/**
	 * @return the handleFunction
	 */
	public String getHandleFunction() {
		return handleFunction;
	}

	/**
	 * @return the hookChilds
	 */
	public Int2ObjectOpenHashMap<DialogStateMachineFactoryEntry> getHookChilds() {
		return hookChilds;
	}

	public Object[] getArguments() {
		return arguments;
	}

	/**
	 * @return the startState
	 */
	public String getStartState() {
		return startState;
	}

	/**
	 * @param startState the startState to set
	 */
	public void setStartState(String startState) {
		this.startState = startState;
	}

	/**
	 * @return the endState
	 */
	public String getEndState() {
		return endState;
	}

	/**
	 * @param endState the endState to set
	 */
	public void setEndState(String endState) {
		this.endState = endState;
	}

	/**
	 * @return the followUp
	 */
	public DialogTextEntryHookLua getFollowUp() {
		return followUp;
	}

	/**
	 * @param followUp the followUp to set
	 */
	public void setFollowUp(DialogTextEntryHookLua followUp) {
		this.followUp = followUp;
	}

	/**
	 * @return the condition
	 */
	public DialogTextEntryHookLua getCondition() {
		return condition;
	}

	/**
	 * @param condition the condition to set
	 */
	public void setCondition(DialogTextEntryHookLua condition) {
		this.condition = condition;
	}

}
