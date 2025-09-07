package org.schema.game.network.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.SerializationInterface;
import org.schema.common.util.linAlg.Vector3i;

public class PlayerKeyConfig implements SerializationInterface {
	public Vector3i blockPos;
	public byte key;
	public String uid;

	public boolean remove;
	public boolean empty;

	public PlayerKeyConfig() {
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		b.writeUTF(uid);
		b.writeBoolean(empty);
		if (!empty) {

			b.writeByte(key);
			b.writeBoolean(remove);
			if (!remove) {
				b.writeShort((short) blockPos.x);
				b.writeShort((short) blockPos.y);
				b.writeShort((short) blockPos.z);

			}
		}

	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
	                        boolean isOnServer) throws IOException {
		uid = b.readUTF();
		empty = b.readBoolean();
		if (!empty) {
			key = b.readByte();
			remove = b.readBoolean();

			if (!remove) {
				blockPos = new Vector3i(b.readShort(), b.readShort(), b.readShort());
			}
		}
		assert ((empty || remove) || blockPos != null);
	}

}
