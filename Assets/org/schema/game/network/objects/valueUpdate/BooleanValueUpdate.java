package org.schema.game.network.objects.valueUpdate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class BooleanValueUpdate extends ValueUpdate {

	protected boolean val;
	

	@Override
	public void serialize(DataOutput buffer, boolean onServer) throws IOException {
		buffer.writeBoolean(val);
	}

	@Override
	public void deserialize(DataInput stream, int updateSenderStateId, boolean onServer) throws IOException {
		boolean v = stream.readBoolean();
		val = v;
	}
}
