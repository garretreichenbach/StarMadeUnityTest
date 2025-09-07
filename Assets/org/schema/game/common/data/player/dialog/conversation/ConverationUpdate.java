package org.schema.game.common.data.player.dialog.conversation;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;

public abstract class ConverationUpdate {
	protected static final byte CLIENT_HAIL = 0;
	protected static final byte CLIENT_SELECT = 1;
	protected static final byte SERVER_UPDATE = 2;
	protected static final byte SERVER_END = 3;
	private int conversationParterId;

	public ConverationUpdate(int id) {
		conversationParterId = id;
	}

	public ConverationUpdate() {
	}

	public static ConverationUpdate deserializeStatic(DataInputStream buffer) throws IOException {
		byte type = buffer.readByte();
		ConverationUpdate c = switch(type) {
			case (CLIENT_HAIL) -> new ConversationUpdateClientHail();
			case (CLIENT_SELECT) -> new ConversationUpdateClientSelect();
			case (SERVER_UPDATE) -> new ConversationUpdateServerUpdate();
			case (SERVER_END) -> new ConversationUpdateServerEnd();
			default -> throw new IllegalArgumentException("type unknown: " + type);
		};
		c.deserialize(buffer);
		assert (c.getType() == type);
		return c;
	}

	protected void deserialize(DataInputStream buffer) throws IOException {
		this.conversationParterId = buffer.readInt();
	}

	public void serialize(DataOutput buffer) throws IOException {
		buffer.writeByte(getType());
		buffer.writeInt(conversationParterId);
	}

	public abstract byte getType();

	/**
	 * @return the conversationParterId
	 */
	public int getConversationParterId() {
		return conversationParterId;
	}
}
