package org.schema.schine.graphicsengine.core.settings.states;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

public class FloatStates extends States<Float> {

	private final float min;
	private final float max;

	private float state;
	private float staticStep = 0.1f;

	public FloatStates(float startState, float min, float max) {
		this.min = min;
		this.max = max;
		this.state = startState;
	}

	public FloatStates(float startState, float min, float max, float staticStep) {
		this.min = min;
		this.max = max;
		this.state = startState;
		this.staticStep = staticStep;
	}

	public static float round(float value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.floatValue();
	}

	@Override
	public boolean contains(Float state) {
		return state >= min && state <= max;
	}

	@Override
	public Float getFromString(String arg)
			throws StateParameterNotFoundException {
		try {
			return Math.max(min, Math.min(max, Float.parseFloat(arg)));
		} catch (NumberFormatException e) {
			throw new StateParameterNotFoundException(arg, null);
		}
	}

	@Override
	public String getType() {
		return "Float";
	}

	@Override
	public Float next() throws StateParameterNotFoundException {
		float value = state + staticStep > max ? min : state + staticStep;
		;
		state = round(value, 2);
		return state;
	}

	@Override
	public Float previous() throws StateParameterNotFoundException {
		float value = state - staticStep < min ? max : state - staticStep;
		state = round(value, 2);
		return state;
	}

	@Override
	public Tag toTag() {
		return new Tag(Type.FLOAT, null, state);
	}

	@Override
	public Float readTag(Tag tag) {
		this.state = (Float) tag.getValue();
		return this.state;
	}

	@Override
	public Float getCurrentState() {
		return state;
	}

	@Override
	public void setCurrentState(Float state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "FloatStates(" + state + " [" + min + "," + max + "])";
	}



	
	
}
