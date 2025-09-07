package org.schema.game.client.view.meshlod;

import java.util.List;

import org.schema.schine.graphicsengine.core.Timer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;


public class LODMeshSystem<E extends LODCapable> {
	
	private List<LODMesh<E> > levels = new ObjectArrayList<LODMesh<E> >();
	
	
	public void addLevelSingleMesh(float maxDistance, String mesh){
		LODSingleMesh<E> m = new LODSingleMesh<E>(levels.size(), maxDistance, mesh);
		assert(m.meshA != null);
		levels.add(m);
	}
	public void addLevelDoubleMesh(float maxDistance, String meshA, String meshB){
		LODDoubleMesh<E> m = new LODDoubleMesh<E>(levels.size(), maxDistance, meshA, meshB);
		assert(m.meshA != null);
		assert(m.meshB != null);
		levels.add(m);
	}
	public void addLevelSpriteMesh(float maxDistance, String meshA, String sprite, int subSpriteIndex){
		LODMeshSprite<E> m = new LODMeshSprite<E>(levels.size(), maxDistance, meshA, sprite, subSpriteIndex);
		assert(m.meshA != null);
		assert(m.spriteName != null);
		levels.add(m);
	}
	public void addLevelSprite(float maxDistance, String sprite, int subSpriteIndex){
		LODSprite<E> m = new LODSprite<E>(levels.size(), maxDistance, sprite, subSpriteIndex);
		assert(m.spriteName != null);
		levels.add(m);
	}
	
	public void update(Timer timer, LODDrawerSystemInterface<E>  ff) {
		for(int i = 0; i < levels.size(); i++) {
			ff.update(i, timer, levels.get(i));
		}
	}
	public void draw(LODDrawerSystemInterface<E> ff, LODDeferredSpriteCollection<E> defferedSprites) {
		for(int i = 0; i < levels.size(); i++) {
			ff.drawLevel(i, levels.get(i), defferedSprites);
		}
	}
	public int getLevelCount() {
		return levels.size();
	}
	public static class LODStage{
		public final float distance;
		public final float marginIn;
		public final boolean spriteMode;
		public final String name;
		public final int subSpriteIndex;
		public LODStage(float distance, float marginIn, boolean spriteMode, String name, int subSpriteIndex) {
			super();
			this.distance = distance;
			this.marginIn = marginIn;
			this.spriteMode = spriteMode;
			this.name = name;
			this.subSpriteIndex = subSpriteIndex;
		}
	}
	public void init(LODStage ... stages ) {
		float distance = 0;
		for(int i = 0; i < stages.length; i++) {
			
			if(i > 0) {
				//insert transition stage
				LODStage stageBef = stages[i-1];
				LODStage stage = stages[i];
				distance += stage.marginIn;
				
				assert(stageBef.name != null):stageBef+"; "+i;
				if(stage.spriteMode) {
					addLevelSpriteMesh(distance, stageBef.name, stage.name, stage.subSpriteIndex);
				}else {
					addLevelDoubleMesh(distance, stageBef.name, stage.name);
				}
			}
			
			//insert stage
			LODStage stage = stages[i];
			distance += (i == stages.length-1 ? Float.POSITIVE_INFINITY : (stage.distance));
			if(stage.spriteMode) {
				addLevelSprite(distance, stage.name, stage.subSpriteIndex);
			}else {
				addLevelSingleMesh(distance, stage.name);
			}
		}
		((ObjectArrayList<LODMesh<E> >)levels).trim();
		
	}
	public int getLevelFromDistance(float distance) {
		for(int i = 0; i < levels.size(); i++) {
			if(distance <= levels.get(i).maxDistance) {
				return i;
			}
		}
		return levels.size()-1;
	}
	public void cleanUp() {
		
	}
	public float getBlendingValue(int lodLevel, float distance) {
		if(!levels.get(lodLevel).isBlending()) {
			return 0;
		}
		float minDist = 0;
		if(lodLevel > 0) {
			minDist = levels.get(lodLevel-1).maxDistance;
		}
		float maxDistance = levels.get(lodLevel).maxDistance;
		
//		System.err.println("DUS;: "+distance+"; "+minDist+" <-> "+maxDistance);
		if(maxDistance == Float.POSITIVE_INFINITY) {
			return 0;
		}else {
			return (distance-minDist) / (maxDistance - minDist);
		}
	}
	
}
