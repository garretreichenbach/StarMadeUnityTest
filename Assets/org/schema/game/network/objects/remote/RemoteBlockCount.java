package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map.Entry;

import org.schema.common.util.ByteUtil;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

import it.unimi.dsi.fastutil.shorts.Short2IntOpenHashMap;

public class RemoteBlockCount extends RemoteField<Short2IntOpenHashMap> {

	public RemoteBlockCount(Short2IntOpenHashMap entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteBlockCount(Short2IntOpenHashMap entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_INT;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {

		int size = stream.readInt();

		for (int i = 0; i < size; i++) {
			short type = stream.readShort();
			int count = stream.readInt();
			get().put(type, count);
		}

	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {

		buffer.writeInt(get().size());
		for (Entry<Short, Integer> c : get().entrySet()) {
			buffer.writeShort(c.getKey());
			buffer.writeInt(c.getValue());
		}

		return byteLength();
	}

}
