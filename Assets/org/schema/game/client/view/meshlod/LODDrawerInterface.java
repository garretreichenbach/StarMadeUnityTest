package org.schema.game.client.view.meshlod;

import javax.vecmath.Vector3f;

import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.forms.Sprite;

public interface LODDrawerInterface<E> {
	public void drawInstances(Mesh m, boolean blendA);

	public void update(Timer timer, Vector3f camPos);
	public void drawSprites(Sprite sprite, int subSpriteIndex);
}
