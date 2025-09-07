package org.schema.game.client.view.cubes.occlusion;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;

import theleo.jstruct.Struct;

@Struct
public class NormalizerNew {
	

	private final Vector3i centerPos = new Vector3i();
	
	
	private final SideProcessor[] normalBlockProcessors = new SideProcessor[]{
			new FrontProcessor(0),
			new BackProcessor(1),
			new TopProcessor(2),
			new BottomProcessor(3),
			new RightProcessor(4),
			new LeftProcessor(5)
	};
	private final ExtraSideProcessor extraSide = new ExtraSideProcessor(6);
	
	public NormalizerNew() {
//		BlockShapeAlgorithm.initializeNormalizeLightingProcessors(centerPos);
	}

	
	

	private class ExtraSideProcessor extends SideProcessor{
		
		public ExtraSideProcessor(int sideId) {
			super(sideId);
		}
		@Override
		public boolean checkOverlapShare(CenterVertex c,
				OverlappingPosition o, int relX, int relY, int relZ) {
//			System.err.println("CHECKING OVERLAPPING "+c+" -> "+relX+", "+relY+", "+relZ);
			return relX == c.x && relY == c.y && relZ == c.z;
		}
		@Override
		public int getRelevantCoord(OverlappingPosition o) {
			return o.z;
		}
	}
	
	
	private class FrontProcessor extends SideProcessor{

		public FrontProcessor(int sideId) {
			super(sideId);
		}
		@Override
		public boolean checkOverlapShare(CenterVertex c,
				OverlappingPosition o, int relX, int relY, int relZ) {
			return relX == c.x && relY == c.y;
		}
		@Override
		public int getRelevantCoord(OverlappingPosition o) {
			return o.z;
		}
	}
	
	private class BackProcessor extends SideProcessor{

		public BackProcessor(int sideId) {
			super(sideId);
		}
		@Override
		public boolean checkOverlapShare(CenterVertex c,
				OverlappingPosition o, int relX, int relY, int relZ) {
			return relX == c.x && relY == c.y;
		}
		@Override
		public int getRelevantCoord(OverlappingPosition o) {
			return o.z;
		}
	}
	private class TopProcessor extends SideProcessor{
		public TopProcessor(int sideId) {
			super(sideId);
		}
		@Override
		public boolean checkOverlapShare(CenterVertex c,
				OverlappingPosition o, int relX, int relY, int relZ) {
			return relX == c.x && relZ == c.z;
		}
		@Override
		public int getRelevantCoord(OverlappingPosition o) {
			return o.y;
		}
	}
	
	
	
	private class BottomProcessor extends SideProcessor{
		public BottomProcessor(int sideId) {
			super(sideId);
		}
		@Override
		public boolean checkOverlapShare(CenterVertex c,
				OverlappingPosition o, int relX, int relY, int relZ) {
			return relX == c.x && relZ == c.z;
		}
		@Override
		public int getRelevantCoord(OverlappingPosition o) {
			return o.y;
		}
	}
	private class RightProcessor extends SideProcessor{
		public RightProcessor(int sideId) {
			super(sideId);
		}
		@Override
		public boolean checkOverlapShare(CenterVertex c,
				OverlappingPosition o, int relX, int relY, int relZ) {
			return relY == c.y && relZ == c.z;
		}
		@Override
		public int getRelevantCoord(OverlappingPosition o) {
			return o.x;
		}
		
	}
	private class LeftProcessor extends SideProcessor{

		public LeftProcessor(int sideId) {
			super(sideId);
		}
		@Override
		public boolean checkOverlapShare(CenterVertex c,
				OverlappingPosition o, int relX, int relY, int relZ) {
			return relY == c.y && relZ == c.z;
		}
		@Override
		public int getRelevantCoord(OverlappingPosition o) {
			return o.x;
		}
		
	}
	

	
	

	private byte orientation;

	private BlockShapeAlgorithm algo;
	
	
	public void normalize(Occlusion occlusion, int x, int y, int z, byte vis, ElementInformation info){
		
		centerPos.set(x,y,z);
		
		int containIndex = Occlusion.getContainIndex(x, y, z);
		
		this.orientation = occlusion.orientation.get(containIndex);
		algo = null;
		if(info.getBlockStyle() != BlockStyle.NORMAL){
			algo = BlockShapeAlgorithm.getAlgo(info.getBlockStyle(), orientation);
			if(info.hasLod() && info.lodShapeStyle == 1){
				algo = ((Oriencube)algo).getSpriteAlgoRepresentitive();
			}
		}
		/*
		 * each visible side has 4 vertices. We need to calculate the average light for
		 * each one. Vertices of not visible sides can obviously be skipped! 
		 * 
		 * that is a total max of 6*4 = 24 vertices to calculate depending on
		 * adjacent blocks/shapes, a vertex will average over different sides of
		 * the adjacent blocks. Furthermore, corners will create local occlusion
		 */
		
		/*
		 * for each visible vertex, there is a subset of 8 blocks surrounding it.
		 * Obviously it's never all 8, or the vertex would not be visible. 
		 * Glass/transparent blocks count as air in this case and will not
		 * be counted when calculating a vertex of an opaque block
		 */
		
		for(int sideId = Element.SIDE_FLAG.length-1; sideId >= 0; sideId--){
			int flag = Element.SIDE_FLAG[sideId];
			if(vis >= flag){
				normalBlockProcessors[sideId].process(sideId, centerPos, algo, occlusion);
				vis -= flag;
			}
		}
		if(info.getBlockStyle() == BlockStyle.HEPTA){
//			assert(false):algo;
			extraSide.process(6, centerPos, algo, occlusion);
		}
	}


}
