package org.schema.schine.physics;

import javax.vecmath.Vector3f;

import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.container.PhysicsDataContainer;

import com.bulletphysics.linearmath.Transform;

public interface Physical {
	public void createConstraint(Physical a, Physical b, Object userData);

	public Transform getInitialTransform();

	public float getMass();

	/**
	 * @return the physicsDataContainer
	 */
	public PhysicsDataContainer getPhysicsDataContainer();

	/**
	 * @param physicsDataContainer the physicsDataContainer to set
	 */
	public void setPhysicsDataContainer(PhysicsDataContainer physicsDataContainer);

	public StateInterface getState();

	public void getTransformedAABB(Vector3f oMin, Vector3f oMax, float margin, Vector3f tmpMin, Vector3f tmpMax, Transform instead);

	public void initPhysics();

}
