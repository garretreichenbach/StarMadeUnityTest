package org.schema.game.client.view.effects.segmentcontrollereffects;

import javax.vecmath.Vector3f;

import org.schema.game.client.view.effects.OcclusionLensflare;

public class JumpFlare extends OcclusionLensflare {

	Vector3f pos = new Vector3f();

	/* (non-Javadoc)
	 * @see org.schema.game.client.view.effects.OcclusionLensflare#getLightPos()
	 */
	@Override
	public Vector3f getLightPos() {
		return pos;
	}

}
