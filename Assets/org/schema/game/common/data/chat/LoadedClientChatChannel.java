package org.schema.game.common.data.chat;

public class LoadedClientChatChannel {
	public boolean sticky;
	public boolean fullSticky;
	public boolean open;
	public String password;
	public String uid;
	public boolean joinRequestDone;

	public long firstCheck;

	public LoadedClientChatChannel(String uid, boolean sticky, boolean fullSticky,
	                               boolean open, String password) {
		super();
		this.uid = uid;
		this.sticky = sticky;
		this.fullSticky = fullSticky;
		this.open = open;
		this.password = password;
	}

}
