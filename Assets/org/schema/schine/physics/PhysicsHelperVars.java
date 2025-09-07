package org.schema.schine.physics;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

public class PhysicsHelperVars {

	public Vector3f currentForward = new Vector3f();
	public Vector3f currentUp = new Vector3f();
	public Vector3f axisYaw = new Vector3f();
	public Vector3f axisRoll = new Vector3f();
	public Vector3f axisPitch = new Vector3f();
	public Vector3f axis = new Vector3f();
	public Vector3f toForward = new Vector3f();
	public Vector3f toRight = new Vector3f();
	public Vector3f toUp = new Vector3f();
	public Vector3f currentRight = new Vector3f();
	public Vector3f lastAxis = new Vector3f();
	public Matrix3f mat = new Matrix3f();

}
