package org.schema.game.network.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.SerializationInterface;

public class LongStringPair implements SerializationInterface {
	public String stringVal;
	public long longVal;

	public LongStringPair() {
	}

	public LongStringPair(long longVal, String stringVal) {
		super();
		this.stringVal = stringVal;
		this.longVal = longVal;
	}

	public LongStringPair(LongStringPair rA) {
		this.stringVal = rA.stringVal;
		this.longVal = rA.longVal;
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		b.writeLong(longVal);
		b.writeUTF(stringVal);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
	                        boolean isOnServer) throws IOException {
		longVal = b.readLong();
		stringVal = b.readUTF();
	}

	;

}
