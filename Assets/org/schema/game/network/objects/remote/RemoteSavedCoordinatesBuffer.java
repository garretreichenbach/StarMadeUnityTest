package org.schema.game.network.objects.remote;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.data.player.SavedCoordinate;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteBuffer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RemoteSavedCoordinatesBuffer extends RemoteBuffer<RemoteSavedCoordinate> {

	public RemoteSavedCoordinatesBuffer(NetworkObject synchOn) {
		super(RemoteSavedCoordinate.class, synchOn);
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {

		int collectionSize = buffer.readInt();

		for (int n = 0; n < collectionSize; n++) {
			RemoteSavedCoordinate instance = new RemoteSavedCoordinate(new SavedCoordinate(), onServer);
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

			for (RemoteSavedCoordinate remoteField : get()) {
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
