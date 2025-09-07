package org.schema.schine.sound.loaders;

import java.io.IOException;

import org.schema.schine.sound.controller.asset.AudioAsset;
import org.schema.schine.sound.manager.engine.AudioData;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class AudioLoaderManager {
	private final Object2ObjectOpenHashMap<AudioAsset.AudioFileType, AudioLoader> loaders = new Object2ObjectOpenHashMap<AudioAsset.AudioFileType, AudioLoader>();
	
	
	public AudioLoaderManager() {
		
		
		register(new OGGLoader());
		register(new WAVLoader());
	}
	
	
	public void register(AudioLoader al) {
		loaders.put(al.getFileType(), al);
	}


	public AudioData load(AudioAsset audioAsset) throws IOException {
		AudioLoader audioLoader = loaders.get(audioAsset.getFileType());
		if(audioLoader == null) {
			throw new IOException("No loader found for asset: "+audioAsset.getFile().getAbsolutePath());
		}
		return audioLoader.load(audioAsset);
	}
	
}
