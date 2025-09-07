package org.schema.game.client.view.cubes.occlusion;

import org.schema.common.util.linAlg.Vector3i;

public class BlockSide extends Vector3i{
	
	int sideId;
	
	protected final CenterVertex[] sideVerts = new CenterVertex[4];

	public int normal;

	
	public BlockSide(int sideId){
		
		this.sideId = sideId;
		this.normal = -1;
		
		for (int i = 0; i < sideVerts.length; i++) {
			sideVerts[i] = new CenterVertex();
			
		}
		
		
	}
	public void reset(){
		normal = -1;
		for(int i = 0; i < sideVerts.length; i++){
			sideVerts[i].reset();
		}
	}
	public void processOverlapping(SideProcessor processor, Occlusion occlusion, Vector3i centerPos){
		for(int i = 0; i < sideVerts.length; i++){
			sideVerts[i].calculatePossibleOverlapping(processor, centerPos, occlusion);
		}
	}
	
}
