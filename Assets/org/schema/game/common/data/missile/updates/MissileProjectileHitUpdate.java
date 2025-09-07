package org.schema.game.common.data.missile.updates;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.missile.Missile;

import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public class MissileProjectileHitUpdate extends MissileUpdate {

	public float percent;
	public MissileProjectileHitUpdate(byte type, short id) {
		super(type, id);
		assert (type == PROJECTILE_HIT);
	}

	public MissileProjectileHitUpdate(short id) {
		this(PROJECTILE_HIT, id);
	}

	@Override
	protected void decode(DataInputStream stream) throws IOException {
		percent = (stream.readByte() & 255) / 255f;
	}

	@Override
	protected void encode(DataOutputStream buffer) throws IOException {
		buffer.writeByte((int)(percent*255f));
	}

	@Override
	public void encodeMissile(DataOutputStream buffer) throws IOException {
		super.encodeMissile(buffer);
		assert (type == PROJECTILE_HIT);
	}

	@Override
	public void handleClientUpdate(GameClientState state,
			Short2ObjectOpenHashMap<Missile> missiles, ClientChannel channel) {
		Missile m = missiles.get(id);
		if (m != null) {
			m.onClientProjectileHit(percent);
			
		} else {
			System.err.println("[CLIENT][MISSILEUPDATE][PROJECTILEHIT] MISSILE CANNOT BE FOUND: " + id);
		}
	}


}
