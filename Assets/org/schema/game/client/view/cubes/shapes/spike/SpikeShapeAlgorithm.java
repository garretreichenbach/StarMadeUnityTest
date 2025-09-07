package org.schema.game.client.view.cubes.shapes.spike;

import java.util.Arrays;

import org.schema.common.FastMath;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.IconInterface;
import org.schema.game.client.view.cubes.shapes.wedge.WedgeShapeAlgorithm;

public abstract class SpikeShapeAlgorithm extends BlockShapeAlgorithm implements IconInterface {

	private int[][] normalCache;
	private int[] normal;
	private Vector3i[][] angledSideVerts;
	private int[] angledSideCache;
	private int currentPerm;

	protected abstract int[][] getNormals();
	@Override
	public boolean isAngled(int sideId) {
		return sideId == getSidesAngled()[0] || sideId == getSidesAngled()[1];
	}
	public SpikeShapeAlgorithm(){
		normalCache = getNormals();
		angledSideCache = getSidesAngled();
		if(normalCache != null){
			
			
			
			normal = new int[normalCache.length];
			
			for(int c = 0; c < normalCache.length; c++){
				int n = 0;
				
				int f = 1;
				int[] js = normalCache[c];
				int[] cc = Arrays.copyOf(js, js.length);
				Arrays.sort(cc);
				//combine sides that the wedge point at into mask that represents the wedge normal
				for(int i = 0; i < cc.length; i++){
					n += (cc[i]) * f; 
					f *= 10;
				}
				this.normal[c] = n;
			
			}
			
			angledSideVerts = getAngledSideVerts();
		}
	}
	
	@Override
	public void modAngledVertex(int s) {
		
		int index = 0;
		if(Math.abs(s) >= 100){
			index = 1;
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
				angledSideVerts[index][a],
				angledSideVerts[index][b],
				angledSideVerts[index][c],
				angledSideVerts[index][d],
						};
		
		angledSideVerts[index] = tmp;
		
		System.err.println("INDEX::: "+index);
		for(int i = 0; i < 4; i++){
			System.err.println("new Vector3i"+angledSideVerts[index][i]+",");
		}
	}

	public void calcAngledSideVertsFromWedges(){
		Vector3i[][] s = new Vector3i[2][4];
		for(int i = 0; i < BlockShapeAlgorithm.algorithms[0].length-1;i++){
			WedgeShapeAlgorithm a = (WedgeShapeAlgorithm) BlockShapeAlgorithm.algorithms[0][i];
			
//			System.err.println("::::: "+a.getWedgeNormal()+" == "+normal[0]+" or "+normal[1]);
			if(a.getWedgeNormal() == normal[0]){
				s[0] = a.getAngledSideVerts();
			}
			if(a.getWedgeNormal() == normal[1]){
				s[1] = a.getAngledSideVerts();
			}
			
		}
		System.err.println("NAME: "+this);
		System.err.println("	protected Vector3i[][] getAngledSideVerts() {");
		System.err.println("		return new Vector3i[][]{");
		
		for(int i = 0; i < 2; i++){
			System.err.println("			{");
			for(int c = 0; c < 4; c++){
				System.err.println("				new Vector3i"+s[i][c]+",");
			}
			System.err.println("			},");
		}
		System.err.println("		};");
		System.err.println("	}");
	}
	public Vector3i[][] getAngledSideVerts() {
		
		
		return null;
	}
	
	@Override
	protected int getAngledSideLightRepresentitive(int sideId, int normal) {
		if(normal == this.normal[0] ){
			return getSidesAngled()[0];
		}else if(this.normal[1] == normal){
			return getSidesAngled()[1];
		}else if(sideId == normalCache[0][0] || sideId == normalCache[0][1] || sideId == normalCache[1][0] || sideId == normalCache[1][1]){
			//no side with that normal exists
			return -1;
		}else{
			return super.getAngledSideLightRepresentitive(sideId, normal);
		}
	}
	@Override
	protected int getRepresentitiveNormal(int sideId, int normal) {
		if(normal == this.normal[0] || this.normal[1] == normal){
			return normal;
		}else if(sideId == normalCache[0][0] || sideId == normalCache[0][1] || sideId == normalCache[1][0] || sideId == normalCache[1][1]){
			//no side with that normal exists
			return -1;
		}else{
			return super.getRepresentitiveNormal(sideId, normal);
		}
	}
	@Override
	protected Vector3i[] getSideByNormal(int sideId, int normal) {
		if(normal == this.normal[0]){
			return angledSideVerts[0];
		}else if(normal == this.normal[1]){
			return angledSideVerts[1];
		}else if(sideId == normalCache[0][0] || sideId == normalCache[0][1] || sideId == normalCache[1][0] || sideId == normalCache[1][1]){
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
			return this.normal[0];
		}else if(sideId == getSidesAngled()[1]){
			return this.normal[1];
		}else if(sideId == normalCache[0][0] || sideId == normalCache[0][1] || sideId == normalCache[1][0] || sideId == normalCache[1][1]){
			return -1;
		}
		return super.getNormalBySide(sideId);
	}
}
