package org.schema.game.common.data.chat;

import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import it.unimi.dsi.fastutil.objects.*;
import org.schema.common.util.StringTools;
import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.chat.prefixprocessors.AbstractPrefixProcessor;
import org.schema.game.common.data.chat.prefixprocessors.PrefixProcessorAdminCommand;
import org.schema.game.common.data.chat.prefixprocessors.PrefixProcessorEngineSettings;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.SimplePlayerCommands;
import org.schema.game.network.objects.ChatChannelModification;
import org.schema.game.network.objects.ChatChannelModification.ChannelModType;
import org.schema.game.network.objects.ChatMessage;
import org.schema.game.network.objects.ChatMessage.ChatMessageType;
import org.schema.game.network.objects.remote.RemoteChatChannel;
import org.schema.game.network.objects.remote.RemoteChatMessage;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.PlayerNotFountException;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.ChatListener;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.ColoredTimedText;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.server.ServerStateInterface;
import org.schema.schine.resource.DiskWritable;
import org.schema.schine.resource.FileExt;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChannelRouter implements DiskWritable {

	public static final String LOG_DIR = "." + File.separator + "chatlogs" + File.separator;

	public static final String FILENAME = "chatchannels.tag";
	public static final boolean VISIBLE_CHAT_INSERT_AT_ZERO = true;
	private static final boolean ALLOW_ADMINS_AS_MODS = true;
	public static int idGen;
	public static SimpleDateFormat logDateFormatter = StringTools.getSimpleDateFormat(Lng.str("yyyy/MM/dd - HH:mm:ss"), "yyyy/MM/dd - HH:mm:ss");
	public static String CHAT_LOG_DIR = "." + File.separator + "chatlogs" + File.separator;
	public final ObjectArrayFIFOQueue<ChatMessage> receivedChat = new ObjectArrayFIFOQueue<ChatMessage>();
	private final boolean onServer;
	private final StateInterface state;
	private final List<ChatMessage> defaultVisibleChatLog = new ObjectArrayList<ChatMessage>();
	private final List<ChatMessage> defaultChatLog = new ObjectArrayList<ChatMessage>();
	private final List<AbstractPrefixProcessor> prefixProcessors = new ObjectArrayList<AbstractPrefixProcessor>();
	private Object2ObjectOpenHashMap<String, ChatChannel> channels = new Object2ObjectOpenHashMap<String, ChatChannel>();

	public ChannelRouter(StateInterface state) {
		this.state = state;
		this.onServer = state instanceof ServerStateInterface;

		if (onServer) {
			createAndLoadChannels();
		} else {
			createPrefixProcessors(prefixProcessors);
		}
		(new FileExt(CHAT_LOG_DIR)).mkdirs();
	}

	public static void updateVisibleChat(Timer timer, List<? extends ColoredTimedText> chatLog) {
		for (int i = 0; i < chatLog.size(); i++) {
			chatLog.get(i).update(timer);
			if (!chatLog.get(i).isAlive()) {
				chatLog.remove(i);
				i--;
			}
		}
	}

	public static boolean allowAdminClient(PlayerState f) {
		return ALLOW_ADMINS_AS_MODS && f.getNetworkObject().isAdminClient.get();
	}

	private void createPrefixProcessors(List<AbstractPrefixProcessor> prefixProcessors) {
		prefixProcessors.add(new PrefixProcessorAdminCommand());
		prefixProcessors.add(new PrefixProcessorEngineSettings());

		Collections.sort(prefixProcessors);

		assert (prefixProcessors.get(0).getPrefixCommon().equals("/"));
	}

	private void createAndLoadChannels() {
		AllChannel all = new AllChannel(state, ++idGen);
		channels.put(all.getUniqueChannelName(), all);

		// PublicChannel testChannel = new PublicChannel(state, ++idGen, "Test Permanent Channel", "test", true, new
		// String[]{"schema"});
		// channels.put(testChannel.getUniqueChannelName(), testChannel);
		File over = new FileExt(GameServerState.ENTITY_DATABASE_PATH + FILENAME);
		try {
			System.err.println(state+" Reading CHAT CHANNEL ROUTER "+over.getAbsolutePath());
			try{
				fromTagStructure(Tag.readFrom(new BufferedInputStream(new FileInputStream(over)), true, false));
			}catch(NullPointerException e){
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			System.err.println("[SERVER] Cant load chat channels. no saved data found");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public ChatChannel createClientPMChannel(PlayerState from, PlayerState to) {
		String mm = DirectChatChannel.getChannelName(from, to);

		ChatChannel chatChannel = channels.get(mm);
		if (chatChannel == null) {
			System.err.println("CREATING NEW CLIENT CHAT CHANNEL: " + mm);
			chatChannel = new DirectChatChannel(state, -(++idGen), from, to);
			channels.put(mm, chatChannel);
			from.getPlayerChannelManager().addDirectChannel(chatChannel);
			to.getPlayerChannelManager().addDirectChannel(chatChannel);
		}
		return chatChannel;
	}

	public void receive(ChatMessage message) {
		System.err.println("[CHANNELROUTER] RECEIVED MESSAGE ON " + state + ": " + message.toDetailString());
		PlayerChatEvent event = new PlayerChatEvent(message, this);
		StarLoader.fireEvent(PlayerChatEvent.class, event, this.onServer);
		if (event.isCanceled()) {
			return;
		}
		if (onServer) {
			PlayerState b = ((GameServerState) state).getPlayerFromNameIgnoreCaseWOException(message.sender);
			if(b != null) {
				b.getRuleEntityManager().triggerPlayerChat(); //this player chatted
			}
			// route and send message
			log(message);
			sendAsServer(message);
		} else {
			
			if(!onServer && state.isPassive()){
				ObjectArrayList<ChatListener> chatListeners = ((GameClientState)state).getChatListeners();
				for(ChatListener c : chatListeners){
					c.notifyOfChat(message);
				}
			}
			PlayerState a = ((GameClientState) state).getOnlinePlayersLowerCaseMap().get(
					message.sender.toLowerCase(Locale.ENGLISH));
			if (!((GameClientState) state).getPlayer().isIgnored(message.sender) || a.getNetworkObject().isAdminClient.get()) {
				if (message.receiverType == ChatMessageType.DIRECT) {
					String mm = DirectChatChannel.getChannelName(message.sender, message.receiver); // unique per
					// combination
					ChatChannel chatChannel = channels.get(mm);
					if (chatChannel == null) {
						PlayerState b = ((GameClientState) state).getOnlinePlayersLowerCaseMap().get(
								message.receiver.toLowerCase(Locale.ENGLISH));
						if (a != null && b != null) {
							System.err.println("CLIENT RECEIVED MESSAGE ON NOT EXISTING CLIENT CHANNEL, CREATING: "
									+ mm + "; Current: " + channels);
							chatChannel = new DirectChatChannel(state, -(++idGen), a, b);
						} else {
							((GameClientState) state).getController().popupAlertTextMessage(
									Lng.str("Received direct message from unknown\n%s \n-> %s",  message.sender, 
											message.receiver), 0);
							return;
						}
						a.getPlayerChannelManager().addDirectChannel(chatChannel);
						b.getPlayerChannelManager().addDirectChannel(chatChannel);

						channels.put(mm, chatChannel);
						assert (channels.size() > 0);
					}

					chatChannel.receive(message);
				} else {
					ChatChannel chatChannel = channels.get(message.receiver);
					if (chatChannel != null) {
						chatChannel.receive(message);
					} else {
						((GameClientState) state).getController().popupAlertTextMessage(
								Lng.str("Received message from unknown\n%s \n-> %s",  message.sender,  message.receiver), 0);
					}
				}
			}
		}
	}

	private void log(ChatMessage message) {

		if (message.receiverType == ChatMessageType.DIRECT) {
			try {

				String mm = DirectChatChannel.getChannelName(message.sender, message.receiver); // unique per
				// combination

				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(CHAT_LOG_DIR + mm + ".txt", true)));
				out.println(logDateFormatter.format(new Date(System.currentTimeMillis())) + " [" + message.sender
						+ " -> " + message.receiver + "]: " + message.text);
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			ChatChannel channel = channels.get(message.receiver);
			if (channel != null) {
				try {

					String mm = channel.getUniqueChannelName();

					PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(CHAT_LOG_DIR + mm + ".txt",
							true)));
					out.println(logDateFormatter.format(new Date(System.currentTimeMillis())) + " [" + message.sender
							+ "]: " + message.text);
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void sendAsServer(ChatMessage message) {
		assert (onServer);

		if (message.receiverType == ChatMessageType.DIRECT) {
			// directly relay message
			System.err.println("[SERVER][CHANNELROUTER] DIRECT MESSAGE RELAY: " + message.toDetailString());
			try {
				PlayerState psA = ((GameServerState) state).getPlayerFromName(message.receiver);
				PlayerState psB = ((GameServerState) state).getPlayerFromName(message.sender);

				boolean isMuted = getAllChannel().isMuted(psB);

				if (!psA.isIgnored(message.sender) || !isMuted) {
					if (psA.getClientChannel() != null) {
						psA.getClientChannel().getNetworkObject().chatBuffer.add(new RemoteChatMessage(message, onServer));
					}
					if (psB.getClientChannel() != null) {
						psB.getClientChannel().getNetworkObject().chatBuffer.add(new RemoteChatMessage(message, onServer));
					}
				} else {
					if(isMuted){
					psB.sendServerMessagePlayerError(Lng.astr("Can't send message:\nYou are muted\nfrom chatting!"));
					}else{
						psB.sendServerMessagePlayerError(Lng.astr("Can't send message:\nThis player ignores you."));
					}
				}
			} catch (PlayerNotFountException e) {
				e.printStackTrace();
			}
		} else {
			ChatChannel channel = channels.get(message.receiver);
			assert (channel != null) : "Channel not found: " + message.receiver;
			try {
				PlayerState sender = ((GameServerState) state).getPlayerFromName(message.sender);
				if (channel != null && !channel.isMuted(sender)) {
					ChatMessage returnMessage = channel.process(message);
					channel.send(returnMessage);
				} else {
					if (channel != null) {
						sender.sendServerMessagePlayerError(Lng.astr("Can't send message:\nyou have been muted!"));
					} else {
						System.err.println("[SERVER] Exception: channel " + message.receiver + " not found");
					}
				}
			} catch (PlayerNotFountException e) {
				e.printStackTrace();
			}
		}
	}

	public void update(Timer timer) {

		if (onServer) {
			ObjectIterator<ChatChannel> iterator = channels.values().iterator();
			while (iterator.hasNext()) {

				ChatChannel next = iterator.next();
				next.update();

				if (!next.isAlive()) {
					System.err.println("[SERVER][ChannelRouter] CHANNEL NO LONGER ALIVE: "
							+ next.getUniqueChannelName());
					iterator.remove();

					sendChannelRemovalOnServer(next);
				}
			}
		}
		while (!receivedChat.isEmpty()) {
			receive(receivedChat.dequeue());
		}

		if (!onServer) {
			updateVisibleChat(timer, defaultVisibleChatLog);
		}
	}

	private void sendChannelRemovalOnServer(ChatChannel removed) {
		for (PlayerState c : ((GameServerState) state).getPlayerStatesByName().values()) {
			System.err.println("[SERVER] sending channel removed flag of " + removed.getUniqueChannelName() + " to "
					+ c.getName());
			if (c.getClientChannel() != null) {
				c.getClientChannel().getNetworkObject().chatChannelBuffer.add(new RemoteChatChannel(
						new ChatChannelModification(ChannelModType.REMOVED_ON_NOT_ALIVE, removed), onServer));
			}
		}
	}

	public void onFactionChangedServer(ClientChannel c) {
		if (onServer) {

			System.err.println("[SERVER][ChannelRouter] Faction Changed by " + c.getPlayer() + " to "
					+ c.getPlayer().getFactionId());

			// create faction channel if necessary
			getOrCreateFactionChannel(c.getPlayer().getFactionId(), c);

			ObjectIterator<ChatChannel> iterator = channels.values().iterator();
			while (iterator.hasNext()) {
				ChatChannel next = iterator.next();
				next.onFactionChangedServer(c);
			}
		}

	}

	public void onLogin(ClientChannel c) {

		if (onServer) {
			addDefaultChannels(c, c.getPlayer().getPlayerChannelManager().getAvailableChannels());
			ObjectIterator<ChatChannel> iterator = channels.values().iterator();
			System.err.println("[SERVER] Sending all available channels to " + c.getPlayer() + "; total existing: "
					+ channels.size());

			while (iterator.hasNext()) {
				ChatChannel next = iterator.next();
				next.onLoginServer(c);

				if (next instanceof FactionChannel) {
					assert (next.getClientChannelsInChannel().size() > 0);
				}

				if (c.getPlayer().getPlayerChannelManager().getAvailableChannels().contains(next)) {
					System.err.println("[SERVER] Sending available channel to " + c.getPlayer() + ": "
							+ next.getUniqueChannelName());
					if (next.isAutoJoinFor(c)) {
						next.sendCreateUpdateToClient(c);
					} else {
						next.sendCreateWithoutJoinUpdateToClient(c);
					}
				}

			}

		}
	}

	public void onLogoff(PlayerState player) {
		System.err.println("[SERVER][ChannelRouter] log off of player " + player + "; removing from all channels");
		ObjectIterator<ChatChannel> iterator = channels.values().iterator();
		while (iterator.hasNext()) {
			ChatChannel next = iterator.next();
			next.onLogoff(player);
		}
	}

	public void addDefaultChannels(ClientChannel c, ObjectOpenHashSet<ChatChannel> availableChats) {

		assert (onServer);

		ObjectIterator<ChatChannel> iterator = channels.values().iterator();

		while (iterator.hasNext()) {
			ChatChannel next = iterator.next();
			if (next.isPublic()) {
				availableChats.add(next);
			}
		}

		FactionChannel factionChannel = getOrCreateFactionChannel(c.getPlayer().getFactionId(), c);
		if (factionChannel != null) {
			availableChats.add(factionChannel);
		}
	}

	public FactionChannel getOrCreateFactionChannel(int id, ClientChannel askFrom) {
		assert (onServer);
		if (((FactionState) state).getFactionManager().existsFaction(id)) {
			String uid = "Faction" + id;
			ChatChannel chatChannel = channels.get(uid);
			if (chatChannel == null) {

				chatChannel = new FactionChannel(state, ++idGen, id);
				System.err.println("[SERVER][ChannelRouter] created Faction channel: "
						+ chatChannel.getUniqueChannelName());
				channels.put(uid, chatChannel);
			}
			return (FactionChannel) chatChannel;
		} else {
			if (id != 0) {
				System.err
						.println("[SERVER][ChannelRouter] ERROR: cannot create faction channel because faction doesnt exist: "
								+ id);
				assert (false) : "ERROR: cannot create faction channel because faction doesnt exist: " + id;
			}
		}
		return null;
	}

	public ChatChannel handleClientReceivedChannel(PlayerState received, ChatChannelModification cc) {

		if (cc.type == ChannelModType.REMOVED_ON_NOT_ALIVE || cc.type == ChannelModType.DELETE) {
			ChatChannel chatChannel = channels.remove(cc.channel);
			// return so it can be removed from available
			return chatChannel;
		} else {
			ChatChannel chatChannel = channels.get(cc.channel);
			if (chatChannel == null) {
				if (cc.type == ChannelModType.CREATE) {
					System.err.println("[CLIENT] Client received new channel: " + cc.channel + "; creating...");
					chatChannel = cc.createNewChannel(state);
					assert (chatChannel != null) : "NO CHANNEL: " + cc.channel + ", " + cc.type;
					channels.put(chatChannel.getUniqueChannelName(), chatChannel);
				}
			} else {
				System.err.println("[CLIENT] Client received existing channel mod: " + cc.channel + "; "
						+ cc.type.name());
			}
			if (chatChannel != null) {
				chatChannel.handleMod(cc);
			}
			return chatChannel;
		}
	}

	public void createNewChannelOnClient(PlayerState creator, String name, String pw, boolean permanent) {
		PublicChannel tmp = new PublicChannel(state, ++idGen, name, pw, permanent, new String[]{creator.getName()
				.toLowerCase(Locale.ENGLISH)});
		ChatChannelModification cc = new ChatChannelModification(ChannelModType.CREATE, tmp, creator.getId());

		creator.getClientChannel().getNetworkObject().chatChannelBuffer.add(new RemoteChatChannel(cc, onServer));
	}

	public void makeNewCreatedAllAvailableServer(ChatChannel chan) {
		for (PlayerState s : ((GameServerState) state).getPlayerStatesByName().values()) {
			s.getPlayerChannelManager().addedToChat(chan);
			if (s.getClientChannel() != null) {
				chan.sendCreateUpdateToClient(s.getClientChannel());
			}
		}

	}

	public void handleReceivedChannelOnServer(PlayerState playerStateSender, ChatChannelModification cc) {

		assert (onServer);
		if (cc.type == ChannelModType.CREATE) {

			PublicChannel chan = new PublicChannel(state, ++idGen, cc.channel, cc.createPublicChannelPassword,
					cc.createPublicChannelAsPermanent, cc.mods);
			chan.addToModerator(playerStateSender.getName().toLowerCase(Locale.ENGLISH));
			if (!channels.containsKey(chan)) {
				channels.put(chan.getUniqueChannelName(), chan);

				if (chan.isPublic()) {
					makeNewCreatedAllAvailableServer(chan);
				}

				chan.joinOnServerAndSend(playerStateSender.getClientChannel());

			}

		} else {
			ChatChannel chatChannel = channels.get(cc.channel);
			if (chatChannel != null) {
				if (cc.type == ChannelModType.DELETE) {
					if (((GameServerState) state).isAdmin(playerStateSender.getName())) {
						chatChannel.deleteChannelOnServerAndSend();
						channels.remove(cc.channel);
						System.err.println("[SERVER] Channel deleted by admin: " + chatChannel.getUniqueChannelName());
					}

				} else if (cc.type == ChannelModType.JOINED) {
					if (!chatChannel.isBanned(playerStateSender)) {
						if (!chatChannel.hasPassword() || chatChannel.checkPassword(cc)
								|| ((GameServerState) state).isAdmin(playerStateSender.getName())) {
							System.err.println("[SERVER] JOINING ON SERVER: hasPasswd: " + chatChannel.hasPassword()
									+ "; Check " + chatChannel.checkPassword(cc) + "; pw provided: " + cc.joinPw);
							chatChannel.joinOnServerAndSend(playerStateSender.getClientChannel());
						} else {
							playerStateSender.sendSimpleCommand(
									SimplePlayerCommands.FAILED_TO_JOIN_CHAT_INVALLID_PASSWD,
									chatChannel.getUniqueChannelName());
							playerStateSender.sendServerMessagePlayerError(Lng.astr("Can't join the channel\n%s:\nIncorrect password!",  chatChannel.getName()));
						}
					} else {
						playerStateSender.sendServerMessagePlayerError(Lng.astr("Can't join the channel\n%s:\nYou are banned there!",  chatChannel.getName()));
					}
				} else if (cc.type == ChannelModType.LEFT) {
					chatChannel.leaveOnServerAndSend(playerStateSender.getClientChannel());
				} else {
					chatChannel.handleMod(cc);
				}
			} else {
				System.err.println("[SERVER] Error: chat channel to modify does not exist: " + cc.channel);
			}
		}
	}

	/**
	 * @return the onServer
	 */
	public boolean isOnServer() {
		return onServer;
	}

	public void addToDefaultChannelVisibleChatLogOnClient(ChatMessage message) {
		assert (!onServer);
		ChatMessage chatMessage = new ChatMessage(message);
		ChatChannel chatChannel;
		if (message.receiverType == ChatMessageType.CHANNEL) {
			chatChannel = channels.get(message.receiver);
		} else {
			String mm = DirectChatChannel.getChannelName(message.sender, message.receiver); // unique per combination
			chatChannel = channels.get(mm);
		}
		chatMessage.setChannel(chatChannel);
		if (ChannelRouter.VISIBLE_CHAT_INSERT_AT_ZERO) {
			defaultVisibleChatLog.add(0, chatMessage);
		} else {
			defaultVisibleChatLog.add(chatMessage);
		}
	}

	public void addToDefaultChannelChatLogOnClient(ChatMessage message) {
		assert (!onServer);
		ChatChannel chatChannel;
		if (message.receiverType == ChatMessageType.CHANNEL) {
			chatChannel = channels.get(message.receiver);
		} else {
			String mm = DirectChatChannel.getChannelName(message.sender, message.receiver); // unique per combination
			chatChannel = channels.get(mm);
		}
		message.setChannel(chatChannel);
		defaultChatLog.add(message);
	}

	/**
	 * @return the defaultVisibleChatLog
	 */
	public List<ChatMessage> getDefaultVisibleChatLog() {
		return defaultVisibleChatLog;
	}

	public List<ChatMessage> getDefaultChatLog() {
		return defaultChatLog;
	}

	public AllChannel getAllChannel() {
		ChatChannel chatChannel = channels.get("all");
		assert (chatChannel != null) : channels;

		return (AllChannel) chatChannel;
	}

	public boolean checkPrefixClient(ChatMessage message, ChatChannel chatChannel) {
		for (int i = 0; i < prefixProcessors.size(); i++) {
			AbstractPrefixProcessor ps = prefixProcessors.get(i);
			if (ps.fits(message)) {
				ps.process(message, chatChannel, (GameClientState) state);
				return ps.sendChatMessageAfterProcessing();
			}
		}

		return true;
	}

	@Override
	public String getUniqueIdentifier() {
		return null;
	}

	@Override
	public boolean isVolatile() {
		return false;
	}

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] top = (Tag[]) tag.getValue();

		ObjectArrayList<PublicChannel> loaded = new ObjectArrayList<PublicChannel>();
		byte version = (Byte) top[0].getValue();

		Tag[] channelTags = (Tag[]) top[1].getValue();

		for (int i = 0; i < channelTags.length - 1; i++) {
			loaded.add(ChatChannel.loadServerChannel((GameServerState) state, channelTags[i]));
		}

		for (int i = 0; i < loaded.size(); i++) {
			channels.put(loaded.get(i).getUniqueChannelName(), loaded.get(i));
		}
	}

	@Override
	public Tag toTagStructure() {

		ObjectArrayList<PublicChannel> toSave = new ObjectArrayList<PublicChannel>();
		for (ChatChannel c : channels.values()) {
			if (c instanceof PublicChannel && ((PublicChannel) c).isPermanent()) {
				toSave.add((PublicChannel) c);
			}
		}

		Tag[] chans = new Tag[toSave.size() + 1];
		chans[chans.length - 1] = FinishTag.INST;

		for (int i = 0; i < toSave.size(); i++) {
			chans[i] = toSave.get(i).toServerTag();
		}

		return new Tag(Type.STRUCT, null, new Tag[]{new Tag(Type.BYTE, "VERSION", (byte) 0),
				new Tag(Type.STRUCT, "CHANNELS", chans), FinishTag.INST});
	}

	public ChatChannel getChannel(String string) {
		return channels.get(string);
	}

	public enum ChannelType {
		ALL, FACTION, PUBLIC, PARTY, DIRECT;

	}

}
