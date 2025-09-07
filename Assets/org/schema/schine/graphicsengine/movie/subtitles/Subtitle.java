package org.schema.schine.graphicsengine.movie.subtitles;

import javax.vecmath.Vector4f;

public class Subtitle {
	public final String text;
	public final Vector4f color;
	public final long startTime;
	public final long endTime;
	public Subtitle(String text, Vector4f color, long startTime, long endTime) {
		super();
		this.text = text;
		this.color = color;
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (startTime ^ (startTime >>> 32));
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Subtitle)) {
			return false;
		}
		Subtitle other = (Subtitle) obj;
		if (startTime != other.startTime) {
			return false;
		}
		return true;
	}
	
	
	
}
