package org.schema.schine.graphicsengine.core.settings;

public class PrefixNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	

	public PrefixNotFoundException(String prefix) {
		super("ERROR: prefix not found: " + prefix);
	}

}
