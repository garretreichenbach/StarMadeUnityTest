package org.schema.game.mod.listeners;

public interface NetworkInitializationListener extends ModListener {
	/**
	 * Register Sendable classes:
	 * <p/>
	 * classes must derive from the Sendable interface
	 * Theirs interfaces can be synchornized over the network
	 * <p/>
	 * Their id must be unique for server+client
	 * <p/>
	 * Use the org.schema.schine.network.IdGen.getFreeObjectId(range)
	 * <p/>
	 * on server to get ids.
	 * <p/>
	 * On client there is a command to request a range of ids. However,
	 * Sendable objects should NOT be initialized on client for
	 * security reasons.
	 */

	public void registerNetworkClasses();

	/**
	 * Remote classes are fields of network Objects
	 * <p/>
	 * E.g. RemoteInteger, RemoteLong, RemoteInventory
	 * <p/>
	 * These define the low level synchronization.
	 * <p/>
	 * Remote fields are automatically read with reflection,
	 * and have observers to flag changed fields which then
	 * will be sent to the clients/server.
	 */
	public void registerRemoteClasses();
}
