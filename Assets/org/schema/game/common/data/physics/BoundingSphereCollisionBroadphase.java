package org.schema.game.common.data.physics;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class BoundingSphereCollisionBroadphase<E extends BoundingSphereObject> {

	
	
	public final BoundingSphereCollisionManagerLocal<E> man;
	public final List<BoundingSpherePair<E>> out = new ObjectArrayList<BoundingSpherePair<E>>();
	
	
	
	
	public BoundingSphereCollisionBroadphase(
			BoundingSphereCollisionManagerLocal<E> man) {
		super();
		this.man = man;
	}

	public List<BoundingSpherePair<E>> calculate(){
		setupPhase(man.bbs);
		List<BoundingSpherePair<E>> broadPhase = broadPhase(man, out);
		return broadPhase;
	}
	
	public void free(){
		man.freeAll(out);
		
	}
	
	protected abstract void setupPhase(List<E> bbs);
	protected abstract List<BoundingSpherePair<E>> broadPhase(BoundingSphereCollisionManagerLocal<E> man, List<BoundingSpherePair<E>> out);
}
