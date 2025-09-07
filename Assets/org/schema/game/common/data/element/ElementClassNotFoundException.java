package org.schema.game.common.data.element;

public class ElementClassNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	

	public ElementClassNotFoundException(short key) {
		super("class for type " + key + " not found");
	}

}
