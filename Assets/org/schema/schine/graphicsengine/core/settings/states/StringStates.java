package org.schema.schine.graphicsengine.core.settings.states;

import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class StringStates extends States<String> {

	private String state;

	public StringStates(String startState) {
		this.state = startState;
	}

	@Override
	public boolean contains(String state) {
		return true;
	}

	@Override
	public String getFromString(String arg)
			throws StateParameterNotFoundException {
		return arg;

	}

	@Override
	public String getType() {
		return "String";
	}

	@Override
	public String next() throws StateParameterNotFoundException {
		return state;
	}

	@Override
	public String previous() throws StateParameterNotFoundException {
		return state;
	}

	@Override
	public Tag toTag() {
		return new Tag(Type.STRING, null, state);
	}

	@Override
	public String readTag(Tag tag) {
		this.state = (String) tag.getValue();
		return this.state;
	}

	@Override
	public String getCurrentState() {
		return state;
	}

	@Override
	public void setCurrentState(String state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "StringStates(" + state + ")";
	}
	
}
