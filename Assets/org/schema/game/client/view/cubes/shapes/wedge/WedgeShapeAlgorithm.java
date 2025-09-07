package org.schema.game.client.view.cubes.shapes.wedge;

import java.util.Arrays;

import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.IconInterface;

public abstract class WedgeShapeAlgorithm extends BlockShapeAlgorithm implements IconInterface {

	
	private int normal;
	private final byte[] wedgeOrientationCache;
	private Vector3i[] angledSideVerts;

	
	
	public WedgeShapeAlgorithm(){
		wedgeOrientationCache = getWedgeOrientation();
		
		if(wedgeOrientationCache != null){
			byte[] cc = Arrays.copyOf(wedgeOrientationCache, wedgeOrientationCache.length);
			Arrays.sort(cc);
			
			int n = 0;
			
			int f = 1;
			
			//combine sides that the wedge point at into mask that represents the wedge normal
			for(int i = 0; i < cc.length; i++){
				n += (cc[i]) * f; 
				f *= 10;
			}
			this.normal = n;
			
			angledSideVerts = getAngledSideVerts();
		}
	}
	
	public abstract Vector3i[] getAngledSideVerts();
		
	public int getWedgeNormal(){
		return normal;
	}
	@Override
	public int[] getSidesToTestSpecial(){
		return getSidesOpenToAir();
	}
	
	@Override
	public boolean isAngled(int sideId) {
		return sideId == getSidesAngled()[0];
	}

	int currentPerm;
	@Override
	public void modAngledVertex(int s) {
		
		if(Math.abs(s) >= 100){
			if(s > 0){
				s -= 100;
			}else{
				s += 100;	
			}
		}
		
		currentPerm = FastMath.cyclicModulo(currentPerm+s, vertexPermutations4.length);
		
		int a = vertexPermutations4[currentPerm][0]-1;
		int b = vertexPermutations4[currentPerm][1]-1;
		int c = vertexPermutations4[currentPerm][2]-1;
		int d = vertexPermutations4[currentPerm][3]-1;
		Vector3i[] tmp = new Vector3i[]{
				angledSideVerts[a],
				angledSideVerts[b],
				angledSideVerts[c],
				angledSideVerts[d],
						};
		
		angledSideVerts = tmp;
		
		for(int i = 0; i < 4; i++){
			System.err.println("new Vector3i"+angledSideVerts[i]+",");
		}
		 
	}
	@Override
	protected int getAngledSideLightRepresentitive(int sideId, int normal) {
		if(normal == this.normal){
			return getSidesAngled()[0];
		}else if(sideId == wedgeOrientationCache[0] || sideId == wedgeOrientationCache[1]){
			//no side with that normal exists
			return -1;
		}else{
			return super.getAngledSideLightRepresentitive(sideId, normal);
		}
	}
	@Override
	protected int getRepresentitiveNormal(int sideId, int normal) {
		if(normal == this.normal){
			return normal;
		}else if(sideId == wedgeOrientationCache[0] || sideId == wedgeOrientationCache[1]){
			//no side with that normal exists
			return -1;
		}else{
			return super.getRepresentitiveNormal(sideId, normal);
		}
	}
	@Override
	protected Vector3i[] getSideByNormal(int sideId, int normal) {
		if(normal == this.normal){
			return angledSideVerts;
		}else if(sideId == wedgeOrientationCache[0] || sideId == wedgeOrientationCache[1]){
			//no side with that normal exists
			return none;
		}else{
//				assert(normal < 6):this+"; "+normal+"; "+sideId+"; normal of this: "+getWedgeNormal();
			if(normal < 6){
				return super.getSideByNormal(sideId, normal);
			}else{
				//happens when for example 2 different wedges compare themselves for their angled normal. 
				//return null since those normals dont have faces in common
				return null; 
			}
		}
	}
	@Override
	protected int getNormalBySide(int sideId) {
		if(sideId == getSidesAngled()[0]){
			return this.normal;
		}else if(sideId == wedgeOrientationCache[0] || sideId == wedgeOrientationCache[1]){
			return -1;
		}
		return super.getNormalBySide(sideId);
	}
}
