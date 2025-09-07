package org.schema.game.client.view.cubes.occlusion;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.cubes.CubeMeshBufferContainer;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.pentahedron.topbottom.PentaShapeAlgorithm;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;

abstract class SideProcessor{
	
	protected final BlockSide blockSide;
	private final Vector4f tmpColor = new Vector4f();
	private final Vector3f tmpLightDir = new Vector3f();
	public int[] blockedSides = new int[2];
	int blockP = 0;
	public SideProcessor(int sideId){
		this.blockSide = new BlockSide(sideId);
	}
	
	public void calculateActualOverlap(final CenterVertex c, final Vector3i centerPos,
			final OverlappingPosition other, final Occlusion occlusion) {
		/*
		 * We now need gather the actual overlapping vertices together.
		 * 
		 * if the OverlappingPosition is 0 it is on the same level as the vertex
		 * being a potential partner to average on
		 * 
		 * if the OverlappingPosition is NOT 0 it is shifted by one level. This
		 * potentialls causes local occlusion (corners)
		 * 
		 */
		int relevantCoord = getRelevantCoord(other);
		
		if(relevantCoord == 0 || (blockSide.normal >= 6)){
			//both block sides are on the same level
			
			int otherX = other.x*2;
			int otherY = other.y*2;
			int otherZ = other.z*2;
			int otherNormal = BlockShapeAlgorithm.getRepresentitiveNormal(blockSide.sideId, blockSide.normal, other.blockShape);
			
			Vector3i[] sideVerticesOfOther = BlockShapeAlgorithm.getSideVerticesByNormal(blockSide.sideId, blockSide.normal, other.blockShape);
			
			if(sideVerticesOfOther != null){
				for(int i = 0; i < sideVerticesOfOther.length; i++){
					int relX = otherX + sideVerticesOfOther[i].x;
					int relY = otherY + sideVerticesOfOther[i].y;
					int relZ = otherZ + sideVerticesOfOther[i].z;
					
					boolean vertexOverlapping;
					
					if(blockSide.normal >= 6 && blockSide.normal != otherNormal){
						vertexOverlapping = false;
						break;
					}else{
						vertexOverlapping = checkOverlapShare(c, other, relX, relY, relZ);
					}
					
					if(vertexOverlapping){
						int sideOfOtherFromForLight = blockSide.sideId; 
						if(blockSide.normal >= 6 && blockSide.normal == otherNormal){
							sideOfOtherFromForLight = BlockShapeAlgorithm.getAngledSideLightRepresentitive(blockSide.sideId, blockSide.normal, other.blockShape);
//							System.err.println("SIDE FROM:::: "+sideOfOtherFromForLight);
							assert(sideOfOtherFromForLight < 6):blockSide.sideId+"; "+blockSide.normal+"; "+other.blockShape;
						}
						
						
						int normalOfOtherBlock = blockSide.normal;
						
						boolean shareOk = true;
						if(blockSide.normal < 6){
							/*
							 * a flat surface is sharing a vertex.
							 * Make sure that the face we are sharing with doesn't have a solid on it,
							 * as it would for example be on 2 sides if a wedge that has its triangle on the surface.
							 * Or a slab wall.
							 * This special condition needs to be handled as only vertices that *just* share with
							 * the next surface, but do *NOT* share a vertex of the model on top of it can be used.
							 *
							 */
							Vector3i dirOfSide = Element.DIRECTIONSi[blockSide.normal];
							
							
							
							shareOk = checkCloseBlock(centerPos, other, dirOfSide, occlusion);
							shareOk = shareOk && checkRemoteBlock(centerPos, other, dirOfSide, occlusion);
							
						}
						if(shareOk){
							c.addShareWith( centerPos, other, sideOfOtherFromForLight, normalOfOtherBlock, occlusion);
						}else{
							c.addBlackShare();
						}
						
					}
				}
				
			}else{
				//adjacent block has no side with the same normal as the currently processing
			}
		}else{
			
		}
		
	}

	private boolean checkCloseBlock(Vector3i centerPos,
			OverlappingPosition other, Vector3i dirOfSide, Occlusion occlusion) {
		int containIndex = Occlusion.getContainIndex(centerPos.x+dirOfSide.x, centerPos.y+dirOfSide.y, centerPos.z+dirOfSide.z);
		int type = Math.abs(occlusion.contain.get(containIndex));
		if(type != 0){
//			boolean active = occlusion.active[containIndex];
			byte orientation = occlusion.orientation.get(containIndex);
			ElementInformation info = ElementKeyMap.getInfoFast(type);
			int[] sidesToCheckForVis = BlockShapeAlgorithm.getSidesToCheckForVis(info, orientation);
			
			
			//ONLY FOR SLAB!
			if(info.slab > 0 && sidesToCheckForVis.length > 0){
//					int dirOfOverlappingBlock = Element.getDirectionFromCoords(other.x, other.y, other.z);
				
				for(int b = 0; b < sidesToCheckForVis.length; b++){
					int visBlocker = sidesToCheckForVis[b];
					Vector3i visBlockerVec = Element.DIRECTIONSi[visBlocker];
					if(
						(visBlockerVec.x != 0 && visBlockerVec.x == other.x) || 
						(visBlockerVec.y != 0 && visBlockerVec.y == other.y) || 
						(visBlockerVec.z != 0 && visBlockerVec.z == other.z)){
						return false;
					}
				}
			}
		}
		return true;
	}

	private boolean checkRemoteBlock(Vector3i centerPos, OverlappingPosition other, Vector3i dirOfSide, Occlusion occlusion) {
		int containIndex = Occlusion.getContainIndex(centerPos.x+other.x+dirOfSide.x, centerPos.y+other.y+dirOfSide.y, centerPos.z+other.z+dirOfSide.z);
		int type = Math.abs(occlusion.contain.get(containIndex));
		if(type != 0){
//			boolean active = occlusion.active[containIndex];
			byte orientation = occlusion.orientation.get(containIndex);
			ElementInformation info = ElementKeyMap.getInfoFast(type);
			int[] sidesToCheckForVis = BlockShapeAlgorithm.getSidesToCheckForVis(info, orientation);
			if(sidesToCheckForVis.length > 0){
				if(other.touchingNeighbor){
					/* 
					 * these are all the full sides of a shape.
					 * We should not share vertices across the edges defined by those
					 * sides
					 */
					int dirOfOverlappingBlock = Element.getDirectionFromCoords(other.x, other.y, other.z);
					
					for(int b = 0; b < sidesToCheckForVis.length; b++){
						int visBlocker = sidesToCheckForVis[b];
						if(dirOfOverlappingBlock == Element.getOpposite(visBlocker)){
							blockedSides[blockP] = dirOfOverlappingBlock;
							blockP++;
							return false;
						}
					}
				}else{
					//this is the 1,1 position of the block in the same plane
					//0,1 and 1,0 have already been processed
					if(blockP > 0){
						for(int n = 0; n < blockP; n++){
							int dirOfOverlappingBlock = blockedSides[n];
							for(int b = 0; b < sidesToCheckForVis.length; b++){
								int visBlocker = sidesToCheckForVis[b];
								if(dirOfOverlappingBlock == Element.getOpposite(visBlocker)){
									return false;
								}
							}
						}
					}
				}
			}
			
			
			
//				ConvexHullShapeExt shape = (ConvexHullShapeExt)BlockShapeAlgorithm.getShape(info.getBlockStyle(), orientation, active);
//				for(Vector3f p : shape.getPoints()){
//					float oX = otherX + (p.x*2f);
//					float oY = otherY + (p.y*2f);
//					float oZ = otherZ + (p.z*2f);
//					
//					if(Math.abs(c.x - oX)< 0.05f &&  Math.abs(c.y - oY)< 0.05f && Math.abs(c.z - oZ)< 0.05f){
//						shareOk = false;
//						System.err.println("SHARE NOT OK");
//						break;
//					}
//				}
			
		}
		return true;
	}

	public boolean checkOverlapShare(CenterVertex c,
			OverlappingPosition o, int relX, int relY, int relZ) {
		return false;
	}

	public int getRelevantCoord(OverlappingPosition o) {
		return 0;
	}
	
	public void process(final int sideId, final Vector3i centerPos, final BlockShapeAlgorithm algo, final Occlusion occlusion) {
		blockSide.normal = BlockShapeAlgorithm.getNormalBySide(sideId, algo);
		
		if(blockSide.normal < 0){
			return; //trying to solve for nonexising side
		}
		
		Vector3i[] sideVerts = BlockShapeAlgorithm.getSideVerticesByNormal(blockSide.sideId, blockSide.normal, algo);
		
		
		
		if(sideVerts != null){
			if(sideVerts.length == 0){
				//TODO optimize: dont even process this side
				return;
			}
			for(int i = 0; i < blockSide.sideVerts.length; i++){
				if(i < sideVerts.length){
					blockSide.sideVerts[i].set(sideVerts[i]);
				}
				blockSide.sideVerts[i].reset();
			}
			if(sideVerts.length == 3){
				//its a triangle face. fill int foth vertex by clone
				blockSide.sideVerts[3].set(sideVerts[algo.getDoubleVertex()]);
				blockSide.sideVerts[3].reset();
			}
			blockSide.processOverlapping(this, occlusion, centerPos);
			
			
		}else{
			for(int i = 0; i < blockSide.sideVerts.length; i++){
				blockSide.sideVerts[i].setError();
			}
			//this side doesn't exist for this shape
			
			//TODO include angled shapes in processor
			
		}
		int subSubIndex = sideId * CubeMeshBufferContainer.VERTICES_PER_SIDE;
		for(int i = 0; i < blockSide.sideVerts.length; i++){
			CenterVertex v = blockSide.sideVerts[i];
			if(sideId < 6){
				v.addSelfColor(centerPos, sideId, occlusion);
//				System.err.println(centerPos+" "+Element.getSideString(sideId)+" ADDING SELF COLOR :::: "+v.color);
			}else{
				v.addSelfColor(centerPos, algo.getSidesAngled()[0], occlusion);
			}
			
			Vector4f avgLight = v.getAverage(tmpColor, tmpLightDir);
			
//			if(algo != null ){
//				System.err.println("OVER: "+algo +" -- sideProcessor: "+Element.getSideString(sideId)+" - : "+v.sharedNum+" / "+v.overlap+" :: "+v.color+" :: "+avgLight);
//			}
			int lightInfoIndex = CubeMeshBufferContainer.getLightInfoIndex(centerPos.x, centerPos.y, centerPos.z);
			CubeMeshBufferContainer container = occlusion.getContainer();
			container.setFinalLight(lightInfoIndex, (byte) FastMath.round((FastMath.clamp(avgLight.x, 0f, 1f) * Occlusion.COLOR_PERM)), subSubIndex + i, 0);
			container.setFinalLight(lightInfoIndex, (byte) FastMath.round((FastMath.clamp(avgLight.y, 0f, 1f) * Occlusion.COLOR_PERM)), subSubIndex + i, 1);
			container.setFinalLight(lightInfoIndex, (byte) FastMath.round((FastMath.clamp(avgLight.z, 0f, 1f) * Occlusion.COLOR_PERM)), subSubIndex + i, 2);
			container.setFinalLight(lightInfoIndex, (byte) FastMath.round((FastMath.clamp(avgLight.w, 0f, 1f) * Occlusion.COLOR_PERM)), subSubIndex + i, 3);
//			FastMath.normalizeCarmack(tmpLightDir);
////			assert(tmpLightDir.lengthSquared() == 0):tmpLightDir+"; "+encode(tmpLightDir.x)+"; "+encode(tmpLightDir.y)+"; "+encode(tmpLightDir.z);
//			container.setFinalLight(lightInfoIndex, encode(tmpLightDir.x), subSubIndex + i, 4);
//			container.setFinalLight(lightInfoIndex, encode(tmpLightDir.y), subSubIndex + i, 5);
//			container.setFinalLight(lightInfoIndex, encode(tmpLightDir.z), subSubIndex + i, 6);

			v.reset();
		}
		
	}
	public static final int lDirBits = 8;
	public static final int lDirBitLow = lDirBits/2-1;
	public byte encode(float x){
		return (byte)((byte)FastMath.fastCeil(Math.abs(x * lDirBitLow)) + (x < 0f ? lDirBits : 0));  
	}
}
