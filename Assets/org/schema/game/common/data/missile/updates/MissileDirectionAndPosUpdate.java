package org.schema.game.common.data.missile.updates;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.vecmath.Vector3f;

import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.missile.Missile;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public class MissileDirectionAndPosUpdate extends MissileUpdate {

	private static final Transform t = new Transform();
	private static final Transform wt = new Transform();
	public Vector3f direction = new Vector3f();
	public Vector3f position = new Vector3f();

	public MissileDirectionAndPosUpdate(byte type, short id) {
		super(type, id);
		assert (type == DIRECTION_AND_POS);
	}

	public MissileDirectionAndPosUpdate(short id) {
		this(DIRECTION_AND_POS, id);
	}

	@Override
	protected void decode(DataInputStream stream) throws IOException {
		direction.set(stream.readFloat(), stream.readFloat(), stream.readFloat());
		position.set(stream.readFloat(), stream.readFloat(), stream.readFloat());
	}

	@Override
	protected void encode(DataOutputStream buffer) throws IOException {
		buffer.writeFloat(direction.x);
		buffer.writeFloat(direction.y);
		buffer.writeFloat(direction.z);

		buffer.writeFloat(position.x);
		buffer.writeFloat(position.y);
		buffer.writeFloat(position.z);
	}

	@Override
	public void handleClientUpdate(GameClientState state,
			Short2ObjectOpenHashMap<Missile> missiles, ClientChannel channel) {
		Missile missile = missiles.get(id);
		if (missile != null) {

//			//lag compensation by prediction of the location of the missile from the time
//			//the update was sent on server and interpolate to that position
//			long updateTimestamp = timeStampServerSentToClient - state.getServerTimeDifference();
//			long now = System.currentTimeMillis();
//
//			long diff = now - updateTimestamp;
//			float diffFrameTime = diff / 1000f;
//
//			t.setIdentity();
//			wt.setIdentity();
//			wt.origin.set(position);
//			Vector3f dir = new Vector3f(direction);
//			missile.updateTransform(diffFrameTime, wt, dir, t, true);
//			position.set(t.origin);
//
//			//position should now be where it now is on server
//
//			Vector3f dist = new Vector3f();
//			dist.sub(missile.getWorldTransform().origin, position);
//			missile.serverPos.set(position);
//
//			if (dist.length() > 100) {
//				missile.getWorldTransform().origin.set(position);
//				System.err.println("[CLIENT][MISSILE] JUMP-DIST " + dist.length() + ": translating trail: " + position);
//				missile.translateTrail();
//			} else if (dist.length() > 20) {
//				missile.getWorldTransform().origin.set(position);
//			} else {
//				//interpolate 20%
//				missile.getWorldTransform().origin.interpolate(position, 0.2f);
//			}
//			Vector3f dInt = missile.getDirection(new Vector3f());
//			dInt.interpolate(dir, 0.4f);
//			missile.setDirection(dInt);//
		} else {
			state.getController().getClientMissileManager().onMissingMissile(id, channel);

		}
	}

}
