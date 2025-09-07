package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.common.util.FileStreamSegment;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteControlledFileStream extends RemoteField<FileStreamSegment> {

	public RemoteControlledFileStream(boolean synchOn, int bufferSize) {
		super(new FileStreamSegment(bufferSize), synchOn);
	}

	public RemoteControlledFileStream(FileStreamSegment stream, boolean synchOn) {
		super(stream, synchOn);
	}

	public RemoteControlledFileStream(FileStreamSegment stream, NetworkObject synchOn) {
		super(stream, synchOn);
	}

	public RemoteControlledFileStream(NetworkObject synchOn, int bufferSize) {
		super(new FileStreamSegment(bufferSize), synchOn);
	}

	@Override
	public int byteLength() {
		return get().length;
	}

	@Override
	public void fromByteStream(DataInputStream inputStream, int updateSenderStateId) throws IOException {
		get().last = inputStream.readBoolean();
		get().length = inputStream.readShort();
		inputStream.readFully(get().buffer, 0, get().length);
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		buffer.writeBoolean(get().last);
		buffer.writeShort(get().length);
		buffer.write(get().buffer, 0, get().length);
		return byteLength();
	}
}
