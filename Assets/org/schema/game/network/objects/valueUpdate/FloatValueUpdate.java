package org.schema.game.network.objects.valueUpdate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class FloatValueUpdate extends ValueUpdate {

	protected float val;

	@Override
	public void serialize(DataOutput buffer, boolean onServer) throws IOException {
		buffer.writeFloat(val);
	}

	@Override
	public void deserialize(DataInput stream, int updateSenderStateId, boolean onServer) throws IOException {
		val = stream.readFloat();
	}
}
