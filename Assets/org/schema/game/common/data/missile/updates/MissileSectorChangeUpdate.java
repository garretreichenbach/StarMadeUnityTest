package org.schema.game.common.data.missile.updates;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.client.controller.ClientChannel;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.missile.Missile;
import org.schema.schine.network.objects.remote.RemoteShort;

import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

public class MissileSectorChangeUpdate extends MissileUpdate {

	public int sectorId;

	public MissileSectorChangeUpdate(byte type, short id) {
		super(type, id);
		assert (type == SECTOR_CHANGED);
	}

	public MissileSectorChangeUpdate(short id) {
		this(SECTOR_CHANGED, id);
	}

	@Override
	protected void decode(DataInputStream stream) throws IOException {
		sectorId = stream.readInt();
	}

	@Override
	protected void encode(DataOutputStream buffer) throws IOException {
		buffer.writeInt(sectorId);
	}

	@Override
	public void handleClientUpdate(GameClientState state,
			Short2ObjectOpenHashMap<Missile> missiles, ClientChannel channel) {
		Missile missile = missiles.get(id);
		if (missile != null) {
			missile.setSectorId(sectorId, false);
		} else {
			channel.getNetworkObject().missileMissingRequestBuffer.add(new RemoteShort(id, false));
		}
	}

}
