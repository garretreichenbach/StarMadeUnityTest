package org.schema.game.common.data.player;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.schema.game.client.data.ClientStatics;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.chat.ChatChannel;
import org.schema.game.common.data.chat.LoadedClientChatChannel;
import org.schema.game.network.objects.ChatChannelModification;
import org.schema.game.network.objects.ChatChannelModification.ChannelModType;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.gui.GUIObservable;
import org.schema.schine.resource.FileExt;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class PlayerChannelManager extends GUIObservable {
	private static final byte CLIENT_TAG_VERSION = 0;
	private final PlayerState playerState;
	private final boolean onServer;
	private final ObjectOpenHashSet<ChatChannel> availableChannels = new ObjectOpenHashSet<ChatChannel>();
	public ObjectArrayFIFOQueue<ChatChannelModification> receivedChannelMods = new ObjectArrayFIFOQueue<ChatChannelModification>();
	private List<LoadedClientChatChannel> loadedChannels;

	public PlayerChannelManager(PlayerState playerState) {
		this.playerState = playerState;
		this.onServer = playerState.isOnServer();
		if (!onServer) {
			this.loadedChannels = loadChannelsClient();
		}
	}

	public void handleFailedJoinInvalidPassword(String channel) {
		for (int i = 0; i < loadedChannels.size(); i++) {
			LoadedClientChatChannel ld = loadedChannels.get(i);
			if (ld.uid.equals(channel)) {
				loadedChannels.remove(i);
				break;
			}
		}
	}

	public void update(Timer timer) {
		if (playerState.getClientChannel() != null && playerState.getClientChannel().isConnectionReady() && ((FactionState) playerState.getState()).getFactionManager().getFactionCollection().size() > 0) {
			while (!receivedChannelMods.isEmpty()) {
				ChatChannelModification cc = receivedChannelMods.dequeue();
				if (onServer) {
					handleReceivedChannelServer(cc);
				} else {
					handleReceivedChannelClient(cc);
				}
			}
		}

		if (!onServer) {
			for (ChatChannel c : availableChannels) {
				c.updateVisibleChat(timer);
			}

			for (int i = 0; i < loadedChannels.size(); i++) {
				LoadedClientChatChannel ld = loadedChannels.get(i);
				boolean remove = false;
				if (ld.firstCheck == 0) {
					ld.firstCheck = System.currentTimeMillis();
				}
//				System.err.println("CHECKING:::: "+ld.uid+"; "+ld.open);
				for (ChatChannel c : availableChannels) {

					if (!ld.joinRequestDone && c.getUniqueChannelName().equals(ld.uid)) {

						c.handleClientLoadedChannel(playerState, ld);

					}
					if (ld.joinRequestDone && c.getUniqueChannelName().equals(ld.uid) && c.getMemberPlayerStates().contains(playerState)) {
						//we are in the channel. open it in gui now if necessary

						if (((GameClientState) playerState.getState()).getWorldDrawer() != null && 
								((GameClientState) playerState.getState()).getWorldDrawer().getGuiDrawer() != null &&
								((GameClientState) playerState.getState()).getWorldDrawer().getGuiDrawer().getPlayerPanel() != null &&
								((GameClientState) playerState.getState()).getWorldDrawer().getGuiDrawer().getPlayerPanel().getChat() != null) {
							System.err.println("[CLIENT] successfully rejoined " + c.getUniqueChannelName() + "; opening chat: " + ld.open);
							((GameClientState) playerState.getState()).getWorldDrawer().getGuiDrawer().getChatNew().handleLoaded(c, ld);
						}
						remove = true;
						break;
						

					}
				}
				if (remove || (System.currentTimeMillis() > ld.firstCheck + 30000)) {
					loadedChannels.remove(i);
					i--;
				}
			}
		}
	}

	private void handleReceivedChannelClient(ChatChannelModification cc) {

		ChatChannel chatChannel = ((GameClientState) playerState.getState()).getChannelRouter().handleClientReceivedChannel(playerState, cc);

		if (chatChannel != null) {
			if (cc.type == ChannelModType.CREATE || cc.type == ChannelModType.JOINED || cc.type == ChannelModType.UPDATE || cc.type == ChannelModType.INVITED) {
				availableChannels.add(chatChannel);
			} else if (cc.type == ChannelModType.REMOVED_ON_NOT_ALIVE || cc.type == ChannelModType.DELETE) {
				System.err.println("[CLIENT] removing channel from available because of received mod: " + cc.type.name() + ": " + chatChannel.getUniqueChannelName());
				chatChannel.getMemberPlayerStates().clear();
				availableChannels.remove(chatChannel);
			}
		}

		notifyObservers();
	}

	private void handleReceivedChannelServer(ChatChannelModification cc) {
		((GameServerState) playerState.getState()).getChannelRouter().handleReceivedChannelOnServer(playerState, cc);

	}

	public void removedFromChat(ChatChannel chatChannel) {
		availableChannels.remove(chatChannel);
		notifyObservers();
	}

	public void addedToChat(ChatChannel chatChannel) {
		availableChannels.add(chatChannel);
		notifyObservers();
	}

	/**
	 * @return the onServer
	 */
	public boolean isOnServer() {
		return onServer;
	}

	/**
	 * @return the playerState
	 */
	public PlayerState getPlayerState() {
		return playerState;
	}

	/**
	 * @return the availableChannels
	 */
	public ObjectOpenHashSet<ChatChannel> getAvailableChannels() {
		return availableChannels;
	}

	public void addDirectChannel(ChatChannel chatChannel) {
		availableChannels.add(chatChannel);
		notifyObservers();
	}

	public void removeDirectChannel(ChatChannel chatChannel) {
		availableChannels.remove(chatChannel);
		notifyObservers();
	}

	public Set<ChatChannel> getJoinedChannels() {
		Set<ChatChannel> joined = new ObjectOpenHashSet<ChatChannel>();
		for (ChatChannel a : availableChannels) {
			if (a.getMemberPlayerStates().contains(playerState)) {
				joined.add(a);
			}
		}

		return joined;
	}

	public List<LoadedClientChatChannel> loadChannelsClient() {
		List<LoadedClientChatChannel> l = new ObjectArrayList<LoadedClientChatChannel>();
		String path = getClientSaveChannelPath();
		File f = new FileExt(path);
		if (f.exists()) {

			try {
				Tag readFrom = Tag.readFrom(new BufferedInputStream(new FileInputStream(f)), true, false);
				Tag[] t = (Tag[]) readFrom.getValue();
				byte version = (Byte) t[0].getValue();

				Tag[] channels = (Tag[]) t[1].getValue();

				for (int i = 0; i < channels.length - 1; i++) {
					l.add(ChatChannel.loadClientChannel(channels[i]));
				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.err.println("[CLIENT] chat channels loaded " + l.size()+". (Loaded on instantiating player state)");
		return l;
	}

	public String getClientSaveChannelPath() {
		return ClientStatics.ENTITY_DATABASE_PATH + ((GameClientState) playerState.getState()).getController().getConnection().getHost() + File.separator + "chatConfig.tag";
	}

	public void saveChannelsClient() {
		String path = getClientSaveChannelPath();
		File f = new FileExt(path);
		if (!f.exists()) {
			f.getParentFile().mkdirs();
		}

		Tag saveChannelsClientTag = getSaveChannelsClientTag();

		try {
			saveChannelsClientTag.writeTo(new BufferedOutputStream(new FileOutputStream(f)), true);
			System.err.println("[CLIENT] chat channels saved");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Tag getSaveChannelsClientTag() {

		Set<ChatChannel> joinedChannels = getJoinedChannels();
		Tag[] tags = new Tag[joinedChannels.size() + 1];
		tags[tags.length - 1] = FinishTag.INST;
		int i = 0;
		for (ChatChannel c : joinedChannels) {
			Tag t = c.toClientTag();
			tags[i] = t;
			i++;
		}

		return new Tag(Type.STRUCT, null, new Tag[]{new Tag(Type.BYTE, null, CLIENT_TAG_VERSION), new Tag(Type.STRUCT, null, tags), FinishTag.INST});
	}

	public void onJoinChannelAttempt(ChatChannel c, String passwd) {
		for (int i = 0; i < loadedChannels.size(); i++) {
			LoadedClientChatChannel ld = loadedChannels.get(i);
			if (c.getUniqueChannelName().equals(ld.uid)) {
				//set new password if manual join attempt was done
				ld.password = passwd;
				break;
			}
		}
	}

}
