/**
 * <H1>Project R<H1>
 * <p/>
 * <p/>
 * <H2>RegisteredClientOnServer</H2>
 * <H3>org.schema.schine.network</H3>
 * RegisteredClientOnServer.java
 * <HR>
 * Description goes here. If you see this message, please contact me and the
 * description will be filled.<BR>
 * <BR>
 *
 * @author Robin Promesberger (schema)
 * @mail <A HREF="mailto:schemaxx@gmail.com">schemaxx@gmail.com</A>
 * @site <A
 * HREF="http://www.the-schema.com/">http://www.the-schema.com/</A>
 * @project JnJ / VIR / Project R
 * @homepage <A
 * HREF="http://www.the-schema.com/JnJ">
 * http://www.the-schema.com/JnJ</A>
 * @copyright Copyright � 2004-2010 Robin Promesberger (schema)
 * @licence Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.schema.schine.network;

import java.io.IOException;

import org.schema.schine.network.commands.MessageCommandPackage;
import org.schema.schine.network.server.ServerMessage;
import org.schema.schine.network.server.ServerProcessorInterface;
import org.schema.schine.network.server.ServerStateInterface;

/**
 * The Class RegisteredClientOnServer.
 */
public class RegisteredClientOnServer implements RegisteredClientInterface, Recipient, Identifiable {

	private final NetworkStateContainer localAndRemoteContainer;
	private final SynchronizationContainerController synchController;
	public boolean wasFullSynched;
	/**
	 * The id.
	 */
	private int id;
	/**
	 * The player description.
	 */
	private String playerName;
	/**
	 * The server processor.
	 */
	private ServerProcessorInterface serverProcessor;
	/**
	 * The connected.
	 */
	private boolean connected;
	private Object player;
	private String starmadeName;

	private boolean upgradedAccount;

	/**
	 * Instantiates a new registered client on server.
	 *
	 * @param id          the id
	 * @param description the description
	 */
	public RegisteredClientOnServer(int id, String playerName, ServerStateInterface state) {
		this.id = id;
		this.playerName = playerName;
		connected = true;

		localAndRemoteContainer = new NetworkStateContainer(true, state);
		synchController = new SynchronizationContainerController(localAndRemoteContainer, state, true);

	}

	public boolean checkConnection() {
		if (!connected) {
			return false;
		}
		if (!serverProcessor.isConnectionAlive()) {
			return false;
		}

		return true;
	}

	@Override
	public void executedAdminCommand() {
		//nothing to do
		//maybe log?
	}

	@Override
	public int getId() {
		return id;
	}

	/**
	 * Gets the player description.
	 *
	 * @return the player description
	 */
	@Override
	public String getClientName() {
		return playerName;
	}

	@Override
	public void serverMessage(String message) throws IOException {
		System.err.println("[SEND][SERVERMESSAGE] " + message + " to " + this);
		ServerMessage m = new ServerMessage();
		m.setMessage(new Object[] {"[SERVER] "+message});
		
		m.type = ServerMessage.MESSAGE_TYPE_SIMPLE;
		m.receiverPlayerId = 0; //server message
		
		MessageCommandPackage pack = new MessageCommandPackage();
		pack.message = m;
		pack.send(serverProcessor);
		
	}

	/**
	 * @param playerName the playerName to set
	 */
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	@Override
	public void setId(int id) {
		this.id = id;

	}

	public String getIp() {
		try {
			return serverProcessor.getClientIp().toString().replace("/", "");
		} catch (Exception e) {
			e.printStackTrace();
			return "0.0.0.0";
		}
	}

	public NetworkStateContainer getLocalAndRemoteObjectContainer() {
		return localAndRemoteContainer;
	}

	public Object getPlayerObject() {
		return player;
	}

	public void setPlayerObject(Object player) {
		this.player = player;
	}

	/**
	 * Gets the processor.
	 *
	 * @return the processor
	 */
	public ServerProcessorInterface getProcessor() {
		return serverProcessor;
	}

	/**
	 * Sets the processor.
	 *
	 * @param serverProcessor the new processor
	 */
	public void setProcessor(ServerProcessorInterface serverProcessor) {
		this.serverProcessor = serverProcessor;
	}

	/**
	 * @return the synchController
	 */
	public SynchronizationContainerController getSynchController() {
		return synchController;
	}


	/**
	 * Checks if is connected.
	 *
	 * @return true, if is connected
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Sets the connected.
	 *
	 * @param b the new connected
	 */
	public void setConnected(boolean b) {
		this.connected = false;
	}


	public void serverMessage(ServerMessage message) throws IOException {
		System.err.println("[SEND][SERVERMESSAGE] " + message + " to " + this);
		MessageCommandPackage pack = new MessageCommandPackage();
		pack.message = message;
		pack.send(serverProcessor);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[RegisteredClient: " + playerName + " (ID " + id + ") " + (starmadeName != null ? "[" + starmadeName + "]" : "") + "con: "
				+ connected+"]";
	}

	/**
	 * @return the starmadeName
	 */
	public String getStarmadeName() {
		return starmadeName;
	}

	/**
	 * @param starmadeName the starmadeName to set
	 */
	public void setStarmadeName(String starmadeName) {
		this.starmadeName = starmadeName;
	}

	/**
	 * @return the upgradedAccount
	 */
	public boolean isUpgradedAccount() {
		return upgradedAccount;
	}

	/**
	 * @param upgradedAccount the upgradedAccount to set
	 */
	public void setUpgradedAccount(boolean upgradedAccount) {
		this.upgradedAccount = upgradedAccount;
	}

	@Override
	public void blockFromLogout() {
				
	}

	@Override
	public void disconnect() {
				
	}
}
