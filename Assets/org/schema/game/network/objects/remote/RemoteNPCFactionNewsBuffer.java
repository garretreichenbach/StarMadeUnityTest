package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.server.data.simulation.npc.news.NPCFactionNews.NPCFactionNewsEventType;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteBuffer;

public class RemoteNPCFactionNewsBuffer extends RemoteBuffer<RemoteNPCFactionNews> {

	public RemoteNPCFactionNewsBuffer(NetworkObject synchOn) {
		super(RemoteNPCFactionNews.class, synchOn);
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {

		int collectionSize = buffer.readInt();

		for (int n = 0; n < collectionSize; n++) {
			
			NPCFactionNewsEventType t = NPCFactionNewsEventType.values()[buffer.readByte()];
			
			RemoteNPCFactionNews instance = new RemoteNPCFactionNews(t.instance(), onServer);
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

			for (RemoteNPCFactionNews remoteField : get()) {
				buffer.writeByte(remoteField.get().getType().ordinal());
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
