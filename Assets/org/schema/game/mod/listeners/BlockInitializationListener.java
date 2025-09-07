package org.schema.game.mod.listeners;

public interface BlockInitializationListener extends ModListener {

	/**
	 * this callback is called after the block config is parsed and
	 * initialized in the static functions of ElementKeyMap
	 */
	public void onInitializeBlockData();
}
