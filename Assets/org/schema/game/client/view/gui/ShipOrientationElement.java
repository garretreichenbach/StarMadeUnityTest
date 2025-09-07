package org.schema.game.client.view.gui;

import org.schema.game.client.data.GameClientState;

import com.bulletphysics.linearmath.Transform;

public class ShipOrientationElement extends OrientationElement {

	public ShipOrientationElement(GameClientState state) {
		super(state);
	}

	@Override
	public Transform getWorldTransform() {
		return state.getShip().getWorldTransform();
	}

	@Override
	public boolean canDraw() {
		boolean act = state.getShip() != null && !state.getGlobalGameControlManager().getIngameControlManager()
				.getPlayerGameControlManager().getMapControlManager().isActive();
		return act;
	}
}
