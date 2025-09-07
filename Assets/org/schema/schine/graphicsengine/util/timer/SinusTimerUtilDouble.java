package org.schema.schine.graphicsengine.util.timer;

import org.schema.schine.graphicsengine.core.Timer;

public class SinusTimerUtilDouble extends TimerUtil {
	double t = 0;

	public SinusTimerUtilDouble() {
		super();
	}

	public SinusTimerUtilDouble(float speed) {
		super(speed);
	}

	@Override
	public void update(Timer timer) {
		t += (double)timer.getDelta() * (double)speed;
		
		time = (float)(0.5 + Math.sin(t) * 0.5);
	}

}
