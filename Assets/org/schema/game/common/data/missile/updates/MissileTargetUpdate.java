package org.schema.game.common.data.missile.updates;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.missile.Missile;
import org.schema.game.common.data.missile.TargetChasingMissile;
import org.schema.schine.network.objects.remote.RemoteShort;

import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public class MissileTargetUpdate extends MissileUpdate {

	public int target;

	public MissileTargetUpdate(byte type, short id) {
		super(type, id);
	}

	public MissileTargetUpdate(short id) {
		this(TARGET_CHANGED, id);
	}

	@Override
	protected void decode(DataInputStream stream) throws IOException {
		target = stream.readInt();
		assert (type == TARGET_CHANGED);
	}

	@Override
	protected void encode(DataOutputStream buffer) throws IOException {
		buffer.writeInt(target);
	}

	@Override
	public void handleClientUpdate(GameClientState state,
			Short2ObjectOpenHashMap<Missile> missiles, ClientChannel channel) {
		Missile missile = missiles.get(id);
		if (missile != null && missile instanceof TargetChasingMissile) {
			((TargetChasingMissile) missile).setTarget(target);
		} else {
			channel.getNetworkObject().missileMissingRequestBuffer.add(new RemoteShort(id, false));
		}
	}

}
