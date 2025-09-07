package org.schema.game.common.data.player.dialog;

import javax.script.Bindings;

import org.schema.game.client.controller.tutorial.factory.AbstractFSMFactory;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.network.StateInterface;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class DialogStateMachineFactory extends AbstractFSMFactory {

	private DialogStateMachineFactoryEntry rootEntry;
	private AiEntityStateInterface gObj;
	private State thisEndState;
	private Bindings bindings;
	private Object2ObjectOpenHashMap<String, State> directStates;

	public DialogStateMachineFactory(AiEntityStateInterface gObj, State start, State resetStepState, State endState,
	                                 State totalEndState, StateInterface state, Bindings bindings) {
		super(start, resetStepState, totalEndState, state);
		this.gObj = gObj;
		this.thisEndState = endState;
		this.bindings = bindings;
	}

	@Override
	protected State create(State startState) {
		directStates = new Object2ObjectOpenHashMap<String, State>();

		rootEntry.createFrom(directStates, new ObjectOpenHashSet<DialogStateMachineFactoryEntry>(), startState, thisEndState, null, null, gObj, state, this, bindings);

		return thisEndState;
	}

	@Override
	protected void defineStartAndEnd() {
		VoidState voidState = new VoidState(gObj);
		setStartState(voidState);
		setEndState(thisEndState);
	}

	/**
	 * @return the rootEntry
	 */
	public DialogStateMachineFactoryEntry getRootEntry() {
		return rootEntry;
	}

	/**
	 * @param rootEntry the rootEntry to set
	 */
	public void setRootEntry(DialogStateMachineFactoryEntry rootEntry) {
		this.rootEntry = rootEntry;
	}

	/**
	 * @return the directStates
	 */
	public Object2ObjectOpenHashMap<String, State> getDirectStates() {
		return directStates;
	}

}
