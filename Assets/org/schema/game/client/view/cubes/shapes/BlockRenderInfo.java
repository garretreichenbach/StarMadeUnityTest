package org.schema.game.client.view.cubes.shapes;

import org.schema.game.client.view.cubes.CubeMeshBufferContainer;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm.TexOrderStyle;

public class BlockRenderInfo {
	public boolean threeSided; 
	public int orientation; 
	public int sideId; 
	public byte layer; 
	public short typeCode; 
	public byte hitPointsCode; 
	public byte animatedCode; 
	public int lightIndex; 
	public int sideOccId; 
	public int index; 
	public float segIndex; 
	public int halvedFactor; //slab
	public CubeMeshBufferContainer container; 
	public int resOverlay; 
	public TexOrderStyle pointToOrientation; 
	public boolean onlyInBuildMode;
	public boolean extendedBlockTexture;
	public BlockStyle blockStyle;
}
