package org.schema.game.common.data.chat;

import java.util.Locale;
import java.util.Set;

import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.network.StateInterface;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public abstract class MuteEnabledChatChannel extends ChatChannel {
	protected final Set<String> mutedLowerCase = new ObjectOpenHashSet<String>();

	public MuteEnabledChatChannel(StateInterface state, int id) {
		super(state, id);
	}

	@Override
	protected void removeFromMuted(String... muted) {
		for (int i = 0; i < muted.length; i++) {
			mutedLowerCase.remove(muted[i].toLowerCase(Locale.ENGLISH));
		}
		notifyObservers();
	}

	@Override
	protected void addToMuted(String... muted) {
		for (int i = 0; i < muted.length; i++) {
			mutedLowerCase.add(muted[i].toLowerCase(Locale.ENGLISH));
		}
		notifyObservers();
	}

	@Override
	public boolean hasChannelMuteList() {
		return true;
	}

	@Override
	public boolean isMuted(PlayerState player) {
		String playerName = player.getName().toLowerCase(Locale.ENGLISH);
		return mutedLowerCase.contains(playerName)
				|| ((GameStateInterface) getState()).getChannelRouter().getAllChannel().mutedLowerCase
				.contains(playerName);
	}

	@Override
	public String[] getMuted() {
		String[] muted = new String[mutedLowerCase.size()];
		int i = 0;
		for (String s : mutedLowerCase) {
			muted[i] = s;
			i++;
		}
		return muted;
	}

}
