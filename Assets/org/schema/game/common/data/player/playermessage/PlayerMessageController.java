package org.schema.game.common.data.player.playermessage;

import api.listener.events.player.MailReceiveEvent;
import api.mod.StarLoader;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.observer.DrawerObservable;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionPermission;
import org.schema.game.network.objects.remote.RemotePlayerMessage;
import org.schema.game.network.objects.remote.RemotePlayerMessageBuffer;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.PlayerNotFountException;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.commands.LoginRequest;
import org.schema.schine.network.objects.remote.RemoteIntBuffer;
import org.schema.schine.network.server.ServerMessage;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

public class PlayerMessageController extends DrawerObservable {

	private final ClientChannel channel;
	public ObjectArrayList<PlayerMessage> messagesReceived = new ObjectArrayList<PlayerMessage>();
	public boolean initialRequest;
	ObjectArrayFIFOQueue<PlayerMessage> received = new ObjectArrayFIFOQueue<PlayerMessage>();
	private int requCount;
	private boolean unreadMessages;
	public PlayerMessageController(ClientChannel player) {
		this.channel = player;
	}

	public static PlayerMessage getNew(String from, String to, String topic, String message) {
		PlayerMessage m = new PlayerMessage();
		m.setFrom(from);
		m.setTo(to);
		m.setTopic(topic);
		m.setSent(System.currentTimeMillis());
		m.setMessage(message);
		m.setRead(false);
		return m;
	}

	public void retriveInitial(int count) {
		GameServerState state = (GameServerState) channel.getState();
		try {
			messagesReceived.addAll(state.getDatabaseIndex().getTableManager().getPlayerMessagesTable().loadPlayerMessages(channel.getPlayer().getName().toLowerCase(Locale.ENGLISH), System.currentTimeMillis(), count));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Collections.sort(messagesReceived);

		if (channel.getPlayer().isNewPlayerServer()) {

			PlayerMessage m = new PlayerMessage();
			m.setFrom("StarMade");
			m.setTo(channel.getPlayer().getName());
			m.setTopic("Welcome to the new Mail System");
			m.setSent(System.currentTimeMillis());
			m.setMessage("Messages are sent even when people are offline.\nThey are delivered when they come online!\n\nThanks for playing StarMade\n - schema");
			m.setRead(false);
			messagesReceived.add(m);
		}

		for (PlayerMessage p : messagesReceived) {
			channel.getNetworkObject().playerMessageBuffer.add(new RemotePlayerMessage(p, true));
		}
	}

	public void retriveMore(int count) {
		try {

			GameServerState state = (GameServerState) channel.getState();
			long t = System.currentTimeMillis();
			if (messagesReceived.size() > 0) {
				t = messagesReceived.get(messagesReceived.size() - 1).getSent();
			}
			ObjectArrayList<PlayerMessage> loadPlayerMessages = state.getDatabaseIndex().getTableManager().getPlayerMessagesTable().loadPlayerMessages(channel.getPlayer().getName().toLowerCase(Locale.ENGLISH), t, count);
			System.err.println("[SERVER][MESSAGES] retrieveing " + count + " more messages for " + channel.getPlayer() + "; loaded: " + loadPlayerMessages.size());

			messagesReceived.addAll(loadPlayerMessages);
			Collections.sort(messagesReceived);

			for (PlayerMessage p : loadPlayerMessages) {
				channel.getNetworkObject().playerMessageBuffer.add(new RemotePlayerMessage(p, true));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void update() {
		if (!received.isEmpty()) {
			while (!received.isEmpty()) {
				PlayerMessage r;
				synchronized (received) {
					r = received.dequeue();
				}
				System.err.println("[PLAYERMESSAGE] " + channel.getState() + " Handle Received PlayerMessage: " + r);

				//INSERTED CODE @109
				MailReceiveEvent event = new MailReceiveEvent(r, this.channel);
				StarLoader.fireEvent(MailReceiveEvent.class, event, this.channel.isOnServer());
				if(!event.isCanceled()) {
					if (this.channel.isOnServer()) {
						this.handleReceivedOnServer(r, false);
					} else {
						this.handleReceivedOnClient(r);
					}
				}
				/////

			}
			Collections.sort(messagesReceived);
		}
		this.unreadMessages = false;
		for (PlayerMessage g : messagesReceived) {
			if (!g.isRead()) {
				this.unreadMessages = true;
				break;
			}
		}
		if (requCount > 0) {
			int c = requCount;
			requCount = 0;
			if (messagesReceived.isEmpty()) {
				retriveInitial(c);
			} else {
				retriveMore(c);
			}
		}
	}

	/**
	 * this is in update loop, so it's synched
	 * and no racing conditions apply
	 *
	 * @param r
	 */
	private void handleReceivedOnClient(PlayerMessage r) {

		if (r.isDeleted()) {
			boolean remove = messagesReceived.remove(r);
			System.err.println("[CLIENT][PLAYERMESSAGES] remove " + r + "; removed: " + remove);

		} else {
			messagesReceived.add(r);
		}
		for (PlayerMessage g : messagesReceived) {
			if (!g.isRead()) {
				((GameClientState) channel.getState()).getController().popupInfoTextMessage(Lng.str("You have unread mail!"), 0);
				break;
			}
		}
		notifyObservers(r);
	}

	/**
	 * this is in update loop, so it's synched
	 * and no racing conditions apply
	 *
	 * @param r
	 * @param b
	 */
	private void handleReceivedOnServer(PlayerMessage r, boolean quiet) {
		GameServerState state = (GameServerState) channel.getState();
		String to = r.getTo();
		if (to.toLowerCase(Locale.ENGLISH).startsWith("faction[")) {

			if (!to.toLowerCase(Locale.ENGLISH).endsWith("]")) {
				return;
			}
			if ("faction[".length() < to.length() - 1) {
				String factionName = to.substring("faction[".length(), to.length() - 1);

				Collection<Faction> factionCollection = state.getFactionManager().getFactionCollection();
				for (Faction f : factionCollection) {
					if (f.getName().toLowerCase(Locale.ENGLISH).equals(factionName.toLowerCase(Locale.ENGLISH))) {

						for (FactionPermission m : f.getMembersUID().values()) {
							PlayerMessage p = getNew(r.getFrom(), m.playerUID, "[FACTION MAIL] " + r.getTopic(), r.getMessage());
							System.err.println("[CLIENT][PLAYERMESSAGE] sending message: " + p);
							handleReceivedOnServer(p, true);
						}
						return;
					}
				}
				return;
			}
			return;
		} else {

			if (r.isDeleted()) {

				try {
					System.err.println("[SERVER][PLAYERMESSAGES] remove " + r);
					state.getDatabaseIndex().getTableManager().getPlayerMessagesTable().deleteMessage(r);
					messagesReceived.remove(r);
					channel.getNetworkObject().playerMessageBuffer.add(new RemotePlayerMessage(r, channel.getNetworkObject()));
					return;
				} catch (SQLException e) {
					e.printStackTrace();
				}

			}

			//insert message into database
			try {
				state.getDatabaseIndex().getTableManager().getPlayerMessagesTable().updateOrInsertMessage(r);
			} catch (SQLException e) {
				e.printStackTrace();
			}

			boolean exists = false;

			for (PlayerMessage m : messagesReceived) {
				if (m.getFrom().toLowerCase(Locale.ENGLISH).equals(r.getFrom().toLowerCase(Locale.ENGLISH)) && m.getTo().toLowerCase(Locale.ENGLISH).equals(r.getTo().toLowerCase(Locale.ENGLISH)) && m.getSent() == r.getSent()) {
					m.setRead(r.isRead());
					exists = true;
					System.err.println("[SERVER][PLAYERMESSAGES] changed Player message " + m);
					break;
				}
			}

			if (!exists) {

				//route message to receiver
				try {
					PlayerState receiver = state.getPlayerFromName(r.getTo());

					receiver.getClientChannel().getPlayerMessageController().receiveOnServer(r);

					if (!quiet) {
						ServerMessage m = new ServerMessage(Lng.astr("Message delivered to\n%s",  r.getTo()), ServerMessage.MESSAGE_TYPE_INFO, channel.getPlayer().getId());
						channel.getPlayer().sendServerMessage(m);
					}
				} catch (PlayerNotFountException e) {
					//Player not online or doesn't exist
					//insert message into database
					if (!quiet) {
						ServerMessage m = new ServerMessage(Lng.astr("Message can't be delivered\nat this time.\nPlayer '%s'\nwill receive it when joining.",  r.getTo()), ServerMessage.MESSAGE_TYPE_WARNING, channel.getPlayer().getId());
						channel.getPlayer().sendServerMessage(m);
					}
				}

			}
		}
	}

	/**
	 * this is called on server
	 * for the actual receiving player
	 *
	 * @param r
	 */
	private void receiveOnServer(PlayerMessage r) {
		messagesReceived.add(r);
		Collections.sort(messagesReceived);

		//send this message to its receiver's client
		channel.getNetworkObject().playerMessageBuffer.add(new RemotePlayerMessage(r, channel.getNetworkObject()));
	}

	public boolean clientSend(String from, String to, String topic, String message) {
		assert (!channel.isOnServer());

		if (to.length() < 3) {
			((GameClientState) channel.getState()).getController()
					.popupAlertTextMessage(Lng.str("Cannot send!\nName too short!"), 0);
			return false;
		}
		if (to.length() > 32) {
			((GameClientState) channel.getState()).getController()
					.popupAlertTextMessage(Lng.str("Cannot send!\nName too long!"), 0);
			return false;
		}
		if (LoginRequest.reserved.contains(to)) {
			((GameClientState) channel.getState()).getController()
					.popupAlertTextMessage(Lng.str("Cannot send!\nReceiver's name %s\nis reserved for system!",  to), 0);
			return false;
		}

		if (to.toLowerCase(Locale.ENGLISH).startsWith("faction[")) {
			if (!to.toLowerCase(Locale.ENGLISH).endsWith("]")) {
				((GameClientState) channel.getState()).getController()
						.popupAlertTextMessage(Lng.str("Cannot send!\nReceiver's faction name has\ninvalid format!\n(faction name must between [ ])"), 0);
				return false;
			}
			if ("faction[".length() < to.length() - 1) {
				String factionName = to.substring("faction[".length(), to.length() - 1);

				Collection<Faction> factionCollection = ((GameClientState) channel.getState()).getFactionManager().getFactionCollection();
				for (Faction f : factionCollection) {
					if (f.getName().toLowerCase(Locale.ENGLISH).equals(factionName.toLowerCase(Locale.ENGLISH))) {
						PlayerMessage m = getNew(from, to, topic, message);

						System.err.println("[CLIENT][PLAYERMESSAGE] sending message: " + m);

						channel.getNetworkObject().playerMessageBuffer.add(new RemotePlayerMessage(m, channel.getNetworkObject()));
						((GameClientState) channel.getState()).getController()
								.popupInfoTextMessage(Lng.str("Message sent to faction\n%s",  factionName), 0);

						return true;
					}
				}
				((GameClientState) channel.getState()).getController()
						.popupAlertTextMessage(Lng.str("Cannot send!\nFaction unknown:\n%s", factionName), 0);
				return false;
			}
			((GameClientState) channel.getState()).getController()
					.popupAlertTextMessage(Lng.str("Cannot send!\nFaction unknown!"), 0);
			return false;
		}
		PlayerMessage m = getNew(from, to, topic, message);

		System.err.println("[CLIENT][PLAYERMESSAGE] sending message: " + m);

		channel.getNetworkObject().playerMessageBuffer.add(new RemotePlayerMessage(m, channel.getNetworkObject()));
		((GameClientState) channel.getState()).getController()
				.popupInfoTextMessage(Lng.str("Message sent to\n%s",  m.getTo()), 0);
		return true;
	}

	public void serverSend(String from, String to, String topic, String message) {
		assert (channel.isOnServer());

		PlayerMessage m = getNew(from, to, topic, message);
		System.err.println("[SERVER] sending message: " + m);
		messagesReceived.add(m);
		Collections.sort(messagesReceived);
		channel.getNetworkObject().playerMessageBuffer.add(new RemotePlayerMessage(m, true));

	}

	public void handleReceived(RemotePlayerMessageBuffer buffer, RemoteIntBuffer playerMessageRequests) {
		for (int i = 0; i < buffer.getReceiveBuffer().size(); i++) {
			PlayerMessage playerMessage = buffer.getReceiveBuffer().get(i).get();
			synchronized (received) {
				System.err.println("[PLAYERMESSAGE] " + channel.getState() + " Received PlayerMessage: " + playerMessage);
				received.enqueue(playerMessage);
			}
		}
		for (int i = 0; i < playerMessageRequests.getReceiveBuffer().size(); i++) {
			int requCount = playerMessageRequests.getReceiveBuffer().get(i);
			System.err.println("[SERVER][PLAYERMESSAGE] received message request from " + channel.getPlayer() + " for " + requCount + " messages");
			this.requCount = requCount;
		}
	}

	public void updateToNetworkObject() {

	}

	public void clientDelete(PlayerMessage f) {
		assert (!channel.isOnServer());
		f.setDeleted(true);
		channel.getNetworkObject().playerMessageBuffer.add(new RemotePlayerMessage(f, channel.getNetworkObject()));
	}

	/**
	 * @return the unreadMessages
	 */
	public boolean hasUnreadMessages() {
		return unreadMessages;
	}

	/**
	 * @return the channel
	 */
	public ClientChannel getChannel() {
		return channel;
	}

	public void deleteAllMessagesClient() {
		for (PlayerMessage f : messagesReceived) {
			f.setDeleted(true);
			channel.getNetworkObject().playerMessageBuffer.add(new RemotePlayerMessage(f, channel.getNetworkObject()));

		}
	}

}
