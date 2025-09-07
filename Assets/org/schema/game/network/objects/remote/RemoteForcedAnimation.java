package org.schema.game.network.objects.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.data.player.ForcedAnimation;
import org.schema.schine.graphicsengine.animation.LoopMode;
import org.schema.schine.graphicsengine.animation.structure.classes.AnimationIndex;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.objects.remote.RemoteField;

public class RemoteForcedAnimation extends RemoteField<ForcedAnimation> {

	public RemoteForcedAnimation(ForcedAnimation entry, boolean synchOn) {
		super(entry, synchOn);
	}

	public RemoteForcedAnimation(ForcedAnimation entry, NetworkObject synchOn) {
		super(entry, synchOn);
	}

	@Override
	public int byteLength() {
		return ByteUtil.SIZEOF_INT;
	}

	@Override
	public void fromByteStream(DataInputStream b, int updateSenderStateId) throws IOException {
		long time = b.readLong();
		get().time = time;
		if (time <= 0) {
			return;
		}
		String animation = b.readUTF();
		String loopModeString = b.readUTF();
		float speed = b.readFloat();
		boolean fullBody = b.readBoolean();

		for (int i = 0; i < AnimationIndex.animations.length; i++) {
			if (AnimationIndex.animations[i].toString().toLowerCase(Locale.ENGLISH).equals(animation.toUpperCase(Locale.ENGLISH))) {
				LoopMode loopMode = LoopMode.valueOf(loopModeString.toUpperCase(Locale.ENGLISH));

				get().animation = AnimationIndex.animations[i];
				get().fullBody = fullBody;
				get().speed = speed;
				get().loopMode = loopMode;
				get().received = true;
				return;
			}
		}
		assert (false) : "'" + animation + "'; " + Arrays.toString(AnimationIndex.animations);
	}

	@Override
	public int toByteStream(DataOutputStream buffer) throws IOException {

		buffer.writeLong(get().time);

		System.err.println("[SERVER] sending remote forced animation " + get().time + ": " + get());
		if (get().time <= 0) {
			return 1;
		}
		buffer.writeUTF(get().animation.toString());
		buffer.writeUTF(get().loopMode.name());
		buffer.writeFloat(get().speed);
		buffer.writeBoolean(get().fullBody);
		return byteLength();
	}

}
