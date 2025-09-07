package org.schema.game.common.data.missile.updates;

import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.missile.Missile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class MissileUpdate {
	public static final byte SPAWN = 1;
	public static final byte POSITION = 2;
	public static final byte DEAD = 3;
	public static final byte TARGET_CHANGED = 4;
	public static final byte SECTOR_CHANGED = 5;
	public static final byte DIRECTION_AND_POS = 6;
	public static final byte LOCKSTEP = 7;
	public static final byte OBJECT_TRANS = 8;
	public static final byte PROJECTILE_HIT = 9;
	public final short id;
	public final byte type;
	public long timeStampServerSentToClient;

	public MissileUpdate(byte type, short id) {
		this.type = type;
		this.id = id;
		assert (type != 0);
	}

	public static MissileUpdate decodeMissile(DataInputStream stream) throws IOException {
		byte type = stream.readByte();
		short id = stream.readShort();
		MissileUpdate u = switch(type) {
			case (SPAWN) -> new MissileSpawnUpdate(type, id);
			case (POSITION) -> new MissilePositionUpdate(type, id);
			case (DEAD) -> new MissileDeadUpdate(type, id);
			case (TARGET_CHANGED) -> new MissileTargetUpdate(type, id);
			case (SECTOR_CHANGED) -> new MissileSectorChangeUpdate(type, id);
			case (DIRECTION_AND_POS) -> new MissileDirectionAndPosUpdate(type, id);
			case (LOCKSTEP) -> new MissileLockStepUpdate(type, id);
			case (OBJECT_TRANS) -> new MissileTargetPositionUpdate(type, id);
			case (PROJECTILE_HIT) -> new MissileProjectileHitUpdate(type, id);
			default -> throw new IllegalArgumentException("Missile Update type not found " + type);
		};
		u.decode(stream);
		return u;
	}

	protected abstract void decode(DataInputStream stream) throws IOException;

	protected abstract void encode(DataOutputStream buffer) throws IOException;

	public void encodeMissile(DataOutputStream buffer) throws IOException {
		assert (type != 0);
		buffer.writeByte(type);
		buffer.writeShort(id);
		encode(buffer);
	}

	public abstract void handleClientUpdate(GameClientState state, Short2ObjectOpenHashMap<Missile> missiles, ClientChannel channel);
}
