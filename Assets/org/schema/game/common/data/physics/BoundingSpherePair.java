package org.schema.game.common.data.physics;

public class BoundingSpherePair<E extends BoundingSphereObject> {
	public E a;
	public E b;
	
	public float getDistanceY(){
		return Math.abs(a.getWorldTransform().origin.y - b.getWorldTransform().origin.y);
	}
	public float getDistanceZ(){
		return Math.abs(a.getWorldTransform().origin.z - b.getWorldTransform().origin.z);
	}
	
	
	public boolean overlapY() {
		return getDistanceY() - (a.getBoundingSphereTotal().radius + b.getBoundingSphereTotal().radius) <= 0;
	}
	public boolean overlapZ() {
		return getDistanceZ() - (a.getBoundingSphereTotal().radius + b.getBoundingSphereTotal().radius) <= 0;
	}
}
