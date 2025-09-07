package org.schema.game.common.data.physics;

import java.util.List;

import javax.vecmath.Vector3f;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestConvexResultCallback;
import com.bulletphysics.util.ObjectArrayList;

public class ClosestConvexResultCallbackExt extends ClosestConvexResultCallback {

	public Object userData;
	public boolean checkHasHitOnly;
	public boolean completeCheck;
	public ModifiedDynamicsWorld dynamicsWorld;
	public ObjectArrayList<CollisionObject> overlapping = new ObjectArrayList<CollisionObject>();
	public CollisionObject ownerObject;
	public List<Vector3f> sphereOverlapping;
	public List<Vector3f> sphereOverlappingNormals;
	public boolean sphereDontHitOwner;

	public ClosestConvexResultCallbackExt(Vector3f arg0, Vector3f arg1) {
		super(arg0, arg1);
	}

}
