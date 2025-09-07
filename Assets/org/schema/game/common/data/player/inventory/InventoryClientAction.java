package org.schema.game.common.data.player.inventory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.SerializationInterface;

public class InventoryClientAction implements SerializationInterface {

	public int ownInventoryOwnerId;
	public long ownInventoryPosId;

	public int otherInventoryOwnerId;
	public long otherInventoryPosId;

	public int slot;
	public int otherSlot;
	public int subSlot;
	public int count;

	public InventoryClientAction() {
	}

	public InventoryClientAction(Inventory first,
	                             Inventory other, int slot, int otherSlot, int subSlot,
	                             int count) {

		ownInventoryOwnerId = first.getInventoryHolder().getId();
		if (first.getParameter() != Long.MIN_VALUE) {
			ownInventoryPosId = first.getParameter();
		} else {
			ownInventoryPosId = Long.MIN_VALUE;
		}
		otherInventoryOwnerId = other.getInventoryHolder().getId();
		if (other.getParameter() != Long.MIN_VALUE) {
			otherInventoryPosId = other.getParameter();
		} else {
			otherInventoryPosId = Long.MIN_VALUE;
		}
		this.slot = slot;
		this.otherSlot = otherSlot;
		this.subSlot = subSlot;
		this.count = count;

	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {

		b.writeInt(ownInventoryOwnerId);
		b.writeLong(ownInventoryPosId);
		b.writeInt(otherInventoryOwnerId);
		b.writeLong(otherInventoryPosId);
		b.writeInt(slot);
		b.writeInt(otherSlot);
		b.writeInt(subSlot);
		b.writeInt(count);

	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
	                        boolean isOnServer) throws IOException {

		ownInventoryOwnerId = b.readInt();
		ownInventoryPosId = b.readLong();
		otherInventoryOwnerId = b.readInt();
		otherInventoryPosId = b.readLong();

		slot = b.readInt();
		otherSlot = b.readInt();
		subSlot = b.readInt();
		count = b.readInt();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "InventoryClientAction [ownInventoryOwnerId="
				+ ownInventoryOwnerId + ", ownInventoryPosId="
				+ ownInventoryPosId + ", otherInventoryOwnerId="
				+ otherInventoryOwnerId + ", otherInventoryPosId="
				+ otherInventoryPosId + ", slot=" + slot + ", otherSlot="
				+ otherSlot + ", subSlot=" + subSlot + ", count=" + count + "]";
	}

}
