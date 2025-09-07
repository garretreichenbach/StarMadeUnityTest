package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.data.mines.updates.MineUpdate;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteBuffer;

public class RemoteMineUpdateBuffer extends RemoteBuffer<RemoteMineUpdate> {

	//	private Constructor<RemoteMissileUpdate> constructor;
	//	private static Constructor<RemoteMissileUpdate> staticConstructor;

	public RemoteMineUpdateBuffer(NetworkObject synchOn) {
		super(RemoteMineUpdate.class, synchOn);
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {
		long ts = buffer.readLong();
		int collectionSize = buffer.readInt();

		for (int n = 0; n < collectionSize; n++) {

			MineUpdate instance = MineUpdate.deserializeStatic(buffer, updateSenderStateId, onServer);
			RemoteMineUpdate r = new RemoteMineUpdate(instance, onServer);
			
			getReceiveBuffer().add(r);
			
			instance.timeStampServerSent = ts;
		}

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		int size = 0;
		synchronized (get()) {
			buffer.writeLong(System.currentTimeMillis());
			//add size of collection
			buffer.writeInt(get().size());
			size += ByteUtil.SIZEOF_INT;

			for (RemoteMineUpdate remoteField : get()) {
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
