package org.schema.game.common.data.chat;

import java.util.Collection;
import java.util.List;

import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.graphicsengine.forms.gui.GUIScrollMarkedReadInterface;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIActivatableTextBar;

public interface ChatCallback extends GUIScrollMarkedReadInterface {

	public List<? extends Object> getChatLog();

	public List<? extends Object> getVisibleChatLog();

	public boolean localChatOnClient(String text);

	public void chat(String text);

	public Collection<PlayerState> getMemberPlayerStates();

	public String getName();

	public Object getTitle();

	public void onWindowDeactivate();

	public boolean isSticky();

	public void setSticky(boolean b);

	public boolean isFullSticky();

	public void setFullSticky(boolean b);

	public void leave(PlayerState player);

	public boolean canLeave();

	public GUIActivatableTextBar getChatBar();

	public void setChatBar(GUIActivatableTextBar chatBar);

	public boolean isClientOpen();

	public void setClientOpen(boolean clientOpen);

	public boolean isModerator(PlayerState player);

	public boolean hasChannelBanList();

	public boolean hasChannelMuteList();

	public boolean hasChannelModList();

	public boolean hasPossiblePassword();

	public void requestModUnmodOnClient(String f, boolean b);

	public void requestPasswordChangeOnClient(String passwd);

	public void requestKickOnClient(PlayerState f);

	public boolean isBanned(PlayerState f);

	public void requestBanUnbanOnClient(String f, boolean b);

	public String[] getBanned();

	public boolean isMuted(PlayerState f);

	public void requestMuteUnmuteOnClient(String f, boolean b);

	public String[] getMuted();

	public void requestIgnoreUnignoreOnClient(String f, boolean b);
}
