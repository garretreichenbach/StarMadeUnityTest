package org.schema.game.client.view.mines;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.util.CompareTools;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.game.client.view.meshlod.LODCapable;
import org.schema.game.client.view.meshlod.LODMeshSystem;
import org.schema.game.common.data.mines.Mine;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.network.objects.container.TransformTimed;

public class MineDrawableData implements LODCapable{

	public static final float MAX_ALPHA = 0.999f;
	public final Vector4f color = new Vector4f(1,1,1,MAX_ALPHA);
	public float distance;
	public Mine mine;
	private int lodLevel = -1;
	private float blend;
	private boolean alive = true;
	
	
	public MineDrawableData(Mine mine) {
		this.mine = mine;
	}
	
	@Override
	public float getScale(long time) {
		return 0.05f;
	}

	@Override
	public int getSubSprite(Sprite sprite) {
		return mine.getType().subSpriteIndex;
	}

	@Override
	public boolean canDraw() {
		return mine.isVisibleForClientAt(distance);
	}

	@Override
	public Vector3f getPos() {
		return mine.getWorldTransformOnClient().origin;
	}

	@Override
	public Vector4f getColor() {
		return color;
	}

	public void update(Timer timer, Vector3f camPos) {
		updateDistance(camPos);
	}
	public void updateDistance(Vector3f camPos) {
		Vector3f pos = getWorldTransform().origin;
		distance = Vector3fTools.distance(pos.x, pos.y, pos.z, camPos.x, camPos.y, camPos.z);		
	}
	@Override
	public TransformTimed getWorldTransform() {
		return mine.getWorldTransformOnClient();
	}

	@Override
	public int getCurrentLODLevel() {
		return lodLevel;
	}

	@Override
	public<E extends LODCapable> int updateCurrentLevel(Vector3f camPos, LODMeshSystem<E> meshSystem) {
		int oldLevel = lodLevel;
		lodLevel = meshSystem.getLevelFromDistance(distance);
		blend = meshSystem.getBlendingValue(lodLevel, distance);
		return oldLevel;
	}

	@Override
	public boolean isAlive() {
		return alive;
	}

	@Override
	public int hashCode() {
		return mine.getId();
	}

	@Override
	public boolean equals(Object obj) {
		return mine.equals(((MineDrawableData)obj).mine);
	}

	@Override
	public String toString() {
		return "MineDrawable["+mine+"]";
	}

	@Override
	public float getDistance() {
		return distance;
	}

	@Override
	public int compareTo(LODCapable o) {
		//farthest first
		return CompareTools.compare(o.getDistance(), distance);
	}


	@Override
	public float getBlending() {
		return blend;
	}

	@Override
	public float getMaxAlpha() {
		return MAX_ALPHA;
	}

	@Override
	public void kill() {
		alive = false;
	}


	

}
