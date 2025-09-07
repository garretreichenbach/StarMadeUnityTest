package org.schema.game.client.view.cubes.lodshapes;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.ConvexHullShapeExt;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.graphicsengine.forms.Mesh;

import com.bulletphysics.linearmath.Transform;

public class CubeLodIndexedShape implements CubeLodShapeInterface{

	
	
	
	private SegmentData segmentData;
	private int x;
	private int y;
	private int z;
	
	private Oriencube orientcube;
	private byte orientation;
	private boolean active;
	
	
	private Transform primary = new Transform();
	private Transform secondary = new Transform();
	
	private Vector3f localPos = new Vector3f();
	private short type;
	
	private Vector4f[] lightingColor = new Vector4f[4];
	private Vector3f[] lightingDir = new Vector3f[4];

	private Transform worldTransform = new Transform();
	private Transform localTransform = new Transform();
	private Vector4f[] sideColors = new Vector4f[6];
	private Vector4f[] sideColorsTmp = new Vector4f[6];
	
	public CubeLodIndexedShape(SegmentData segmentData, int index, byte segX,
			byte segY, byte segZ) {
		
		for(int i = 0; i < lightingColor.length; i++){
			lightingColor[i] = new Vector4f(1,1,1,1);
			lightingDir[i] = new Vector3f(0,0,0);
		}
		for(int i = 0; i < sideColorsTmp.length; i++){
			sideColorsTmp[i] = new Vector4f(0,0,0,0);
		}
		
		this.segmentData = segmentData;
		this.type = segmentData.getType(index);
		
		this.x = segmentData.getSegment().pos.x + segX;
		this.y = segmentData.getSegment().pos.y + segY;
		this.z = segmentData.getSegment().pos.z + segZ;
		
		
		this.orientation = segmentData.getOrientation(index);
		this.active = segmentData.isActive(index);
		
		
		orientcube = (Oriencube) BlockShapeAlgorithm.getAlgo(6, orientation);
		
		
		localPos.set(this.x-SegmentData.SEG_HALF,this.y-SegmentData.SEG_HALF,this.z-SegmentData.SEG_HALF);
		
		orientcube.getPrimaryTransform(localPos, 0, primary);
		orientcube.getSecondaryTransform(secondary);
		
		
		localTransform.set(primary);		
		localTransform.mul(secondary);
	}

	@Override
	public float getLodDistance() {
		return 10;
	}

	@Override
	public boolean isBlockAtDistance() {
		return true;
	}

	@Override
	public short getBlockTypeAtDistance() {
		return type;
	}

	@Override
	public byte getOrientation() {
		return orientation;
	}

	@Override
	public boolean isPhysical() {
		return false;
	}

	@Override
	public boolean isPhysicalMesh() {
		return false;
	}

	@Override
	public List<ConvexHullShapeExt> getPhysicalMesh() {
		return null;
	}

	@Override
	public Mesh getModel(int lod, boolean active) {
		return ElementKeyMap.getInfoFast(type).getModel(lod, active);
	}

	@Override
	public byte[] getBlockRepresentation() {
		return null;
	}

	@Override
	public Vector4f[] getLighting() {
		return lightingColor;
	}

	@Override
	public short getType() {
		return type;
	}

	@Override
	public Transform getClientTransform() {
		worldTransform.set(segmentData.getSegmentController().getWorldTransformOnClient());
		worldTransform.mul(localTransform);
		return worldTransform;
	}

	@Override
	public int compareTo(CubeLodShapeInterface o) {
		return type - o.getType();
	}

	private Vector3f tmp = new Vector3f(); 
	private Vector4f cTmp = new Vector4f(); 
	@Override
	public void fillLightBuffers(Transform clientTransform, FloatBuffer lightVecBuffer,
			FloatBuffer lightDiffuseBuffer) {
		lightVecBuffer.rewind();
		lightDiffuseBuffer.rewind();
		
		for(int i = 0; i < 4; i++){
			Vector4f col = lightingColor[i];
			lightDiffuseBuffer.put(col.x);
			lightDiffuseBuffer.put(col.y);
			lightDiffuseBuffer.put(col.z);
			lightDiffuseBuffer.put(col.w);
			
			
			tmp.set(lightingDir[0]);
			clientTransform.transform(tmp);
			
			lightVecBuffer.put(tmp.x);
			lightVecBuffer.put(tmp.y);
			lightVecBuffer.put(tmp.z);
		}
		lightVecBuffer.rewind();
		lightDiffuseBuffer.rewind();
	}
	@Override
	public void resetLight(){
		Arrays.fill(sideColors, null);
	}
	@Override
	public void setLight(int sideId, float gatR, float gatG, float gatB,
			float occ) {
		sideColorsTmp[sideId].set(gatR, gatG, gatB, occ);
		
		sideColors[sideId] = sideColorsTmp[sideId];
	}

	
	
	@Override
	public void calcLight() {
		
		int index = 0;
		for(int i = 0; i < 6; i++){
			if(i != orientcube.getOrientCubePrimaryOrientation() && i != Element.getOpposite(orientcube.getOrientCubePrimaryOrientation())){
				
				Vector4f lightCol = lightingColor[index];
				Vector3f lightPos = lightingDir[index];
				
				lightCol.set(0,0,0,0);
				float coloring = 0;
				
				lightPos.set(Element.DIRECTIONSf[orientcube.getOrientCubePrimaryOrientation()]);
				lightPos.add(Element.DIRECTIONSf[i]);
				
				if(sideColors[i] != null){
					lightCol.add(sideColors[i]);
					coloring++;
				}
				Vector4f sT = sideColors[orientcube.getOrientCubePrimaryOrientation()];
				float primFac = 0.01f;
				if(sT != null){
					lightCol.x += sT.x * primFac;
					lightCol.y += sT.y * primFac;
					lightCol.z += sT.z * primFac;
					lightCol.w += sT.w * primFac;
					coloring+= primFac;
				}
				if(coloring > 0f){
					lightCol.scale(1f/coloring);
				}
				index++;
				
//				System.err.println("LIGHT COL OF "+lightPos+" ---> "+lightCol);
			}
		}
		assert(index == 4);
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
