package org.schema.game.common.controller;

import org.schema.game.common.data.element.ElementInformation;

public class CannotBeControlledException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	public final ElementInformation info;
	public final ElementInformation fromInfo;

	public CannotBeControlledException(ElementInformation fromInfo, ElementInformation toInfo) {
		this.fromInfo = fromInfo;
		this.info = toInfo;
	}

}
