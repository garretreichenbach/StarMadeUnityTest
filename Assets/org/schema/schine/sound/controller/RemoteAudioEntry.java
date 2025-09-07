package org.schema.schine.sound.controller;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.schema.common.SerializationInterface;
import org.schema.schine.sound.controller.config.AudioEntry;

public class RemoteAudioEntry implements SerializationInterface{
	public int audioId;
	public int targetId;
	public AudioArgument audioArgument;
	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeInt(audioId);
		b.writeInt(targetId);
		if(audioArgument != null) {
			b.writeBoolean(true);
			audioArgument.serialize(b, true);
		}else {
			b.writeBoolean(false);
		}			
	}
	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		this.audioId = b.readInt();
		this.targetId = b.readInt();
		
		AudioEntry audioEntry = AudioController.instance.getConfig().entries.get(audioId);
		boolean argPresent = b.readBoolean();
		assert(audioEntry.argumentPresent == argPresent);
		
		if(argPresent) {
			this.audioArgument = AudioArgument.deserializeStatic(b, updateSenderStateId, false);
		}
		
		AudioController.instance.onRemoteEntryFired(this);			
	}
}
