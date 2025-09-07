package org.schema.game.common.data.world;

import org.schema.game.common.data.player.AbstractOwnerState;
import org.schema.schine.graphicsengine.forms.Light;

public interface LightTransformable extends SimpleTransformable{
	public Light getLight();
	public void setLight(Light light);
	public AbstractOwnerState getOwnerState();
	
}
