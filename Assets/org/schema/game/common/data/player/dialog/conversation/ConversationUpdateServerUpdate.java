package org.schema.game.common.data.player.dialog.conversation;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.game.common.data.player.dialog.PlayerConversation;

public class ConversationUpdateServerUpdate extends ConverationUpdate {
	private PlayerConversation conversation;

	public ConversationUpdateServerUpdate() {
	}

	public ConversationUpdateServerUpdate(int id) {
		super(id);
	}

	public ConversationUpdateServerUpdate(PlayerConversation converation) {
		super(converation.getConverationPartner().getId());
		this.conversation = converation;
	}

	@Override
	protected void deserialize(DataInputStream buffer) throws IOException {
		super.deserialize(buffer);

		conversation = new PlayerConversation();
		conversation.deserialize(buffer);
	}

	@Override
	public void serialize(DataOutput buffer) throws IOException {
		super.serialize(buffer);
		conversation.serialize(buffer);
	}

	@Override
	public byte getType() {
		return SERVER_UPDATE;
	}

	/**
	 * @return the conversation
	 */
	public PlayerConversation getConversation() {
		return conversation;
	}
}
