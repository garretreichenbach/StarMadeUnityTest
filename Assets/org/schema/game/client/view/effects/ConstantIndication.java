package org.schema.game.client.view.effects;

import com.bulletphysics.linearmath.Transform;

public class ConstantIndication extends Indication {
	public ConstantIndication(Transform start, Object text) {
		super(start, text);
	}

	@Override
	public Transform getCurrentTransform() {
		return start;
	}

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.effects.Indication#isAlive()
	 */
	@Override
	public boolean isAlive() {
		return true;
	}

}
