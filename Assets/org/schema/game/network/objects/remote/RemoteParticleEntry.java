package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.vecmath.Vector3f;

import org.schema.game.common.controller.ParticleEntry;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteParticleEntry extends RemoteField<ParticleEntry> {

	public RemoteParticleEntry(ParticleEntry modification, boolean synchOn) {
		super(modification, synchOn);
	}

	public RemoteParticleEntry(ParticleEntry modification, NetworkObject synchOn) {
		super(modification, synchOn);
	}

	@Override
	public int byteLength() {
		return 1;
	}

	@Override
	public void fromByteStream(DataInputStream stream, int updateSenderStateId) throws IOException {
		get().setParticleName(stream.readUTF());
		get().setOrigin(new Vector3f(stream.readFloat(), stream.readFloat(), stream.readFloat()));
	}

	@Override
	public int toByteStream(DataOutputStream stream) throws IOException {
		stream.writeUTF(get().getParticleName());
		stream.writeFloat(get().getOrigin().x);
		stream.writeFloat(get().getOrigin().y);
		stream.writeFloat(get().getOrigin().z);
		return byteLength();
	}
}
