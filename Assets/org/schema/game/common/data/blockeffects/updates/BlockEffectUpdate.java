package org.schema.game.common.data.blockeffects.updates;

import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.data.blockeffects.BlockEffect;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class BlockEffectUpdate {
	public static final byte SPAWN = 1;
	public static final byte DEAD = 3;
	public final short id;
	public final long blockIdentifyer;
	public final byte type;

	public BlockEffectUpdate(byte type, short id, long blockId) {
		this.type = type;
		this.id = id;
		this.blockIdentifyer = blockId;
	}

	public static BlockEffectUpdate decodeEffect(DataInputStream stream) throws IOException {
		byte type = stream.readByte();
		short id = stream.readShort();
		long blockId = stream.readLong();

		BlockEffectUpdate u = switch(type) {
			case (SPAWN) -> new BlockEffectSpawnUpdate<>(type, id, blockId);
			case (DEAD) -> new BlockEffectDeadUpdate(type, id, blockId);
			default -> throw new IllegalArgumentException("Missile Update type not found " + type);
		};
		u.decode(stream);

		return u;
	}

	protected abstract void decode(DataInputStream stream) throws IOException;

	protected abstract void encode(DataOutputStream buffer) throws IOException;

	public void encodeEffect(DataOutputStream buffer) throws IOException {
		buffer.writeByte(type);
		buffer.writeShort(id);
		buffer.writeLong(blockIdentifyer);
		encode(buffer);
	}

	public abstract void handleClientUpdate(
			Short2ObjectOpenHashMap<BlockEffect> effects, SendableSegmentController controller);

}
