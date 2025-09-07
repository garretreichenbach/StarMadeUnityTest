package org.schema.schine.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.sound.controller.RemoteAudioEntry;


public class RemoteAudioEvent extends RemoteField<RemoteAudioEntry> {

	
	
	public RemoteAudioEvent(RemoteAudioEntry modification, boolean synchOn) {
		super(modification, synchOn);
	}

	public RemoteAudioEvent(RemoteAudioEntry modification, NetworkObject synchOn) {
		super(modification, synchOn);
	}

	@Override
	public int byteLength() {
		return 1;
	}

	@Override
	public void fromByteStream(DataInputStream b, int updateSenderStateId) throws IOException {
		get().deserialize(b, updateSenderStateId, onServer);
	}

	@Override
	public int toByteStream(DataOutputStream b) throws IOException {
		get().serialize(b, onServer);
		return 1;
	}
}
