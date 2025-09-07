package org.schema.game.client.view.gui;

import org.schema.game.client.data.GameClientState;

import com.bulletphysics.linearmath.Transform;

public class GalaxyOrientationElement extends OrientationElement {

	private final Transform transform;

	public GalaxyOrientationElement(GameClientState state) {
		super(state);
		transform = new Transform();
		transform.setIdentity();
	}

	@Override
	public Transform getWorldTransform() {
		return transform;
	}

	@Override
	public boolean canDraw() {
		return true;
	}
}
