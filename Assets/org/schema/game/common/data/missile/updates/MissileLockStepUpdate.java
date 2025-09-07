package org.schema.game.common.data.missile.updates;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.vecmath.Vector3f;

import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.missile.Missile;
import org.schema.game.common.data.missile.MissileTargetPosition;
import org.schema.game.common.data.missile.TargetChasingMissile;

import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public class MissileLockStepUpdate extends MissileUpdate {

	public Vector3f position = new Vector3f();

	public MissileLockStepUpdate(byte type, short id) {
		super(type, id);
		assert (type == LOCKSTEP);
	}

	public MissileLockStepUpdate(short id) {
		this(LOCKSTEP, id);
	}

	@Override
	protected void decode(DataInputStream stream) throws IOException {
		position.x = stream.readFloat();
		position.y = stream.readFloat();
		position.z = stream.readFloat();
	}

	@Override
	protected void encode(DataOutputStream buffer) throws IOException {
		buffer.writeFloat(position.x);
		buffer.writeFloat(position.y);
		buffer.writeFloat(position.z);
	}

	@Override
	public void handleClientUpdate(GameClientState state,
			Short2ObjectOpenHashMap<Missile> missiles, ClientChannel channel) {
		Missile missile = missiles.get(id);
		if (missile != null) {
			MissileTargetPosition pos = new MissileTargetPosition();
			pos.targetPosition = new Vector3f(position);
			pos.time = ((TargetChasingMissile)missile).spawnTime + ((TargetChasingMissile)missile).steps * TargetChasingMissile.UPDATE_LEN;
			((TargetChasingMissile)missile).steps++;
			
			((TargetChasingMissile)missile).targetPositions.add(pos);


		} else {
			if(id != -1){
				state.getController().getClientMissileManager().onMissingMissile(id, channel);
			}
		}
	}

}
