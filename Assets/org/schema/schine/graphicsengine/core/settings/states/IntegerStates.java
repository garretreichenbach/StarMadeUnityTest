package org.schema.schine.graphicsengine.core.settings.states;

import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class IntegerStates extends States<Integer> {

	private int min;
	private int max;

	private int state;

	public IntegerStates(int startState, int min, int max) {
		this.min = min;
		this.max = max;
		this.state = startState;
	}

	@Override
	public boolean contains(Integer state) {
		return state >= min && state <= max;
	}

	@Override
	public Integer getFromString(String arg)
			throws StateParameterNotFoundException {
		try {
			return Math.max(min, Math.min(max, Integer.parseInt(arg)));
		} catch (NumberFormatException e) {
//			e.printStackTrace();
			throw new StateParameterNotFoundException(arg, null);
		}
	}

	@Override
	public String getType() {
		return "Int";
	}

	@Override
	public Integer next() throws StateParameterNotFoundException {
		state = state + 1 > max ? min : state + 1;
		return state;
	}

	@Override
	public Integer previous() throws StateParameterNotFoundException {
		state = state - 1 < min ? max : state - 1;
		return state;
	}

	@Override
	public Tag toTag() {
		return new Tag(Type.INT, null, state);
	}

	@Override
	public Integer readTag(Tag tag) {
		this.state = (Integer) tag.getValue();
		return this.state;
	}

	@Override
	public Integer getCurrentState() {
		return state;
	}

	@Override
	public void setCurrentState(Integer state) {
		this.state = state;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IntStates(" + state + " [" + min + "," + max + "])";
	}
	
}
