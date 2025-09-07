package org.schema.schine.sound.controller.assignment;

import org.schema.schine.sound.controller.AudioArgument;
import org.schema.schine.sound.controller.AudioController;
import org.schema.schine.sound.controller.AudioParam;
import org.schema.schine.sound.controller.asset.AudioAsset;

public abstract class AudioAssignmentTarget {
	public AudioAssignment assignment;
	public AudioAsset asset;
	
	
	
	
	public void execute(int eventId, String name, AudioController controller, AudioParam param, AudioArgument arg) {
		switch(param) {
			case ONE_TIME -> controller.playOnce(eventId, name, asset, assignment.getSettings(), arg);
			case START -> controller.playStart(eventId, name, asset, assignment.getSettings(), arg);
			case STOP -> controller.playStop(eventId, name, asset, assignment.getSettings(), arg);
			case UPDATE -> controller.playUpdate(eventId, name, asset, assignment.getSettings(), arg);
			case VOLUME -> controller.playVolume(eventId, name, asset, assignment.getSettings(), arg);
			default -> throw new RuntimeException("Unknown param " + param.name());
		}
	}
	
}
