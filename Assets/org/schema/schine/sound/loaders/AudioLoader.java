package org.schema.schine.sound.loaders;

import java.io.IOException;

import org.schema.schine.sound.controller.asset.AudioAsset;
import org.schema.schine.sound.manager.engine.AudioData;

public interface AudioLoader {

	public AudioAsset.AudioFileType getFileType();
	public AudioData load(AudioLoadEntry info) throws IOException;
}
