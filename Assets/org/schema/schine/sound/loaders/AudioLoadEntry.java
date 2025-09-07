package org.schema.schine.sound.loaders;

import org.schema.common.util.AssetInfo;
import org.schema.schine.sound.manager.engine.AudioId;

public abstract class AudioLoadEntry extends AssetInfo<AudioId> {

	public final short transientID;
	private static short idGen = 1;

	public AudioLoadEntry(String name) {
		super(name);
		this.transientID = idGen++;
	}

	@Override
	public LoadEntryType getType() {
		return LoadEntryType.AUDIO;
	}

}
