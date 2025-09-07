package org.schema.game.common.data.chat;

import java.util.Collection;

import org.schema.game.client.controller.ClientChannel;
import org.schema.game.common.data.chat.ChannelRouter.ChannelType;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.network.objects.ChatMessage;
import org.schema.schine.network.StateInterface;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class AllChannel extends MuteEnabledChatChannel {

	private final ObjectArrayList<ClientChannel> clientChannels = new ObjectArrayList<ClientChannel>();

	public AllChannel(StateInterface state, int id) {
		super(state, id);
	}

	@Override
	public Collection<ClientChannel> getClientChannelsInChannel() {
		return clientChannels;
	}

	@Override
	public boolean isPermanent() {
		return true;
	}

	@Override
	public void onFactionChangedServer(ClientChannel c) {
	}

	@Override
	public void onLoginServer(ClientChannel c) {
		assert (isOnServer());
		clientChannels.add(c);
		onClientChannelAddServer(c);
	}

	@Override
	public void update() {
	}

	@Override
	public boolean isAlive() {
		return true;
	}

	@Override
	public ChatMessage process(ChatMessage message) {
		return message;
	}

	@Override
	public String getUniqueChannelName() {
		return "all";
	}

	@Override
	public boolean isPublic() {
		return true;
	}

	@Override
	public ChannelType getType() {
		return ChannelType.ALL;
	}

	@Override
	public boolean canLeave() {
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
		return "General";
	}

	@Override
	public Object getTitle() {
		return "General Channel";
	}

	@Override
	public boolean isModerator(PlayerState player) {
		return false;
	}

	@Override
	public boolean isBanned(PlayerState f) {
		return false;
	}

}
