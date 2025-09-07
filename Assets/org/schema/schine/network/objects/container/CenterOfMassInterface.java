package org.schema.schine.network.objects.container;

import javax.vecmath.Vector3f;

public interface CenterOfMassInterface {
	public Vector3f getCenterOfMass();

	public boolean isVirtual();

	public float getTotalPhysicalMass();
}
