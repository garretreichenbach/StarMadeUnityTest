package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.network.objects.BitsetResponse;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

import com.googlecode.javaewah.EWAHCompressedBitmap;

public class RemoteBitset extends RemoteField<BitsetResponse> {

	public RemoteBitset(BitsetResponse entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteBitset(BitsetResponse entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_INT;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {

		get().segmentBufferIndex = stream.readLong();

		//		System.err.println("REC SegmentBufferIndex: "+get().segmentBufferIndex);

		get().pos = new Vector3i();
		get().pos.set(stream.readInt(), stream.readInt(), stream.readInt());

		get().data = stream.readBoolean();

		if (get().data) {
			get().bitmap = new EWAHCompressedBitmap();
			get().bitmap.deserialize(stream);
		}

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		//		System.err.println("SEND SegmentBufferIndex: "+get().segmentBufferIndex);
		buffer.writeLong(get().segmentBufferIndex);
		buffer.writeInt(get().pos.x);
		buffer.writeInt(get().pos.y);
		buffer.writeInt(get().pos.z);
		if (get().data) {
			buffer.writeBoolean(true);
			get().bitmap.serialize(buffer);
		} else {
			buffer.writeBoolean(false);
		}

		return byteLength();
	}

}
