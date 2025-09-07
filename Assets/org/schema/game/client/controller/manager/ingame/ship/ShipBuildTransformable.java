package org.schema.game.client.controller.manager.ingame.ship;

import org.schema.schine.graphicsengine.forms.Transformable;
import org.schema.schine.network.objects.container.TransformTimed;

import com.bulletphysics.linearmath.Transform;

public class ShipBuildTransformable implements Transformable {

	TransformTimed transform = new TransformTimed();

	public ShipBuildTransformable(Transform initial) {
		transform.set(initial);
	}

	@Override
	public TransformTimed getWorldTransform() {
		return transform;
	}

}
