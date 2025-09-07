package org.schema.game.common.data.physics;

import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;

public class BondingSpherePairPool{
	private ObjectArrayFIFOQueue<BoundingSpherePair<?>> pool = new ObjectArrayFIFOQueue<BoundingSpherePair<?>>();

	public <E extends BoundingSphereObject>BoundingSpherePair<E> getPair(){
		if(pool.isEmpty()){
			return new BoundingSpherePair();
		}else{
			return (BoundingSpherePair<E>) pool.dequeue();
		}
	}
	
	public void free(BoundingSpherePair<?> e){
		pool.enqueue(e);
	}
}
