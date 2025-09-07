package org.schema.game.common.data.missile.updates;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.vecmath.Vector3f;

import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.missile.Missile;

import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public class MissilePositionUpdate extends MissileUpdate {

	public Vector3f position = new Vector3f();

	public MissilePositionUpdate(byte type, short id) {
		super(type, id);
		assert (type == POSITION);
	}

	public MissilePositionUpdate(short id) {
		this(POSITION, id);
	}

	@Override
	protected void decode(DataInputStream stream) throws IOException {
		position.set(stream.readFloat(), stream.readFloat(), stream.readFloat());
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
//			assert (timeStampServerSentToClient > 0) : timeStampServerSentToClient;
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
////			System.err.println("POSITION BEFORE: "+position);
//			missile.updateTransform(diffFrameTime, wt, missile.getDirection(new Vector3f()), t, true);
//			position.set(t.origin);
////			System.err.println("POSITION AFTER : "+position+"; "+updateTimestamp+"; FT: "+diffFrameTime+"; DIFF: "+diff);
//			//position should now be where it now is on server
//
//			Vector3f dist = new Vector3f();
//			dist.sub(missile.getWorldTransform().origin, position);
//			missile.serverPos.set(position);
//
//			if (dist.length() > 100) {
//				missile.getWorldTransform().origin.set(position);
//				System.err.println("[CLIENT][MISSILE] translating trail: " + position);
//				missile.translateTrail();
//			} else if (dist.length() > 20) {
//				missile.getWorldTransform().origin.set(position);
//			} else {
//				//interpolate 20%
//				missile.getWorldTransform().origin.interpolate(position, 0.2f);
//			}

		} else {
			state.getController().getClientMissileManager().onMissingMissile(id, channel);

		}
	}

}
