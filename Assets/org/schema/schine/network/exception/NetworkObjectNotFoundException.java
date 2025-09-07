package org.schema.schine.network.exception;

import java.util.HashMap;

import org.schema.schine.network.objects.NetworkObject;

public class NetworkObjectNotFoundException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	

	public NetworkObjectNotFoundException(int id, Class<? extends NetworkObject> c, String updateString, HashMap<Integer, ? extends NetworkObject> available) {
		super(c.getSimpleName() + " with id " + id + " not found. updateString: " + updateString + ". available: " + available);
	}
}
