package org.schema.schine.network.commands;

import api.StarLoaderHooks;
import api.listener.events.network.ClientLoginEvent;
import api.mod.StarLoader;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.schema.common.LogUtil;
import org.schema.common.util.Version;
import org.schema.schine.auth.SessionCallback;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.RegisteredClientOnServer;
import org.schema.schine.network.server.*;

public class LoginRequest{

	public static ObjectOpenHashSet<String> reserved = new ObjectOpenHashSet<String>();

	static {
		reserved.add("[FACTION]");
		reserved.add("[STARMADE]");
		reserved.add("[SYSTEM]");
		reserved.add("[SERVER]");
	}
	public enum LoginCode {

		SUCCESS_LOGGED_IN(0, ""),
		ERROR_GENRAL_ERROR(-1, "Server: general error"),
		ERROR_ALREADY_LOGGED_IN(-2, "Server: name already logged in on this server\n" +
				"\n\n\n(If you are retrying after a socket exception,\n" +
				"please wait 3 minutes for the server to time-out your old connection)"),
		ERROR_ACCESS_DENIED(-3, "Server: access denied"),
		ERROR_SERVER_FULL(-4, "Server: this server is full. Please try again later."),
		ERROR_WRONG_CLIENT_VERSION(-5, ""),
		ERROR_YOU_ARE_BANNED(-6, "Server: you are banned from this server"),
		ERROR_AUTHENTICATION_FAILED(-7, "Server Reject: this login name is protected on this server!\n\n" +
				"You either aren't logged in via uplink,\n" +
				"or the protected name belongs to another user!\n\n" +
				"Please use the \"Uplink\" menu to authenticate this name!"),
		ERROR_NOT_ON_WHITELIST(-8, "Server: you are not whitelisted on this server"),
		ERROR_INVALID_USERNAME(-9, "Server: your username is not allowed.\nMust be at least 3 characters.\nOnly letters, numbers, '-' and '_' are allowed."),
		ERROR_AUTHENTICATION_FAILED_REQUIRED(-10, ""
				+ "Server: this server requires authentication via the StarMade registry.\n"
				+ "This requirement is usually turned on by a server admin to deal with trolling/griefing.\n"
				+ "Please use the 'Uplink' button to enter your StarMade Registry credentials.\n"
				+ "\n"
				+ "If you don't have StarMade credentials yet, create one for free on www.star-made.org.\n"
				+ "And if you are playing on steam you can upgrade your account via steam link."
				+ ""),
		ERROR_NOT_ADMIN(-11, "This login can only be made as admin"),;

		public final int code;
		private final String msg;

		LoginCode(int code, String msg) {
			this.code = code;
			this.msg = msg;
		}

		public static LoginCode getById(int id) {
			for (LoginCode c : values()) {
				if (c.code == id) {
					return c;
				}
			}
			throw new IllegalArgumentException("ID: " + id);
		}

		public String errorMessage() {
			return msg;
		}
	}
	public String playerName;
	public Version version;
	public String uniqueSessionID;
	public String lastLogin;
	public int id;
	public ServerProcessorInterface serverProcessor;
	public ServerStateInterface state;
	public String authCodeToken;
	public byte userAgent;
	private LoginCode returnCode;
	private boolean authd = false;
	private RegisteredClientOnServer c;
	public static boolean isLoginNameValid(String playerName){
		if (playerName.length() <= 0) {
			return false;
		} else if (playerName.length() > 32) {
			return false;
		} else if (playerName.length() <= 2) {
			return false;
		} else if (playerName.matches("[_-]+")) {
			return false;
		} else if (reserved.contains(playerName)) {
			return false;
		} else {
			if (!playerName.matches("[a-zA-Z0-9_-]+")) {
				return false;
			}
		}
		return true;
	}
	void login() {
		assert(state.isSynched());
		System.err.println("[SERVER] PROCESSING LOGIN ("+playerName+")");
		
		// attach this processor to the client
		c = new RegisteredClientOnServer(id, playerName, state);
		c.setProcessor(serverProcessor);
		serverProcessor.setClient(c);

		returnCode = null;
		SessionCallback sessionCallback;
		sessionCallback = state.getSessionCallBack(uniqueSessionID, authCodeToken);
		try {
			if (!state.getController().authenticate(playerName, sessionCallback)) {
				throw new IllegalArgumentException();
			} else {
				authd = true;
			}
		} catch (AuthenticationRequiredException e1) {
			e1.printStackTrace();
			returnCode = LoginCode.ERROR_AUTHENTICATION_FAILED_REQUIRED;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			returnCode = LoginCode.ERROR_AUTHENTICATION_FAILED;
		}

		if(!isLoginNameValid(playerName)){
			returnCode = LoginCode.ERROR_INVALID_USERNAME;
		}
		if (!state.checkUserAgent(userAgent, playerName)) {
			returnCode = LoginCode.ERROR_NOT_ADMIN;
		}

		try {

			StringBuffer failReason = new StringBuffer();
			if (returnCode == null && authd) {
				System.err.println("[LOGIN] successful auth: now protecting username " + playerName + " with " + sessionCallback.getStarMadeUserName());
				state.getController().protectUserName(playerName, sessionCallback);
				c.setStarmadeName(sessionCallback.getStarMadeUserName());
				c.setUpgradedAccount(sessionCallback.isUpgraded());
				returnCode = state.getController().registerClient(c, version, failReason);
			}else {
				if(returnCode == null) {
					returnCode = LoginCode.ERROR_AUTHENTICATION_FAILED_REQUIRED;
				}
			}

			LoginAnswerCommandPackage pack = new LoginAnswerCommandPackage();
			pack.idOrFailedReturnCode = returnCode == LoginCode.SUCCESS_LOGGED_IN ? id : returnCode.code;
			pack.playerName = playerName;
			pack.starMadeName = c.getStarmadeName() != null ? c.getStarmadeName() : "";
			pack.upgradedAccount = sessionCallback.isUpgraded();
			pack.serverVersion = state.getVersion();
			pack.failReson = failReason.toString();



			if (returnCode != LoginCode.SUCCESS_LOGGED_IN) {
				System.err.println("[SERVER][LOGIN] login failed (" + returnCode.name() + "): SET CLIENT TO NULL");
				LogUtil.log().fine("[LOGIN] login failed for " + serverProcessor.getIp() + " (" + returnCode.name() + ")");
				serverProcessor.setClient(null);
				serverProcessor.disconnectAfterSent();
				if (!state.filterJoinMessages()) {
					state.getController().broadcastMessage(Lng.astr("%s's connection failed (%s).", playerName, returnCode.name()), ServerMessage.MESSAGE_TYPE_SIMPLE);
				}
			} else {
				System.out.println("[SERVER][LOGIN] login received. returning login info for " + serverProcessor.getClient() + ": returnCode: " + returnCode);
				//INSERTED CODE
				//Todo: Update event
					ClientLoginEvent event = new ClientLoginEvent(this, this.returnCode.code, this.authd, version.toString(), (ServerProcessor) serverProcessor, c, this.playerName);
					StarLoaderHooks.onClientLoginEvent(event);
					StarLoader.fireEvent(event, true);
					///LogUtil.log().fine("[LOGIN] logged in " + serverProcessor.getClient() + " (" + serverProcessor.getIp() + ")");
				if (!state.filterJoinMessages()) {
					state.getController().broadcastMessage(Lng.astr("%s has joined the game.",  playerName), ServerMessage.MESSAGE_TYPE_SIMPLE);
				}
			}
			pack.send(serverProcessor);

			state.onClientRegistered(c);

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("[SERVER] LOGIN EXCEPTION CATCH: "+e.getClass()+" "+e.getMessage());
		}
	}

	public void prepare() {
		System.err.println("[SERVER] PREPARING LOGIN ("+playerName+")");
	}

}
