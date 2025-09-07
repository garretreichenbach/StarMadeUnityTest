package org.schema.game.common.controller;

import org.schema.common.util.linAlg.Vector3i;

public class BlockNotBuildTooFast extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	public Vector3i to;

	public BlockNotBuildTooFast(Vector3i to) {
		this.to = to;
	}
}
