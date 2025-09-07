package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.data.element.SendableControlMod;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteControlMod extends RemoteField<SendableControlMod> {
	public RemoteControlMod(SendableControlMod entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteControlMod(SendableControlMod entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_INT;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {

		get().add = stream.readBoolean();
		get().controlledType = stream.readShort();
		get().from = stream.readLong();
		get().to = stream.readLong();
		if (get().controlledType <= 0) {
			System.err.println("Exception: received invalid controller from ID: " + updateSenderStateId + "; controller " + get().controlledType);
		}
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		buffer.writeBoolean(get().add);

		if (get().controlledType <= 0) {
			System.err.println("Exception: sending invalid controller: " + get().controlledType);
		}
		buffer.writeShort(get().controlledType);
		buffer.writeLong(get().from);
		buffer.writeLong(get().to);
		return 1;
	}

}
