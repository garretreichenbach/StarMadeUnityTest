package org.schema.game.network.objects.valueUpdate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class StringValueUpdate extends ValueUpdate {

	protected String val;

	@Override
	public void serialize(DataOutput buffer, boolean onServer) throws IOException {
		assert(val != null);
		buffer.writeUTF(val);
	}

	@Override
	public void deserialize(DataInput stream, int updateSenderStateId, boolean onServer) throws IOException {
		val = stream.readUTF();
	}
}
