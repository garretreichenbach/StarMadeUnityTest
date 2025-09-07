package org.schema.schine.graphicsengine.util.timer;

import org.schema.schine.graphicsengine.core.Timer;

public abstract class TimerUtil {
	protected float timeMult = 1;
	protected float time = 1;
	protected float speed = 6;

	public TimerUtil() {
		this(6);
	}

	public TimerUtil(float speed) {
		this.speed = speed;
	}

	public float getTime() {
		return time;
	}

	public void reset() {
		time = 1;
		timeMult = 1;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public abstract void update(Timer timer);
}
