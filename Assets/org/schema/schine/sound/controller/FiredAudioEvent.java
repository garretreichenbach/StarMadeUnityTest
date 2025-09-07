package org.schema.schine.sound.controller;

import java.util.Date;

import org.schema.schine.sound.controller.config.AudioEntry;

public class FiredAudioEvent {
	
	public static final FiredAudioEventHeader[] COLUMNS = new FiredAudioEventHeader[] {
			new FiredAudioEventHeader("ID", Integer.class, 10, r -> r.entry.id),
			new FiredAudioEventHeader("HasAudio", String.class, 10, r -> r.entry.assignmnetID.type.getShortName(r.entry.assignmnetID.getAssignment())),
			new FiredAudioEventHeader("Tags", String.class, 200, r -> r.entry.tags.toString()),
			
			new FiredAudioEventHeader("Call", String.class, 300, r -> r.stackTraceElements[4].getLineNumber()+": "+r.stackTraceElements[4].getClassName().substring(r.stackTraceElements[4].getClassName().lastIndexOf(".")+1)),
			
			new FiredAudioEventHeader("Assignment", String.class, 200, r -> AudioController.describeAudioAssignment(r.entry)),
			
		};
	
	public AudioEntry entry;
	public StackTraceElement[] stackTraceElements;
	public AudioArgument argument;
	public int networkId;

	public Date time;
	public FiredAudioEvent(AudioEntry entry) {
		this.entry = entry;
		stackTraceElements = Thread.currentThread().getStackTrace();
		time = new Date();
		
	}
	public interface ValueRetriever{
		public Object getValue(FiredAudioEvent r);
	}
	public static class FiredAudioEventHeader{
		public final String name;
		public final Class<?> clazz;
		public final ValueRetriever r;
		public final int preferredWidth;
		public FiredAudioEventHeader(String name, Class<?> clazz, int preferredWidth, ValueRetriever r) {
			super();
			this.name = name;
			this.clazz = clazz;
			this.r = r;
			this.preferredWidth = preferredWidth;
		}
	}
	

	public Object getValue(int columnIndex) {
		return COLUMNS[columnIndex].r.getValue(this);
	}
}
