package org.schema.game.common.controller.ntsmothers;

import com.bulletphysics.linearmath.Transform;

public class TimedTransformation implements Comparable<Long> {
	public Transform transform = new Transform();
	public long timestamp;

	@Override
	public int compareTo(Long o) {
		return (int) (this.timestamp - o);
	}

	public TimedTransformation set(Transform transform, long timestamp) {
		this.transform.set(transform);
		this.timestamp = timestamp;
		return this;
	}

}
