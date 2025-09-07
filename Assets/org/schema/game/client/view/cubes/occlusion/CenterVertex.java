package org.schema.game.client.view.cubes.occlusion;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.util.linAlg.Vector3i;

import theleo.jstruct.Struct;

@Struct
public class CenterVertex {
	
	int x;
	int y;
	int z;
	int overlap = 0;
	private final OverlappingPosition[] overlappingPositions = new OverlappingPosition[7]; 
	private final OverlappingPosition[] toTest = new OverlappingPosition[7];
	
	public CenterVertex(){
		for (int i = 0; i < overlappingPositions.length; i++) {
			overlappingPositions[i] = new OverlappingPosition();
		}
		
	}
	public void reset() {
		color.set(0,0,0,0);
		lightDir.set(0,0,0);
		sharedNum = 0;
	}
	public void setError(){
		sharedNum = 0;
		color.set(1,0,0,0);
		lightDir.set(0,0,0);
	}
	public void calculatePossibleOverlapping(final SideProcessor proc, final Vector3i centerPos, final Occlusion occlusion) {
		
		
		overlap = 0;
		overlappingPositions[0].set(x, 0, 0, occlusion);
		overlappingPositions[1].set(0, 0, z, occlusion);
		overlappingPositions[2].set(0, y, 0, occlusion);
		overlappingPositions[3].set(0, y, z, occlusion);
		overlappingPositions[4].set(x, y, 0, occlusion);
		overlappingPositions[5].set(x, 0, z, occlusion);
		overlappingPositions[6].set(x, y, z, occlusion);
		
		
		proc.blockedSides[0] = -1;
		proc.blockedSides[1] = -1;
		proc.blockP = 0;
		for(int i = 0; i < overlappingPositions.length; i++){
			if(overlappingPositions[i].exists(occlusion, centerPos)){
				//does this block even exist?
				toTest[overlap] = overlappingPositions[i];
				proc.calculateActualOverlap(this, centerPos, toTest[overlap], occlusion);
				overlap++;
				
			}
		}
	}

	Vector4f color = new Vector4f();
	private Vector4f colorTmp = new Vector4f();
	private Vector3f lightDir = new Vector3f();
	private Vector3f lightDirTmp = new Vector3f();
	float sharedNum;
	private Vector3i absPosTmp = new Vector3i();
	
	public void addShareWith(
			final Vector3i centerPos, 
			final OverlappingPosition other, 
			final int sideOfOtherFrom, 
			final int normalOfOtherBlock, 
			final Occlusion occlusion) {
		
		
		Vector3i absPos = other.getAbsPosRelativeFrom(centerPos, this.absPosTmp);
		
		
		assert(sideOfOtherFrom < 6);
		Vector4f light = occlusion.getLight(absPos, sideOfOtherFrom, colorTmp, lightDirTmp);
		lightDir.add(lightDirTmp);
		color.add(light);
		sharedNum++;
		
		
	
	}
	public void addBlackShare() {
		sharedNum++;
	}
	
	public Vector4f getAverage(final Vector4f tmp, Vector3f dirTmp){
		float f = 1f / sharedNum;
		
		tmp.set(color.x * f, color.y * f, color.z * f, color.w * f);
		
		tmp.scale(Occlusion.LIGHT_SCALE);
		
		dirTmp.set(lightDir.x * f, lightDir.y * f, lightDir.z * f);
		return tmp;
	}
	public void addSelfColor(Vector3i centerPos, int sideId, final Occlusion occlusion) {
		Vector4f light = occlusion.getLight(centerPos, sideId, colorTmp, lightDirTmp);
		lightDir.add(lightDirTmp);
		color.add(light);
		sharedNum++;
		
	}
	public void set(Vector3i v) {
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
	}

	
	
}
