package org.schema.schine.auth;

import org.schema.schine.network.server.AuthenticationRequiredException;
import org.schema.schine.network.server.ServerController;

public interface SessionCallback {

	public String getStarMadeUserName();

	boolean isUpgraded();

	boolean authorize(String playerName,
	                  ServerController serverController, boolean requireAuth,
	                  boolean useAuthProtect, boolean isUserProtected) throws AuthenticationRequiredException;

}
