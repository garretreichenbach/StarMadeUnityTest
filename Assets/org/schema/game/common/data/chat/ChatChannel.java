package org.schema.game.common.data.chat;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.vecmath.Vector4f;

import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.chat.ChannelRouter.ChannelType;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.network.objects.ChatChannelModification;
import org.schema.game.network.objects.ChatChannelModification.ChannelModType;
import org.schema.game.network.objects.ChatMessage;
import org.schema.game.network.objects.ChatMessage.ChatMessageType;
import org.schema.game.network.objects.remote.RemoteChatChannel;
import org.schema.game.network.objects.remote.RemoteChatMessage;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.PlayerNotFountException;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActivatableTextBar;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerStateInterface;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class ChatChannel extends GUIObservable implements ChatCallback {

	private static final byte CLIENT_TAG_VERSION = 1;
	private static final byte SERVER_TAG_VERSION = 2;
	protected final ObjectArrayList<PlayerState> members = new ObjectArrayList<PlayerState>();
	private final boolean onServer;
	private final StateInterface state;
	private final ObjectArrayList<ChatMessage> messageLog = new ObjectArrayList<ChatMessage>();
	private final ObjectArrayList<ChatMessage> visibleChatLog = new ObjectArrayList<ChatMessage>();
	private final int id;
	protected Vector4f color = new Vector4f(1, 1, 1, 1);
	private int read = 0;
	private boolean sticky = false;
	private boolean stickyFull = false;
	private GUIActivatableTextBar chatBar;
	private boolean clientOpen;
	private String clientCurrentPassword = "";
	public ChatChannel(StateInterface state, int id) {
		this.state = state;
		this.onServer = state instanceof ServerStateInterface;
		this.id = id;
	}

	public static PublicChannel loadServerChannel(GameServerState state, Tag tag) {
		Tag[] t = (Tag[]) tag.getValue();
		byte version = (Byte) t[0].getValue();

		String uid = (String) t[1].getValue();

		Tag[] modTags = (Tag[]) t[2].getValue();
		String[] mods = new String[modTags.length - 1];
		for (int i = 0; i < mods.length; i++) {
			mods[i] = (String) modTags[i].getValue();
		}

		Tag[] bannedTags = (Tag[]) t[3].getValue();
		String[] banned = new String[bannedTags.length - 1];
		for (int i = 0; i < banned.length; i++) {
			banned[i] = (String) bannedTags[i].getValue();
		}

		/*
		 * check version for backwards compatibility
		 */

		if (version <= 1) {
			String passwd = (String) t[4].getValue();

			boolean permanent = (Byte) t[5].getValue() > 0;
			//boolean publicChannel = (Byte) t[6].getValue() > 0;

			PublicChannel c = new PublicChannel(state, ++ChannelRouter.idGen, uid, passwd, permanent, mods);
			c.addToBanned(banned);
			return c;

		} else {
			Tag[] mutedTags = (Tag[]) t[4].getValue();
			String[] muted = new String[mutedTags.length - 1];
			for (int i = 0; i < muted.length; i++) {
				muted[i] = (String) mutedTags[i].getValue();
			}

			String passwd = (String) t[5].getValue();

			boolean permanent = (Byte) t[6].getValue() > 0;
			//boolean publicChannel = (Byte) t[6].getValue() > 0;

			PublicChannel c = new PublicChannel(state, ++ChannelRouter.idGen, uid, passwd, permanent, mods);
			c.addToBanned(banned);
			c.addToMuted(muted);
			return c;
		}

	}

	public static LoadedClientChatChannel loadClientChannel(Tag tag) {
		Tag[] t = (Tag[]) tag.getValue();
		// byte version = (Byte) t[0].getValue();
		String uid = (String) t[1].getValue();
		boolean sticky = (Byte) t[2].getValue() > 0;
		boolean fullStick = (Byte) t[3].getValue() > 0;
		boolean open = (Byte) t[4].getValue() > 0;
		String passwd = (String) t[5].getValue();

		return new LoadedClientChatChannel(uid, sticky, fullStick, open, passwd);
	}

	public abstract Collection<ClientChannel> getClientChannelsInChannel();

	@Override
	public void markRead() {
		read = messageLog.size();
	}

	@Override
	public int getUnread() {
		return messageLog.size() - read;
	}

	public void send(ChatMessage message) {
		if (onServer) {
			Collection<ClientChannel> clientChannelsInChannel = getClientChannelsInChannel();
			for (ClientChannel c : clientChannelsInChannel) {
				c.getNetworkObject().chatBuffer.add(new RemoteChatMessage(message, onServer));
			}
		} else {
			boolean send = checkPrefixClient(message);
			if (send) {
				((GameClientState) state).getController().getClientChannel().getNetworkObject().chatBuffer
						.add(new RemoteChatMessage(message, onServer));
			}
		}

	}

	/**
	 * @param message
	 * @return true if message has to be sent
	 */
	public boolean checkPrefixClient(ChatMessage message) {

		boolean needsToBeChecked = message.receiverType == ChatMessageType.DIRECT || isCheckForPrefixesOnClient();

		if (!needsToBeChecked) {
			return true;
		}

		return ((GameClientState) state).getChannelRouter().checkPrefixClient(message, this);
	}

	public abstract boolean isPermanent();

	private void addToDefaults(ChatMessage message) {
		if (!onServer) {
			if (sticky) {
				ChatMessage chatMessage = new ChatMessage(message);
				chatMessage.setChannel(this);
				if (ChannelRouter.VISIBLE_CHAT_INSERT_AT_ZERO) {
					visibleChatLog.add(0, chatMessage);
				} else {
					visibleChatLog.add(chatMessage);
				}
			} else {
				((GameClientState) state).getChannelRouter().addToDefaultChannelVisibleChatLogOnClient(message);
			}
			((GameClientState) state).getChannelRouter().addToDefaultChannelChatLogOnClient(message);
		}
	}

	public void receive(ChatMessage message) {
		messageLog.add(message);
		addToDefaults(message);
		notifyObservers();
	}

	protected void onClientChannelRemoveServer(ClientChannel clientChannel) {
		assert (onServer);
		boolean remove = members.remove(clientChannel.getPlayer());

		if (remove) {
			System.err.println("[SERVER][ClientChannel] successfully removed player on logoff "
					+ clientChannel.getPlayer() + " from " + getUniqueChannelName());
			clientChannel.getPlayer().getPlayerChannelManager().removedFromChat(this);

			sendLeftUpdateToClients(clientChannel);
		} else {
			assert (false) : "Not send note to clients! " + getUniqueChannelName() + ", Members didnt contain: "
					+ clientChannel.getPlayer();
		}

		notifyObservers();
	}

	public void requestChannelDeleteClient() {
		ChatChannelModification cc = new ChatChannelModification(ChannelModType.DELETE, this);
		sendToServer(cc);
	}

	public void sendJoinRequestToServer(ClientChannel from, String passwd) {
		// set password so it is saved locally for this channel if join is indeed successfull
		clientCurrentPassword = passwd;
		ChatChannelModification chatChannelModification = new ChatChannelModification(ChannelModType.JOINED, this,
				from.getPlayerId());
		chatChannelModification.joinPw = passwd;
		from.getNetworkObject().chatChannelBuffer.add(new RemoteChatChannel(chatChannelModification, onServer));
		System.err.println("[CLIENT] sending join request to server: " + getUniqueChannelName());
		from.getPlayer().getPlayerChannelManager().onJoinChannelAttempt(this, passwd);
	}

	public void sendLeaveRequestToServer(ClientChannel from, String passwd) {
		ChatChannelModification chatChannelModification = new ChatChannelModification(ChannelModType.LEFT, this,
				from.getPlayerId());
		from.getNetworkObject().chatChannelBuffer.add(new RemoteChatChannel(chatChannelModification, onServer));
		System.err.println("[CLIENT] sending leave request to server: " + getUniqueChannelName());
	}

	public void sendModRequestToServer(ClientChannel from, String name, boolean add) {
		ChatChannelModification chatChannelModification = new ChatChannelModification(add ? ChannelModType.MOD_ADDED
				: ChannelModType.MOD_REMOVED, this, from.getPlayerId());
		chatChannelModification.mods = new String[]{name};
		from.getNetworkObject().chatChannelBuffer.add(new RemoteChatChannel(chatChannelModification, onServer));
		System.err.println("[CLIENT] sending leave request to server: " + getUniqueChannelName());
	}	@Override
	public boolean hasChannelBanList() {
		return false;
	}

	public void sendBannedRequestToServer(ClientChannel from, String name, boolean add) {
		ChatChannelModification chatChannelModification = new ChatChannelModification(add ? ChannelModType.BANNED
				: ChannelModType.UNBANNED, this, from.getPlayerId());
		chatChannelModification.banned = new String[]{name};
		from.getNetworkObject().chatChannelBuffer.add(new RemoteChatChannel(chatChannelModification, onServer));
		System.err.println("[CLIENT] sending leave request to server: " + getUniqueChannelName());
	}	@Override
	public boolean hasChannelModList() {
		return false;
	}

	private void sendLeftUpdateToClients(ClientChannel removed) {
		assert (onServer);

		// send to all to update counts in channel list
		for (PlayerState ps : ((GameServerState) state).getPlayerStatesByName().values()) {
			if (ps.getClientChannel() != null) {
				ClientChannel c = ps.getClientChannel();
				if (removed != c) {
					c.getNetworkObject().chatChannelBuffer.add(new RemoteChatChannel(new ChatChannelModification(
							ChannelModType.LEFT, this, removed.getPlayerId()), onServer));
				}
			}
		}
		removed.getNetworkObject().chatChannelBuffer.add(new RemoteChatChannel(new ChatChannelModification(
				ChannelModType.LEFT, this, removed.getPlayerId()), onServer));
	}	@Override
	public boolean hasPossiblePassword() {
		return false;
	}

	private void sendDeleteUpdateToClients(ClientChannel removed) {
		assert (onServer);
		removed.getNetworkObject().chatChannelBuffer.add(new RemoteChatChannel(new ChatChannelModification(
				ChannelModType.DELETE, this), onServer));
	}

	protected boolean checkPassword(ChatChannelModification cc) {
		return true;
	}

	public boolean hasPassword() {
		return false;
	}

	private void sendAddUpdateToClients(ClientChannel added) {
		assert (onServer);

		Collection<ClientChannel> clientChannelsInChannel = getClientChannelsInChannel();
		// send to all to update counts in channel list
		for (PlayerState ps : ((GameServerState) state).getPlayerStatesByName().values()) {
			if (ps.getClientChannel() != null) {
				ClientChannel c = ps.getClientChannel();
				if (added != c) {
					c.getNetworkObject().chatChannelBuffer.add(new RemoteChatChannel(new ChatChannelModification(
							ChannelModType.JOINED, this, added.getPlayerId()), onServer));
				}
			}
		}
		int i = 0;
		int ids[] = new int[clientChannelsInChannel.size()];
		for (ClientChannel c : clientChannelsInChannel) {
			ids[i] = c.getPlayerId();
			i++;
		}
		ChatChannelModification cc = new ChatChannelModification(ChannelModType.CREATE, this, ids);
		cc.mods = getModerators();
		cc.banned = getBanned();
		cc.muted = getMuted();
		added.getNetworkObject().chatChannelBuffer.add(new RemoteChatChannel(cc, onServer));
	}

	protected String[] getModerators() {
		return null;
	}

	public void sendCreateUpdateToClient(ClientChannel added) {
		assert (onServer);

		Collection<ClientChannel> clientChannelsInChannel = getClientChannelsInChannel();

		for (PlayerState ps : ((GameServerState) state).getPlayerStatesByName().values()) {
			if (ps.getClientChannel() != null) {
				ClientChannel c = ps.getClientChannel();
				if (added != c) {
					c.getNetworkObject().chatChannelBuffer.add(new RemoteChatChannel(new ChatChannelModification(
							ChannelModType.JOINED, this, added.getPlayerId()), onServer));
				}
			}
		}
		int i = 0;
		int ids[] = new int[clientChannelsInChannel.size()];
		for (ClientChannel c : clientChannelsInChannel) {
			ids[i] = c.getPlayerId();
			i++;
		}

		ChatChannelModification cc = new ChatChannelModification(ChannelModType.CREATE, this, ids);
		cc.mods = getModerators();
		cc.banned = getBanned();
		cc.muted = getMuted();
		added.getNetworkObject().chatChannelBuffer.add(new RemoteChatChannel(cc, onServer));
	}	@Override
	public String[] getBanned() {
		return null;
	}

	public void sendCreateWithoutJoinUpdateToClient(ClientChannel sendTo) {
		assert (onServer);

		Collection<ClientChannel> clientChannelsInChannel = getClientChannelsInChannel();

		int i = 0;
		int ids[] = new int[clientChannelsInChannel.size()];
		for (ClientChannel c : clientChannelsInChannel) {
			ids[i] = c.getPlayerId();
			i++;
		}
		ChatChannelModification cc = new ChatChannelModification(ChannelModType.CREATE, this, ids);
		cc.mods = getModerators();
		cc.banned = getBanned();
		cc.muted = getMuted();
		sendTo.getNetworkObject().chatChannelBuffer.add(new RemoteChatChannel(cc, onServer));
	}

	protected void onClientChannelAddServer(ClientChannel clientChannel) {
		assert (onServer);
		clientChannel.getPlayer().getPlayerChannelManager().addedToChat(this);
		boolean add = false;

		assert (getClientChannelsInChannel().contains(clientChannel));
		if (!members.contains(clientChannel.getPlayer())) {
			add = members.add(clientChannel.getPlayer());
		}

		if (add) {
			sendAddUpdateToClients(clientChannel);
		}

		notifyObservers();
	}

	public abstract void onFactionChangedServer(ClientChannel c);

	// protected abstract void addClientChannel(ClientChannel clientChannel);

	public abstract void onLoginServer(ClientChannel c);

	public final void onLogoff(PlayerState player) {
		if (player.getClientChannel() != null) {
			boolean remove = removeFromClientChannels(player.getClientChannel());

			if (remove) {
				System.err.println("[SERVER][FactionChannel] removed logged out player from " + getUniqueChannelName()
						+ ": " + player);
				onClientChannelRemoveServer(player.getClientChannel());
			}
		}
	}

	public abstract void update();

	public void updateVisibleChat(Timer timer) {
		ChannelRouter.updateVisibleChat(timer, visibleChatLog);
	}

	public abstract boolean isAlive();

	public abstract ChatMessage process(ChatMessage message);

	/**
	 * @return the state
	 */
	public StateInterface getState() {
		return state;
	}

	/**
	 * @return the onServer
	 */
	public boolean isOnServer() {
		return onServer;
	}

	/**
	 * @return the messageLog
	 */
	public ObjectArrayList<ChatMessage> getMessageLog() {
		return messageLog;
	}

	public abstract String getUniqueChannelName();

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.schema.game.common.data.chat.ChatCallback#getVisibleChat()
	 */
	@Override
	public List<ChatMessage> getChatLog() {
		return messageLog;
	}

	/**
	 * @return the visibleChatLog
	 */
	@Override
	public ObjectArrayList<ChatMessage> getVisibleChatLog() {
		return visibleChatLog;
	}

	/**
	 * this chat will only be displayed for the client it is executed on
	 *
	 * @param text
	 * @return
	 */
	@Override
	public boolean localChatOnClient(String text) {

		assert (!onServer);
		ChatMessage c = new ChatMessage();
		c.sender = getName();
		c.receiver = ((GameClientState) state).getPlayerName();
		c.text = text;

		boolean putInChat = checkPrefixClient(c);

		if (putInChat) {
			getChatLog().add(c);
			visibleChatLog.add(new ChatMessage(c));
			addToDefaults(c);
		}
		return putInChat;
	}

	@Override
	public void chat(String text) {
		ChatMessage chatMessage = new ChatMessage();
		chatMessage.text = text;
		chatMessage.sender = ((GameClientState) state).getPlayerName();
		chatMessage.receiver = getUniqueChannelName();
		chatMessage.receiverType = ChatMessageType.CHANNEL;
		send(chatMessage);
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see org.schema.game.common.data.chat.ChatCallback#getMemberPlayerStates()
	 */
	@Override
	public Collection<PlayerState> getMemberPlayerStates() {

		return members;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.schema.game.common.data.chat.ChatCallback#onWindowDeactivate()
	 */
	@Override
	public void onWindowDeactivate() {

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.schema.game.common.data.chat.ChatCallback#isSticky()
	 */
	@Override
	public boolean isSticky() {
		return sticky;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.schema.game.common.data.chat.ChatCallback#setSticky(boolean)
	 */
	@Override
	public void setSticky(boolean b) {
		this.sticky = b;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.schema.game.common.data.chat.ChatCallback#isScrollLock()
	 */
	@Override
	public boolean isFullSticky() {
		return stickyFull;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.schema.game.common.data.chat.ChatCallback#setScrollLock(boolean)
	 */
	@Override
	public void setFullSticky(boolean b) {
		this.stickyFull = b;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.schema.game.common.data.chat.ChatCallback#leave(org.schema.game.common.data.player.PlayerState)
	 */
	@Override
	public void leave(PlayerState player) {
		// DirectChannel overrides

		if (getMemberPlayerStates().contains(player)) {
			sendLeaveRequestToServer(player.getClientChannel(), "");
		}
	}

	@Override
	public abstract boolean canLeave();

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return id == ((ChatChannel) obj).id;
	}

	public abstract boolean isPublic();

	public abstract ChannelType getType();

	public void handleMod(ChatChannelModification cc) {
		if (onServer) {
			switch (cc.type) {
				case JOINED:
					// handled by caller
					assert (false);
					break;
				case CREATE:
					// handled by caller
					assert (false);
					break;
				case INVITED:
					break;
				case LEFT:
					// handled by caller
					assert (false);
					break;
				case KICKED:
					handleModModeratorKickServer(cc);
					break;
				case BANNED:
					handleModBannedAddServer(cc);
					break;
				case UNBANNED:
					handleModBannedRemoveServer(cc);
					break;
				case MUTED:
					handleModMutedAddServer(cc);
					break;
				case UNMUTED:
					handleModMutedRemoveServer(cc);
					break;
				case IGNORED:
					handleModIgnoredAddServer(cc);
					break;
				case UNIGNORED:
					handleModIgnoredRemoveServer(cc);
					break;
				case UPDATE:
					break;
				case MOD_ADDED:
					handleModModeratorAddServer(cc);
					break;
				case MOD_REMOVED:
					handleModModeratorRemoveServer(cc);
					break;
				case REMOVED_ON_NOT_ALIVE:
					// handled by caller
					assert (false);
					break;
				case PASSWD_CHANGE:
					handleModPasswordChangeServer(cc);
					break;
				default:
					break;

			}
		} else {
			switch (cc.type) {

				case JOINED:
					handleModJoinClient(cc);
					break;
				case CREATE:
					handleModJoinClient(cc);
					assert (getType() != ChannelType.PUBLIC || cc.createChannelType == ChannelType.PUBLIC) : cc.createChannelType;
					assert (getType() != ChannelType.PUBLIC || cc.mods != null) : cc.createChannelType.name();
					addToModerator(cc.mods);
					addToBanned(cc.banned);
					break;
				case INVITED:
					break;
				case LEFT:
				case KICKED:
					for (int e : cc.user) {
						Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(e);
						if (sendable instanceof PlayerState) {
							boolean remove = members.remove(sendable);
							if (!remove) {
								System.err.println("[CLIENT] WARNING: member in channel: " + getUniqueChannelName()
										+ " could not be removed: " + sendable + ": " + members);
							}
							if (!isAvailableWithoutMyself()) {
								((PlayerState) sendable).getPlayerChannelManager().removedFromChat(this);
							}
						}
					}
					break;
				case BANNED:
					handleModBannedAddClient(cc);
					break;
				case UNBANNED:
					handleModBannedRemoveClient(cc);
					break;
				case MUTED:
					handleModMutedAddClient(cc);
					break;
				case UNMUTED:
					handleModMutedRemoveClient(cc);
					break;
				case UPDATE:
					break;
				case MOD_ADDED:
					handleModModeratorAddClient(cc);
					break;
				case MOD_REMOVED:
					handleModModeratorRemoveClient(cc);
					break;
				case PASSWD_CHANGE:
					// assert(false):("##################### CLIENT RECEIVED PASSWD: "+cc.changePasswd);
					setNewPassword(cc.changePasswd);
					break;
				case REMOVED_ON_NOT_ALIVE:
				default:
					break;

			}
			notifyObservers();
		}
	}

	private void handleModJoinClient(ChatChannelModification cc) {
		for (int e : cc.user) {
			Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(e);
			if (sendable instanceof PlayerState) {
				if (!members.contains(sendable)) {
					members.add((PlayerState) sendable);
				} else {
					System.err.println("[CLIENT] WARNING: member in channel: " + getUniqueChannelName()
							+ " already existed");
				}
			}
		}
	}

	private void handleModModeratorAddClient(ChatChannelModification cc) {
		addToModerator(cc.mods);
	}

	private void handleModModeratorRemoveClient(ChatChannelModification cc) {
		removeFromModerator(cc.mods);
	}

	private void handleModBannedAddClient(ChatChannelModification cc) {
		addToBanned(cc.banned);
	}

	private void handleModBannedRemoveClient(ChatChannelModification cc) {
		removeFromBanned(cc.banned);
	}

	private void handleModMutedAddClient(ChatChannelModification cc) {
		addToMuted(cc.muted);
	}

	private void handleModMutedRemoveClient(ChatChannelModification cc) {
		removeFromMuted(cc.muted);
	}

	private void handleModModeratorKickServer(ChatChannelModification cc) {
		PlayerState senderPlayer;
		try {
			senderPlayer = ((GameServerState) state).getPlayerFromStateId(cc.sender);

			if (isModerator(senderPlayer) || ((GameServerState) state).isAdmin(senderPlayer.getName())) {
				Sendable sendable = state.getLocalAndRemoteObjectContainer().getLocalObjects().get(cc.user[0]);
				if (sendable instanceof PlayerState) {
					((PlayerState) sendable)
							.sendServerMessagePlayerError(Lng.astr("You have been kicked\nfrom the chat channel:\n%s",  getName()));

					System.err.println("[SERVER][ChatChannel] " + senderPlayer + " kicked " + sendable + " on channel "
							+ getName() + " (" + getUniqueChannelName() + ")");
					log("[SERVER][ChatChannel] " + senderPlayer + " kicked " + sendable + " on channel " + getName()
							+ " (" + getUniqueChannelName() + ")");

					leaveOnServerAndSend(((PlayerState) sendable).getClientChannel());
				} else {
					assert (false);
				}
			} else {
				System.err.println("[SERVER][ChatChannel] not kick from request by client id " + cc.sender
						+ "; player is not a moderator in this channel!");
			}
		} catch (PlayerNotFountException e) {
			System.err.println("[SERVER][ChatChannel] not kick from request by client id " + cc.sender
					+ "; no player found!");
		}
	}

	private void handleModModeratorAddServer(ChatChannelModification cc) {
		PlayerState senderPlayer;
		try {
			senderPlayer = ((GameServerState) state).getPlayerFromStateId(cc.sender);

			if (isModerator(senderPlayer) || ((GameServerState) state).isAdmin(senderPlayer.getName())) {
				System.err.println("[SERVER][ChatChannel] " + senderPlayer + " added mods: " + Arrays.toString(cc.mods)
						+ " on channel " + getName() + " (" + getUniqueChannelName() + ")");
				log("[SERVER][ChatChannel] " + senderPlayer + " added mods: " + Arrays.toString(cc.mods)
						+ " on channel " + getName() + " (" + getUniqueChannelName() + ")");

				addToModerator(cc.mods);
			} else {
				System.err.println("[SERVER][ChatChannel] not adding mod from request by client id " + cc.sender
						+ "; player is not a moderator in this channel!");
			}
		} catch (PlayerNotFountException e) {
			System.err.println("[SERVER][ChatChannel] not adding mod from request by client id " + cc.sender
					+ "; no player found!");
		}
		sendToAllClients(cc);
	}

	private void handleModModeratorRemoveServer(ChatChannelModification cc) {
		PlayerState senderPlayer;
		try {
			senderPlayer = ((GameServerState) state).getPlayerFromStateId(cc.sender);

			if (isModerator(senderPlayer) || ((GameServerState) state).isAdmin(senderPlayer.getName())) {
				removeFromModerator(cc.mods);
				System.err.println("[SERVER][ChatChannel] " + senderPlayer + " removed mods: "
						+ Arrays.toString(cc.mods) + " on channel " + getName() + " (" + getUniqueChannelName() + ")");
				log("[SERVER][ChatChannel] " + senderPlayer + " removed mods: " + Arrays.toString(cc.mods)
						+ " on channel " + getName() + " (" + getUniqueChannelName() + ")");

			} else {
				System.err.println("[SERVER][ChatChannel] not removing mod from request by client id " + cc.sender
						+ "; player is not a moderator in this channel!");
			}
		} catch (PlayerNotFountException e) {
			System.err.println("[SERVER][ChatChannel] not removing mod from request by client id " + cc.sender
					+ "; no player found!");
		}
		sendToAllClients(cc);
	}

	private void handleModPasswordChangeServer(ChatChannelModification cc) {
		PlayerState senderPlayer;
		try {
			senderPlayer = ((GameServerState) state).getPlayerFromStateId(cc.sender);

			if (isModerator(senderPlayer) || ((GameServerState) state).isAdmin(senderPlayer.getName())) {
				setNewPassword(cc.changePasswd);
				// change to hint to send to client
				cc.changePasswd = cc.changePasswd.length() > 0 ? "#" : "";

				System.err.println("[SERVER][ChatChannel] " + senderPlayer + " changed password on channel "
						+ getName() + " (" + getUniqueChannelName() + ")");
				log("[SERVER][ChatChannel] " + senderPlayer + " changed password on channel " + getName() + " ("
						+ getUniqueChannelName() + ")");

			} else {
				System.err.println("[SERVER][ChatChannel] not set password from request by client id " + cc.sender
						+ "; player is not a moderator in this channel!");
			}
		} catch (PlayerNotFountException e) {
			System.err.println("[SERVER][ChatChannel] not set password from request by client id " + cc.sender
					+ "; no player found!");
		}
		sendToAllClients(cc);
	}

	protected void setNewPassword(String changePasswd) {
	}

	private void handleModBannedRemoveServer(ChatChannelModification cc) {
		PlayerState senderPlayer;
		try {
			senderPlayer = ((GameServerState) state).getPlayerFromStateId(cc.sender);

			if (isModerator(senderPlayer) || ((GameServerState) state).isAdmin(senderPlayer.getName())) {
				removeFromBanned(cc.banned);
				System.err
						.println("[SERVER][ChatChannel] " + senderPlayer + " removed bans: "
								+ Arrays.toString(cc.banned) + " on channel " + getName() + " ("
								+ getUniqueChannelName() + ")");
				log("[SERVER][ChatChannel] " + senderPlayer + " removed bans: " + Arrays.toString(cc.banned)
						+ " on channel " + getName() + " (" + getUniqueChannelName() + ")");

			} else {
				System.err.println("[SERVER][ChatChannel] not removing banned from request by client id " + cc.sender
						+ "; player is not a moderator in this channel!");
			}
		} catch (PlayerNotFountException e) {
			System.err.println("[SERVER][ChatChannel] not removing banned from request by client id " + cc.sender
					+ "; no player found!");
		}
		sendToAllClients(cc);
	}

	private void handleModBannedAddServer(ChatChannelModification cc) {
		PlayerState senderPlayer;
		try {
			senderPlayer = ((GameServerState) state).getPlayerFromStateId(cc.sender);

			if (isModerator(senderPlayer) || ((GameServerState) state).isAdmin(senderPlayer.getName())) {
				addToBanned(cc.banned);
				System.err
						.println("[SERVER][ChatChannel] " + senderPlayer + " added bans: " + Arrays.toString(cc.banned)
								+ " on channel " + getName() + " (" + getUniqueChannelName() + ")");
				log("[SERVER][ChatChannel] " + senderPlayer + " added bans: " + Arrays.toString(cc.banned)
						+ " on channel " + getName() + " (" + getUniqueChannelName() + ")");

			} else {
				System.err.println("[SERVER][ChatChannel] not adding banned from request by client id " + cc.sender
						+ "; player is not a moderator in this channel!");
			}
		} catch (PlayerNotFountException e) {
			System.err.println("[SERVER][ChatChannel] not adding banned from request by client id " + cc.sender
					+ "; no player found!");
		}
		sendToAllClients(cc);
	}

	private void handleModMutedRemoveServer(ChatChannelModification cc) {
		PlayerState senderPlayer;
		try {
			senderPlayer = ((GameServerState) state).getPlayerFromStateId(cc.sender);

			if (isModerator(senderPlayer) || ((GameServerState) state).isAdmin(senderPlayer.getName())) {
				removeFromMuted(cc.muted);
				System.err.println("[SERVER][ChatChannel] " + senderPlayer + " removed mutes: "
						+ Arrays.toString(cc.muted) + " on channel " + getName() + " (" + getUniqueChannelName() + ")");
				log("[SERVER][ChatChannel] " + senderPlayer + " removed mutes: " + Arrays.toString(cc.muted)
						+ " on channel " + getName() + " (" + getUniqueChannelName() + ")");

			} else {
				System.err.println("[SERVER][ChatChannel] not removing muted from request by client id " + cc.sender
						+ "; player is not a moderator in this channel!");
			}
		} catch (PlayerNotFountException e) {
			System.err.println("[SERVER][ChatChannel] not removing muted from request by client id " + cc.sender
					+ "; no player found!");
		}
		sendToAllClients(cc);
	}

	private void handleModMutedAddServer(ChatChannelModification cc) {
		PlayerState senderPlayer;
		try {
			senderPlayer = ((GameServerState) state).getPlayerFromStateId(cc.sender);

			if (isModerator(senderPlayer) || ((GameServerState) state).isAdmin(senderPlayer.getName())) {
				addToMuted(cc.muted);
				System.err.println("[SERVER][ChatChannel] " + senderPlayer + " added mutes: "
						+ Arrays.toString(cc.muted) + " on channel " + getName() + " (" + getUniqueChannelName() + ")");
				log("[SERVER][ChatChannel] " + senderPlayer + " added mutes: " + Arrays.toString(cc.muted)
						+ " on channel " + getName() + " (" + getUniqueChannelName() + ")");

			} else {
				System.err.println("[SERVER][ChatChannel] not adding muted from request by client id " + cc.sender
						+ "; player is not a moderator in this channel!");
			}
		} catch (PlayerNotFountException e) {
			System.err.println("[SERVER][ChatChannel] not adding muted from request by client id " + cc.sender
					+ "; no player found!");
		}
		sendToAllClients(cc);
	}

	private void handleModIgnoredRemoveServer(ChatChannelModification cc) {
		PlayerState senderPlayer;
		try {
			senderPlayer = ((GameServerState) state).getPlayerFromStateId(cc.sender);
			this.removeFromIgnored(senderPlayer, cc.ignored);

			System.err.println("[SERVER][ChatChannel] " + senderPlayer + " removed ignores: "
					+ Arrays.toString(cc.ignored));
			log("[SERVER][ChatChannel] " + senderPlayer + " removed ignores: " + Arrays.toString(cc.ignored));

		} catch (PlayerNotFountException e) {
			System.err.println("[SERVER][ChatChannel] not removing ignored from request by client id " + cc.sender
					+ "; no player found!");
		}
		sendToAllClients(cc);
	}

	private void handleModIgnoredAddServer(ChatChannelModification cc) {
		PlayerState senderPlayer;
		try {
			senderPlayer = ((GameServerState) state).getPlayerFromStateId(cc.sender);
			this.addToIgnored(senderPlayer, cc.ignored);

			System.err.println("[SERVER][ChatChannel] " + senderPlayer + " added ignores: "
					+ Arrays.toString(cc.ignored));
			log("[SERVER][ChatChannel] " + senderPlayer + " added ignores: " + Arrays.toString(cc.ignored));
		} catch (PlayerNotFountException e) {
			System.err.println("[SERVER][ChatChannel] not adding ignored from request by client id " + cc.sender
					+ "; no player found!");
		}
		sendToAllClients(cc);
	}

	private void sendToAllClients(ChatChannelModification cc) {
		for (PlayerState ps : ((GameServerState) state).getPlayerStatesByName().values()) {
			if (ps.getClientChannel() != null) {
				ClientChannel c = ps.getClientChannel();
				c.getNetworkObject().chatChannelBuffer.add(new RemoteChatChannel(cc, onServer));
			}
		}
	}

	private void log(String string) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(ChannelRouter.CHAT_LOG_DIR + getUniqueChannelName()
					+ ".txt", true)));

			out.println(ChannelRouter.logDateFormatter.format(new Date(System.currentTimeMillis())) + " ** " + string);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	protected void removeFromModerator(String... mods) {
	}

	protected void addToModerator(String... mods) {
	}

	protected void removeFromBanned(String... banned) {
	}

	protected void addToBanned(String... banned) {
	}

	protected void removeFromMuted(String... muted) {
	}

	protected void addToMuted(String... muted) {
	}

	protected void removeFromIgnored(PlayerState player, String... ignored) {
		for (String toIgnoreStr : ignored) {
			player.removeIgnore(toIgnoreStr);
		}
	}

	protected void addToIgnored(PlayerState player, String... ignored) {
		for (String toIgnoreStr : ignored) {
			player.addIgnore(toIgnoreStr);
		}
	}

	protected boolean isAvailableWithoutMyself() {
		return true;
	}

	public void joinOnServerAndSend(ClientChannel clientChannel) {
		if (!members.contains(clientChannel.getPlayer())) {
			addToClientChannels(clientChannel);

			members.add(clientChannel.getPlayer());

			sendAddUpdateToClients(clientChannel);
		}
	}

	public void leaveOnServerAndSend(ClientChannel clientChannel) {
		removeFromClientChannels(clientChannel);
		members.remove(clientChannel.getPlayer());
		sendLeftUpdateToClients(clientChannel);
	}

	public void deleteChannelOnServerAndSend() {
		// a delete is sent to everybody
		for (PlayerState p : ((GameServerState) state).getPlayerStatesByName().values()) {
			ClientChannel c = p.getClientChannel();
			if (c != null) {
				removeFromClientChannels(c);
				members.remove(c.getPlayer());
				c.getPlayer().getPlayerChannelManager().removedFromChat(this);
				sendDeleteUpdateToClients(c);
			}
		}
	}

	protected abstract boolean addToClientChannels(ClientChannel clientChannel);

	protected abstract boolean removeFromClientChannels(ClientChannel clientChannel);

	public boolean isAutoJoinFor(ClientChannel c) {
		return false;
	}

	public boolean isCheckForPrefixesOnClient() {
		return true;
	}

	public Tag toServerTag() {
		System.err.println("[CLIENT] SAVING SERVER CHANNEL " + getUniqueChannelName() + " AS: " + clientOpen);

		assert (this instanceof PublicChannel);

		String[] banned = getBanned();
		String[] moderators = getModerators();
		String[] muted = getMuted();
		Tag moderatorsTag = Tag.listToTagStruct(moderators, Type.STRING, null);
		Tag bannedTag = Tag.listToTagStruct(banned, Type.STRING, null);
		Tag mutedTag = Tag.listToTagStruct(muted, Type.STRING, null);

		return new Tag(Type.STRUCT, null, new Tag[]{new Tag(Type.BYTE, null, SERVER_TAG_VERSION),
				new Tag(Type.STRING, null, getUniqueChannelName()), moderatorsTag, bannedTag, mutedTag,
				new Tag(Type.STRING, null, getPassword()),
				new Tag(Type.BYTE, null, isPermanent() ? (byte) 1 : (byte) 0),
				new Tag(Type.BYTE, null, isPublic() ? (byte) 1 : (byte) 0), FinishTag.INST});
	}

	public Tag toClientTag() {
		System.err.println("[CLIENT] SAVING CLIENT CHANNEL " + getUniqueChannelName() + " Open: " + clientOpen
				+ ";");
		return new Tag(Type.STRUCT, null, new Tag[]{new Tag(Type.BYTE, null, CLIENT_TAG_VERSION),
				new Tag(Type.STRING, null, getUniqueChannelName()),
				new Tag(Type.BYTE, null, sticky ? (byte) 1 : (byte) 0),
				new Tag(Type.BYTE, null, stickyFull ? (byte) 1 : (byte) 0),
				new Tag(Type.BYTE, null, clientOpen ? (byte) 1 : (byte) 0),
				new Tag(Type.STRING, null, clientCurrentPassword), FinishTag.INST});
	}

	private String getClientCurrentPassword() {
		return clientCurrentPassword;
	}	/*
	 * (non-Javadoc)
	 * 
	 * @see org.schema.game.common.data.chat.ChatCallback#setChatBar(org.schema.schine.graphicsengine.forms.gui.newgui.
	 * GUIActivatableTextBar)
	 */
	@Override
	public void setChatBar(GUIActivatableTextBar chatBar) {
		this.chatBar = chatBar;
	}

	/**
	 * @param clientCurrentPassword the clientCurrentPassword to set
	 */
	public void setClientCurrentPassword(String clientCurrentPassword) {
		this.clientCurrentPassword = clientCurrentPassword;
	}	/*
	 * (non-Javadoc)
	 * 
	 * @see org.schema.game.common.data.chat.ChatCallback#getChatBar()
	 */
	@Override
	public GUIActivatableTextBar getChatBar() {
		return chatBar;
	}

	public void handleClientLoadedChannel(PlayerState playerState, LoadedClientChatChannel ld) {
		if (!members.contains(playerState)) {
			if (!isAutoJoinFor(playerState.getClientChannel())) {
				sendJoinRequestToServer(playerState.getClientChannel(), ld.password);
			}
		}
		ld.joinRequestDone = true;
	}

	private void sendToServer(ChatChannelModification cc) {
		((GameClientState) state).getPlayer().getClientChannel().getNetworkObject().chatChannelBuffer
				.add(new RemoteChatChannel(cc, onServer));
	}

	public String getPassword() {
		return "";
	}

	public Vector4f getColor() {
		return color;
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see org.schema.game.common.data.chat.ChatCallback#isClientOpen()
	 */
	@Override
	public boolean isClientOpen() {
		return clientOpen;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.schema.game.common.data.chat.ChatCallback#setClientOpen()
	 */
	@Override
	public void setClientOpen(boolean clientOpen) {
		this.clientOpen = clientOpen;
	}







	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.schema.game.common.data.chat.ChatCallback#requestModUnmodOnClient(org.schema.game.common.data.player.PlayerState
	 * , boolean)
	 */
	@Override
	public void requestModUnmodOnClient(String player, boolean b) {
		ChatChannelModification cc = new ChatChannelModification(b ? ChannelModType.MOD_ADDED
				: ChannelModType.MOD_REMOVED, this);
		cc.mods = new String[]{player.toLowerCase(Locale.ENGLISH)};

		sendToServer(cc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.schema.game.common.data.chat.ChatCallback#requestModUnmodOnClient(org.schema.game.common.data.player.PlayerState
	 * , boolean)
	 */
	@Override
	public void requestPasswordChangeOnClient(String passwd) {
		ChatChannelModification cc = new ChatChannelModification(ChannelModType.PASSWD_CHANGE, this);
		cc.changePasswd = passwd;
		sendToServer(cc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.schema.game.common.data.chat.ChatCallback#requestKickOnClient(org.schema.game.common.data.player.PlayerState)
	 */
	@Override
	public void requestKickOnClient(PlayerState player) {
		ChatChannelModification cc = new ChatChannelModification(ChannelModType.KICKED, this, player.getId());
		sendToServer(cc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.schema.game.common.data.chat.ChatCallback#requestBanUnbanOnClient(org.schema.game.common.data.player.PlayerState
	 * , boolean)
	 */
	@Override
	public void requestBanUnbanOnClient(String player, boolean b) {
		if (!((GameClientState) state).getPlayerName().toLowerCase(Locale.ENGLISH).equals(player.toLowerCase(Locale.ENGLISH))) {
			ChatChannelModification cc = new ChatChannelModification(b ? ChannelModType.BANNED
					: ChannelModType.UNBANNED, this);
			cc.banned = new String[]{player.toLowerCase(Locale.ENGLISH)};

			sendToServer(cc);
		}
	}

	@Override
	public void requestMuteUnmuteOnClient(String player, boolean b) {
		if (!((GameClientState) state).getPlayerName().toLowerCase(Locale.ENGLISH).equals(player.toLowerCase(Locale.ENGLISH))) {
			ChatChannelModification cc = new ChatChannelModification(b ? ChannelModType.MUTED : ChannelModType.UNMUTED,
					this);
			cc.muted = new String[]{player.toLowerCase(Locale.ENGLISH)};

			sendToServer(cc);
		} else {

		}
	}

	@Override
	public void requestIgnoreUnignoreOnClient(String f, boolean b) {
		if (!((GameClientState) state).getPlayerName().toLowerCase(Locale.ENGLISH).equals(f.toLowerCase(Locale.ENGLISH))) {
			ChatChannelModification cc = new ChatChannelModification(b ? ChannelModType.IGNORED
					: ChannelModType.UNIGNORED, this);
			cc.ignored = new String[]{f.toLowerCase(Locale.ENGLISH)};

			PlayerState me = ((GameClientState) state).getPlayer();

			if (me.isIgnored(f)) {
				me.removeIgnore(f);
			} else {
				me.addIgnore(f);
			}

			sendToServer(cc);
		}
	}





}
