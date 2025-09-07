package org.schema.game.common.data.player.inventory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.SerializationInterface;

public class InventorySlotRemoveMod implements SerializationInterface{
	public int slot;
	public long parameter = Long.MIN_VALUE;

	public InventorySlotRemoveMod() {
		super();
	}

	public InventorySlotRemoveMod(int slot, long parameter) {
		super();
		this.slot = slot;
		this.parameter = parameter;
	}

	@Override
	public void serialize(DataOutput b, boolean isOnServer)
			throws IOException {
		boolean p = parameter != Long.MIN_VALUE;
		b.writeBoolean(p);
		if(p){
			b.writeLong(parameter);
		}
		b.writeInt(slot);
	}

	@Override
	public void deserialize(DataInput b, int updateSenderStateId,
			boolean isOnServer) throws IOException {
		boolean p = b.readBoolean();
		
		if(p){
			this.parameter = b.readLong();
		}
		this.slot = b.readInt();
	}

}
