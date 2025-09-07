package org.schema.game.common.controller.ntsmothers;

import java.util.ArrayList;

public class TimedTransformationPool {
	private static final int MAX_POOL_SIZE = 13000;
	private ArrayList<TimedTransformation> pool = new ArrayList<TimedTransformation>();

	public TimedTransformation get() {
		synchronized (pool) {
			if (pool.isEmpty()) {
				return new TimedTransformation();
			} else {
				return pool.remove(pool.size() - 1);
			}
		}
	}

	public void release(TimedTransformation r) {
		synchronized (pool) {

			if (pool.size() + 1 > MAX_POOL_SIZE) {
				r.transform = null;
				r.timestamp = -1;
				r = null;
			} else {
				pool.add(r);
			}
		}
	}
}
