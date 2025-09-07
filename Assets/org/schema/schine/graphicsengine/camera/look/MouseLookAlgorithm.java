package org.schema.schine.graphicsengine.camera.look;

import com.bulletphysics.linearmath.Transform;

public interface MouseLookAlgorithm {
	public void fix();

	public void force(Transform t);

	public void mouseRotate(boolean server, float dx, float dy, float dz, float xSensitivity, float ySensitivity, float zSensibility);

	public void lookTo(Transform n);
}
