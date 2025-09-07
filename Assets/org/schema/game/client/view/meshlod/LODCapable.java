package org.schema.game.client.view.meshlod;

import javax.vecmath.Vector3f;

import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.PositionableSubColorSprite;
import org.schema.schine.graphicsengine.forms.Transformable;

public interface LODCapable extends PositionableSubColorSprite, Transformable, Comparable<LODCapable>{

	public void update(Timer timer, Vector3f camPos);
	public int getCurrentLODLevel();
	/**
	 * 
	 * @param camPos
	 * @param meshSystem 
	 * @return old level
	 */
	public<E extends LODCapable> int updateCurrentLevel(Vector3f camPos, LODMeshSystem<E> meshSystem);
	public boolean isAlive();
	public float getDistance();
	public float getBlending();
	public float getMaxAlpha();
	public void kill();
}
