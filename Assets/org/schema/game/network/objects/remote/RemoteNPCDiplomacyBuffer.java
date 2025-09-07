package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.server.data.FactionState;
import org.schema.game.server.data.simulation.npc.diplomacy.NPCDiplomacyEntity;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteBuffer;

public class RemoteNPCDiplomacyBuffer extends RemoteBuffer<RemoteNPCDiplomacy> {

	private final FactionState state;

	public RemoteNPCDiplomacyBuffer(FactionState state, NetworkObject synchOn) {
		super(RemoteNPCDiplomacy.class, synchOn);
		this.state = state;
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {
		
		int collectionSize = buffer.readInt();

		for (int n = 0; n < collectionSize; n++) {
			
			RemoteNPCDiplomacy instance = new RemoteNPCDiplomacy(new NPCDiplomacyEntity(state, 0), onServer);
			instance.fromByteStream(buffer, updateSenderStateId);
			getReceiveBuffer().add(instance);
		}

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		int size = 0;
		synchronized (get()) {
			//add size of collection
			buffer.writeInt(get().size());
			size += ByteUtil.SIZEOF_INT;

			for (RemoteNPCDiplomacy remoteField : get()) {
				
				size += remoteField.toByteStream(buffer);
			}

			get().clear();

		}
		return size;

	}

	@Override
	protected void cacheConstructor() {
	}

	@Override
	public void clearReceiveBuffer() {
		getReceiveBuffer().clear();
	}

}
