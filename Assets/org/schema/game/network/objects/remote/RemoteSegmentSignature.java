package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.SegmentSignature;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteSegmentSignature extends RemoteField<SegmentSignature> {

	static int len = ByteUtil.SIZEOF_LONG + 4 * ByteUtil.SIZEOF_INT;

	public RemoteSegmentSignature(boolean synchOn) {
		super(new SegmentSignature(), synchOn);
	}

	public RemoteSegmentSignature(NetworkObject synchOn) {
		super(new SegmentSignature(), synchOn);
	}

	public RemoteSegmentSignature(SegmentSignature segment, boolean synchOn) {
		super(segment, synchOn);
	}

	public RemoteSegmentSignature(SegmentSignature segment, NetworkObject synchOn) {
		super(segment, synchOn);
	}

	@Override
	public int byteLength() {
		return len;
	}

	@Override
	public void fromByteStream(DataInputStream inputStream, int updateSenderStateId) throws IOException {
		long lastChanged = inputStream.readLong();
		boolean empty = false;
		if (lastChanged < 0) {
			lastChanged = Math.abs(lastChanged - 1);
			empty = true;
		}
		int x = inputStream.readShort();
		int y = inputStream.readShort();
		int z = inputStream.readShort();

		short size = -1;
		if (!empty) {
			size = inputStream.readShort();
		}

		assert (lastChanged >= 0);
		set(new SegmentSignature(new Vector3i(x, y, z), lastChanged, empty, size));
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		//encode empty bit with long (the sign byte is never used in lastChangedAnyway
		assert (get().getLastChanged() >= 0);
		buffer.writeLong(get().empty ? -(get().getLastChanged() + 1) : get().getLastChanged());
		buffer.writeShort(get().getPos().x);
		buffer.writeShort(get().getPos().y);
		buffer.writeShort(get().getPos().z);
		if (!get().empty) {
			buffer.writeShort(get().getSize());
		}
		return 1;
	}
}
