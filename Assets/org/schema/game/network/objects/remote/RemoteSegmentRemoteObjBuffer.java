package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.common.controller.SegmentController;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteBuffer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class RemoteSegmentRemoteObjBuffer extends RemoteBuffer<RemoteSegmentRemoteObj> {

	//	private Constructor<RemoteMissileUpdate> constructor;
	//	private static Constructor<RemoteMissileUpdate> staticConstructor;

	private final SegmentController attachedController;

	public RemoteSegmentRemoteObjBuffer(NetworkObject synchOn, SegmentController attachedController) {
		super(RemoteSegmentRemoteObj.class, synchOn);
		this.attachedController = attachedController;
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {

		int collectionSize = buffer.readInt();

		for (int n = 0; n < collectionSize; n++) {
			RemoteSegmentRemoteObj.decode(buffer, updateSenderStateId, onServer, attachedController);
		}

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		//add size of collection
		buffer.writeInt(get().size());

		for (RemoteSegmentRemoteObj remoteField : get()) {
			remoteField.toByteStream(buffer);
		}

		get().clear();

		return 1;
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.objects.remote.RemoteBuffer#clearReceiveBuffer()
	 */
	@Override
	public void clearReceiveBuffer() {
		assert (receiveBuffer.size() == 0);
	}

	/* (non-Javadoc)
	 * @see org.schema.schine.network.objects.remote.RemoteBuffer#getReceiveBuffer()
	 */
	@Override
	public ObjectArrayList<RemoteSegmentRemoteObj> getReceiveBuffer() {
		return null;
	}

}
