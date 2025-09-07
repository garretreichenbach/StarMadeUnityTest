package org.schema.schine.graphicsengine.movie;

public class VideoMetadata {
	public final int width, height;
	public final float framerate;
	public final float totalTimeSecs;

	public VideoMetadata(int width, int height, float framerate, float total) {
		this.width = width;
		this.height = height;
		this.framerate = framerate;
		this.totalTimeSecs = total;
	}

	@Override
	public String toString() {
		return "VideoMetadata[" + width + "x" + height + " @ " + framerate + "fps]";
	}
}
