package org.schema.game.client.view.cubes.occlusion;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.schema.common.FastMath;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.MemoryManager;
import org.schema.common.util.MemoryManager.*;
import org.schema.common.util.StringTools;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.view.SegmentDrawer;
import org.schema.game.client.view.cubes.CubeMeshBufferContainer;
import org.schema.game.client.view.cubes.lodshapes.CubeLodShapeInterface;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;
import org.schema.game.common.controller.SegmentBufferOctree;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.SegmentRetrieveCallback;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.DrawableRemoteSegment;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentData4Byte;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.DebugBox;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.Random;

public class Occlusion {
	public static Vector3i dbPos = new Vector3i(1, 9, 9);
	
	public static final float COLOR_PERM = 31;
	public static final byte COLOR_LIGHT_PERM_BYTE = 13;
	private static final int cacheRange = 8;
	private final static short none = 0;
	
	
	public static final int border = 2;
	public static final int segmentSize = SegmentData4Byte.SEG+border*2;
	public static final int segmentSizeX2 = segmentSize * segmentSize;
	public static final int segmentArraySize = segmentSize * segmentSize * segmentSize;
	public static final int minBorder = -border;
	public static final int maxBorder = SegmentData4Byte.SEG+border;
	
	private static final int CRANGE = 3;
	public static final int D4 = 4;
	public static final int D8 = 8;
	public static final int SEG_D4 = SegmentData4Byte.SEG / D4;
	public static final int SEG_D8 = SegmentData4Byte.SEG / D8;
	
	private static final int CLINE 		= CRANGE * SegmentData4Byte.SEG;
	private static final int CLINE_D4 	= CRANGE * (SEG_D4);
	private static final int CLINE_D8 	= CRANGE * (SEG_D8);
	private static final int CLINEX2 	= CLINE*CLINE;
	private static final int CLINEX2_D4 = CLINE_D4*CLINE_D4;
	private static final int CLINEX2_D8 = CLINE_D8*CLINE_D8;
	
	private static final int CCOUNT 	= CLINE*CLINE*CLINE;
	private static final int CCOUNT_D4 	= CLINE_D4*CLINE_D4*CLINE_D4;
	private static final int CCOUNT_D8 	= CLINE_D8*CLINE_D8*CLINE_D8;
	
	public static final MemoryManager man = new MemoryManager();
	
	public static MemIntArray minusOneToSeventeenIndices;
	public static MemBoolArray minusOneToSeventeenValid;
	public static MemIntArray minusOneToSeventeenInfoIndexDiv3;
	public static MemIntArray minusOneToSeventeenOGIndex;
	public static MemBoolArray normalValid;
	public static MemBoolArray allInside;
	private static MemIntArray LOD4_Indices;
	private static MemIntArray LOD8_Indices;
	private static MemIntArray CENTRAL_MAP;
	private static MemIntArray CENTRAL_MAP_D4;
	private static MemIntArray CENTRAL_MAP_D8;
	
	public static Occlusion[] occluders;
	
	
	private MemIntArray centralIndex;
	private MemIntArray centralIndexD4;
	private MemIntArray centralIndexD8;

	MemShortArray contain;
	MemByteArray orientation;
	MemBoolArray active;
	private MemByteArray slab;
	private MemFloatArray occlusion;
	private MemFloatArray gather;
	private MemFloatArray lightDir;
	private MemFloatArray light;
	private MemFloatArray lightDirPerSide;
	private MemByteArray ambience;
	private MemByteArray airBlocksWithNeighbors;
	private MemByteArray affectedBlocksFromAirBlocks;
	
	private MemIntArray dataTmp;
	private MemIntArray cacheMissesByPos;
	
	private MemFloatArray lodDatabySide;//light data for lod

	
	public void initNativeMemory(MemoryManager m) {
		centralIndex = m.intArray(CCOUNT);
		centralIndexD4 = m.intArray(CCOUNT_D4);
		centralIndexD8 = m.intArray(CCOUNT_D8);
		
		
		contain = m.shortArray(segmentArraySize);
		orientation = m.byteArray(segmentArraySize);
		active = m.boolArray(segmentArraySize);
		slab = m.byteArray(segmentArraySize);
		occlusion = m.floatArray(segmentArraySize * 6);
		gather = m.floatArray(segmentArraySize * 6);
		lightDir = m.floatArray(segmentArraySize * 6);
		light = m.floatArray(segmentArraySize * 6 * 4);
		lightDirPerSide = m.floatArray(segmentArraySize * 6 * 3);
		ambience = m.byteArray(segmentArraySize * 6);
		airBlocksWithNeighbors = m.byteArray(segmentArraySize * 3);
		affectedBlocksFromAirBlocks = m.byteArray(segmentArraySize * 4);
		cacheMissesByPos = m.intArray((cacheRange + 1) * (cacheRange + 1) * (cacheRange + 1));
		dataTmp = m.intArray(SegmentData4Byte.TOTAL_SIZE);
		
		lodDatabySide = m.floatArray(4096 * 6 * 4);//light data for lod
		
	}
	
	private final Vector3i posTmp = new Vector3i();
	private final SegmentRetrieveCallback[] cacheByPos = new SegmentRetrieveCallback[(cacheRange + 1) * (cacheRange + 1) * (cacheRange + 1)]; // 0,1,2,3
	private final Vector3i testObj = new Vector3i(-16, 32, 32);
	private final Vector3b helperPosGlobal = new Vector3b();
	
	private static final int getCentralIndex(final int x, final int y, final int z){
		return ((z+SegmentData4Byte.SEG) * CLINEX2 + (y+SegmentData4Byte.SEG) * CLINE + (x+SegmentData4Byte.SEG)); 
	}
	private static final int getCentralIndexD4(final int x, final int y, final int z){
		return ((z+SEG_D4) * CLINEX2_D4 + (y+SEG_D4) * CLINE_D4 + (x+SEG_D4)); 
	}
	private static final int getCentralIndexD8(final int x, final int y, final int z){
		return ((z+SEG_D8) * CLINEX2_D8 + (y+SEG_D8) * CLINE_D8 + (x+SEG_D8)); 
	}
	private static boolean initialized = false;
	public static void initializeOccluders(final int count) {
		if(initialized) {
			return;
		}
		initialized = true;
	
		
		occluders = new Occlusion[count];
		for(int i = 0; i < count; i++) {
			occluders[i] = new Occlusion(i);
			occluders[i].initNativeMemory(man);
		}
		
		minusOneToSeventeenIndices = man.register(new MemIntArray(segmentArraySize));
		minusOneToSeventeenValid =  man.register(new MemBoolArray(segmentArraySize));
		minusOneToSeventeenInfoIndexDiv3 =  man.register(new MemIntArray(segmentArraySize));
		minusOneToSeventeenOGIndex =  man.register(new MemIntArray(segmentArraySize));
		normalValid =  man.register(new MemBoolArray(segmentArraySize));
		allInside =  man.register(new MemBoolArray(segmentArraySize));
		LOD4_Indices =  man.register(new MemIntArray(SEG_D4*SEG_D4*SEG_D4 * (D4*D4*D4)));
		LOD8_Indices =  man.register(new MemIntArray(SEG_D8*SEG_D8*SEG_D8 * (D8*D8*D8)));
		CENTRAL_MAP =  man.register(new MemIntArray(CLINE*CLINE*CLINE));
		CENTRAL_MAP_D4 =  man.register(new MemIntArray(CLINE_D4*CLINE_D4*CLINE_D4));
		CENTRAL_MAP_D8 =  man.register(new MemIntArray(CLINE_D8*CLINE_D8*CLINE_D8));
		
		
		//allocate memory for all statics and all occluders
		man.allocateMemory();
		
	
		/*
		 * initial static calculations to cache certain helper values (removes need to calculate indices, etc)
		 */
		assert(CENTRAL_MAP.length() > 0):CLINE+"; "+CRANGE+"; "+SegmentData4Byte.BLOCK_COUNT+"; "+(CLINE*CLINE*CLINE);
		int local = 0;
		int localSeg4 = 0;
		int localSeg8 = 0;
		for (int zSeg = -1; zSeg < 2; zSeg++) {
			for (int ySeg = -1; ySeg < 2; ySeg++) {
				for (int xSeg = -1; xSeg < 2 ; xSeg++) {
					
					
					
					for(int z = 0; z < SegmentData4Byte.SEG; z++){
						for(int y = 0; y < SegmentData4Byte.SEG; y++){
							for(int x = 0; x < SegmentData4Byte.SEG; x++){
								
								int relX = xSeg * SegmentData4Byte.SEG + x;
								int relY = ySeg * SegmentData4Byte.SEG + y;
								int relZ = zSeg * SegmentData4Byte.SEG + z;
								CENTRAL_MAP.put(local, getCentralIndex(relX, relY, relZ));
								
								assert(CENTRAL_MAP.get(local) < CCOUNT):relX+", "+relY+", "+relZ+"; "+getCentralIndex(relX, relY, relZ)+"; local: "+local;
								
								local++;
							}
						}
					}
					for(int z = 0; z < SEG_D4; z++){
						for(int y = 0; y < SEG_D4; y++){
							for(int x = 0; x < SEG_D4; x++){
								
								int relX = xSeg * SEG_D4 + x;
								int relY = ySeg * SEG_D4 + y;
								int relZ = zSeg * SEG_D4 + z;
								
								CENTRAL_MAP_D4.put(localSeg4, getCentralIndexD4(relX, relY, relZ));
								
								assert(CENTRAL_MAP_D4.get(localSeg4) < CCOUNT_D4):relX+", "+relY+", "+relZ+"; "+getCentralIndexD4(relX, relY, relZ)+"; local: "+localSeg4;
								localSeg4++;
							}
						}
					}
					for(int z = 0; z < SEG_D8; z++){
						for(int y = 0; y < SEG_D8; y++){
							for(int x = 0; x < SEG_D8; x++){
								
								int relX = xSeg * SEG_D8 + x;
								int relY = ySeg * SEG_D8 + y;
								int relZ = zSeg * SEG_D8 + z;
								
								CENTRAL_MAP_D8.put(localSeg8, getCentralIndexD8(relX, relY, relZ));
								
								assert(CENTRAL_MAP_D8.get(localSeg8) < CCOUNT_D8):relX+", "+relY+", "+relZ+"; "+getCentralIndexD4(relX, relY, relZ)+"; local: "+localSeg8;
								localSeg8++;
							}
						}
					}
				}
			}
		}
	
		int p = 0;
		int innerIndex = 0;
		for (int zB = 0; zB < SEG_D4; zB++) {
			for (int yB = 0; yB < SEG_D4; yB++) {
				for (int xB = 0; xB < SEG_D4; xB++) {
			
					
					for (byte z = (byte) (zB*D4); z < (zB*D4)+D4; z++) {
						for (byte y = (byte) (yB*D4); y < (yB*D4)+D4; y++) {
							for (byte x = (byte) (xB*D4); x < (xB*D4)+D4; x++) {
								LOD4_Indices.put(p, SegmentData4Byte.getInfoIndex(x, y, z));
								p++;
							}
						}
					}
					innerIndex++;
				}
			}
		}
	
		p = 0;
		innerIndex = 0;
		for (int zB = 0; zB < SEG_D8; zB++) {
			for (int yB = 0; yB < SEG_D8; yB++) {
				for (int xB = 0; xB < SEG_D8; xB++) {
					
					
					for (byte z = (byte) (zB*D8); z < (zB*D8)+D8; z++) {
						for (byte y = (byte) (yB*D8); y < (yB*D8)+D8; y++) {
							for (byte x = (byte) (xB*D8); x < (xB*D8)+D8; x++) {
								LOD8_Indices.put(p, SegmentData4Byte.getInfoIndex(x, y, z));
								p++;
							}
						}
					}
					innerIndex++;
				}
			}
		}


		int i = 0;
		for (byte z = (byte)minBorder; z < maxBorder; z++) {
			for (byte y = (byte)minBorder; y < maxBorder; y++) {
				for (byte x = (byte)minBorder; x < maxBorder; x++) {
					
					minusOneToSeventeenIndices.put(i, getContainIndex(x, y, z));
					
					minusOneToSeventeenValid.put(i, SegmentData4Byte.valid(x, y, z));
					
					minusOneToSeventeenOGIndex.put(i, getOGIndex(x, y, z));
					
					normalValid.put(i, isInNormalSegment(x, y, z));
					
					allInside.put(i, x < SegmentData4Byte.SEG_MINUS_ONE
							&& y < SegmentData4Byte.SEG_MINUS_ONE
							&& z < SegmentData4Byte.SEG_MINUS_ONE && x >= 1
							&& y >= 1 && z >= 1);

					if (minusOneToSeventeenValid.get(i)) {
						minusOneToSeventeenInfoIndexDiv3.put(i, SegmentData4Byte
								.getInfoIndex(x, y, z));
					}
					i++;
				}
			}
		}
	}
	private static final int[] relativeIndexBySide = new int[]{
		getContainIndex(-border, -border, -(border-1)), -getContainIndex(-border, -border, -(border-1)),
		getContainIndex(-border, -(border-1), -border), -getContainIndex(-border, -(border-1), -border),
		getContainIndex(-(border-1), -border, -border), -getContainIndex(-(border-1), -border, -border),
	};
	
	
	/**
	 * A dummy is used if the adjacent sector is out of bounds
	 */
	private final SegmentRetrieveCallback callback = new SegmentRetrieveCallback();
	private final Vector3i callBackPos = new Vector3i();
	private SegmentRetrieveCallback[] iSegments;
	int debugOG = -1;
	Vector3i ppT = new Vector3i();
	SegmentRetrieveCallback cbTmp = new SegmentRetrieveCallback();
	Vector3i ppTmp = new Vector3i();
	Vector3b helperPosGlobalB = new Vector3b();
	private SegmentData data;
	Random r = new Random();
	// private float[] occlusion;
	private Sample sample;
	private int rayCount = EngineSettings.LIGHT_RAY_COUNT.getInt(); // 128;
	public static final int RAY_LENGTH = 22;
	
	public static final float LIGHT_SCALE = 1.28f;
	
	
	private CubeMeshBufferContainer container;
	private int airBlocksWithNeighborsPointer = 0;
	private boolean failed = false;
	private int iSegmentSize;
	private int iArraySize;
	private int iSize;
	private int iSingleSize;
	private int iSegmentSize2;
	private float percentageDrawn;
	private boolean isNotCompletelyDrawn;
	private final NormalizerNew normalizerNew;
	private final Int2IntOpenHashMap lodIndices = new Int2IntOpenHashMap(SegmentData4Byte.BLOCK_COUNT);
	
	private Occlusion(int id) {
		
		
		for (int i = 0; i < cacheByPos.length; i++) {
			cacheByPos[i] = new SegmentRetrieveCallback();
		}
		lodIndices.defaultReturnValue(-1);
		normalizerNew = new NormalizerNew();
		sample = new Sample(rayCount);
		sample.initRays();

		initSegmentBuffersByRaycount(rayCount);

	}

	private static final float getArray(int ogIndex, MemFloatArray in, int index) {
		return in.get(ogIndex + index);
	}

	// private float[] getOcclusion(int ogIndex, float[] out){
	// return get(ogIndex, occlusion, out);
	// }
	static final int getContainIndex(int x, int y, int z) {
		// plus one: -1 to 17
		return ((z + border) * segmentSizeX2 + (y + border) * segmentSize + (x + border));
	}

	private static final int getOGIndex(int x, int y, int z) {
		return getContainIndex(x, y, z) * 6;
	}

	public static final boolean isInNormalSegment(byte x, byte y, byte z) {
		return (z < SegmentData4Byte.SEG) && (y < SegmentData4Byte.SEG) && (x < SegmentData4Byte.SEG) && (z >= 0) && (y >= 0)
				&& (x >= 0);
	}

	// /**
	// * -2, -1, 0 1, 2
	// * +2 +2, +2 +2 +2
	// * -> 0, 1, 2, 3, 4
	// *
	// *
	// * @param in
	// * @return
	// */
	// private int getCacheIndex(int in){
	// return ByteUtil.div16(in) + cacheFak;
	// }

	public static final boolean normalValid(int x, int y, int z) {
		return (z < SegmentData4Byte.SEG) && (y < SegmentData4Byte.SEG) && (x < SegmentData4Byte.SEG) && (z >= 0) && (y >= 0)
				&& (x >= 0);
	}

	public static final boolean ogValid(byte x, byte y, byte z) {
		return (z < maxBorder) && (y < maxBorder) && (x < maxBorder) && (z >= minBorder) && (y >= minBorder)
				&& (x >= minBorder);
	}

	public static final boolean biggerSegmentValid(int x, int y, int z) {
		return (z < maxBorder) && (y < maxBorder) && (x < maxBorder) && (z >= minBorder) && (y >= minBorder)
				&& (x >= minBorder);
	}


	public static final boolean valid(int x, int y, int z) {
		return (z < SegmentData4Byte.SEG) && (y < SegmentData4Byte.SEG) && (x < SegmentData4Byte.SEG)
				&& (z >= 0) && (y >= 0) && (x >= 0);
	}

	public void initSegmentBuffersByRaycount(int rayCount) {
		iSingleSize = SegmentData4Byte.SEG;
		iSegmentSize = 3;
		assert (iSegmentSize % 2 == 1) : iSize;

		iSegmentSize2 = iSegmentSize * iSegmentSize;

		iArraySize = iSegmentSize * iSegmentSize * iSegmentSize;

		
		
		iSegments = new SegmentRetrieveCallback[iArraySize];

		for (int i = 0; i < iSegments.length; i++) {
			iSegments[i] = new SegmentRetrieveCallback();
		}
	}

	private boolean iNeighbors(byte xIn, byte yIn, byte zIn) {
		
		for (int i = 0; i < 6; i++) {
			if (containsFast(getCentralIndex(
					xIn + Element.DIRECTIONSb[i].x, 
					yIn + Element.DIRECTIONSb[i].y, 
					zIn + Element.DIRECTIONSb[i].z))) {
				return true;
			}
		}
		
		return false;
	}
	
	
	
	
	private void createCentral(){
		centralIndex.zero();
		centralIndexD4.zero();
		centralIndexD8.zero();
		
		for(int segIn = 0; segIn < iSegments.length; segIn++){
			
			int segStartIndex = segIn*SegmentData4Byte.BLOCK_COUNT;
			
			if(iSegments[segIn].state >= 0){
				
				MemIntArray dt = iSegments[segIn].segment.getSegmentData().getAsIntBuffer(dataTmp);
			
				
				for(int inner = 0; inner < SegmentData4Byte.TOTAL_SIZE; inner ++){
					
					int central = CENTRAL_MAP.get(segStartIndex+inner); //already got blockSize in it
					
					centralIndex.put(central, dt.get(inner));
				}	
			}
		}
		
		final int blockCountD4 = SEG_D4 * SEG_D4 * SEG_D4; 
		final int blockCountD8 = SEG_D8 * SEG_D8 * SEG_D8; 
		final int blockCountD4Half = blockCountD4 / 2;
		final int blockCountD8Half = blockCountD8 / 2;
		final int lod4Block = D4*D4*D4;
		final int lod8Block = D8*D8*D8;
		for(int segIn = 0; segIn < iSegments.length; segIn++){
			
			int segDataStartIndexD4 = segIn*blockCountD4;
			int segDataStartIndexD8 = segIn*blockCountD8;
			
			if(iSegments[segIn].state >= 0){
				
//				int[] dt = iSegments[segIn].segment.getSegmentData4Byte().getAsIntBuffer(dataTmp);
				SegmentData d = iSegments[segIn].segment.getSegmentData();
				
				
				
				int p = 0;
				for(int inner = 0; inner < blockCountD4; inner ++){
					int containing = 0;
					for(int i = 0; i < lod4Block; i++) {
						if(d.containsFast(LOD4_Indices.get(p))) {
							containing++;
						}
						p++;
					}
					if(containing > 0 ) {
						int central = CENTRAL_MAP_D4.get(segDataStartIndexD4+inner); //already got blockSize in it
						centralIndexD4.put(central, 1);
					}
				}
				
				p = 0;
				for(int inner = 0; inner < blockCountD8; inner ++){
					int containing = 0;
					for(int i = 0; i < lod8Block; i++) {
						if(d.containsFast(LOD8_Indices.get(p))) {
							containing++;
						}
						p++;
					}
					if(containing > 0 ) {
						int central = CENTRAL_MAP_D8.get(segDataStartIndexD8+inner); //already got blockSize in it
						centralIndexD8.put(central, 1);
					}
				}
				
//				int innerIndex = 0;
//				for (int zB = 0; zB < SEG_D4; zB++) {
//					for (int yB = 0; yB < SEG_D4; yB++) {
//						for (int xB = 0; xB < SEG_D4; xB++) {
//					
//							int containing = 0;
////							System.err.println("CHECKING FROM "+(zB*D4)+" -> "+((zB*D4)+D4));
//							
//							for (byte z = (byte) (zB*D4); z < (zB*D4)+D4; z++) {
//								for (byte y = (byte) (yB*D4); y < (yB*D4)+D4; y++) {
//									for (byte x = (byte) (xB*D4); x < (xB*D4)+D4; x++) {
//										if(d.containsFast(SegmentData4Byte.getInfoIndex(x, y, z))) {
//											containing++;
//										}
//									}
//								}
//							}
//							
//							if(containing > 0 ) {
////								System.err.println("GOT ONE: "+containing);
//								int central = CENTRAL_MAP_D4[segDataStartIndex+innerIndex]; //already got blockSize in it
//								
//								centralIndexD4[central]   = 1;
//							}
//							innerIndex++;
//						}
//					}
//				}	
			}
		}
		
		
	}
	private boolean fillISegments() {
		DrawableRemoteSegment center = (DrawableRemoteSegment)data.getSegment();
		int index = 0;
		for (int z = -1; z < 2; z++) {
			for (int y = -1; y < 2; y++) {
				for (int x = -1; x < 2; x++) {
					ppT.set(center.pos);
					ppT.add(x * SegmentData4Byte.SEG, y * SegmentData4Byte.SEG, z * SegmentData4Byte.SEG);

					iSegments[index].abspos.set(0, 0, 0);
					iSegments[index].pos.set(0, 0, 0);
					iSegments[index].segment = null;
					iSegments[index].state = 0;

					getSegmentController().getSegmentBuffer().get(ppT.x, ppT.y, ppT.z, iSegments[index]);

					index++;
				}
			}
		}
		assert (index == iSegments.length) : index + " / " + iSegments.length + "; " + iSegmentSize;

		if(center.lightTries < 20){
		}
		createCentral();
		return true;
	}

	private final void addGather(int index, float value) {
		gather.add(index, value);
	}
	private final void addLightDir(int index, float x, float y, float z) {
		lightDir.add(index, x);
		lightDir.add(index+1, y);
		lightDir.add(index+2, z);
	}
	private final void addOcclusion(int index, float value) {

		occlusion.add(index, value);
	}
	private void applyAndNormalize(OccludeTime tm) {
		long t = System.nanoTime();
		boolean isVisible = false;
		int i = 0;
		for (byte z = (byte)minBorder; z < maxBorder; z++) {
			for (byte y = (byte)minBorder; y < maxBorder; y++) {
				for (byte x = (byte)minBorder; x < maxBorder; x++) {

					helperPosGlobal.set(x, y, z);
					int containIndex = minusOneToSeventeenIndices.get(i);
					
					short type = contain.get(containIndex);
					int index = 0;
					if (type != 0 && minusOneToSeventeenValid.get(i)
							&& data.containsFast(index = SegmentData4Byte
							.getInfoIndex(x, y, z))) {
						ElementInformation info = ElementKeyMap
								.getInfoFast(FastMath.abs(type));
						byte vis = getVisibilityMask(containIndex,
								helperPosGlobal, info);
						
						container.setVis(
								minusOneToSeventeenInfoIndexDiv3.get(i), vis);
						isVisible = isVisible || vis > 0;

					}
					i++;
				}

			}
		}
		tm.vis += (System.nanoTime() - t);
		t = System.nanoTime();
		i = 0;
		for (byte z = (byte)minBorder; z < maxBorder; z++) {
			for (byte y = (byte)minBorder; y < maxBorder; y++) {
				for (byte x = (byte)minBorder; x < maxBorder; x++) {

					helperPosGlobal.set(x, y, z);
					int containIndex = minusOneToSeventeenIndices.get(i);// getContainIndex(helperPosGlobal.x,
					
					short type = contain.get(containIndex);
					
					if (type <= 0 || (type > 0 && ElementKeyMap.getInfoFast(type).isLightPassOnBlockItself())) {
						// type <= 0 == air or transparent. apply lighting to
						// solid blocks from air blocks

						boolean isInBorder = !normalValid.get(i);
						int containIndexAir = minusOneToSeventeenIndices.get(i);// getContainIndex(helperPosGlobal.x,
						CubeLodShapeInterface cc = null;
						
						for (int sideId = 0; sideId < 6; sideId++) {
							//
							setLightFromAirBlock(helperPosGlobal,
									containIndexAir,
									sideId,
									isInBorder);
							//
						}
					}
					i++;
				}

			}
		}
		tm.apply += (System.nanoTime() - t);
		
		t = System.nanoTime();
		
		if (isVisible) {
			((DrawableRemoteSegment) data.getSegment())
					.setHasVisibleElements(true);
			int infoIndex = 0;
			for (byte z = 0; z < SegmentData4Byte.SEG; z++) {
				for (byte y = 0; y < SegmentData4Byte.SEG; y++) {
					for (byte x = 0; x < SegmentData4Byte.SEG; x++) {
						posTmp.set(x, y, z);
						//TODO AND FUCKING DONT FORGET: this is an easy optiomization by recording what block light was applied to
						short type = data.getType(infoIndex);
						if(type != 0){
							ElementInformation info = ElementKeyMap.getInfoFast(Math.abs(type));
							if(!info.hasLod() || info.lodShapeStyle != 1){
								normalize(posTmp);
							}else{
								for(int si = 0; si < 6; si++){
									testNormalizeFill(posTmp, data, si);
								}
							}
						}
						infoIndex ++;
					}
				}
			}
		} else {
			((DrawableRemoteSegment) data.getSegment())
					.setHasVisibleElements(false);
		}
		tm.normalize += System.nanoTime() - t;
	}
	private OccludeTime timeStats = new OccludeTime();
	public void computeNew(SegmentData4Byte data, CubeMeshBufferContainer container) {
		if(data.getSegment().isEmpty()){
			return;
		}
		if (rayCount != EngineSettings.LIGHT_RAY_COUNT.getInt()) {
			rayCount = EngineSettings.LIGHT_RAY_COUNT.getInt();
			sample = new Sample(rayCount);
			sample.initRays();
			
			initSegmentBuffersByRaycount(rayCount);
		}
		
		this.data = data;
		
		this.container = container;
		r.setSeed(data.getSegment().pos.code());
		this.percentageDrawn = data.getSegment().getSegmentController().percentageDrawn;
		this.isNotCompletelyDrawn = data.getSegment().getSegmentController().percentageDrawn < 1f;
		
		failed = false;
		
		boolean filled = fillISegments();
		
		lodIndices.clear();
		container.lodShapes.clear();
		
		if(filled){
			if(data.getLodShapes() != null){
				container.lodShapes.addAll(data.getLodShapes());
				prepareLod(container.lodShapes, lodIndices);
			}
			
			occludeNew(timeStats);
		}else{
			failed = true;
		}
		if (failed) {
			((DrawableRemoteSegment) data.getSegment()).occlusionFailed = true;
			((DrawableRemoteSegment) data.getSegment()).occlusionFailTime = System
					.currentTimeMillis();
		}
		((DrawableRemoteSegment) data.getSegment()).lightTries++;
	}
	public void compute(SegmentData data, CubeMeshBufferContainer container ) {
		if(data.getSegment().isEmpty()){
			return;
		}
		if (rayCount != EngineSettings.LIGHT_RAY_COUNT.getInt()) {
			rayCount = EngineSettings.LIGHT_RAY_COUNT.getInt();
			sample = new Sample(rayCount);
			sample.initRays();

			initSegmentBuffersByRaycount(rayCount);
		}

		this.data = data;

		this.container = container;
		r.setSeed(data.getSegment().pos.code());
		this.percentageDrawn = data.getSegment().getSegmentController().percentageDrawn;
		this.isNotCompletelyDrawn = data.getSegment().getSegmentController().percentageDrawn < 1f;
		
		failed = false;

		
		boolean filled = fillISegments();
		if(filled){
			if(SegmentDrawer.drawLOD && !failed) {
				
				doLod4(container);
				
				doLod8(container);
			}
			if(SegmentDrawer.drawNormal && !failed) {
				doFull(container);
			}
//			System.err.println("COMPUTING "+container.LOD4Buffer.position());
		}else{
			System.err.println("NOT FILLED");
			failed = true;
		}
		
		if (failed) {
			((DrawableRemoteSegment) data.getSegment()).occlusionFailed = true;
			((DrawableRemoteSegment) data.getSegment()).occlusionFailTime = System
					.currentTimeMillis();
		}
		
		((DrawableRemoteSegment) data.getSegment()).lightTries++;
		
	}
	
	private void doLod4(CubeMeshBufferContainer container) {
		
		
		
		Vector3i pos = data.getSegment().pos;
		for (byte z = 0; z < SEG_D4; z++) {
			for (byte y = 0; y < SEG_D4; y++) {
				for (byte x = 0; x < SEG_D4; x++) {
					
					final int centralIndexI = getCentralIndexD4(x, y, z);
					final int type = centralIndexD4.get(centralIndexI);
					if(type > 0) {
//						System.err.println("FOUND AT "+x+", "+y+", "+z);
//						for(int side = 0; side < 6; side++) {
//							container.putCube((pos.x - Segment.HALF_DIM) + x * D4, (pos.y- Segment.HALF_DIM) + y * D4, (pos.z- Segment.HALF_DIM) + z * D4, side);
//						}

						
						for(int side = 0; side < 6; side++) {
							
							
							final int centralIndexISide = getCentralIndexD4(x+Element.DIRECTIONSi[side].x, y+Element.DIRECTIONSi[side].y, z+Element.DIRECTIONSi[side].z);
							final int typeSide = centralIndexD4.get(centralIndexISide);
							if(typeSide == 0) {
								container.putCube((pos.x - Segment.HALF_DIM) + x * D4, (pos.y- Segment.HALF_DIM) + y * D4, (pos.z- Segment.HALF_DIM) + z * D4, side, container.LOD4Buffer);
							}
						}
						
					}
				}
			}
		}
	}
	private void doLod8(CubeMeshBufferContainer container) {
		
		
		
		Vector3i pos = data.getSegment().pos;
		for (byte z = 0; z < SEG_D8; z++) {
			for (byte y = 0; y < SEG_D8; y++) {
				for (byte x = 0; x < SEG_D8; x++) {
					
					final int centralIndexI = getCentralIndexD8(x, y, z);
					final int type = centralIndexD8.get(centralIndexI);
					if(type > 0) {
//						System.err.println("FOUND AT "+x+", "+y+", "+z);
//						for(int side = 0; side < 6; side++) {
//							container.putCube((pos.x - Segment.HALF_DIM) + x * D8, (pos.y- Segment.HALF_DIM) + y * D8, (pos.z- Segment.HALF_DIM) + z * D8, side, container.LOD8Buffer);
//						}

						
						for(int side = 0; side < 6; side++) {
							
							
							final int centralIndexISide = getCentralIndexD8(x+Element.DIRECTIONSi[side].x, y+Element.DIRECTIONSi[side].y, z+Element.DIRECTIONSi[side].z);
							final int typeSide = centralIndexD8.get(centralIndexISide);
							if(typeSide == 0) {
								container.putCube((pos.x - Segment.HALF_DIM) + x * D8, (pos.y- Segment.HALF_DIM) + y * D8, (pos.z- Segment.HALF_DIM) + z * D8, side, container.LOD8Buffer);
							}
						}
						
					}
				}
			}
		}
	}
	private void doFull(CubeMeshBufferContainer container) {
		lodIndices.clear();
		container.lodShapes.clear();
		if(data.getLodShapes() != null){
			container.lodShapes.addAll(data.getLodShapes());
			prepareLod(container.lodShapes, lodIndices);
		}
		
		occlude(timeStats);
		
		
	}

	public void prepareLod(IntArrayList lodShapes, Int2IntOpenHashMap reference) {
		
		for(int i = 0; i < lodShapes.size(); i++){
			reference.put(lodShapes.getInt(i), i);
		}
	}
	public final short get(byte x, byte y, byte z) {
		return data.getType(x, y, z);
	}

	/**
	 * @return the container
	 */
	public CubeMeshBufferContainer getContainer() {
		return container;
	}

	/**
	 * @param container the container to set
	 */
	public void setContainer(CubeMeshBufferContainer container) {
		this.container = container;
	}

	float getLight(Vector3i pos, int sideId, int coordinate) {
		int index = 0;
		if (biggerSegmentValid(pos.x, pos.y, pos.z)
				&& contain.get(index = getContainIndex(pos.x, pos.y, pos.z)) != 0) {
			// index*6 == ogIndex
			return light.get(index * 6 * 4 + sideId * 4 + coordinate);
		}
		return -1;
	}
	Vector4f getLight(Vector3i pos, int sideId, Vector4f c, Vector3f dir) {
		int index = 0;
		c.set(0,0,0,0);
		dir.set(0,0,0);
		if (biggerSegmentValid(pos.x, pos.y, pos.z)
				&& contain.get(index = getContainIndex(pos.x, pos.y, pos.z)) != 0) {
			// index*6 == ogIndex
			int lindex = index * 6 * 4 + sideId * 4;
			c.set(light.get(lindex + 0),
			light.get(lindex + 1),
			light.get(lindex + 2),
			light.get(lindex + 3));
			
			int lindexDir = index * 6 * 3 + sideId * 3;
			dir.set(
					lightDirPerSide.get(lindexDir + 0), 
					lightDirPerSide.get(lindexDir + 1), 
					lightDirPerSide.get(lindexDir + 2));
			return c;
		}
		return c;
	}


	public final SegmentController getSegmentController() {
		return data.getSegmentController();
	}

	public final byte getVisibilityMask(int containIndex, Vector3b pos,
	                                    ElementInformation info) {

		// containing in data

		// bit mask for all sides visible
		byte mask = 63;
		
		if(info.hasLod()){
			return mask;
		}
		
		// for all six sides of the cube
		boolean selfBlend = contain.get(containIndex) < 0 ;
		if (info.blockStyle.solidBlockStyle) {
			int infoIndex = SegmentData4Byte.getInfoIndex(pos);
			BlockShapeAlgorithm algo = BlockShapeAlgorithm.getAlgo(
					info.getBlockStyle(), data.getOrientation(infoIndex));
			int[] sides = algo.getSidesToCheckForVis();

			for (int i = 0; i < sides.length; i++) {
				if (hasNeighbor(info, sides[i], containIndex, selfBlend, algo, false)) {
					
					// this side is not visible. subtract from mask
					mask -= Element.SIDE_FLAG[sides[i]];
				}
			}
			
			sides = algo.getSidesToTestSpecial();

			for (int i = 0; i < sides.length; i++) {
				if (hasNeighbor(info, sides[i], containIndex, selfBlend, algo, true)) {
					
					// this side is not visible. subtract from mask
					mask -= Element.SIDE_FLAG[sides[i]];
				}
			}
			// mask = 63;
		} else if (info.getBlockStyle() == BlockStyle.SPRITE || (info.hasLod() && info.lodShapeStyle == 1)) {
			// mask -= (Element.FLAG_BOTTOM + Element.FLAG_TOP);
//			byte orientation = data.getOrientation(SegmentData4Byte
//					.getInfoIndex(pos));
			byte orientation = this.orientation.get(containIndex);
			if (orientation == Element.TOP || orientation == Element.BOTTOM) {
				mask = 51;
			} else if (orientation == Element.FRONT
					|| orientation == Element.BACK) {
				mask = 63 - (Element.FLAG_FRONT + Element.FLAG_BACK);
			} else {
				mask = 63 - (Element.FLAG_LEFT + Element.FLAG_RIGHT);
			}
		} else {
			if(this.slab.get(containIndex) == 0){
				for (int i = 0; i < 6; i++) {
					if (hasNeighbor(info, i, containIndex, selfBlend, null, false)) {
						
							// this side is not visible. subtract from mask
							mask -= Element.SIDE_FLAG[i];
					}
				}
			}else{
				byte orientation = Element.switchLeftRight(this.orientation.get(containIndex));
				
				for (int i = 0; i < 6; i++) {
					
					
					if(orientation != i){
						
						if (hasNeighbor(info, i, containIndex, selfBlend, null, false)) {
							// this side is not visible. subtract from mask
							mask -= Element.SIDE_FLAG[i];
						}
					}
				}
				//handle slab vis
			}
		}

		return mask;
	}

	private boolean hasNeighbor(ElementInformation oInfo, int sideId, final int containIndex,
	                            boolean oBlended, BlockShapeAlgorithm ownAlgo, boolean specialSide) {
		final int nContainIndex = containIndex + relativeIndexBySide[sideId];
		final short nType = contain.get(nContainIndex); 
		
		if (nType != 0) {
			final ElementInformation nInfo = ElementKeyMap.getInfoFast(FastMath
					.abs(nType));
			if (nInfo == null) {
				// fixme remove when ok
				throw new NullPointerException("ERROR: info null: " + nInfo
						+ "; type: " + FastMath.abs(nType));
			}
			if(nInfo.hasLod()){
				return false;
			}
			int nSlab = this.slab.get(nContainIndex);
			if(nSlab >= 4){
				return false; //transparent block
			}
			byte nOrientation = this.orientation.get(nContainIndex);
			
			//SLABS
			if(this.slab.get(nContainIndex) > 0){
				if (oBlended && nType < 0) {
					return false; 
				}
				byte oOrientation = this.orientation.get(containIndex);
				if(this.slab.get(containIndex) >= nSlab){
					if(oOrientation == nOrientation && Element.switchLeftRight(nOrientation%6) != Element.getOpposite(sideId) ){
						return true;
					}
				}
				if(Element.switchLeftRight(nOrientation%6) != sideId){
					return false;
				}
			}
			if(oBlended && nType > 0 && 
					oInfo.isNormalBlockStyle() && !oInfo.hasLod() &&
					nInfo.isNormalBlockStyle() && !nInfo.hasLod()
					){
				//both are normal blocks. own block is blended, neighbor is not. no need to draw the other side
				return true;
			}
			if (isVisMaskException(ownAlgo, nInfo, nType, sideId, containIndex, specialSide)) {
				return false;
			}
			
			/*
			 * if the block we are searching neighbors for is blended itself,
			 * don't care, if neighbors are blended itself, otherwise don't hide
			 * side of a solid object, if it's neighbor is blended
			 */
			if (oBlended) {
				return nType < 0; // only accept as neighbor is neighbor
				// is also blended
			}
			if (nType < 0) {
				// only accept as neighbor is neighbor is also blended
				return false; 
			}

			return true;


		}
		return false;
	}

	public boolean isTestObject() {
		return data.getSegment().pos.equals(testObj);
	}

	private final boolean isVisMaskException(BlockShapeAlgorithm ownAlgo, ElementInformation neighborInfo,
	                                         short containIndexType, int sideId, int containIndex, boolean specialSide) {
		if (ElementKeyMap.isInvisible(neighborInfo.getId()) ) {
			return true;
		}
		if (neighborInfo.blockStyle.solidBlockStyle) {
			// int infoIndex = Math.abs(containIndexType);

			
			
			byte orientation = this.orientation.get(containIndex + relativeIndexBySide[sideId]);

			assert (BlockShapeAlgorithm.isValid(neighborInfo.getBlockStyle(),orientation)) : neighborInfo.getName()
					+ " on " + orientation ;
			BlockShapeAlgorithm otherAlgo = BlockShapeAlgorithm.getAlgo(
					neighborInfo.getBlockStyle(), orientation);
			if(neighborInfo.hasLod() && neighborInfo.lodShapeStyle == 1){
				otherAlgo = ((Oriencube)otherAlgo).getSpriteAlgoRepresentitive();
			}
			
			int[] sides = otherAlgo.getSidesToCheckForVis();
			assert (sides != null) : otherAlgo;
			int opposite = Element.getOpposite(sideId);
			for (int i = 0; i < sides.length; i++) {
				if (opposite == sides[i]) {
					return false;
				}
			}
			if(specialSide && ownAlgo == otherAlgo){
				sides = otherAlgo.getSidesToTestSpecial();
				for (int i = 0; i < sides.length; i++) {
					if (opposite == sides[i]) {
						return false;
					}
				}
			}
			return true;
		} else {
			return neighborInfo.getBlockStyle() == BlockStyle.SPRITE || (neighborInfo.hasLod() && neighborInfo.lodShapeStyle == 1);
		}
	}

	

	

	private void normalize(Vector3i pos) {
		byte vis;
		if (contain.get(getContainIndex(pos.x, pos.y, pos.z)) != 0 && (vis = container.getVis(pos.x, pos.y, pos.z)) > 0) {
			int type = Math.abs(contain.get(getContainIndex(pos.x, pos.y, pos.z)));
			ElementInformation info = ElementKeyMap.getInfoFast(type);
			if(info != null) normalizerNew.normalize(this, pos.x, pos.y, pos.z, vis, info);
		}
	}
	private static class OccludeTime{
		long occludeTmBlock;
		long vis;
		long apply;
		long normalize;
		long lod;
		double occludeTmbBlockAvg;
		double visAvg;
		double applyAvg;
		double normalizeAvg;
		double lodAvg;
		public void reset(){
			occludeTmBlock = 0;
			vis = 0;
			apply = 0;
			normalize = 0;
			lod = 0;
		}
		public void div(double testCount) {
			occludeTmbBlockAvg = (occludeTmBlock/testCount)/1000000d;
			visAvg = (vis/testCount)/1000000d;
			applyAvg = (apply/testCount)/1000000d;
			normalizeAvg = (normalize/testCount)/1000000d;
			lodAvg = (lod/testCount)/1000000d;
			
			occludeTmBlock /= 1000000L;
			vis /= 1000000L;
			apply /= 1000000L;
			normalize /= 1000000l;
			lod /= 1000000L;
		}
		@Override
		public String toString() {
			return "OccludeTime [occludeBlock=" + occludeTmBlock + "("+StringTools.formatPointZero(occludeTmbBlockAvg)+"), vis=" + vis + "("+StringTools.formatPointZero(visAvg)+"), apply=" + apply + ",("+StringTools.formatPointZero(applyAvg)+") normalize="
					+ normalize + ",("+StringTools.formatPointZero(normalizeAvg)+") lod=" + lod + "("+StringTools.formatPointZero(lodAvg)+")]";
		}
		
		
	}
	
	private static final int[] side3 = new int[]{Element.RIGHT, Element.TOP, Element.FRONT};
	private static final int[] side3Opp = new int[]{Element.LEFT, Element.BOTTOM, Element.BACK};
	private void occludeNew(OccludeTime tm) {
		//do vis first
		
		final byte minX = (byte)Math.max((byte)-1, (byte) (data.getMin().x - 2));
		final byte minY = (byte)Math.max((byte)-1, (byte) (data.getMin().y - 2));
		final byte minZ = (byte)Math.max((byte)-1, (byte) (data.getMin().z - 2));

		//bb for data is always maxExcluding (0...16 is full)
		final byte maxX = (byte)Math.min((byte)SegmentData4Byte.SEG+1, (byte) (data.getMax().x + 2));
		final byte maxY = (byte)Math.min((byte)SegmentData4Byte.SEG+1, (byte) (data.getMax().y + 2));
		final byte maxZ = (byte)Math.min((byte)SegmentData4Byte.SEG+1, (byte) (data.getMax().z + 2));
		
		for (byte z = minZ; z < maxZ; z++) {
			for (byte y = minY; y < maxY; y++) {
				for (byte x = minX; x < maxX; x++) {
					
					final int centralIndexI = getCentralIndex(x, y, z);
					final int type = centralIndex.get(centralIndexI);
					
					if((type & SegmentData4Byte.typeMask) != 0){
						//there is a block in this position
						
						for(int s = 0; s < 3; s++){
							final int sideId = side3[s]; 
							final int sideOpp = side3Opp[s];
							
						}
					}
//					
//					helperPosGlobal.set(x, y, z);
//					int containIndex = minusOneToSeventeenIndices[i];// getContainIndex(helperPosGlobal.x,
//					
//					short type = contain.get(containIndex];
//					int index = 0;
//					if (type != 0 && minusOneToSeventeenValid[i]
//							&& data.containsFast(index = SegmentData4Byte
//							.getInfoIndex(x, y, z))) {
//						ElementInformation info = ElementKeyMap
//								.getInfoFast(FastMath.abs(type));
//						byte vis = getVisibilityMask(containIndex,
//								helperPosGlobal, info);
//						
//						getContainer().setVis(
//								minusOneToSeventeenInfoIndexDiv3[i], vis);
//						isVisible = isVisible || vis > 0;
//
//					}
//					i++;
				}
			}
		}
		
	}
	private void occlude(OccludeTime tm) {
		
		long t = System.nanoTime();
		container.beacons.clear();
		assert (container.beacons.isEmpty());


		final byte minX = (byte)Math.max((byte)-1, (byte) (data.getMin().x - 2));
		final byte minY = (byte)Math.max((byte)-1, (byte) (data.getMin().y - 2));
		final byte minZ = (byte)Math.max((byte)-1, (byte) (data.getMin().z - 2));

		//bb for data is always maxExcluding (0...16 is full)
		final byte maxX = (byte)Math.min((byte)SegmentData4Byte.SEG+1, (byte) (data.getMax().x + 2));
		final byte maxY = (byte)Math.min((byte)SegmentData4Byte.SEG+1, (byte) (data.getMax().y + 2));
		final byte maxZ = (byte)Math.min((byte)SegmentData4Byte.SEG+1, (byte) (data.getMax().z + 2));

		for (byte z = minZ; z < maxZ; z++) {
			for (byte y = minY; y < maxY; y++) {
				for (byte x = minX; x < maxX; x++) {
					int index = (z + border) * segmentSizeX2 + (y + border) * segmentSize + (x + border);
					occludeBlock(x, y, z, index);
					
					int type;
					ElementInformation info;
					if(!normalValid.get(index) && border > 1 && (type = Math.abs(contain.get(index))) != 0 && (info = ElementKeyMap.getInfoFast(type)).blockStyle.solidBlockStyle ){
						byte xBef = x;
						byte yBef = y;
						byte zBef = z;
						
						if(x < 0){
							x-=1;
							index = (z + border) * segmentSizeX2 + (y + border) * segmentSize + (x + border);
							occludeBlock(x, y, z, index);
							x = xBef;
						}else if(x == SegmentData4Byte.SEG){
							x+=1;
							index = (z + border) * segmentSizeX2 + (y + border) * segmentSize + (x + border);
							occludeBlock(x, y, z, index);
							x = xBef;
						}
						
						if(y < 0){
							y-=1;
							index = (z + border) * segmentSizeX2 + (y + border) * segmentSize + (x + border);
							occludeBlock(x, y, z, index);
							y = yBef;
						}else if(y == SegmentData4Byte.SEG){
							y+=1;
							index = (z + border) * segmentSizeX2 + (y + border) * segmentSize + (x + border);
							occludeBlock(x, y, z, index);
							y = yBef;
						}
						if(z < 0){
							z-=1;
							index = (z + border) * segmentSizeX2 + (y + border) * segmentSize + (x + border);
							occludeBlock(x, y, z, index);
							z = zBef;
						}else if(z == SegmentData4Byte.SEG){
							
							z+=1;
							index = (z + border) * segmentSizeX2 + (y + border) * segmentSize + (x + border);
							occludeBlock(x, y, z, index);
							z = zBef;
						}
						
						
						assert(x == xBef);
						assert(y == yBef);
						assert(z == zBef);
						
					}
				}
			}
		}
		
		tm.occludeTmBlock += System.nanoTime() - t;
		
		//		Arrays.fill(occlusion, 1.0f);
		if (EngineSettings.P_PHYSICS_DEBUG_ACTIVE.isOn() && EngineSettings.P_PHYSICS_DEBUG_ACTIVE_OCCLUSION.isOn()) {
			if (failed) {

				DebugBox b = new DebugBox(
						new Vector3f(data.getSegment().pos.x - SegmentData4Byte.SEG_HALF - 0.1f,
								data.getSegment().pos.y - SegmentData4Byte.SEG_HALF - 0.1f, data.getSegment().pos.z - SegmentData4Byte.SEG_HALF),
						new Vector3f(data.getSegment().pos.x + SegmentData4Byte.SEG_HALF - 0.1f,
								data.getSegment().pos.y + SegmentData4Byte.SEG_HALF + 0.1f, data.getSegment().pos.z + SegmentData4Byte.SEG_HALF + 0.1f),
						getSegmentController().getWorldTransformOnClient(),
						1, 0, 0, 1);
				DebugDrawer.boxes.add(b);
			} else {
				DebugBox b = new DebugBox(
						new Vector3f(data.getSegment().pos.x - SegmentData4Byte.SEG_HALF - 0.1f,
								data.getSegment().pos.y - SegmentData4Byte.SEG_HALF - 0.1f, data.getSegment().pos.z - SegmentData4Byte.SEG_HALF - 0.1f),
						new Vector3f(data.getSegment().pos.x + SegmentData4Byte.SEG_HALF + 0.1f,
								data.getSegment().pos.y + SegmentData4Byte.SEG_HALF + 0.1f, data.getSegment().pos.z + SegmentData4Byte.SEG_HALF + 0.1f),
						getSegmentController().getWorldTransformOnClient(),
						1, 1, 1, 1);
				DebugDrawer.boxes.add(b);
			}
		}
		applyAndNormalize(tm);
		
		t = System.nanoTime();
		if(data.getLodShapes() != null){
			try{
				final int size = data.getLodShapes().size();
				for(int j = 0; j < size; j++){
					int infoIndex = data.getLodShapes().getInt(j);
					data.calculateLodLight(j, infoIndex, lodDatabySide, container.lodData, container.lodTypeAndOrientcubeIndex);
				}
			}catch(Exception e){
				e.printStackTrace();
				failed = true;
				return;
			}
		}
		tm.lod += System.nanoTime() - t;
		//		System.err.println("-------------------------------------- "+data+": Cache: "+cacheHits+"; "+cacheMisses+" --- "+cacheRepeatMisses);
		// System.err.println("Occlusion DONE: "+data.getSegment().pos+" "+data.getSegmentController()+" Arrays: "+timeArrays+"; ResetOcclusion: "+timeResetOcclusion+"; Occ "+timeOcc+"; Norm: "+timeNorm);
	}

	public void getRelativePos(int x, int y, int z, Vector3b posInSeg, SegmentRetrieveCallback out) {

		//bring coordinates relative to 0 to calc the index
		int mX = ByteUtil.divUSeg(x + iSingleSize); //divUSeg
		int mY = ByteUtil.divUSeg(y + iSingleSize);
		int mZ = ByteUtil.divUSeg(z + iSingleSize);

		int index = mZ * iSegmentSize2 + mY * iSegmentSize + mX;

		SegmentRetrieveCallback segmentRetrieveCallback = iSegments[index];

		int xInSeg = ByteUtil.modUSeg(x);
		int yInSeg = ByteUtil.modUSeg(y);
		int zInSeg = ByteUtil.modUSeg(z);

		posInSeg.set((byte) xInSeg, (byte) yInSeg, (byte) zInSeg);

		out.pos.set(segmentRetrieveCallback.pos);
		out.segment = segmentRetrieveCallback.segment;
		out.state = segmentRetrieveCallback.state;
	}

	public void getRelativePos(int x, int y, int z, SegmentRetrieveCallback out) {

		//bring coordinates relative to 0 to calc the index
		int mX = (x + iSingleSize) >> 4; //divU16
		int mY = (y + iSingleSize) >> 4;
		int mZ = (z + iSingleSize) >> 4;

		int index = mZ * iSegmentSize2 + mY * iSegmentSize + mX;

		SegmentRetrieveCallback segmentRetrieveCallback = iSegments[index];

		out.pos.set(segmentRetrieveCallback.pos);
		out.segment = segmentRetrieveCallback.segment;
		out.state = segmentRetrieveCallback.state;
	}

	// private void setArray(int ogIndex, float[] in, int index, float val){
	// in[ogIndex+index] = val;
	// }
	public boolean isActive(int dataIndex) {
		return (centralIndex.get(dataIndex) & SegmentData4Byte.activationMask) > 0;
	}
	public byte getOrientation(int dataIndex) {
		return (byte) ((SegmentData4Byte.orientationMask & centralIndex.get(dataIndex)) >> SegmentData4Byte.orientationIndexStart);
	}
	
	private void occludeBlock(final byte x, final byte y, final byte z, final int pIndex) {
		int containIndex = minusOneToSeventeenIndices.get(pIndex);
		contain.put(containIndex, none);
		orientation.put(containIndex, (byte)none);
		slab.put(containIndex,  (byte)none);
		
		int centralIndex = getCentralIndex(x, y, z);
		short type = Element.TYPE_NONE;
		boolean act = false;
		if(containsFast(centralIndex) && (!isNotCompletelyDrawn || r.nextFloat() <= percentageDrawn)) { // possible NUllPointer

			type = getType(centralIndex);

			act = isActive(centralIndex);
			
			ElementInformation info = ElementKeyMap.getInfoFast(type);

			//only add when active
			if (act && normalValid.get(pIndex) && info.beacon) {
				// only add beacon if block is in normal range
				container.beacons.add(SegmentData4Byte.getInfoIndex(x, y, z));
			}

			
			byte orientation = getOrientation(centralIndex);

			if(normalValid.get(pIndex) && !((DrawableRemoteSegment)data.getSegment()).exceptDockingBlock || 
					type != ElementKeyMap.SHIPYARD_CORE_POSITION){
			
				if (info.isBlended() || info.isBlendBlockStyle()
						|| (ElementInformation.isBlendedSpecial(type, act))) {
	
					contain.put(containIndex,  (short) -type);
					
				} else {
					contain.put(containIndex,  type);
				}
				this.active.put(containIndex,  act);
				
				this.orientation.put(containIndex,  type == ElementKeyMap.CARGO_SPACE ? Element.TOP : orientation);
				
				this.slab.put(containIndex, (byte) info.getSlab(orientation));
			}

		}
		int xoff, yoff, zoff;
		boolean collided;

		ElementInformation infoAir = null;
		
		boolean airOrSolidThatNeedsInnerLight = ((type == Element.TYPE_NONE
				|| (infoAir = ElementKeyMap.getInfoFast(type)).isBlended() || 
				infoAir.isLightPassOnBlockItself() || 
				(infoAir.hasLod() && infoAir.lodShapeStyle == 1) ||
				!infoAir.isPhysical(act)));
		
		if (airOrSolidThatNeedsInnerLight && iNeighbors(x, y, z)) {

			
			airBlocksWithNeighbors.put(airBlocksWithNeighborsPointer + 0, x);
			airBlocksWithNeighbors.put(airBlocksWithNeighborsPointer + 1, y);
			airBlocksWithNeighbors.put(airBlocksWithNeighborsPointer + 2, z);
			airBlocksWithNeighborsPointer += 3;

			int ogIndex = minusOneToSeventeenOGIndex.get(pIndex);// getOGIndex(x,y,z);

			callBackPos.set(callback.pos);
			
			
			boolean calcFromInner = infoAir != null && infoAir.isLightPassOnBlockItself() && infoAir.isPhysical(act) && !infoAir.isBlended();
			
			for (int r = 0; r < sample.rays.length; r++) {
				Ray ray = sample.rays[r];
				collided = false;
				
				if(calcFromInner){
					int firstRayDir = Element.getDirectionFromCoords(ray.points[0], ray.points[1], ray.points[2]);
					if(this.slab.get(containIndex) == 0){
						BlockShapeAlgorithm algo = BlockShapeAlgorithm.getAlgo(infoAir.getBlockStyle(), this.orientation.get(containIndex));
						if(infoAir.hasLod() && infoAir.lodShapeStyle == 1){
							algo = ((Oriencube)algo).getSpriteAlgoRepresentitive();
						}
						boolean ok = false;
						for(int s : algo.getSidesAngled() ){
							if(s == firstRayDir){
								ok = true;
								break;
							}
						}
						for(int s : algo.getSidesOpenToAir() ){
							if(s == firstRayDir){
								ok = true;
								break;
							}
						}
						if(!ok){
							continue;
						}
					}
					else{
						//SLABS
						if(firstRayDir == Element.switchLeftRight(Element.getOpposite(orientation.get(containIndex)))){
							//ray wants to pass through the slab solid part
							continue;
						}
					}
					
					
					
				}
				
				for (int i = 0, d = 0; i < ray.points.length; i += 3, d++) {

					
					
					xoff = (x + ray.points[i + 0]);
					yoff = (y + ray.points[i + 1]);
					zoff = (z + ray.points[i + 2]);


					int rayCentralIndex = getCentralIndex(xoff, yoff, zoff);


					short obstacleType = getType(rayCentralIndex);

					if (obstacleType != Element.TYPE_NONE) {

						ElementInformation obstacleInfo = ElementKeyMap
								.getInfo(obstacleType);
						
						boolean rayBlockActive = isActive(rayCentralIndex); 
						byte rayBlockOrientation = getOrientation(rayCentralIndex); 

						boolean lightPassable = obstacleInfo.isBlended() || 
								!obstacleInfo.isPhysical(rayBlockActive) || 
						(isAlgoLightPassable(containIndex, i, obstacleInfo, rayBlockOrientation, rayBlockActive, ray))|| 
								( obstacleInfo.isLightSource() && (obstacleInfo.getBlockStyle() == BlockStyle.SPRITE || obstacleInfo.hasLod()));

						
						if (obstacleInfo.isLightSource()
								&& (rayBlockActive)) { 
							
							Vector4f lightColor = obstacleInfo.getLightSourceColor();
							float f = ray.depths[d] * 2.5f * lightColor.w;
							
							addGather(ogIndex, lightColor.x * f);
							addGather(ogIndex + 1, lightColor.y * f);
							addGather(ogIndex + 2, lightColor.z * f);
							addLightDir(ogIndex, f*ray.points[i + 0], f*ray.points[i + 1], f*ray.points[i + 2]);
						}

						if (!lightPassable) {
							collided = true;
							break;
						}

					}

				}
				if (!collided) {
					for (int i = 0; i < 6; i++) {
						// add rays
						addOcclusion(ogIndex + i, ray.data[i]);
					}
				}

			}

			//normalize occlusion
			for (int i = 0; i < 6; i++) {
				int index = ogIndex + i;
				float bef = occlusion.get(index);
				occlusion.put(index, (bef * sample.dataInv[i]));
			}

		}
	}
	public boolean containsFast(int index) {
		return (centralIndex.get(index) & SegmentData4Byte.typeMask) != 0;
	}
	public short getType(int index) {
		return (short) (SegmentData4Byte.typeMask & centralIndex.get(index));
	}
	private boolean isAlgoLightPassable(int originContainIndex, int i, ElementInformation obstacleInfo,
			byte orientation, boolean active, Ray ray) {
		
		ElementInformation originInfo;
		if(i == 0 && contain.get(originContainIndex) != 0 && (originInfo = ElementKeyMap.getInfoFast(Math.abs(contain.get(originContainIndex)))).isLightPassOnBlockItself()){
			int firstRayDir = Element.getDirectionFromCoords(ray.points[0], ray.points[1], ray.points[2]);
		
			
			
			
			
			if(originInfo.slab != 0){ 
//				if(firstRayDir != orientation ){
//					return true;
//				}else{
					return false;
//				}
			
			}else{
				BlockShapeAlgorithm algo = BlockShapeAlgorithm.getAlgo(originInfo.getBlockStyle(), this.orientation.get(originContainIndex));
				
				for(int s : algo.getSidesAngled() ){
					if(s == firstRayDir){
						return true;
					}
				}
				return false;
			}
		}else if (i > 0){
//			int rayDir = Element.getDirectionFromCoords(ray.points[i-3] - ray.points[i], ray.points[(i+1)-3]- ray.points[(i+1)] , ray.points[(i+2)-3]-ray.points[(i+2)]);
		
//			if(info.slab != 0){ 
//				
//				ray.currentVisFlag = ;
//				boolean blocked = rayDir == orientation || rayDir == Element.getOpposite(orientation);
//				System.err.println("RAY DIR:::: "+i+" --> "+Element.getSideString(rayDir)+" ("+rayDir+") _____ "+blocked);
//				if(blocked){
//					//we crossed a full quad face. its an obsicle from either side
//					return false;
//				}else{
//					return true;
//				}
//			}
		}
		
		return false;
	}

	public void reset(SegmentData segmentData,
	                  CubeMeshBufferContainer containerFromPool) {

		// private ShortBuffer contain =
		// BufferUtils.createShortBuffer(18*18*18);

		// private FloatBuffer occlusion =
		// MemoryUtil.memAllocFloat(18*18*18*6);
		// private FloatBuffer gather =
		// MemoryUtil.memAllocFloat(18*18*18*6);
		// private FloatBuffer ambience =
		// MemoryUtil.memAllocFloat(18*18*18*6);

		// private FloatBuffer light =
		// MemoryUtil.memAllocFloat(18*18*18*6*3);
		byte b = 0;

		// for(int i = 0; i < (18*18*18); i++){
		// contain.put(i,n);
		// }
		containerFromPool.seed = (1 + segmentData.getSegment().pos.hashCode());
		occlusion.zero();
		gather.zero();
		ambience.zero();
		airBlocksWithNeighbors.zero();
		affectedBlocksFromAirBlocks.zero();
		light.zero();
		lightDir.zero();
		lightDirPerSide.zero();
		lodDatabySide.zero();
		cacheMissesByPos.zero();
		containerFromPool.lodData.zero();
		containerFromPool.lodTypeAndOrientcubeIndex.zero();

		airBlocksWithNeighborsPointer = 0;
		// for(int i = 0; i < (18*18*18*6); i++){
		// occlusion.put(i,0);
		// gather.put(i,0);
		// ambience.put(i,b);
		// }
		// for(int i = 0; i < (18*18*18*6*3); i++){
		// light.put(i,0);
		// }
		for (int i = 0; i < cacheByPos.length; i++) {
			cacheByPos[i].segment = null;
			cacheByPos[i].state = SegmentBufferOctree.NOTHING;
		}
		// Arrays.fill(occlusion, 0);
		// Arrays.fill(gather, 0);
		// Arrays.fill(light, 0);
		// Arrays.fill(ambience, (byte)0);
		containerFromPool.reset();

	}
	public boolean isLightSourceOn(ElementInformation obstacleInfo, SegmentData segmentData, int infoIndex){
		return obstacleInfo.isLightSource() && segmentData.isActive(infoIndex);
	}
	// private float[] getGather(int ogIndex, float[] out){
	// return get(ogIndex, gather, out);
	// }
	private void setLight(int ogIndex, float R, float G, float B, float dirX, float dirY, float dirZ,
	                      float occlusion, int sideId) {
		float occ = occlusion;
		float xL = R;
		float yL = G;
		float zL = B;
		int index = ogIndex * 6 * 4 + sideId * 4;
		light.put(index + 0, xL);
		light.put(index + 1, yL);
		light.put(index + 2, zL);
		light.put(index + 3, occ);
		
		int indexDir = ogIndex * 6 * 3 + sideId * 3;
		lightDirPerSide.put(indexDir + 0, dirX); 
		lightDirPerSide.put(indexDir + 1, dirY); 
		lightDirPerSide.put(indexDir + 2, dirZ); 
	}

	private void setLightFromAirBlock(Vector3b origPos, final int containIndexAir,
	                                  final int sideId, final boolean isInBorder) {

		
		final Vector3b dir = Element.DIRECTIONSb[sideId];
		final int oppositeDir = Element.OPPOSITE_SIDE[sideId];
		/*
		 * compute a block position next to the air block
		 */
		int adjacentX = (origPos.x + dir.x);
		int adjacentY = (origPos.y + dir.y);
		int adjacentZ = (origPos.z + dir.z);

		if (!isInBorder || biggerSegmentValid(adjacentX, adjacentY, adjacentZ)) {
			// no recalculation of index needed
			int adjContainIndex = containIndexAir + relativeIndexBySide[sideId]; 
			assert(adjContainIndex >= 0):containIndexAir+" + "+relativeIndexBySide[sideId]+"; Side: "+sideId;
			final int adjType = contain.get(adjContainIndex);
			if (adjType != 0) {

				
				int ogAirIndex = containIndexAir * 6;
				float occ = 0;
				float gatR = Math.min(1f, getArray(ogAirIndex, gather, 0));
				float gatG = Math.min(1f, getArray(ogAirIndex, gather, 1));
				float gatB = Math.min(1f, getArray(ogAirIndex, gather, 2));
				
				float xDir = Math.min(1f, getArray(ogAirIndex, lightDir, 0));
				float yDir = Math.min(1f, getArray(ogAirIndex, lightDir, 1));
				float zDir = Math.min(1f, getArray(ogAirIndex, lightDir, 2));

				occ = getArray(ogAirIndex, occlusion, sideId);

//				if(data.getSegment().pos.equals(0, -16, 16)){
//					
//					System.err.println("id# "+id+" FROM "+data.getSegment().pos+" SET "+sideId+" LIGHT TO "+origPos+" --> "+adjacentX+", "+adjacentY+", "+adjacentZ+" :::: "+ElementKeyMap.getInfoFast(Math.abs(type)));
//				}
				setLight(adjContainIndex, gatR, gatG, gatB, xDir, yDir, zDir, occ, oppositeDir);
				int lodIndex;
				int infoIndex;
				
//				
				if(SegmentData4Byte.valid(adjacentX, adjacentY, adjacentZ) && ElementKeyMap.getInfoFast(Math.abs(adjType)).hasLod() && (lodIndex = lodIndices.get(infoIndex = SegmentData4Byte.getInfoIndex(adjacentX, adjacentY, adjacentZ))) >= 0){
					setLodData(infoIndex, lodIndex, sideId, gatR, gatG, gatB, occ);
				}
			}
		}
	}


	private void setLodData(int infoIndex, int lodIndex, int sideId,
			float gatR, float gatG, float gatB, float occ) {
		int startIndex = lodIndex*(6*4) + sideId * 4;
		lodDatabySide.put(startIndex + 0, gatR);
		lodDatabySide.put(startIndex + 1, gatG);
		lodDatabySide.put(startIndex + 2, gatB);
		lodDatabySide.put(startIndex + 3, occ);
	}

	@SuppressWarnings("unused")
	private void testNormalizeFill(Vector3i pos, SegmentData data, int sideId) {
		int lightInfoIndex = CubeMeshBufferContainer.getLightInfoIndex(
				(byte) pos.x, (byte) pos.y, (byte) pos.z);
		boolean debug = false;
		if (data.getSegment()
				.getAbsoluteElemPos(new Vector3b(pos.x, pos.y, pos.z),
						new Vector3i()).equals(4, 18, 39)) {
			debug = true;

		}
		int vertexSubIndex = sideId * 4;

		for (int i = 0; i < 4; i++) {

			for (int rbgIndex = 0; rbgIndex < 4; rbgIndex++) {
				float lGet = getLight(pos, sideId, rbgIndex);
				if (debug) {
					System.err
							.println("###---SIDE "
									+ Element.getSideString(sideId)
									+ " OCCLUSION :: "
									+ rbgIndex
									+ " -> "
									+ lGet
									+ "; op: "
									+ getOGIndex(pos.x, pos.y, pos.z)
									+ ";;; totalIndex: "
									+ (getOGIndex(pos.x, pos.y, pos.z) + sideId
									* 3 + rbgIndex));
				}

				byte l = (byte) (FastMath.clamp(lGet, 0, 1) * 15);

				container.setFinalLight(lightInfoIndex, l,
						vertexSubIndex + i, rbgIndex);
			}
		}
	}

	/**
	 * @return the failed
	 */
	public boolean isFailed() {
		return failed;
	}

	/**
	 * @param failed the failed to set
	 */
	public void setFailed(boolean failed) {
		// if(failed){
		// if(Keyboard.isKeyDown(GLFW.GLFW_KEY_SPACE)){
		// try{
		// throw new
		// NullPointerException("ADDING: "+this+", "+this.getSegmentController());
		// }catch(Exception xx){
		// xx.printStackTrace();
		// }
		// }
		// }
		this.failed = failed;
	}

	public static boolean isNormnew() {
		return EngineSettings.CUBE_LIGHT_NORMALIZER_NEW_M.isOn();
	}

	// void add_ray(Cell cell, Ray ray){
	// cell.sides[SimplePosElement.LEFT] += ray.left;
	// cell.sides[SimplePosElement.RIGHT] += ray.right;
	// cell.sides[SimplePosElement.TOP] += ray.top;
	// cell.sides[SimplePosElement.BOTTOM] += ray.bottom;
	// cell.sides[SimplePosElement.FRONT] += ray.front;
	// cell.sides[SimplePosElement.BACK] += ray.back;
	// }
	//
	// void normalize(Cell cell, Sample sample){
	// cell.sides[SimplePosElement.LEFT] = 1 - cell.sides[SimplePosElement.LEFT]
	// / sample.left;
	// cell.sides[SimplePosElement.RIGHT] = 1 -
	// cell.sides[SimplePosElement.RIGHT] / sample.right;
	// cell.sides[SimplePosElement.TOP] = 1 - cell.sides[SimplePosElement.TOP] /
	// sample.top;
	// cell.sides[SimplePosElement.BOTTOM] = 1 -
	// cell.sides[SimplePosElement.BOTTOM] / sample.bottom;
	// cell.sides[SimplePosElement.FRONT] = 1 -
	// cell.sides[SimplePosElement.FRONT] / sample.front;
	// cell.sides[SimplePosElement.BACK] = 1 - cell.sides[SimplePosElement.BACK]
	// / sample.back;
	// }
	// public float[] getOcclusion() {
	// return occlusion;
	// }
	// void gatherBlock(int size, byte* data, Color* gathered, Sample* sample){
	// int xoff, yoff, zoff, value, r, p, obstacle;
	// Color* color;
	// Ray *ray, *rayend;
	// Offset *off, *offend;
	// float distance;
	//
	// foreach_xyz(0, size)
	// value = get(x,y,z);
	// if(value == 0 && neighbors(size, data, x, y, z)){
	// color = gathered + index(x,y,z);
	// for(ray=sample->rays, rayend=sample->rays+ray_count; ray<rayend; ray++){
	// for(off=ray->points, offend=ray->points+point_count; off<offend; off++){
	// xoff = x+off->x;
	// yoff = y+off->y;
	// zoff = z+off->z;
	// if(xoff < 0 || xoff >= size){
	// break;
	// }
	// else if(yoff < 0 || yoff >= size){
	// break;
	// }
	// else if(zoff < 0 || zoff >= size){
	// break;
	// }
	// else{
	// obstacle = get(xoff, yoff, zoff);
	// if(obstacle){
	// if(obstacle == LAVA){
	// color->r += 1.0/sqrt(off->depth);
	// color->g += 0.2/sqrt(off->depth);
	// }
	// break;
	// }
	// }
	// }
	// }
	// color->r /= ray_count;
	// color->g /= ray_count;
	// color->b /= ray_count;
	// }
	// endfor
	// }

}
