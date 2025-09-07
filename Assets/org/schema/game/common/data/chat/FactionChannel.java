package org.schema.game.common.data.chat;

import java.util.Collection;

import org.schema.game.client.controller.ClientChannel;
import org.schema.game.common.data.chat.ChannelRouter.ChannelType;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.network.objects.ChatMessage;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.newgui.config.ChatColorPalette;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.client.ClientState;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class FactionChannel extends MuteEnabledChatChannel {
	private final ObjectArrayList<ClientChannel> clientChannels = new ObjectArrayList<ClientChannel>();
	private final int fid;

	public FactionChannel(StateInterface state, int id, int fid) {
		super(state, id);
		this.fid = fid;

		if(state instanceof ClientState){
			color.set(ChatColorPalette.faction);
		}
	}

	@Override
	public Collection<ClientChannel> getClientChannelsInChannel() {
		return clientChannels;
	}

	@Override
	public boolean isPermanent() {
		return false;
	}

	@Override
	public void onFactionChangedServer(ClientChannel c) {
		if (c.getPlayer().getFactionId() != fid && clientChannels.contains(c)) {
			onClientChannelRemoveServer(c);
			clientChannels.remove(c);
		} else if (c.getPlayer().getFactionId() == fid && !clientChannels.contains(c)) {
			System.err.println("[SERVER][FactionChannel] added player automatically to " + getUniqueChannelName() + ": " + c.getPlayer());
			clientChannels.add(c);
			onClientChannelAddServer(c);
		}
	}

	@Override
	public void onLoginServer(ClientChannel c) {
		assert (isOnServer());
		if (c.getPlayer().getFactionId() == fid) {
			if (!clientChannels.contains(c)) {
				System.err.println("[SERVER] ADDING CLIENT CHANNEL TO FACTION CHANNEL: " + getUniqueChannelName());
				clientChannels.add(c);
				onClientChannelAddServer(c);
			}
		} else {
		}
	}

	@Override
	public void update() {
	}

	@Override
	public boolean isAlive() {
		return clientChannels.size() > 0;
	}

	@Override
	public ChatMessage process(ChatMessage message) {
		return message;
	}

	@Override
	public String getUniqueChannelName() {
		return "Faction" + fid;
	}

	@Override
	public boolean canLeave() {
		return false;
	}

	@Override
	public boolean isPublic() {
		return false;
	}

	@Override
	public ChannelType getType() {
		return ChannelType.FACTION;
	}

	@Override
	protected boolean isAvailableWithoutMyself() {
		return false;
	}

	@Override
	protected boolean addToClientChannels(ClientChannel clientChannel) {
		if (!clientChannels.contains(clientChannel)) {
			return clientChannels.add(clientChannel);
		}
		return false;
	}

	@Override
	protected boolean removeFromClientChannels(ClientChannel clientChannel) {
		return clientChannels.remove(clientChannel);
	}

	@Override
	public boolean isAutoJoinFor(ClientChannel c) {
		return true;
	}

	@Override
	public String getName() {
		return Lng.str("Faction");
	}

	@Override
	public Object getTitle() {
		return Lng.str("Faction Channel");
	}

	@Override
	public boolean isModerator(PlayerState player) {
		return false;
	}

	@Override
	public boolean isBanned(PlayerState f) {
		return false;
	}

	public int getFactionId() {
		return fid;
	}
}
