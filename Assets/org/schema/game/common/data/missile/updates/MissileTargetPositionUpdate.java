package org.schema.game.common.data.missile.updates;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.linAlg.TransformTools;
import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.missile.Missile;

import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public class MissileTargetPositionUpdate extends MissileUpdate {
	
	public Transform objectTrans = new Transform();
	public int ticks;
	public int objId;
	public int targetSectorId;

	public MissileTargetPositionUpdate(byte type, short id) {
		super(type, id);
		assert (type == OBJECT_TRANS);
	}

	public MissileTargetPositionUpdate(short id) {
		this(OBJECT_TRANS, id);
	}

	@Override
	protected void decode(DataInputStream stream) throws IOException {
		TransformTools.deserializeFully(stream, objectTrans);
		ticks = stream.readInt();
		objId = stream.readInt();
		targetSectorId = stream.readInt();
	}

	@Override
	protected void encode(DataOutputStream buffer) throws IOException {
		TransformTools.serializeFully(buffer, objectTrans);
		buffer.writeInt(ticks);
		buffer.writeInt(objId);
		buffer.writeInt(targetSectorId);
	}

	@Override
	public void handleClientUpdate(GameClientState state,
			Short2ObjectOpenHashMap<Missile> missiles, ClientChannel channel) {
		state.getController().getClientMissileManager().receivedPosUpdate(this);
	}

}
