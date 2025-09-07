package org.schema.game.network.objects;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.SerializationInterface;

public class ShipKeyConfig implements SerializationInterface {
	public boolean remove;
	public long blockPos;
	public byte slot;

	public ShipKeyConfig() {
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {

		b.writeByte(remove ? -(slot + 1) : slot);
		if (!remove) {
			b.writeLong(blockPos);
		}

	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
	                        boolean isOnServer) throws IOException {

		slot = b.readByte();

		remove = slot < 0;
		slot = remove ? (byte) (Math.abs(slot) - 1) : slot;

		if (!remove) {
			blockPos = b.readLong();
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ShipKeyConfig [slot=" + slot + ", blockPos=" + blockPos
				+ ", remove=" + remove + "]";
	}

}
