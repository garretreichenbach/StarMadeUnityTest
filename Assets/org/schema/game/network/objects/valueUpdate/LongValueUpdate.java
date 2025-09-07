package org.schema.game.network.objects.valueUpdate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class LongValueUpdate extends ValueUpdate {

	protected long val;

	@Override
	public void serialize(DataOutput buffer, boolean onServer) throws IOException {
		buffer.writeLong(val);
	}

	@Override
	public void deserialize(DataInput stream, int updateSenderStateId, boolean onServer) throws IOException {
		val = stream.readLong();
	}
}
