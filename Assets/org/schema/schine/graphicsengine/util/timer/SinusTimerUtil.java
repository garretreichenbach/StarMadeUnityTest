package org.schema.schine.graphicsengine.util.timer;

import org.schema.common.FastMath;
import org.schema.schine.graphicsengine.core.Timer;

public class SinusTimerUtil extends TimerUtil {
	double t = 0;

	public SinusTimerUtil() {
		super();
	}

	public SinusTimerUtil(float speed) {
		super(speed);
	}

	@Override
	public void update(Timer timer) {
		t += (double)timer.getDelta() * (double)speed;
		
		time = 0.5f + FastMath.sinFast((float)t) * 0.5f;
	}

}
