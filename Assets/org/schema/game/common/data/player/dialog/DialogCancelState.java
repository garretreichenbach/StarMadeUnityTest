package org.schema.game.common.data.player.dialog;

import java.util.Collections;

import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.State;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.StateInterface;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public class DialogCancelState extends DialogTimedState {

	/**
	 *
	 */
	
	private long durationInMs;
	private DialogTextEntryHook hook;

	public DialogCancelState(AiEntityStateInterface gObj, StateInterface state, DialogTextEntryHook hook, String message, long durationInMs) {
		this(gObj, state, hook, new Object[]{message}, durationInMs);
		
	}
	public DialogCancelState(AiEntityStateInterface gObj, StateInterface state, DialogTextEntryHook hook, Object[] message, long durationInMs) {
		super(gObj, message, state);
		this.durationInMs = durationInMs;
		this.hook = hook;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.player.dialog.DialogTimedState#onEnter()
	 */
	@Override
	public boolean onEnter() {
		if (hook != null) {
			hook.handle(gObj, false);
		}
		return super.onEnter();
	}

	@Override
	public long getDurationInMs() {
		return durationInMs;
	}

	public AwnserContainer[] getAwnsers() {
//		ObjectArrayList<Transition> transitions = getStateData().getTransitions();
//		int size = 0;
//		for (Transition t : transitions) {
//			if (t instanceof AnswerTransition) {
//				size++;
//			}
//		}
//		AwnserContainer[] awnsers = new AwnserContainer[size + 1];
//
//		int i = 0;
//		for (Transition t : transitions) {
//			if (t instanceof AnswerTransition) {
//				awnsers[i] = new AwnserContainer(((AnswerTransition) t).getAwnser());
//				i++;
//			}
//		}
//		awnsers[i] = new AwnserContainer(new Object[]{Lng.str("Goodbye!")});
//		
//		return awnsers;
		Int2ObjectMap<State> transitions = getStateData().getTransitions();
		int size = 0;
		IntList sortedIds = new IntArrayList(size);
		for (int t : transitions.keySet()) {
			Object object = getStateData().getArguments().get(t);
			if (object != null && object instanceof Object[]) {
				size++;
				sortedIds.add(t);
			}
		}
		int max = 1;
		
		AwnserContainer[] awnsers = new AwnserContainer[size + 1];

		int i = 0;
		Collections.sort(sortedIds);
		
		
		for (int t : sortedIds) {
			Object object = getStateData().getArguments().get(t);
			if (object != null && object instanceof Object[]) {
				awnsers[i] = new AwnserContainer((Object[])object);
				i++;
			}
		}
		awnsers[i] = new AwnserContainer(new Object[]{Lng.str("Goodbye!")});
		return awnsers;
	}

}
