package org.schema.schine.graphicsengine.core.settings.states;

import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class LongStates extends States<Long> {

	private long min;
	private long max;

	private long state;

	public LongStates(long min, long max, long startState) {
		this.min = min;
		this.max = max;
		this.state = startState;
	}

	@Override
	public boolean contains(Long state) {
		return state >= min && state <= max;
	}

	@Override
	public Long getFromString(String arg)
			throws StateParameterNotFoundException {
		try {
			return Math.max(min, Math.min(max, Long.parseLong(arg)));
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new StateParameterNotFoundException(arg, null);
		}
	}

	@Override
	public String getType() {
		return "Long";
	}

	@Override
	public Long next() throws StateParameterNotFoundException {
		state = state + 1 > max ? min : state + 1;
		return state;
	}

	@Override
	public Long previous() throws StateParameterNotFoundException {
		state = state - 1 < min ? max : state - 1;
		return state;
	}

	@Override
	public Tag toTag() {
		return new Tag(Type.LONG, null, state);
	}

	@Override
	public Long readTag(Tag tag) {
		this.state = (Long) tag.getValue();
		return this.state;
	}

	@Override
	public Long getCurrentState() {
		return state;
	}

	@Override
	public void setCurrentState(Long state) {
		this.state = state;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "LongStates(" + state + " [" + min + "," + max + "])";
	}
}
