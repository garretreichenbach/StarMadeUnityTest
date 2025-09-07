package org.schema.game.network.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.SerializationInterface;

public class LongBooleanPair implements SerializationInterface{
	public long l;
	public boolean b;

	public LongBooleanPair() {
	}

	public LongBooleanPair(long l, boolean b) {
		super();
		this.l = l;
		this.b = b;
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeLong(this.l);
		b.writeBoolean(this.b);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		this.l = b.readLong();
		this.b = b.readBoolean();
	}


}
