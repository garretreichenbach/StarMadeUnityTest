package org.schema.game.common.data.element;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;

public class ShootContainer {
	public final Vector3i controlledFromOrig = new Vector3i(); 
	public final Vector3f weapontOutputWorldPos = new Vector3f(); 
	public final Vector3f camPos = new Vector3f(); 
	public final Vector3f shootingDirTemp = new Vector3f(); 
	public final Vector3f shootingUpTemp = new Vector3f(); 
	public final Vector3f shootingRightTemp = new Vector3f(); 
	public final Vector3f shootingForwardTemp = new Vector3f(); 
	public final Vector3f tmpCampPos = new Vector3f();
	public final Vector3i controlledFrom = new Vector3i();
	public final Vector3i centeralizedControlledFromPos = new Vector3i();
	public final Vector3f shootingStraightForwardTemp = new Vector3f();
	public final Vector3f shootingStraightUpTemp = new Vector3f();
	public final Vector3f shootingStraightRightTemp = new Vector3f();
	public final Vector3f  shootingDirStraightTemp = new Vector3f();
}
