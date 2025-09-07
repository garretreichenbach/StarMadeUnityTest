package org.schema.game.common.api;

import java.io.IOException;

import org.json.JSONObject;
import org.schema.schine.auth.SessionCallback;
import org.schema.schine.network.server.AuthenticationRequiredException;
import org.schema.schine.network.server.ServerController;

public class NewSessionCallback implements SessionCallback {
	public final String ackToken;
	public boolean upgraded;
	private String starmadeUsername;
	private String serverName;
	private String createdAtString;
	private double rating;
	private long id;

	public NewSessionCallback(String serverName, String ackToken) {
		super();
		this.ackToken = ackToken;
		this.serverName = serverName;
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
		try {
			if (!useAuthProtect && !requireAuth) {
				System.err.println("[SESSION_CALLBACK] no player verification is necessary!");
				return true;
			}

			System.err.println("[SESSION_CALLBACK] verifying player " + playerName + " with token: " + ackToken);

			JSONObject response = ApiOauthController.verifyAuthToken(ackToken, serverName);

			if (response.has("status") && response.getString("status").equals("ok")) {

				if (response.has("user")) {
					System.err.println("[SERVER] Retrieving public user info");

					JSONObject jsonObject = response.getJSONObject("user");

					this.rating = jsonObject.getDouble("rating");
					this.id = jsonObject.getLong("rating");
					this.starmadeUsername = jsonObject.getString("username");
					this.upgraded = jsonObject.getBoolean("upgraded");
					this.createdAtString = jsonObject.getString("created_at");

					System.err.println("[SERVER] Retrieved public user info: " + starmadeUsername + " for " + playerName);
					System.err.println("[SERVER] [" + starmadeUsername + "] ID: " + id);
					System.err.println("[SERVER] [" + starmadeUsername + "] Rating: " + rating);
					System.err.println("[SERVER] [" + starmadeUsername + "] upgraded: " + upgraded);
					System.err.println("[SERVER] [" + starmadeUsername + "] createdAt: " + createdAtString);

					boolean userProtectionAuthenticated = serverController.isUserProtectionAuthenticated(playerName, starmadeUsername);

					if (useAuthProtect && isUserProtected && !userProtectionAuthenticated) {
						//server uses player protection and user is protected, but the protection
						//doesnt match the username
						throw new IllegalArgumentException();
					}

					return true;
				}
			}
			if (requireAuth) {
				//this server requires authentication,
				//but this player didnt send any
				//or token could not be authenticated
				throw new AuthenticationRequiredException();
			}

			if (useAuthProtect && isUserProtected) {
				//this server uses player protection
				//but this player didnt send any authentication
				//or token could not be authenticated
				throw new IllegalArgumentException();
			}
			return true;
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}

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
