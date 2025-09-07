package org.schema.game.client.view.cubes;

import java.nio.ByteBuffer;
import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.schema.common.FastMath;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.MemoryManager;
import org.schema.common.util.MemoryManager.MemByteArray;
import org.schema.common.util.MemoryManager.MemFloatArray;
import org.schema.common.util.MemoryManager.MemIntArray;
import org.schema.common.util.MemoryManager.MemShortArray;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.game.client.view.cubes.occlusion.Occlusion;
import org.schema.game.client.view.cubes.shapes.AlgorithmParameters;
import org.schema.game.client.view.cubes.shapes.BlockRenderInfo;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm.TexOrderStyle;
import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementInformation.ResourceInjectionType;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.graphicsengine.core.GraphicsContext;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.shader.ShaderLibrary;

import it.unimi.dsi.fastutil.ints.IntArrayList;

public class CubeMeshBufferContainer {

	
	public BlockRenderInfo r = new BlockRenderInfo();
	
	public static final int vertexComponents = ShaderLibrary.CUBE_VERTEX_COMPONENTS;
	public static final int SQUARE_CORNERS = 4;
	//	{0,1,2,3,5,4}, //LEFT
	//	{1,0,3,2,4,5}, //RIGHT
	//
	//	{1,0,5,4,2,3}, //TOP
	//	{0,1,4,5,3,2}, //BOTTOM
	//
	//	{4,5,2,3,0,1}, //FRONT
	//	{5,4,2,3,1,0}, //BACK
	private static final int[][] orientationMapping = new int[][]{

			{5, 4, 3, 2, 0, 1}, //FRONT
			{4, 5, 3, 2, 1, 0}, //BACK

			{1, 0, 5, 4, 2, 3}, //TOP
			{0, 1, 4, 5, 3, 2}, //BOTTOM

			{1, 0, 3, 2, 4, 5}, //RIGHT
			{0, 1, 3, 2, 5, 4}, //LEFT

	};
	private static final int[][] orientationMapping24 = new int[][]{

			{5, 4, 2, 3, 0, 1}, //FRONT
			{4, 5, 2, 3, 1, 0}, //BACK

			{0, 1, 4, 5, 3, 2}, //TOP
			{1, 0, 5, 4, 2, 3}, //BOTTOM

			{1, 0, 3, 2, 4, 5}, //RIGHT
			{0, 1, 2, 3, 5, 4}, //LEFT

	};
	private static final int[] tDim = new int[]{0, 0, 3, 2, 0, 0};
	private static final int[][] orientationMappingThree = new int[][]{

			tDim, //LEFT
			tDim, //RIGHT

			tDim, //TOP
			tDim, //BOTTOM

			tDim, //FRONT
			tDim, //BACK

	};
	
	public MemIntArray blendedElementBuffer;
	public MemShortArray blendedElementTypeBuffer;
	public MemByteArray lightData;
	public MemByteArray visData;
	
	public MemFloatArray lodData;
	public MemShortArray lodTypeAndOrientcubeIndex;
	
	public MemIntArray LOD4Buffer;
	public MemIntArray LOD8Buffer;
	
	public CubeBuffer dataBuffer;
	
	public void putCube(int x, int y, int z, int side, MemIntArray buffer) {
		final int verticesPerSide = 6;
		
		int sideId = side << 2; //3bit (8)
		for(int i = 0; i < verticesPerSide; i++) {
			
			
			buffer.put(x);
			buffer.put(y);
			buffer.put(z);
			
			//encode side and index
			int vertexId = i << 5; //3bit (8)
			
			buffer.put(sideId + vertexId);
			
		}
	}
	
	public ByteBuffer getLod4Buffer() {
		LOD4Buffer.flip();
		return LOD4Buffer.getByteBackingToCurrent();
	}

	public ByteBuffer getLod8Buffer() {
		LOD8Buffer.flip();
		return LOD8Buffer.getByteBackingToCurrent();
	}
	public ByteBuffer GetLod8Buffer() {
		// TODO Auto-generated method stub
		return null;
	}
	public static int DRAW_STYLE = GL11.GL_TRIANGLES;

	private static ElementInformation cargoTextureBlockInBuildMode;
	public static final int VERTS_PER_FACE = isTriangle() ? 6 : 4; //6 vertices are needed if drawn with triangles
	
	
	
	
	
	
	public AlgorithmParameters p = new AlgorithmParameters();
	ByteBuffer b = null;
	
	public Random random = new Random();
	public long seed = 0;
	
	
	
	public IntArrayList beacons;
	public IntArrayList lodShapes = new IntArrayList(SegmentData.BLOCK_COUNT);

	


	public static int getLightInfoIndexFromIndex(int dataIndex) {
		return (dataIndex) * COLOR_COORDS_PER_VERT * (SIDES*VERTICES_PER_SIDE);
	}

	public static int getLightInfoIndex(int x, int y, int z) {
		int i = ((z * SegmentData.SEG_TIMES_SEG)
				+ (y * SegmentData.SEG) + x) * COLOR_COORDS_PER_VERT * (SIDES*VERTICES_PER_SIDE);

		return i;
	}

	public static int getLightInfoIndex(Vector3b pos) {
		return getLightInfoIndex(pos.x, pos.y, pos.z);
	}

	public static byte getOrientationCode3(int sideId, int orientation) {
		return (byte) (5 - (orientationMappingThree[orientation][sideId]));
	}

	public static byte getOrientationCode6(int sideId, int orientation) {

		return (byte) (5 - (orientationMapping[orientation][sideId]));
	}

	public static byte getOrientationCode24(int sideId, int orientation) {
		return (byte) (5 - (orientationMapping24[orientation][sideId]));
	}
	
	public static final int putIndex(CubeMeshBufferContainer container, int index, int dataIndex, SegmentData data, int sideFlag, int sideId, short type) {

		int lightIndex = getLightInfoIndexFromIndex(dataIndex);

		ElementInformation info = ElementKeyMap.getInfo(type);
		int individualSides = info.getIndividualSides();
		boolean animated = info.isAnimated();
		BlockStyle blockStyle = info.getBlockStyle();
		byte orientation = data.getOrientation(dataIndex);
		boolean active = data.isActive(dataIndex);


		int resOverlay = 0;

		int plusTexture = 0;
		if (info.resourceInjection != ResourceInjectionType.OFF) {
			//SegmentData.MAX_ORIENT types of overlay. can be changed per block type
			if (ElementKeyMap.orientationToResOverlayMapping[orientation] > 0) {
				resOverlay = info.resourceInjection.index + ElementKeyMap.orientationToResOverlayMapping[orientation] - 1;
			}
			orientation = 0;
		} 
		int slab = info.getSlab();
		boolean onlyInBuildMode = info.isDrawnOnlyInBuildMode();
		boolean extendedBlockTexture = info.isExtendedTexture();
		if(info.getId() == ElementKeyMap.CARGO_SPACE){
			if(orientation == 4){
//				return 0;
				orientation = Element.TOP;
				onlyInBuildMode = true;
				//change texture to trigger are so it's visible in build mode and transparent else
				if(cargoTextureBlockInBuildMode == null) {
					cargoTextureBlockInBuildMode = ElementKeyMap.getInfoFast(EngineSettings.BLOCK_ID_OF_CARGO_SPACE_BUILD_MODE.getInt());
					if(cargoTextureBlockInBuildMode == null) {
						try {
							throw new Exception("Texture for cargo area could not be set. Id "+EngineSettings.BLOCK_ID_OF_CARGO_SPACE_BUILD_MODE.getInt()+" invalid. check BLOCK_ID_OF_CARGO_SPACE_IN_BUILD_MODE in settings.cfg");
						} catch (Exception e) {
							e.printStackTrace();
						}
						cargoTextureBlockInBuildMode = ElementKeyMap.getInfo(ElementKeyMap.SIGNAL_TRIGGER_AREA);
					}
				}
				info = cargoTextureBlockInBuildMode;
			}else{
				
				slab = orientation;
				if(sideId != Element.TOP && sideId != Element.BOTTOM){
					plusTexture = slab;
				}
				
				container.random.setSeed(container.seed * dataIndex);
				plusTexture += container.random.nextInt(5) * 4;
				orientation = Element.TOP;
			}
		}
		byte orientationCode = 0;

		if (blockStyle == BlockStyle.NORMAL24) { //normal block with 24 orientations
			orientationCode = getOrientationCode24(sideId, orientation / 4);
		} else if (individualSides == 6) {
			orientation = (byte) Math.max(0, Math.min(5, orientation));
			assert (orientation < 6) : "Orientation wrong: " + orientation;
			orientationCode = getOrientationCode6(sideId, orientation);
		} else if (individualSides == 3) {
			orientation = (byte) Math.max(0, Math.min(5, orientation));
			assert (orientation < 6) : "Orientation wrong: " + orientation;
			orientationCode = getOrientationCode3(sideId, orientation);
		}

		short hitpoints = data.getHitpointsByte(dataIndex);
		float hpFac = hitpoints * info.getMaxHitPointsOneDivByByte();
		byte hitPointsCode = 0;

		if (hpFac < 1.0f) {
			hpFac = 1.0f - hpFac;
			hitPointsCode = FastMath.clamp((byte) (hpFac * 7), (byte) 0, (byte) 7);
		}
		byte vis = container.getVisFromDataIndex(dataIndex);

		BlockShapeAlgorithm algo = null;
		if (blockStyle != BlockStyle.NORMAL) {
			algo = BlockShapeAlgorithm.algorithms[blockStyle.id - 1][orientation];
			
			if(info.hasLod() && info.lodShapeStyle == 1){
				algo = algo.getSpriteAlgoRepresentitive();
				vis = Element.FULLVIS;
			}
		}
		if ((vis & sideFlag) == sideFlag) {
			int sideOccId = (sideId * VERTICES_PER_SIDE);

			byte animatedCode = (byte) 0;
			
			if(info.hasLod()){
				//LoD blocks have animated flag and the "only drawn in buildmode" flag to identify them in shader
				animatedCode = (byte) 1;
			}else if (animated) {
				if (individualSides == 3) {
					if (sideId != Element.TOP && sideId != Element.BOTTOM) {
						animatedCode++;
					}
				} else {
					animatedCode++;
				}

			}
			
			byte layer = info.getTextureLayer(active, orientationCode);//(byte)(Math.abs(elementInformation.getTextureId(active) + orientationCode) / 256);
			short typeCode = (short) (info.getTextureIndexLocal(active, orientationCode) + plusTexture);//(short)((elementInformation.getTextureId(active) + orientationCode)%256);
			if(info.isReactorChamberSpecific()){
				typeCode++;
			}
			//			BlockShapeAlgorithm.algorithms[2][0].create(sideId, layer, typeCode, hitPointsCode, animatedCode, lightIndex, sideOccId, index, container);

			int x = ByteUtil.modU256(ByteUtil.divUSeg(data.getSegment().pos.x) + 128);
			int y = ByteUtil.modU256(ByteUtil.divUSeg(data.getSegment().pos.y) + 128);
			int z = ByteUtil.modU256(ByteUtil.divUSeg(data.getSegment().pos.z) + 128);

			float segIndex = z * 65536 + y * 256 + x;
			BlockRenderInfo r = container.r;
			r.sideId = sideId;
			r.layer = layer;
			r.typeCode = typeCode;
			r.hitPointsCode = hitPointsCode;
			r.animatedCode = animatedCode;
			r.lightIndex = lightIndex;
			r.sideOccId = sideOccId;
			r.index = index;
			r.segIndex = segIndex;
			r.orientation = orientation;
			r.halvedFactor = slab;
			r.blockStyle = blockStyle;
			r.container = container;
			r.resOverlay = resOverlay;
			r.onlyInBuildMode = onlyInBuildMode;
			r.extendedBlockTexture = extendedBlockTexture;
			
			r.threeSided = individualSides == 3;
			r.pointToOrientation = 
					info.sideTexturesPointToOrientation ? TexOrderStyle.ORIENT : (info.extendedTexture ? TexOrderStyle.AREA4x4 : TexOrderStyle.NORMAL);
			
			if (blockStyle != BlockStyle.NORMAL) {
				algo.create(r);
			} else {
				BlockShapeAlgorithm.normalBlock(r);
			}

			return VERTS_PER_FACE;
		}

		return 0;
	}

	public static boolean isTriangle() {
		return DRAW_STYLE == GL11.GL_TRIANGLES;
	}

	public static CubeMeshBufferContainer getInstance(MemoryManager man) {
		CubeMeshBufferContainer c = new CubeMeshBufferContainer();
		c.generate(man);
		return c;
	}

	public static void main(String sad[]) {

	}
	
	public static final int lodSides = 6;
	public static final int lodVertexComponents = 4; //four ints per vertex
	public static final int verticesPerSide = 6; //2 triangles
	public static final int possibleCubesD4 = (Occlusion.SEG_D4 * Occlusion.SEG_D4 * Occlusion.SEG_D4);
	public static final int possibleCubesD8 = (Occlusion.SEG_D8 * Occlusion.SEG_D8 * Occlusion.SEG_D8);
	
	public static final int SIDES = 7;
	public static final int VERTICES_PER_SIDE = 4;
	public static final int COLOR_COORDS_PER_VERT = 4+3;
	
	public void generate(MemoryManager man) {
		
		
		
		blendedElementBuffer = man.intArray(CubeInfo.CUBE_COUNT_PER_SEGMENT);
		blendedElementTypeBuffer = man.shortArray(CubeInfo.CUBE_COUNT_PER_SEGMENT);
		lightData = man.byteArray(CubeInfo.CUBE_COUNT_PER_SEGMENT * (SIDES*VERTICES_PER_SIDE) * COLOR_COORDS_PER_VERT);
		visData = man.byteArray(SegmentData.BLOCK_COUNT);
		lodData = man.floatArray(SegmentData.BLOCK_COUNT * SegmentData.lodDataSize);
		lodTypeAndOrientcubeIndex = man.shortArray(SegmentData.BLOCK_COUNT * 2);
		
		LOD4Buffer = man.intArray(possibleCubesD4 * lodSides * verticesPerSide * lodVertexComponents * ByteUtil.SIZEOF_INT);
		LOD8Buffer = man.intArray(possibleCubesD8 * lodSides * verticesPerSide * lodVertexComponents * ByteUtil.SIZEOF_INT);
		
		// half the size, because it's the maximum vertices visible
		beacons = new IntArrayList(SegmentData.BLOCK_COUNT);

		
		
		
		if(GraphicsContext.INTEGER_VERTICES){
			dataBuffer = new CubeBufferInt(man);
		}else{
			dataBuffer = new CubeBufferFloat(man);
		}

		
		
		//		System.err.println("FLOAT BUFFER IS "+(CubeInfo.INDEX_BUFFER_SIZE * vertexComponents * ByteUtil.SIZEOF_FLOAT)/1024+" kb");

		dataBuffer.clearBuffers();
	}

	public byte getFinalLight(int index, int subIndex, int lightCoordinate) {
		return lightData.get(index + subIndex * COLOR_COORDS_PER_VERT + lightCoordinate);
	}

	public byte getVis(int index) {
		return visData.get(index);
	}

	public byte getVis(int x, int y, int z) {
		int index = x + y * SegmentData.SEG + z * SegmentData.SEG_TIMES_SEG;
		return getVis(index);
	}

	public byte getVis(Vector3b pos) {
		return getVis(pos.x, pos.y, pos.z);
	}

	public byte getVisFromDataIndex(int index) {
		return visData.get(index);
	}

	public void reset(){
		visData.zero();
		lightData.zero();
	}
	public void setFinalLight(int index, byte value, int subIndex, int lightCoordinate) {
		lightData.put(index + subIndex * COLOR_COORDS_PER_VERT + lightCoordinate, value);
	}

	public void setVis(byte x, byte y, byte z, byte vis) {
		int index = x + y * SegmentData.SEG + z * SegmentData.SEG_TIMES_SEG;
		setVis(index, vis);

		assert (getVis(x, y, z) == vis);
	}

	public void setVis(int index, byte vis) {
		visData.put(index, vis);
	}

	public void setVis(Vector3b pos, byte vis) {
		setVis(pos.x, pos.y, pos.z, vis);
	}
	


}
