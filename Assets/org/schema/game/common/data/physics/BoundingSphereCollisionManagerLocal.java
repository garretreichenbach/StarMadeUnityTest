package org.schema.game.common.data.physics;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class BoundingSphereCollisionManagerLocal<E extends BoundingSphereObject> {
	public final List<E> bbs = new ObjectArrayList<E>();

	private final static BondingSpherePairPool spherePool = new BondingSpherePairPool();
	
	
	
	public BoundingSphereCollisionManagerLocal(){
		
	}
	
	
	public BoundingSpherePair<E> getPair() {
		return spherePool.getPair();
	}
	public void freePair(BoundingSpherePair<E> pair) {
		spherePool.free(pair);
	}
	
	public void freeAll(List<BoundingSpherePair<E>> out){
		for(int i = 0; i < out.size(); i++){
			freePair(out.get(i));
		}
		out.clear();
	}
}
