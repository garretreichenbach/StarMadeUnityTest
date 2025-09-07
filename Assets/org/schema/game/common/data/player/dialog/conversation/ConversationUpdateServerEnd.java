package org.schema.game.common.data.player.dialog.conversation;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;

public class ConversationUpdateServerEnd extends ConverationUpdate {
	public ConversationUpdateServerEnd() {
	}

	public ConversationUpdateServerEnd(int id) {
		super(id);
	}

	@Override
	protected void deserialize(DataInputStream buffer) throws IOException {
		super.deserialize(buffer);
	}

	@Override
	public void serialize(DataOutput buffer) throws IOException {
		super.serialize(buffer);
	}

	@Override
	public byte getType() {
		return SERVER_END;
	}

}
