package org.schema.game.common.data.chat;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.schema.game.client.controller.ClientChannel;
import org.schema.game.common.data.chat.ChannelRouter.ChannelType;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.network.objects.ChatChannelModification;
import org.schema.game.network.objects.ChatMessage;
import org.schema.schine.graphicsengine.forms.gui.newgui.config.ChatColorPalette;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.client.ClientState;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class PublicChannel extends MuteEnabledChatChannel {

	private final List<ClientChannel> clientChannels = new ObjectArrayList<ClientChannel>();
	private final Set<String> modsLowerCase = new ObjectOpenHashSet<String>();
	private final Set<String> bannedLowerCase = new ObjectOpenHashSet<String>();
	private final String name;
	private boolean permanent;
	private String passwd;

	public PublicChannel(StateInterface state, int id, String name, String passwd, boolean permanent, String[] mods) {
		super(state, id);
		this.name = name;
		this.permanent = permanent;
		this.passwd = passwd;

		if(state instanceof ClientState){
			color.set(ChatColorPalette.other);
		}

		addToModerator(mods);
	}

	@Override
	public Collection<ClientChannel> getClientChannelsInChannel() {
		return clientChannels;
	}

	@Override
	public boolean isPermanent() {
		return permanent;
	}

	@Override
	public boolean hasChannelBanList() {
		return true;
	}

	@Override
	public boolean hasChannelModList() {
		return true;
	}

	@Override
	public boolean hasPossiblePassword() {
		return true;
	}

	@Override
	protected boolean checkPassword(ChatChannelModification cc) {
		return passwd == null || passwd.equals(cc.joinPw);
	}

	@Override
	public boolean hasPassword() {
		return passwd != null && passwd.length() > 0;
	}

	@Override
	protected String[] getModerators() {
		String[] mods = new String[modsLowerCase.size()];
		int i = 0;
		for (String s : modsLowerCase) {
			mods[i] = s;
			i++;
		}
		return mods;
	}

	@Override
	public String[] getBanned() {
		String[] banned = new String[bannedLowerCase.size()];
		int i = 0;
		for (String s : bannedLowerCase) {
			banned[i] = s;
			i++;
		}
		return banned;
	}

	@Override
	public void onFactionChangedServer(ClientChannel c) {
	}

	@Override
	public void onLoginServer(ClientChannel c) {
		//dont join public channel automatically
	}

	@Override
	public void update() {
	}

	@Override
	public boolean isAlive() {
		return permanent || clientChannels.size() > 0;
	}

	@Override
	public ChatMessage process(ChatMessage message) {
		return message;
	}

	/**
	 * @return the name
	 */
	@Override
	public String getUniqueChannelName() {
		return name;
	}

	@Override
	public boolean canLeave() {
		return true;
	}

	@Override
	public boolean isPublic() {
		return true;//getPasswd() == null || getPasswd().length() == 0;
	}

	@Override
	public ChannelType getType() {
		return ChannelType.PUBLIC;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.common.data.chat.ChatChannel#setNewPassword(java.lang.String)
	 */
	@Override
	protected void setNewPassword(String changePasswd) {
		this.passwd = changePasswd;
	}

	@Override
	protected void removeFromModerator(String... mods) {
		for (int i = 0; i < mods.length; i++) {
			modsLowerCase.remove(mods[i].toLowerCase(Locale.ENGLISH));
		}
		notifyObservers();
	}

	@Override
	protected void addToModerator(String... mods) {
		for (int i = 0; i < mods.length; i++) {
			modsLowerCase.add(mods[i].toLowerCase(Locale.ENGLISH));
		}
		notifyObservers();
	}

	@Override
	protected void removeFromBanned(String... banned) {
		for (int i = 0; i < banned.length; i++) {
			bannedLowerCase.remove(banned[i].toLowerCase(Locale.ENGLISH));
		}
		notifyObservers();
	}

	@Override
	protected void addToBanned(String... banned) {
		for (int i = 0; i < banned.length; i++) {
			bannedLowerCase.add(banned[i].toLowerCase(Locale.ENGLISH));
		}
		notifyObservers();
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

	/**
	 * @return the passwd
	 */
	@Override
	public String getPassword() {
		return passwd;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object getTitle() {
		return name;
	}

	@Override
	public boolean isModerator(PlayerState player) {
		return modsLowerCase.contains(player.getName().toLowerCase(Locale.ENGLISH));
	}

	@Override
	public boolean isBanned(PlayerState player) {
		return bannedLowerCase.contains(player.getName().toLowerCase(Locale.ENGLISH));
	}

}
