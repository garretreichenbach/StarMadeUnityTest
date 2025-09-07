package org.schema.game.client.view.cubes.occlusion;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;

import theleo.jstruct.Struct;

@Struct
public class OverlappingPosition{
	
	public int x;
	public int y;
	public int z;
	
//	OverlappingPositionVertex[] verticesOverlapping = new OverlappingPositionVertex[3];
	public BlockShapeAlgorithm blockShape;
	private byte orientation;
	boolean touchingNeighbor;
	
	
	
	public OverlappingPosition() {
		super();
//		for(int i = 0; i < verticesOverlapping.length; i++){
//			verticesOverlapping[i] = new OverlappingPositionVertex();
//		}
	}


	public Vector3i getAbsPosRelativeFrom(Vector3i centerPos, Vector3i out){
		out.set(centerPos.x+x, centerPos.y+y, centerPos.z+z);
		return out;
	}
	/**
	 * 
	 * @return true if this block exists (relative position by x y z)
	 */
	public boolean exists(final Occlusion occlusion, Vector3i centerPos) {
		int containIndex = Occlusion.getContainIndex(centerPos.x+x, centerPos.y+y, centerPos.z+z);
		int type = Math.abs(occlusion.contain.get(containIndex));
		if(type != 0){
			this.orientation = occlusion.orientation.get(containIndex);
			blockShape = null;
			ElementInformation info = ElementKeyMap.getInfoFast(type);
			if(info.getBlockStyle() != BlockStyle.NORMAL){
				blockShape = BlockShapeAlgorithm.getAlgo(info.getBlockStyle(), orientation);
				if(info.hasLod() && info.lodShapeStyle == 1){
					blockShape = blockShape.getSpriteAlgoRepresentitive();
				}
			}
			return true;
		}
		return false;
	}


	public void set(int x, int y, int z, Occlusion occlusion) {
		this.x = x;
		this.y = y;
		this.z = z;
		
		this.touchingNeighbor = (Math.abs(x) + Math.abs(y) + Math.abs(z) == 1);
		
		
		
	}
}
