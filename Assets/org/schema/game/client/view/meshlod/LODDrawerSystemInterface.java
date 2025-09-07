package org.schema.game.client.view.meshlod;

import org.schema.schine.graphicsengine.core.Timer;

public interface LODDrawerSystemInterface<E extends LODCapable> {
	public void drawLevel(int lodIndex, LODMesh<E>  mesh, LODDeferredSpriteCollection<E> defferedSprites);

	public void update(int lodIndex, Timer timer, LODMesh<E>  mesh);
}
