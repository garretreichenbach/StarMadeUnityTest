package org.schema.game.common.api;

import org.schema.schine.auth.SessionCallback;
import org.schema.schine.network.server.AuthenticationRequiredException;
import org.schema.schine.network.server.ServerController;

public class OldSessionCallback implements SessionCallback {
	public final String uid;
	public final String loginCode;
	public boolean upgraded;
	private String starmadeUsername;

	public OldSessionCallback(String sessionId, String loginCode) {
		super();
		this.uid = sessionId;
		this.loginCode = loginCode;
	}

	@Override
	public String getStarMadeUserName() {
		return starmadeUsername;
	}

	@Override
	public boolean isUpgraded() {
		return upgraded;
	}

	@Override
	public boolean authorize(String playerName,
	                         ServerController serverController, boolean requireAuth,
	                         boolean useAuthProtect, boolean isUserProtected) throws AuthenticationRequiredException {
		//deprecated
		throw new IllegalArgumentException();
		//		if(this.uid.length() == 0){
		//			System.err.println("[AUTH] "+playerName+" has no uplink code sent");
		//			return false;
		//		}
		//		SessionOldStyle session = new SessionOldStyle();
		//		session.id = this.uid;
		//		session.login = this.loginCode;
		//		try {
		//
		//			//this will throw an exception if the session is invalid (and therefore return false)
		//			OldApiController.getCheckSessionOldStyleUser(playerName, this);
		//			boolean userProtectionAuthenticated = con.isUserProtectionAuthenticated(playerName, getStarmadeUsername());
		//			System.err.println("[AUTH] Session User: "+getStarmadeUsername()+"; authenticated: "+userProtectionAuthenticated);
		//			return ignoreProtection || userProtectionAuthenticated;
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//		}
		//		return false;
	}

	/**
	 * @return the starmadeUsername
	 */
	public String getStarmadeUsername() {
		return starmadeUsername;
	}

	/**
	 * @param starmadeUsername the starmadeUsername to set
	 */
	public void setStarmadeUsername(String starmadeUsername) {
		this.starmadeUsername = starmadeUsername;
	}

}
