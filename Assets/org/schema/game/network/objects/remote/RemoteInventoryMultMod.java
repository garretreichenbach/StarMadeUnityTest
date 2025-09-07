package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.inventory.InventoryMultMod;
import org.schema.game.common.data.player.inventory.InventorySlot;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteInventoryMultMod extends RemoteField<InventoryMultMod> {

	private static final byte HAS_PARAM_NONE = 1;
	private static final byte HAS_PARAM_BYTE = 2;
	private static final byte HAS_PARAM_SHORT = 4;
	private static final byte HAS_PARAM_INT = 8;
	private static final byte HAS_COUNT_BYTE = 16;
	private static final byte HAS_COUNT_SHORT = 32;
	public RemoteInventoryMultMod(InventoryMultMod entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteInventoryMultMod(InventoryMultMod entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_INT;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {

		byte hasParameter = stream.readByte();
		if ((hasParameter & HAS_PARAM_NONE) == HAS_PARAM_NONE) {
			get().parameter = Long.MIN_VALUE;
		} else {
			int x;
			int y;
			int z;
			if ((hasParameter & HAS_PARAM_BYTE) == HAS_PARAM_BYTE) {
				x = stream.readByte();
				y = stream.readByte();
				z = stream.readByte();
			} else if ((hasParameter & HAS_PARAM_SHORT) == HAS_PARAM_SHORT) {
				x = stream.readShort();
				y = stream.readShort();
				z = stream.readShort();
			} else {
				assert ((hasParameter & HAS_PARAM_INT) == HAS_PARAM_INT) : hasParameter;
				x = stream.readInt();
				y = stream.readInt();
				z = stream.readInt();
			}
			get().parameter = ElementCollection.getIndex(x, y, z);
		}
		int slotsChanged;
		if ((hasParameter & HAS_COUNT_BYTE) == HAS_COUNT_BYTE) {
			slotsChanged = stream.readByte() & 0xFF;
		} else {
			assert ((hasParameter & HAS_COUNT_SHORT) == HAS_COUNT_SHORT);
			slotsChanged = stream.readShort();
		}

		get().receivedMods = new InventorySlot[slotsChanged];

		for (int i = 0; i < slotsChanged; i++) {
			get().receivedMods[i] = new InventorySlot();
			Inventory.deserializeSlotNT(stream, get().receivedMods[i]);
		}
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		byte mask = 0;
		final boolean isByteCount = get().slots.size() < 256;
		if (isByteCount) {
			mask |= HAS_COUNT_BYTE;
		} else {
			mask |= HAS_COUNT_SHORT;
		}
		Inventory inventory = get().inventory;
		if (get().parameter != Long.MIN_VALUE) {
			
			if (Math.abs(ElementCollection.getPosX(get().parameter)) < Byte.MAX_VALUE && Math.abs(ElementCollection.getPosY(get().parameter)) < Byte.MAX_VALUE && Math.abs(ElementCollection.getPosZ(get().parameter)) < Byte.MAX_VALUE) {
				buffer.writeByte(mask | HAS_PARAM_BYTE);
				buffer.writeByte(ElementCollection.getPosX(get().parameter));
				buffer.writeByte(ElementCollection.getPosY(get().parameter));
				buffer.writeByte(ElementCollection.getPosZ(get().parameter));

			} else if (Math.abs(ElementCollection.getPosX(get().parameter)) < Short.MAX_VALUE && Math.abs(ElementCollection.getPosY(get().parameter)) < Short.MAX_VALUE && Math.abs(ElementCollection.getPosZ(get().parameter)) < Short.MAX_VALUE) {
				buffer.writeByte(mask | HAS_PARAM_SHORT);
				buffer.writeShort(ElementCollection.getPosX(get().parameter));
				buffer.writeShort(ElementCollection.getPosY(get().parameter));
				buffer.writeShort(ElementCollection.getPosZ(get().parameter));
			} else {
				buffer.writeByte(mask | HAS_PARAM_INT);
				buffer.writeInt(ElementCollection.getPosX(get().parameter));
				buffer.writeInt(ElementCollection.getPosY(get().parameter));
				buffer.writeInt(ElementCollection.getPosZ(get().parameter));
			}
		} else {
			buffer.writeByte(mask | HAS_PARAM_NONE);
		}

		if (isByteCount) {
			buffer.writeByte((byte) get().slots.size());
		} else {
			buffer.writeShort((short) get().slots.size());
		}
		for (int i : get().slots) {
			Inventory.serializeSlotNT(buffer, i, inventory);
		}

		return byteLength();
	}

}
