package org.schema.game.common.data.missile.updates;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.vecmath.Vector3f;

import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.missile.Missile;

import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public class MissileDeadUpdate extends MissileUpdate {

	public Vector3f position = new Vector3f();
	private int hitId;

	public MissileDeadUpdate(byte type, short id) {
		super(type, id);
		assert (type == DEAD);
	}

	public MissileDeadUpdate(short id) {
		this(DEAD, id);
	}

	@Override
	protected void decode(DataInputStream stream) throws IOException {
		position.set(stream.readFloat(), stream.readFloat(), stream.readFloat());
		hitId = stream.readInt();

		//		System.err.println("DECODING MISSILE DEAD UPATE "+position+"; "+hitId);
	}

	@Override
	protected void encode(DataOutputStream buffer) throws IOException {
		buffer.writeFloat(position.x);
		buffer.writeFloat(position.y);
		buffer.writeFloat(position.z);
		buffer.writeInt(hitId);

	}

	@Override
	public void encodeMissile(DataOutputStream buffer) throws IOException {
		super.encodeMissile(buffer);
		assert (type == DEAD);
	}

	@Override
	public void handleClientUpdate(GameClientState state,
			Short2ObjectOpenHashMap<Missile> missiles, ClientChannel channel) {
		Missile remove = missiles.remove(id);
		if (remove != null) {
			remove.onClientDie(hitId);
			
		} else {
			System.err.println("[CLIENT][MISSILEUPDATE][DEAD] MISSILE CANNOT BE FOUND: " + id);
		}
	}

	/**
	 * @return the hitId
	 */
	public int getHitId() {
		return hitId;
	}

	/**
	 * @param hitId the hitId to set
	 */
	public void setHitId(int hitId) {
		this.hitId = hitId;
	}

}
