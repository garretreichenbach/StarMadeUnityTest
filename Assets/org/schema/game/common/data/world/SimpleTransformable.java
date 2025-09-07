package org.schema.game.common.data.world;

import org.schema.schine.graphicsengine.forms.Transformable;
import org.schema.schine.network.objects.container.TransformTimed;

public interface SimpleTransformable extends Transformable{
	public boolean isOnServer();

	public TransformTimed getWorldTransformOnClient();
	
}
