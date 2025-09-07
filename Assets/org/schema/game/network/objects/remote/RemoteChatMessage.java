package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.game.network.objects.ChatMessage;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteChatMessage extends RemoteField<ChatMessage> {

	public RemoteChatMessage(ChatMessage e, boolean onServer) {
		super(e, onServer);
	}

	public RemoteChatMessage(ChatMessage e, NetworkObject synchOn) {
		super(e, synchOn);
	}

	@Override
	public int byteLength() {
		return 0;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId)
			throws IOException {
		get().deserialize(stream, updateSenderStateId, onServer);
	}

	@Override
	public int toByteStream(DataOutputStream stream) throws IOException {
		get().serialize(stream, onServer);
		return 1;
	}

}
