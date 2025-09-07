package org.schema.game.client.view.cubes.lodshapes;

import java.nio.FloatBuffer;
import java.util.List;

import javax.vecmath.Vector4f;

import org.schema.game.common.data.physics.ConvexHullShapeExt;
import org.schema.schine.graphicsengine.forms.Mesh;

import com.bulletphysics.linearmath.Transform;

public interface CubeLodShapeInterface extends Comparable<CubeLodShapeInterface>{

	public float getLodDistance();
	
	public boolean isBlockAtDistance();
	
	public short getBlockTypeAtDistance();
	
	public byte getOrientation();
	
	public boolean isPhysical();
	
	public boolean isPhysicalMesh();

	public List<ConvexHullShapeExt> getPhysicalMesh();
	
	public Mesh getModel(int lod, boolean active);
	
	public byte[] getBlockRepresentation();
	
	public Vector4f[] getLighting();
	
	public short getType();

	public Transform getClientTransform();

	public void fillLightBuffers(Transform clientTransform, FloatBuffer lightVecBuffer,
			FloatBuffer lightDiffuseBuffer);

	public void setLight(int sideId, float gatR, float gatG, float gatB,
			float occ);
	
	public void resetLight();
	
	public void calcLight();
	
}
