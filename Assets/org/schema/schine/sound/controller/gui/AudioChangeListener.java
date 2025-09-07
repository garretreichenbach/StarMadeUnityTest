package org.schema.schine.sound.controller.gui;

import org.schema.schine.sound.manager.engine.AudioNode;

public interface AudioChangeListener {

	public void onAudioStop(AudioNode n);
	public void onAudioPlay(AudioNode n);
	public void onAudioPause(AudioNode n);

}
