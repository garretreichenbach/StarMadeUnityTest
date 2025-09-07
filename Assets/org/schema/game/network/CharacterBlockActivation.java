package org.schema.game.network;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;

public class CharacterBlockActivation {
	public int charId;
	public int objectId;
	public long location;
	public boolean activate;

	public void deserialize(DataInputStream stream, int updateSenderStateId) throws IOException {
		charId = stream.readInt();
		objectId = stream.readInt();
		location = stream.readLong();
		activate = stream.readBoolean();
	}

	public void serialize(DataOutput buffer) throws IOException {
		buffer.writeInt(charId);
		buffer.writeInt(objectId);
		buffer.writeLong(location);
		buffer.writeBoolean(activate);
	}
}
