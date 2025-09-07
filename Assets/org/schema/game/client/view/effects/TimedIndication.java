package org.schema.game.client.view.effects;

import com.bulletphysics.linearmath.Transform;

public class TimedIndication extends Indication {
	public TimedIndication(Transform start, Object text, float lifetime, float dist) {
		super(start, text);
		this.lifetime = lifetime;
		this.setDist(dist);
	}

	@Override
	public Transform getCurrentTransform() {
		return start;
	}

}
