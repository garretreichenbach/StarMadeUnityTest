package org.schema.schine.sound.controller;

public interface AudioTag {
	public AudioTag getParent();
	public String getTagName();
	public short getTagId();
	public static AudioTag getFromText(String s) {
		return AudioTags.valueOf(s);
	}
	
}
