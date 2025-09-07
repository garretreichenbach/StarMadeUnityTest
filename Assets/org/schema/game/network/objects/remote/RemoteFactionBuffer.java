package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteBuffer;

public class RemoteFactionBuffer extends RemoteBuffer<RemoteFaction> {

	private StateInterface state;

	public RemoteFactionBuffer(NetworkObject synchOn, StateInterface state) {
		super(RemoteFaction.class, synchOn);
		this.state = state;
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {

		int collectionSize = buffer.readInt();

		for (int n = 0; n < collectionSize; n++) {
			int fid = buffer.readInt();
			Faction f;
			if(FactionManager.isNPCFaction(fid)){
				f = new NPCFaction(state, fid);
			}else{
				f = new Faction(state);
			}
			RemoteFaction instance = new RemoteFaction(f, onServer);
			instance.fromByteStream(buffer, updateSenderStateId);
			f.initialize();
			getReceiveBuffer().add(instance);
		}

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		int size = 0;
		//add size of collection
		buffer.writeInt(get().size());

		for (RemoteFaction remoteField : get()) {
			buffer.writeInt(remoteField.get().getIdFaction());
			remoteField.toByteStream(buffer);
		}

		get().clear();

		return 1;

	}


	@Override
	protected void cacheConstructor() {
	}

	@Override
	public void clearReceiveBuffer() {
		getReceiveBuffer().clear();
	}

}
