package org.schema.schine.sound.controller.mixer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.settings.Settings;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;

import java.util.List;

public class AudioMixer {

	private final String name;
	private final Settings volumeSetting;
	private AudioMixer parent;
	private final List<AudioMixer> children = new ObjectArrayList<>();
	private float volumeHirachy;

	public static final List<AudioMixer> mixers = new ObjectArrayList<>();
	public static final List<AudioMixer> mixersExposed = new ObjectArrayList<>();

	public static final AudioMixer MASTER = new AudioMixer("Master", EngineSettings.AUDIO_MIXER_MASTER, null);
	public static final AudioMixer MUSIC = new AudioMixer("Music", EngineSettings.AUDIO_MIXER_MUSIC, MASTER);
	public static final AudioMixer SFX = new AudioMixer("SFX", EngineSettings.AUDIO_MIXER_SFX, MASTER);
	public static final AudioMixer GUI = new AudioMixer("GUI", EngineSettings.AUDIO_MIXER_SFX_GUI, MASTER);
	public static final AudioMixer GAME = new AudioMixer("Ingame", EngineSettings.AUDIO_MIXER_SFX_INGAME, MASTER);

	static {
		mixers.add(MASTER);
		mixers.add(MUSIC);
		mixers.add(SFX);
		mixers.add(GUI);
		mixers.add(GAME);

		//exposed only contains the bottom level
		mixersExposed.add(MUSIC);
		mixersExposed.add(GUI);
		mixersExposed.add(GAME);

		MASTER.recalcAllVolumeHirachy();
	}

	public Settings getVolumeSetting() {
		return volumeSetting;
	}

	private AudioMixer(String name, Settings volumeSetting, AudioMixer parent) {
		this.name = name;
		this.volumeSetting = volumeSetting;

		if(parent != null) {
			parent.children.add(this);
			this.parent = parent;
		} else {
			volumeHirachy = volumeSetting.getFloat();
		}
		mixers.add(this);
	}

	public void setVolume(float volume) {
		this.volumeSetting.setFloat(volume);
		//readjust all volumes
		MASTER.recalcAllVolumeHirachy();
	}

	public String getName() {
		return name;
	}

	public AudioMixer getParent() {
		return parent;
	}

	public List<AudioMixer> getChildren() {
		return children;
	}

	public float getVolumeRaw() {
		return volumeSetting.getFloat();
	}

	public boolean isRoot() {
		return parent == null;
	}

	public float getVolume() {
		return volumeHirachy;
	}

	private float calcVolumeHirachy() {
		return isRoot() ? volumeSetting.getFloat() : (volumeSetting.getFloat() * parent.volumeHirachy);
	}

	private void recalcAllVolumeHirachy() {
		volumeHirachy = calcVolumeHirachy();
		for(AudioMixer a : children) {
			a.recalcAllVolumeHirachy();
		}
	}

	@Override
	public String toString() {
		return name;
	}

}
