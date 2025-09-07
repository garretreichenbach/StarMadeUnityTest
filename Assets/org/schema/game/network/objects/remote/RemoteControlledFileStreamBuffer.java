package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.util.FileStreamSegment;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteBuffer;
import org.schema.schine.network.objects.remote.Streamable;

public class RemoteControlledFileStreamBuffer extends RemoteBuffer<RemoteControlledFileStream> {

	private static final int CONTROL_BATCH = 16;
	private final int fileStreamSize;

	public RemoteControlledFileStreamBuffer(boolean synchOn, int fileStreamSize) {
		super(RemoteControlledFileStream.class, synchOn);
		this.fileStreamSize = fileStreamSize;
	}

	public RemoteControlledFileStreamBuffer(NetworkObject synchOn, int fileStreamSize) {
		super(RemoteControlledFileStream.class, synchOn);
		this.fileStreamSize = fileStreamSize;
	}

	@Override
	public void fromByteStream(DataInputStream buffer, int updateSenderStateId) throws IOException {

		int collectionSize = buffer.readByte();

		for (int n = 0; n < collectionSize; n++) {
			RemoteControlledFileStream instance = new RemoteControlledFileStream(new FileStreamSegment(fileStreamSize), onServer);
			instance.fromByteStream(buffer, updateSenderStateId);
			getReceiveBuffer().add(instance);
		}

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		//add size of collection

		int batchSize = Math.min(CONTROL_BATCH, get().size());

		buffer.writeByte(batchSize);

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
