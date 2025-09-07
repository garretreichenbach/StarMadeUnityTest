package org.schema.game.network.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.SerializationInterface;

public class DragDrop implements SerializationInterface{

	public int slot;
	public int count;
	public short type;
	public int subType;
	public long parameter;
	public int invId;

	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		b.writeInt(slot);
		b.writeInt(count);
		b.writeShort(type);
		b.writeInt(subType);
		b.writeInt(invId);
		b.writeLong(parameter);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
			boolean isOnServer) throws IOException {
		slot = b.readInt();
		count = b.readInt();
		type = b.readShort();
		subType = b.readInt();
		invId = b.readInt();
		parameter = b.readLong();
		
	}

}
