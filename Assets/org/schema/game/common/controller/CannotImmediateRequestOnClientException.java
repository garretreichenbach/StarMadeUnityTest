package org.schema.game.common.controller;

import org.schema.common.util.linAlg.Vector3i;

public class CannotImmediateRequestOnClientException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private final Vector3i segIndex;

	public CannotImmediateRequestOnClientException(Vector3i segIndex) {
		this.segIndex = new Vector3i(segIndex);
	}

	/**
	 * @return the segIndex
	 */
	public Vector3i getSegIndex() {
		return segIndex;
	}

}
