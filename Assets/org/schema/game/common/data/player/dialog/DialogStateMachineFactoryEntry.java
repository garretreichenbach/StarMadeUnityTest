package org.schema.game.common.data.player.dialog;

import java.util.Arrays;
import java.util.Map.Entry;

import javax.script.Bindings;

import org.schema.game.client.controller.tutorial.factory.AbstractFSMFactory;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.network.StateInterface;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public abstract class DialogStateMachineFactoryEntry {

	private final ObjectArrayList<DialogStateMachineFactoryEntry> childs = new ObjectArrayList<DialogStateMachineFactoryEntry>();
	int transitionsMade = 0;
	private DialogTextEntryHookLua hook;
	private Object2ObjectOpenHashMap<DialogStateMachineFactoryEntry, Object[]> awnsers = new Object2ObjectOpenHashMap<DialogStateMachineFactoryEntry, Object[]>();
	private DialogStateMachineFactoryEntry parent;

	private State s;

	private String entryMarking;

	private boolean awnserEntry = true;

	public DialogStateMachineFactoryEntry() {
	}

//	public static AnswerTransition getAwnserTransition(Object[] awnserTransition, int awnserIndex) {
//		return new AnswerTransition(awnserTransition, awnserIndex);
//	}

//	public static HookTransition getHookTransition(int awnserIndex) {
//		return new HookTransition(awnserIndex);
//	}

	public void add(DialogStateMachineFactoryEntry entry, String awnser) {
		add(entry, new Object[]{awnser});
	}
	public void add(DialogStateMachineFactoryEntry entry, Object[] awnser) {
		entry.parent = this;
		childs.add(entry);
		System.err.println("PUT AWNSERS: "+entry+"; "+Arrays.toString(awnser));
		awnsers.put(entry, awnser);
	}

	public boolean isRoot() {
		return parent == null;
	}

	public void addReaction(DialogTextEntryHookLua hook, int returnValue, DialogStateMachineFactoryEntry entry) {
		assert (hook == this.hook) : "HOOK DIFFERENT (DialogStateMachineFactoryEntry) " + hook + "; " + this.hook;
		if (hook != null) {
			hook.getHookChilds().put(returnValue, entry);
		} else {
			assert (false) : hook;
		}
	}

	/**
	 * @return the hook
	 */
	public DialogTextEntryHookLua getHook() {
		return hook;
	}

	/**
	 * @param hook the hook to set
	 */
	public void setHook(DialogTextEntryHookLua hook) {
		this.hook = hook;
	}

	public abstract State getState(AiEntityStateInterface gObj, StateInterface state, Bindings bindings);

	public void createFrom(Object2ObjectOpenHashMap<String, State> directStateMap, ObjectOpenHashSet<DialogStateMachineFactoryEntry> list, State from, State end, DialogStateMachineFactoryEntry fromEntry, Object[] awnserTransition, AiEntityStateInterface gObj, StateInterface state, AbstractFSMFactory fac, Bindings bindings) {
		if (s == null) {
			s = getState(gObj, state, bindings);

			if (entryMarking != null) {
				directStateMap.put(entryMarking, s);
			}
		}
		simpleAdd(from, s, fromEntry, awnserTransition, fac, bindings);

		for (DialogStateMachineFactoryEntry entry : childs) {
			if (entry.s == null) {
				assert (entry != this);
//				System.err.println("[DIALOG] child-> CREATING FOR: "+this+" -> "+entry);
				entry.createFrom(directStateMap, list, s, end, this, awnsers.get(entry), gObj, state, fac, bindings);
			} else {
				entry.simpleAdd(s, entry.s, this, awnsers.get(entry), fac, bindings);
			}
		}
		if (hook != null) {
			for (Entry<Integer, DialogStateMachineFactoryEntry> e : hook.getHookChilds().entrySet()) {
				int key = e.getKey().intValue();
				DialogStateMachineFactoryEntry entry = e.getValue();
//				System.err.println("[DIALOG] CREATING HOOK FOR: "+this+" -"+key+"> "+entry);
				if (entry.s == null) {
					assert (entry != this);

					entry.createHook(directStateMap, list, s, end, this, key, gObj, state, fac, bindings);
				} else {
					entry.hookAdd(s, entry.s, this, key, fac, bindings);
				}
			}
		}

		if (childs.isEmpty()) {
			fac.transition(s, end, s);
		}

	}

	public void createHook(Object2ObjectOpenHashMap<String, State> directStateMap, ObjectOpenHashSet<DialogStateMachineFactoryEntry> list, State from, State end, DialogStateMachineFactoryEntry fromEntry, int hookIndex, AiEntityStateInterface gObj, StateInterface state, AbstractFSMFactory fac, Bindings bindings) {

		if (s == null) {
			s = getState(gObj, state, bindings);
			if (entryMarking != null) {
				directStateMap.put(entryMarking, s);
			}
		}
		hookAdd(from, s, fromEntry, hookIndex, fac, bindings);

		for (DialogStateMachineFactoryEntry entry : childs) {
			if (entry.s == null) {
				assert (entry != this);
//				System.err.println("HOOK: CREATING FOR: "+this+" -> "+entry);
				entry.createFrom(directStateMap, list, s, end, this, awnsers.get(entry), gObj, state, fac, bindings);
			} else {
				entry.simpleAdd(s, entry.s, this, awnsers.get(entry), fac, bindings);
			}
		}
		if (hook != null) {
			for (Entry<Integer, DialogStateMachineFactoryEntry> e : hook.getHookChilds().entrySet()) {
				int key = e.getKey().intValue();
				DialogStateMachineFactoryEntry entry = e.getValue();
//				System.err.println("CREATING HOOK FOR: "+this+" -"+key+"> "+entry);
				if (entry.s == null) {
					assert (entry != this);
					entry.createHook(directStateMap, list, s, end, this, key, gObj, state, fac, bindings);
				} else {
					entry.hookAdd(s, entry.s, this, key, fac, bindings);
				}
			}
		}

		if (childs.isEmpty()) {
			fac.transition(s, end, s);
		}

	}
	
	private void hookAdd(State from, State to, DialogStateMachineFactoryEntry fromEntry, int hookReturnIndex, AbstractFSMFactory fac, Bindings bindings) {
		fac.transition(from, to, from);
		from.addTransition(Transition.DIALOG_HOOK, to, hookReturnIndex, null);
	}

	private void simpleAdd(State from, State to, DialogStateMachineFactoryEntry fromEntry, Object[] awnserTransitionArgument, AbstractFSMFactory fac, Bindings bindings) {
//		if(fromEntry != null){
//			System.err.println("SIMPLE ADD: "+fromEntry+"\n::::: "+fromEntry.getEntryMarking());
//		}

		if (isRoot() || from instanceof VoidState) {
			from.addTransition(Transition.NEXT, to);
		} else {
			fac.transition(from, to, from);
			if (awnserTransitionArgument != null) {
				if (awnserEntry) {
					from.addTransition(Transition.DIALOG_AWNSER, to, fromEntry.transitionsMade++, awnserTransitionArgument);
				}
			}
		}
	}

	/**
	 * @return the entryMarking
	 */
	public String getEntryMarking() {
		return entryMarking;
	}

	/**
	 * @param entryMarking the entryMarking to set
	 */
	public void setEntryMarking(String entryMarking) {
		this.entryMarking = entryMarking;
	}

	/**
	 * @return the awnserEntry
	 */
	public boolean isAwnserEntry() {
		return awnserEntry;
	}

	/**
	 * @param awnserEntry the awnserEntry to set
	 */
	public void setAwnserEntry(boolean awnserEntry) {
		this.awnserEntry = awnserEntry;
	}

}
