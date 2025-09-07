package org.schema.schine.sound.controller;


public interface AudioSender {
	public void sendAudioEvent(int id, int targetId, AudioArgument arg);
}
