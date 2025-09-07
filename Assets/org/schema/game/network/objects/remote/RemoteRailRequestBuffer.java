package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.controller.rails.RailRequest;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteBuffer;
import org.schema.schine.network.objects.remote.Streamable;

public class RemoteRailRequestBuffer extends RemoteBuffer<RemoteRailRequest> {

	private static final int CONTROL_BATCH = 32;

	public RemoteRailRequestBuffer(boolean synchOn) {
		super(RemoteRailRequest.class, synchOn);
	}

	public RemoteRailRequestBuffer(NetworkObject synchOn) {
		super(RemoteRailRequest.class, synchOn);
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {

		int collectionSize = buffer.readInt();

		for (int n = 0; n < collectionSize; n++) {
			RemoteRailRequest instance = new RemoteRailRequest(new RailRequest(), onServer);
			instance.fromByteStream(buffer, updateSenderStateId);
			getReceiveBuffer().add(instance);
		}

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		//add size of collection

		int batchSize = Math.min(CONTROL_BATCH, get().size());

		buffer.writeInt(batchSize);

		int size = 0;
		//			int elementCount = get().size();
		for (int i = 0; i < batchSize; i++) {
			Streamable<?> remoteField = get().remove(0);

			size += remoteField.toByteStream(buffer);
			remoteField.setChanged(false);
		}
		keepChanged = !get().isEmpty();

		return size + ByteUtil.SIZEOF_INT;

	}

	@Override
	protected void cacheConstructor() {

	}

	@Override
	public void clearReceiveBuffer() {
		getReceiveBuffer().clear();
	}

}
