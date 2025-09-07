package org.schema.game.common.data.world;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.SegmentPiece;

import com.bulletphysics.linearmath.Transform;

public class TransformaleObjectTmpVars {

	public Matrix3f rot = new Matrix3f();
	public Vector3i dir = new Vector3i();
	public Vector3i systemPos = new Vector3i();
	public Vector3f otherSecCenter = new Vector3f();
	public Transform t = new Transform();
	public Vector3f bb = new Vector3f();
	public Transform transTmp = new Transform();
	public Vector3i local = new Vector3i();
	public SegmentPiece tmpPiece = new SegmentPiece();
	public Vector3f tmpVec3a = new Vector3f();
	public Vector3f tmpVec3b = new Vector3f();

	
	public final Transform inT = new Transform();
	public final Transform outT = new Transform();
	{
	inT.setIdentity();
	outT.setIdentity();
	}
}
