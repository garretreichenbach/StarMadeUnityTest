package org.schema.game.network.objects.valueUpdate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class ParameterValueUpdate extends ValueUpdate {

	protected long parameter;

	@Override
	public void serialize(DataOutput buffer, boolean onServer) throws IOException {
		buffer.writeLong(parameter);
	}

	@Override
	public void deserialize(DataInput stream, int updateSenderStateId, boolean onServer) throws IOException {
		parameter = stream.readLong();
	}
}
