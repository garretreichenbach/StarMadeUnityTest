package org.schema.game.common.data.chat;

import java.util.Collection;

import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.chat.ChannelRouter.ChannelType;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.network.objects.ChatMessage;
import org.schema.game.network.objects.ChatMessage.ChatMessageType;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.gui.newgui.config.ChatColorPalette;
import org.schema.schine.network.StateInterface;

public class DirectChatChannel extends ChatChannel {

	private final PlayerState a;
	private final PlayerState b;

	public DirectChatChannel(StateInterface state, int id, PlayerState a, PlayerState b) {
		super(state, id);
		this.a = a;
		this.b = b;

		members.add(a);
		members.add(b);
		color.set(ChatColorPalette.whisper);

	}

	public static String getChannelName(PlayerState a, PlayerState b) {
		return getChannelName(a.getName(), b.getName());
	}

	public static String getChannelName(String a, String b) {
		String name;
		if (a.hashCode() < b.hashCode()) {
			name = a + b;
		} else {
			name = b + a;
		}

		return "##" + name;
	}

	@Override
	public Collection<ClientChannel> getClientChannelsInChannel() {
		assert (false) : "this channel should exist on client only";
		return null;
	}

	@Override
	public boolean isPermanent() {
		return false;
	}

	@Override
	public void onFactionChangedServer(ClientChannel c) {

	}

	@Override
	public void onLoginServer(ClientChannel c) {

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
		return getChannelName(a, b);
	}

	@Override
	public void chat(String text) {
		ChatMessage chatMessage = new ChatMessage();
		chatMessage.text = text;
		chatMessage.sender = ((GameClientState) getState()).getPlayerName();
		chatMessage.receiver = getOther().getName();
		chatMessage.receiverType = ChatMessageType.DIRECT;
		send(chatMessage);
	}

	@Override
	public void leave(PlayerState player) {
		player.getPlayerChannelManager().removeDirectChannel(this);
	}

	@Override
	public boolean canLeave() {
		//will leave locally, chat remains in memory on client
		return true;
	}

	@Override
	public boolean isPublic() {
		return false;
	}

	@Override
	public ChannelType getType() {
		return ChannelType.DIRECT;
	}

	@Override
	protected boolean addToClientChannels(ClientChannel clientChannel) {
		return false;
	}

	@Override
	protected boolean removeFromClientChannels(ClientChannel clientChannel) {
		return false;
	}

	@Override
	public String getName() {
		return Lng.str("PM with %s", getOther().getName());
	}

	@Override
	public Object getTitle() {
		return Lng.str("PM with %s", getOther().getName());
	}

	@Override
	public boolean isModerator(PlayerState player) {
		return false;
	}

	@Override
	public boolean hasChannelMuteList() {
				return false;
	}

	@Override
	public boolean isBanned(PlayerState f) {
		return false;
	}

	@Override
	public boolean isMuted(PlayerState f) {
		return false;
	}

	@Override
	public String[] getMuted() {
		return null;
	}

	private PlayerState getOther() {
		return ((GameClientState) getState()).getPlayer() == a ? b : a;
	}
}
