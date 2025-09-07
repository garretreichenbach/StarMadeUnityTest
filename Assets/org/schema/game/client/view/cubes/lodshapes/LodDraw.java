package org.schema.game.client.view.cubes.lodshapes;

import java.nio.FloatBuffer;

import javax.vecmath.Vector3f;

import org.schema.schine.graphicsengine.forms.Mesh;

import com.bulletphysics.linearmath.Transform;

public class LodDraw implements Comparable<LodDraw>{
	
	private static Vector3f posTmp = new Vector3f();
	public Transform transform = new Transform();
	public short type;
	public Mesh mesh;
	public Mesh meshDetail;
	public float[] lightingAndPos;
	public int pointer;
	public boolean faulty;
	@Override
	public int compareTo(LodDraw o) {
		return type - o.type;
	}
	public void fillLightBuffers(
			FloatBuffer lightVecBuffer, FloatBuffer lightDiffuseBuffer) {
		lightVecBuffer.rewind();
		lightDiffuseBuffer.rewind();
		
		for(int i = 0; i < 4; i++){
			lightDiffuseBuffer.put(lightingAndPos, pointer, 4);
			
			int pointerPos = pointer+4;
			posTmp.x = lightingAndPos[pointerPos+0];
			posTmp.y = lightingAndPos[pointerPos+1];
			posTmp.z = lightingAndPos[pointerPos+2];
			
			transform.transform(posTmp);
			
			lightVecBuffer.put(posTmp.x);
			lightVecBuffer.put(posTmp.y);
			lightVecBuffer.put(posTmp.z);
			
			pointer += (4+3);
		}
		lightVecBuffer.rewind();
		lightDiffuseBuffer.rewind();			
	}
}
