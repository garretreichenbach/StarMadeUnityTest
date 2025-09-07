package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.data.player.dialog.conversation.ConverationUpdate;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteConversation extends RemoteField<ConverationUpdate> {

	public RemoteConversation(ConverationUpdate entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteConversation(ConverationUpdate entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_INT;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {
		//nothing to be done. derialization done in buffer
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {
		get().serialize(buffer);
		return byteLength();
	}

}
