package org.schema.game.network.objects.valueUpdate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class IntModuleValueUpdate extends IntValueUpdate {

	protected long parameter;

	/* (non-Javadoc)
	 * @see org.schema.game.network.objects.valueUpdate.FloatValueUpdate#serialize(java.io.DataOutputStream)
	 */
	@Override
	public void serialize(DataOutput buffer, boolean onServer) throws IOException {
		super.serialize(buffer, onServer);
		assert (parameter != Long.MIN_VALUE);
		buffer.writeLong(parameter);
	}

	/* (non-Javadoc)
	 * @see org.schema.game.network.objects.valueUpdate.FloatValueUpdate#deserialize(java.io.DataInputStream)
	 */
	@Override
	public void deserialize(DataInput stream, int updateSenderStateId, boolean onServer) throws IOException {
		super.deserialize(stream, updateSenderStateId, onServer);
		parameter = stream.readLong();
	}
}
