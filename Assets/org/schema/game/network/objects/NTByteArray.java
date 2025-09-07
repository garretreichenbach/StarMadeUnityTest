package org.schema.game.network.objects;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.DeserializationException;
import org.schema.game.common.data.world.RemoteSegment;
import org.schema.schine.network.objects.remote.RemoteSerializable;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;

public class NTByteArray implements RemoteSerializable {

	public long pos;
	private byte[] data;
	private short length;

	public void set(byte[] data, int length, long pos) {
		assert (length < Short.MAX_VALUE);

		this.data = Arrays.copyOf(data, length);
		//		for(int i = 0; i < length; i++){
		//			this.data[i] = data[i];
		//		}
		this.length = (short) length;
		this.pos = pos;
	}

	@Override
	public void serialize(DataOutputStream stream) throws IOException {
		stream.writeLong(pos);
		stream.writeShort(length);
		stream.write(data, 0, length);
	}

	@Override
	public void deserialize(DataInputStream stream) throws IOException {
		pos = stream.readLong();
		length = stream.readShort();
		data = new byte[length];
		stream.read(data, 0, length);
	}

	public RemoteSegment getSegment(SegmentController c, RemoteSegment s) throws IOException, DeserializationException {
		FastByteArrayInputStream in = new FastByteArrayInputStream(data);
		boolean reset = false;
		s.deserialize(new DataInputStream(in), length, true, reset, c.getState().getUpdateTime());
		return s;
	}

}
