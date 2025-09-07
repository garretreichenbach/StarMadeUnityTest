package org.schema.schine.graphicsengine.util.timer;

import org.schema.schine.graphicsengine.core.Timer;

public class LinearTimerUtil extends TimerUtil {
	public LinearTimerUtil() {
		super();
	}

	public LinearTimerUtil(float speed) {
		super(speed);
	}

	@Override
	public void update(Timer timer) {
		if (timeMult > 0) {
			if (getTime() < 1) {
				time = Math.min(time + timer.getDelta() * speed, 1);

			} else {
				timeMult *= -1;
			}
		} else {
			if (getTime() > 0) {
				time = Math.max(time - timer.getDelta() * speed, 0);
			} else {
				timeMult *= -1;
			}
		}
	}
}
