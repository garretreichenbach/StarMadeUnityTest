package org.schema.schine.graphicsengine.forms.debug;

import javax.vecmath.Vector4f;

public class DebugGeometry {
	public long LIFETIME = 1000;
	public float size = 10;
	protected Vector4f color;
	private long creation;

	public DebugGeometry() {
		this.creation = System.currentTimeMillis();
	}

	public DebugGeometry(float size) {
		this.creation = System.currentTimeMillis();
		this.size = size;
	}

	public float getAlpha() {
		return (float) Math.max(0, 1 - (getTimeLived() / (double)LIFETIME));
	}

	public long getLifeTime() {
		return LIFETIME;
	}

	public float getTimeLived() {
		return System.currentTimeMillis() - creation;
	}

	public boolean isAlive() {
		return getTimeLived() < LIFETIME;
	}
	
	public void setColor(Vector4f color){
		this.color = color;
	}
}
