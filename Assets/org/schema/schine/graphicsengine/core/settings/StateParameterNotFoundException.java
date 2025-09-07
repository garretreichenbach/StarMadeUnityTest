package org.schema.schine.graphicsengine.core.settings;

import java.util.Arrays;

public class StateParameterNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private final Object[] states;
	private final String arg;

	public StateParameterNotFoundException(String arg, Object[] states) {
		this.arg = arg;
		this.states = states;
	}

	/**
	 * @return the arg
	 */
	public String getArg() {
		return arg;
	}

	/* (non-Javadoc)
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		return "parameter \"" + arg + "\" not recognized; possible parameters: " + states != null ? Arrays.toString(states) : "clazz state";
	}

	/**
	 * @return the states
	 */
	public Object[] getStates() {
		return states;
	}

}
