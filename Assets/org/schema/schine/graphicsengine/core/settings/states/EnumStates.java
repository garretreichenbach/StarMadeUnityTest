package org.schema.schine.graphicsengine.core.settings.states;

import java.util.Arrays;
import java.util.Locale;

import org.schema.common.FastMath;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class EnumStates<E extends Enum<E>> extends States<E> {

	public E[] states;
	private int pointer;

	public EnumStates(E... states) {
		assert (states.length > 0);
		this.states = states;
	}

	@Override
	public boolean contains(E state) {
		for (int i = 0; i < states.length; i++) {
			if (state == states[i]) {
				return true;
			}
		}
		return false;
	}

	@Override
	public E getFromString(String arg) throws StateParameterNotFoundException {
		for (int i = 0; i < states.length; i++) {
			if (arg.toLowerCase(Locale.ENGLISH).equals(states[i].name().toLowerCase(Locale.ENGLISH))) {
				pointer = i;
				return states[pointer];
			}
		}
		throw new StateParameterNotFoundException(arg, states);
	}

	@Override
	public String getType() {
		return states[0].getClass().getSimpleName();
	}

	@Override
	public E next() throws StateParameterNotFoundException {
		if (states.length <= 1) {
			return states[pointer];
		}
		pointer = (pointer + 1) % states.length;
		return states[pointer];
	}

	@Override
	public E previous() throws StateParameterNotFoundException {
		if (states.length <= 1) {
			return states[pointer];
		}
		pointer = FastMath.cyclicBWModulo(pointer - 1, states.length);
		return states[pointer];
	}

	@Override
	public Tag toTag() {
		return new Tag(Type.STRING, null, states[pointer].name());
	}

	@Override
	public E readTag(Tag tag) {
		try {
			return getFromString((String) tag.getValue());
		} catch (StateParameterNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public E getCurrentState() {
		return states[pointer];
	}

	@Override
	public void setCurrentState(E state) {
		for (int i = 0; i < states.length; i++) {
			if (state == states[i]) {
				pointer = i;
				break;
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Arrays.toString(states) + "->[" + states[pointer] + "]";
	}
}
