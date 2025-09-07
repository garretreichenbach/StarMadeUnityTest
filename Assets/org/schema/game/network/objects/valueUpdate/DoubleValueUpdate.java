package org.schema.game.network.objects.valueUpdate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class DoubleValueUpdate extends ValueUpdate {

	protected double val;
	

	@Override
	public void serialize(DataOutput buffer, boolean onServer) throws IOException {
		buffer.writeDouble(val);
	}

	@Override
	public void deserialize(DataInput stream, int updateSenderStateId, boolean onServer) throws IOException {
		double v = stream.readDouble();
		val = v;
	}
}
