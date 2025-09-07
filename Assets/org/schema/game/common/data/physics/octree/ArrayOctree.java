package org.schema.game.common.data.physics.octree;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.game.common.data.Dodecahedron;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.graphicsengine.forms.BoundingBox;

import com.bulletphysics.linearmath.AabbUtil2;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.linearmath.VectorUtil;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;

@SuppressWarnings("unused")
public class ArrayOctree {
//	public static final short HIT_BIT = 16384;
	public static final int POSSIBLE_LEAF_HITS = SegmentData.SEG == 16 ? 512 : 4096;
	static final int MAX_DEEPNESS = 1+(SegmentData.SEG / 16);
	static final int COUNT_LVLS = MAX_DEEPNESS + 1;
	static final int NODES;
	
	private static byte mod16[] = new byte[32];
	static {
		//root = 1; 	16x16x16
		//lvl0 = 8; 	8x8x8
		//lvl1 = 64;	4x4x4
		//lvl2 = 512;	2x2x2
		//---------
		//585
		
		//root = 1; 	32x32x32
		//lvl0 = 8; 	16x16x16
		//lvl1 = 64; 	8x8x8
		//lvl2 = 512;	4x4x4
		//lvl3 = 4096;	2x2x2
		//---------
		//4681

		int n = 1; //root
		int p = 8;
		for (int i = 0; i < COUNT_LVLS; i++) {
			n += p;
			p *= 8;
		}
		NODES = n;
//		if (SegmentData.SEG == 16 && NODES != 585) {
//			System.err.println("CRITICAL: NODES: " + NODES);
//			assert(false);
//		}
//		if (SegmentData.SEG == 32 && NODES != 4681) {
//			System.err.println("CRITICAL: NODES: " + NODES);
//			assert(false);
//		}

		for(byte i = 0; i < 32; i++){
			mod16[i] = (byte) ByteUtil.modU16(i);
		}
	}
	//	private final byte[] data;
	//	private final boolean[] full;
	static final int BUFFER_SIZE = NODES * (3 * ByteUtil.SIZEOF_SHORT);
	private static final IntOpenHashSet testHashSet = new IntOpenHashSet();
	static final byte[] dimBuffer = new byte[BUFFER_SIZE]; //save start and end
	static final int[] indexBuffer = new int[(SegmentData.SEG_TIMES_SEG_TIMES_SEG * COUNT_LVLS)]; //save start and end
	static final int[] localIndexBuffer = new int[3 * (SegmentData.SEG_TIMES_SEG_TIMES_SEG)]; //save start and end
	static final short[] localIndexShiftedBuffer = new short[3 * (SegmentData.SEG_TIMES_SEG_TIMES_SEG)]; //save start and end
	static final int[] indexBufferIndexed = new int[3 * (SegmentData.SEG_TIMES_SEG_TIMES_SEG * COUNT_LVLS)];
	private static OctreeVariableSet serverSet = new OctreeVariableSet();
	private static OctreeVariableSet clientSet = new OctreeVariableSet();

	private byte[] chunk16aabb = new byte[(3*2)*8];
	
	private static final byte[] toBB16Map = new byte[SegmentData.SEG_TIMES_SEG_TIMES_SEG]; 
	
	static {

		ArrayOctreeGenerator.splitStart(
				new Vector3b(-SegmentData.SEG_HALF, -SegmentData.SEG_HALF, -SegmentData.SEG_HALF), 
				new Vector3b(SegmentData.SEG_HALF, SegmentData.SEG_HALF, SegmentData.SEG_HALF), 
				(byte) SegmentData.SEG_HALF);
		ArrayOctreeTraverse.create();
		
		Vector3b min = new Vector3b();
		Vector3b max = new Vector3b();
		int index = 0;
		for(int z = 0; z < SegmentData.SEG; z++){
			for(int y = 0; y < SegmentData.SEG; y++){
				for(int x = 0; x < SegmentData.SEG; x++){
					
					for(byte i = 1; i < 9; i++){
						getBox(i, min, max);
						min.add((byte)SegmentData.SEG_HALF, (byte)SegmentData.SEG_HALF, (byte)SegmentData.SEG_HALF);
						max.add((byte)SegmentData.SEG_HALF, (byte)SegmentData.SEG_HALF, (byte)SegmentData.SEG_HALF);
						
						if(x >= min.x && x < max.x && y >= min.y && y < max.y && z >= min.z && z < max.z){
							toBB16Map[index] = (byte) (i-1);
							break;
						}
					}
					index++;
				}
			}
		}
	}
	public static void main(String[] sad){
		
		
		
		
	}
	
	private final short[] sizes;
	//TODO put bit inside sizes
	private final boolean[] hits;
	private final OctreeVariableSet set;
	public ArrayOctree(boolean onServer) {
		//		data = new byte[NODES];
		sizes = new short[NODES];
		hits = new boolean[NODES];
		//		full = new boolean[8];
		if (onServer) {
			this.set = serverSet;
		} else {
			this.set = clientSet;
		}
		resetAABB16();
	}
	public void resetAABB16(){
		//resetminmax
		for(int i = 0; i < chunk16aabb.length; i+=6){
			chunk16aabb[i] 		= 	Byte.MAX_VALUE;
			chunk16aabb[i+1] 	= 	Byte.MAX_VALUE;
			chunk16aabb[i+2] 	= 	Byte.MAX_VALUE;
			chunk16aabb[i+3] 	= 	Byte.MIN_VALUE;
			chunk16aabb[i+4] 	= 	Byte.MIN_VALUE;
			chunk16aabb[i+5] 	= 	Byte.MIN_VALUE;
		}
	}
	public static void getBox(int nodeIndex, Vector3b min, Vector3b max) {
		
		//TODO Optimize by giving the correct dimension according to how many blocks are in this tree segment.
		// this extremely viable for the 2x2x2 test as the box size could be cached directly with the iteration management
		
		int bufferIndex = nodeIndex * 6;
		min.set(dimBuffer[bufferIndex + 0], dimBuffer[bufferIndex + 1], dimBuffer[bufferIndex + 2]);
		max.set(dimBuffer[bufferIndex + 3], dimBuffer[bufferIndex + 4], dimBuffer[bufferIndex + 5]);
	}

	public static int getIndex(int localIndex, int level) {

		if (level == 0) {
			return localIndex + 1;
		}

		int n = 1; //root
		int p = 8;
		for (int i = 0; i < level; i++) {
			n += p;
			p *= 8;
		}
		n += localIndex;

		return n;
	}

	public static OctreeVariableSet getSet(boolean onServer) {
		return onServer ? serverSet : clientSet;
	}

	

	public static byte getEnd(int coord, int index) {
		int bufferIndex = index * 6;
		return dimBuffer[bufferIndex + 3 + coord];
	}

	public static byte getStart(int coord, int index) {
		int bufferIndex = index * 6;
		return dimBuffer[bufferIndex + coord];
	}

	public static final int getLocalIndex(byte x, byte y, byte z) {
		return (z % 2) * 4 + (y % 2) * 2 + (x % 2);
	}

	public static final int getLocalIndex(int infoIndex) {
		return localIndexBuffer[infoIndex];
	}

	public static void deserializeOctreeCompression(SegmentData data,
	                                                FastByteArrayInputStream bIn, DataInputStream in, long arrayLength) throws IOException {
		long read = 0;
		int dataPosition = in.readShort() * 3;
		int currentDataPos = 0;
		while (read < dataPosition) {

			short nodeIndex = in.readShort();
			int leafIndex = in.readByte() & 0xFF;
			read += 3;

			long posBefore = bIn.position();

			bIn.position(dataPosition + 2 + currentDataPos);

			byte x = getStart(0, nodeIndex);
			byte y = getStart(1, nodeIndex);
			byte z = getStart(2, nodeIndex);

			byte[][] is = ArrayOctreeTraverse.tMap[leafIndex];
			for (int i = 0; i < is.length; i++) {
				int infoIndex = SegmentData.getInfoIndex((byte) (x + is[i][0] + SegmentData.SEG_HALF), (byte) (y + is[i][1] + SegmentData.SEG_HALF), (byte) (z + is[i][2] + SegmentData.SEG_HALF));
				data.readSingle(infoIndex, in);
				currentDataPos += 3;
			}
			bIn.position(posBefore);
		}
	}

	public static void deserializeOctreeCompressionInterleaf(SegmentData data,
	                                                         DataInputStream in, long arrayLength) throws IOException {
		long read = 0;

		while (read < arrayLength) {

			short nodeIndex = in.readShort();
			int leafIndex = in.readByte() & 0xFF;
			read += 3;

			byte x = getStart(0, nodeIndex);
			byte y = getStart(1, nodeIndex);
			byte z = getStart(2, nodeIndex);

			byte[][] is = ArrayOctreeTraverse.tMap[leafIndex];
			for (int i = 0; i < is.length; i++) {
				int infoIndex = SegmentData.getInfoIndex((byte) (x + is[i][0] + SegmentData.SEG_HALF), (byte) (y + is[i][1] + SegmentData.SEG_HALF), (byte) (z + is[i][2] + SegmentData.SEG_HALF));
				data.readSingle(infoIndex, in);
				read += 3;

			}
			//			System.err.println("READING "+read+" / "+arrayLength);
		}
	}

	private void addAABB2(short mask, Vector3b minOut, Vector3b maxOut) {
		byte[][] aabb = ArrayOctreeTraverse.tAABBMap[mask];
		
//		System.err.println("AABB BEF: "+minOut+" --- "+maxOut+";      "+Arrays.toString(aabb[0])+" --- "+Arrays.toString(aabb[1]));
		
		maxOut.set(minOut);
		minOut.add(aabb[0][0], aabb[0][1], aabb[0][2]);
		maxOut.add(aabb[1][0], aabb[1][1], aabb[1][2]);
		
//		System.err.println("AABB AFT: "+minOut+" --- "+maxOut+";      "+Arrays.toString(aabb[0])+" --- "+Arrays.toString(aabb[1]));
	}
	private IntersectionCallback doIntersectingAABB(int index, int level, OctreeVariableSet set,
	                                                IntersectionCallback intersectionCall, Segment segment,
	                                                Transform selfTrans, Matrix3f absoluteMat, float margin, Vector3f otherMin, Vector3f otherMax, float scale, Vector3f centerIfSphere, float radiusIfSphere) {

		intersectionCall.leafCalcs++;
		int nodeIndex = getIndex(index, level);

		getBox(nodeIndex, set.min, set.max);
		if(level == 0){
			//modify aabb with actual subaabb (16x16x16) of chunk32 to be potentially smaller
			
			addAABB16(index, set.min, set.max);
			
		}else if(level == MAX_DEEPNESS){
			addAABB2(sizes[nodeIndex], set.min, set.max);
		}

		Vector3f tmpMin = set.tmpMin;
		Vector3f tmpMax = set.tmpMax;
		Vector3f tmpMinOut = set.tmpMinOut;
		Vector3f tmpMaxOut = set.tmpMaxOut;

		float x = segment.pos.x - 0.5f;
		float y = segment.pos.y - 0.5f;
		float z = segment.pos.z - 0.5f;

		tmpMin.x = set.min.x + x;
		tmpMin.y = set.min.y + y;
		tmpMin.z = set.min.z + z;

		tmpMax.x = set.max.x + x;
		tmpMax.y = set.max.y + y;
		tmpMax.z = set.max.z + z;

		transformAabb(set, tmpMin, tmpMax, absoluteMat, margin, selfTrans, tmpMinOut, tmpMaxOut);
		
		
		
		boolean intersection;
		if (centerIfSphere != null) {
			set.closest.set(centerIfSphere);

			Vector3fTools.clamp(set.closest, tmpMinOut, tmpMaxOut);

			set.closest.sub(centerIfSphere);
			intersection = set.closest.length() < radiusIfSphere;
		} else {
			intersection = AabbUtil2.testAabbAgainstAabb2(tmpMinOut, tmpMaxOut, otherMin, otherMax);
		}

		setHasHit(nodeIndex, intersection);

		//do this only on leaves
		if (intersection && level == MAX_DEEPNESS) {
			assert (sizes[nodeIndex] > 0);
			
			/*
			 * revert min/max to the origin, as adapted AABB might have changed
			 * it start/end is needed in narrow test to correctly put together
			 * corrdinated
			 */
			getBox(nodeIndex, set.min, set.max);
			
			intersectionCall.addHit(tmpMinOut, tmpMaxOut, 
					set.min.x, set.min.y, set.min.z, 
					set.max.x, set.max.y, set.max.z, sizes[nodeIndex], nodeIndex);
		}

		return intersectionCall;
	}

	
	private IntersectionCallback doIntersectingDodecahedron(int index, int lvl, OctreeVariableSet set,
	                                                        IntersectionCallback intersectionCall, Segment segment,
	                                                        Transform selfTrans, Matrix3f absoluteMat, float margin, Dodecahedron d, float scale) {

		intersectionCall.leafCalcs++;
		int nodeIndex = getIndex(index, lvl);

		getBox(nodeIndex, set.min, set.max);

		Vector3f tmpMin = set.tmpMin;
		Vector3f tmpMax = set.tmpMax;
		Vector3f tmpMinOut = set.tmpMinOut;
		Vector3f tmpMaxOut = set.tmpMaxOut;

		float x = segment.pos.x - 0.5f;
		float y = segment.pos.y - 0.5f;
		float z = segment.pos.z - 0.5f;

		tmpMin.x = set.min.x + x;
		tmpMin.y = set.min.y + y;
		tmpMin.z = set.min.z + z;

		tmpMax.x = set.max.x + x;
		tmpMax.y = set.max.y + y;
		tmpMax.z = set.max.z + z;

		transformAabb(set, tmpMin, tmpMax, absoluteMat, margin, selfTrans, tmpMinOut, tmpMaxOut);

		boolean intersection = d.testAABB(tmpMinOut, tmpMaxOut);

		setHasHit(nodeIndex, intersection);

		//do this only on leaves
		if (intersection && lvl == MAX_DEEPNESS) {
			assert ((sizes[nodeIndex]) > 0);
			intersectionCall.addHit(tmpMinOut, tmpMaxOut, set.min.x, set.min.y, set.min.z, set.max.x, set.max.y, set.max.z, (sizes[nodeIndex]), nodeIndex);
		}

		return intersectionCall;
	}
	@Deprecated
	private IntersectionCallback doIntersectingRay(int index, int level,
	                                               OctreeVariableSet set, IntersectionCallback intersectionCallBack, Transform selfTrans, Matrix3f absoluteMat,
	                                               float margin, Segment segment, Vector3f fromA, Vector3f toA, float scale) {

		int nodeIndex = getIndex(index, level);

		intersectionCallBack.leafCalcs++;

		Vector3f tmpMin = set.tmpMin;
		Vector3f tmpMax = set.tmpMax;
		Vector3f tmpMinOut = set.tmpMinOut;
		Vector3f tmpMaxOut = set.tmpMaxOut;

		getBox(nodeIndex, set.min, set.max);

		float x = segment.pos.x - 0.5f;
		float y = segment.pos.y - 0.5f;
		float z = segment.pos.z - 0.5f;

		tmpMin.x = set.min.x + x;
		tmpMin.y = set.min.y + y;
		tmpMin.z = set.min.z + z;

		tmpMax.x = set.max.x + x;
		tmpMax.y = set.max.y + y;
		tmpMax.z = set.max.z + z;

		transformAabb(set, tmpMin, tmpMax, absoluteMat, margin, selfTrans, tmpMinOut, tmpMaxOut);

		set.param[0] = 1; //should normally be closest hit fraction of the ray result callback
		set.normal.x = 0;
		set.normal.y = 0;
		set.normal.z = 0;

		boolean intersection = rayAabb(fromA, toA, tmpMinOut, tmpMaxOut, set.param, set.normal);

		//only test, if either point is inside AABB
		boolean inside = false;
		if (!intersection) {
			inside = BoundingBox.testPointAABB(toA, tmpMinOut, tmpMaxOut) || BoundingBox.testPointAABB(fromA, tmpMinOut, tmpMaxOut);
		}
		//		intersection = intersection || BoundingBox.testPointAABB(toA, set.tmpMinOut, set.tmpMaxOut);
		boolean hit = intersection || inside;
		setHasHit(nodeIndex, hit);

		if (hit && level == MAX_DEEPNESS) {
			assert ((sizes[nodeIndex]) > 0);
			intersectionCallBack.addHit(tmpMinOut, tmpMaxOut, set.min.x, set.min.y, set.min.z, set.max.x, set.max.y, set.max.z, (sizes[nodeIndex]), nodeIndex);
		}

		return intersectionCallBack;
	}

	private IntersectionCallback findIntersectingDodecahedron(int index, int level,
	                                                          OctreeVariableSet set, IntersectionCallback intersectionCall,
	                                                          Segment segment, Transform selfTrans, Matrix3f absoluteMat, float margin,
	                                                          Dodecahedron d, float scale) {

		int nodeIndex = getIndex(index, level);
		int count = sizes[nodeIndex];//(level == 0 && full[nodeIndex-1]) ? 256 : (data[nodeIndex] & 0xFF);

		if (count > 0) {

			intersectionCall = doIntersectingDodecahedron(index, level, set, intersectionCall, segment, selfTrans, absoluteMat, margin, d, scale);

			if (level < MAX_DEEPNESS) {
				if (isHasHit(nodeIndex)) {
					int childIndex = index * 8;
					for (int i = 0; i < 8; i++) {
						intersectionCall = findIntersectingDodecahedron(childIndex + i, level + 1, set, intersectionCall, segment, selfTrans, absoluteMat, margin, d, scale);
					}
				}
			}
		} else {
			setHasHit(nodeIndex, false);
		}
		return intersectionCall;
	}

	private IntersectionCallback findIntersectingAABB(int index, int level,
	                                                  OctreeVariableSet set, IntersectionCallback intersectionCall,
	                                                  Segment segment, Transform selfTrans, Matrix3f absoluteMat, float margin,
	                                                  Vector3f otherMin, Vector3f otherMax, float scale, Vector3f centerIfSphere, float radiusIfSphere) {

		int nodeIndex = getIndex(index, level);
		int count = sizes[nodeIndex];

		if (count > 0) {

			intersectionCall = doIntersectingAABB(index, level, set, intersectionCall, segment, selfTrans, absoluteMat, margin, otherMin, otherMax, scale, centerIfSphere, radiusIfSphere);

			if (level < MAX_DEEPNESS) {
				if (isHasHit(nodeIndex)) {
					int childIndex = index * 8;
					for (int i = 0; i < 8; i++) {
						intersectionCall = findIntersectingAABB(childIndex + i, level + 1, set, intersectionCall, segment, selfTrans, absoluteMat, margin, otherMin, otherMax, scale, centerIfSphere, radiusIfSphere);
					}
				}
			}
		} else {
			setHasHit(nodeIndex, false);
		}
		return intersectionCall;
	}
	public void insertAABB16(byte x, byte y, byte z, int baseIndex){
//		int index = (z * SegmentData.SEG_TIMES_SEG) + (y * SegmentData.SEG) + x;
		int i = toBB16Map[baseIndex] * 6;
		
		chunk16aabb[i] 		= 	(byte) Math.min(mod16[x], chunk16aabb[i]);
		chunk16aabb[i+1] 	= 	(byte) Math.min(mod16[y], chunk16aabb[i+1]);
		chunk16aabb[i+2] 	= 	(byte) Math.min(mod16[z], chunk16aabb[i+2]);
		
		chunk16aabb[i+3] 	= 	(byte) Math.max(mod16[x]+1, chunk16aabb[i+3]);
		chunk16aabb[i+4] 	= 	(byte) Math.max(mod16[y]+1, chunk16aabb[i+4]);
		chunk16aabb[i+5] 	= 	(byte) Math.max(mod16[z]+1, chunk16aabb[i+5]);
		
		
//		assert(chunk16aabb[i] >= 0 && chunk16aabb[i] <= 16):x+"; "+chunk16aabb[i]+"; "+mod16[x];
//		assert(chunk16aabb[i+1] >= 0 && chunk16aabb[i+1] <= 16):y+"; "+chunk16aabb[i+1]+"; "+mod16[y];
//		assert(chunk16aabb[i+2] >= 0 && chunk16aabb[i+2] <= 16):z+"; "+chunk16aabb[i+2]+"; "+mod16[z];
//		assert(chunk16aabb[i+3] >= 0 && chunk16aabb[i+3] <= 16):x+"; "+chunk16aabb[i+3]+"; "+mod16[x];
//		assert(chunk16aabb[i+4] >= 0 && chunk16aabb[i+4] <= 16):y+"; "+chunk16aabb[i+4]+"; "+mod16[y];
//		assert(chunk16aabb[i+5] >= 0 && chunk16aabb[i+5] <= 16):z+"; "+chunk16aabb[i+5]+"; "+mod16[z];
	}
	private void addAABB16(int topLvlIndex, Vector3b minOut, Vector3b maxOut){
		int i = topLvlIndex * 6;
		
		maxOut.set(minOut);
		minOut.add(chunk16aabb[i  ], chunk16aabb[i+1], chunk16aabb[i+2]);
		maxOut.add(chunk16aabb[i+3], chunk16aabb[i+4], chunk16aabb[i+5]);
		
	}
	
	public boolean getAABB(int index, int lvl, 
			OctreeVariableSet set, Segment segment,
			Transform selfTrans, Matrix3f absoluteMat, float margin, Vector3f minOut, Vector3f maxOut) {
		
		assert(lvl == 0):"only implemented for top lvl (16)";
		
//		int nodeIndex = getIndex(index, lvl);
		int nodeIndex = index+1;
		
		int count = sizes[nodeIndex];
		
		if(count > 0){
			assert(set != null);
			getBox(nodeIndex, set.min, set.max);
			
			addAABB16(index, set.min, set.max);
			
//			System.err.println("DIM ::: "+set.min+"; "+set.max);
			
			
			Vector3f tmpMin = set.tmpMin;
			Vector3f tmpMax = set.tmpMax;
			Vector3f tmpMinOut = set.tmpMinOut;
			Vector3f tmpMaxOut = set.tmpMaxOut;
			
			float x = segment.pos.x - 0.5f;
			float y = segment.pos.y - 0.5f;
			float z = segment.pos.z - 0.5f;
			
			tmpMin.x = set.min.x + x;
			tmpMin.y = set.min.y + y;
			tmpMin.z = set.min.z + z;
			
			tmpMax.x = set.max.x + x;
			tmpMax.y = set.max.y + y;
			tmpMax.z = set.max.z + z;
			
			transformAabb(set, tmpMin, tmpMax, absoluteMat, margin, selfTrans,
					tmpMinOut, tmpMaxOut);
			
			minOut.set(tmpMinOut);
			maxOut.set(tmpMaxOut);
			return true;
		}
		
		return false;
	}
	
	public IntersectionCallback findIntersectingAABB(
			OctreeVariableSet set, IntersectionCallback intersectionCall,
			Segment segment, Transform selfTrans, Matrix3f absoluteMat, float margin,
			Vector3f otherMin, Vector3f otherMax, float scale) {

		return findIntersectingAABB(set, intersectionCall, segment, selfTrans, absoluteMat, margin, otherMin, otherMax, scale, null, -1);
	}

	public IntersectionCallback findIntersectingAABB(
			OctreeVariableSet set, IntersectionCallback intersectionCall,
			Segment segment, Transform selfTrans, Matrix3f absoluteMat, float margin,
			Vector3f otherMin, Vector3f otherMax, float scale, Vector3f centerIfSphere, float radiusIfSphere) {

		for (int i = 0; i < 8; i++) {
			intersectionCall = findIntersectingAABB(i, 0, set, intersectionCall, segment, selfTrans, absoluteMat, margin, otherMin, otherMax, scale, centerIfSphere, radiusIfSphere);
		}
		return intersectionCall;
	}
	public IntersectionCallback findIntersectingAABBFromFirstLvl(int index,
			OctreeVariableSet set, IntersectionCallback intersectionCall,
			Segment segment, Transform selfTrans, Matrix3f absoluteMat, float margin,
			Vector3f otherMin, Vector3f otherMax, float scale) {
		
		intersectionCall = findIntersectingAABB(index, 0, set, intersectionCall, segment, selfTrans, absoluteMat, margin, otherMin, otherMax, scale, null, -1);
		return intersectionCall;
	}

	
	
	public IntersectionCallback findIntersectingDodecahedron(
			OctreeVariableSet set, IntersectionCallback intersectionCall,
			Segment segment, Transform selfTrans, Matrix3f absoluteMat, float margin,
			Dodecahedron d, float scale) {

		for (int i = 0; i < 8; i++) {
			intersectionCall = findIntersectingDodecahedron(i, 0, set, intersectionCall, segment, selfTrans, absoluteMat, margin, d, scale);
		}
		return intersectionCall;
	}
	@Deprecated
	private IntersectionCallback findIntersectingRay(int index, int level,
	                                                 OctreeVariableSet set, IntersectionCallback intersectionCallBack, Transform selfTrans, Matrix3f absoluteMat,
	                                                 float margin, Segment segment, Vector3f fromA, Vector3f toA, float scale) {

		int nodeIndex = getIndex(index, level);

		//		if((data[nodeIndex] & 0xFF) > 0 || level == 0 ){
		if ((sizes[nodeIndex]) > 0 || level == 0) {

			intersectionCallBack = doIntersectingRay(index, level, set, intersectionCallBack, selfTrans, absoluteMat, margin, segment, fromA, toA, scale);

			if (level < MAX_DEEPNESS) {
				if (isHasHit(nodeIndex)) {
					int childIndex = index * 8;
					for (int i = 0; i < 8; i++) {
						intersectionCallBack = findIntersectingRay(childIndex + i, level + 1, set, intersectionCallBack, selfTrans, absoluteMat, margin, segment, fromA, toA, scale);
					}
				}
			}
		} else {
			setHasHit(nodeIndex, false);
		}
		return intersectionCallBack;
	}
	@Deprecated
	public IntersectionCallback findIntersectingRay(
			OctreeVariableSet set, IntersectionCallback intersectionCallBack, Transform selfTrans, Matrix3f absoluteMat,
			float margin, Segment segment, Vector3f fromA, Vector3f toA, float scale) {

		for (int i = 0; i < 8; i++) {
			intersectionCallBack = findIntersectingRay(i, 0, set, intersectionCallBack, selfTrans, absoluteMat, margin, segment, fromA, toA, scale);
		}
		return intersectionCallBack;
	}

	/**
	 * @return the set
	 */
	public OctreeVariableSet getSet() {
		return set;
	}

	public void insert(final byte x, final byte y, final byte z, final int index) {
		//important to use pre-increment here so the assertion is right

		//		assert(ArrayOctreeGenerator.getNodeIndex(x, y, z, 0) == ArrayOctreeGenerator.getNodeIndex(index, 0));
		//		assert(ArrayOctreeGenerator.getNodeIndex(x, y, z, 1) == ArrayOctreeGenerator.getNodeIndex(index, 1));
		//		assert(ArrayOctreeGenerator.getNodeIndex(x, y, z, 2) == ArrayOctreeGenerator.getNodeIndex(index, 2));

		//insert into first 3 levels
		int a = ++sizes[ArrayOctreeGenerator.getNodeIndex(index, 0)];
		int b = ++sizes[ArrayOctreeGenerator.getNodeIndex(index, 1)];
		int c = ++sizes[ArrayOctreeGenerator.getNodeIndex(index, 2)];

		assert (a > 0);
		assert (b > 0);
		assert (c > 0);
		
		int nodeIndex = ArrayOctreeGenerator.getNodeIndex(index, MAX_DEEPNESS);

		//from 0 to 7;
		short mask = ArrayOctree.localIndexShiftedBuffer[index];//(short) (1 << localIndex);
		sizes[nodeIndex] |= mask;

		assert ((sizes[nodeIndex]) > 0);
	}

	public void delete(byte x, byte y, byte z, int index, short type) {

		//important to use pre-decrement here so the assertion is right
		int a = --sizes[ArrayOctreeGenerator.getNodeIndex(index, 0)];
		int b = --sizes[ArrayOctreeGenerator.getNodeIndex(index, 1)];
		int c = --sizes[ArrayOctreeGenerator.getNodeIndex(index, 2)];
		assert (a >= 0);
		assert (b >= 0);
		assert (c >= 0);
		
		int nodeIndex = ArrayOctreeGenerator.getNodeIndex(index, MAX_DEEPNESS);
		int localIndex = getLocalIndex(index);
		//from 0 to 7;
		short mask = (short) (1 << localIndex);
		sizes[nodeIndex] &= ~mask;

	}

	public boolean isHasHit(int nodeIndex) {
		return hits[nodeIndex];
	}

	public boolean rayAabb(Vector3f rayFrom, Vector3f rayTo, Vector3f aabbMin, Vector3f aabbMax, float[] param, Vector3f normal) {
		Vector3f aabbHalfExtent = set.aabbHalfExtent;//new @Stack Vector3f();
		Vector3f aabbCenter = set.aabbCenter;//new @Stack Vector3f();
		Vector3f source = set.source;//new @Stack Vector3f();
		Vector3f target = set.target;//new @Stack Vector3f();
		Vector3f r = set.r;//new @Stack Vector3f();
		Vector3f hitNormal = set.hitNormal;//new @Stack Vector3f();

		aabbHalfExtent.sub(aabbMax, aabbMin);
		aabbHalfExtent.scale(0.5f);

		aabbCenter.add(aabbMax, aabbMin);
		aabbCenter.scale(0.5f);

		source.sub(rayFrom, aabbCenter);
		target.sub(rayTo, aabbCenter);

		int sourceOutcode = AabbUtil2.outcode(source, aabbHalfExtent);
		int targetOutcode = AabbUtil2.outcode(target, aabbHalfExtent);
		if ((sourceOutcode & targetOutcode) == 0x0) {
			float lambda_enter = 0f;
			float lambda_exit = param[0];
			r.sub(target, source);

			float normSign = 1f;
			hitNormal.set(0f, 0f, 0f);
			int bit = 1;

			for (int j = 0; j < 2; j++) {
				for (int i = 0; i != 3; ++i) {
					if ((sourceOutcode & bit) != 0) {
						float lambda = (-VectorUtil.getCoord(source, i) - VectorUtil.getCoord(aabbHalfExtent, i) * normSign) / VectorUtil.getCoord(r, i);
						if (lambda_enter <= lambda) {
							lambda_enter = lambda;
							hitNormal.set(0f, 0f, 0f);
							VectorUtil.setCoord(hitNormal, i, normSign);
						}
					} else if ((targetOutcode & bit) != 0) {
						float lambda = (-VectorUtil.getCoord(source, i) - VectorUtil.getCoord(aabbHalfExtent, i) * normSign) / VectorUtil.getCoord(r, i);
						//btSetMin(lambda_exit, lambda);
						lambda_exit = Math.min(lambda_exit, lambda);
					}
					bit <<= 1;
				}
				normSign = -1f;
			}
			if (lambda_enter <= lambda_exit) {
				param[0] = lambda_enter;
				normal.set(hitNormal);
				return true;
			}
		}
		return false;
	}

	public void reset() {
		Arrays.fill(sizes, (short) 0);
		resetAABB16();
	}

	public void setHasHit(int nodeIndex, boolean hit) {
		hits[nodeIndex] = hit;
	}

	private void transformAabb(OctreeVariableSet set, Vector3f localAabbMin, Vector3f localAabbMax, Matrix3f absoluteMat, float margin, Transform trans, Vector3f aabbMinOut, Vector3f aabbMaxOut) {

		Vector3f localCenter = set.localCenter;
		//		localCenter.add(localAabbMin, OctreeVariableSet.localCentersAdd[lvl]);
		localCenter.add(localAabbMax, localAabbMin);
		localCenter.scale(0.5f);

		Vector3f center = set.center;
		center.set(localCenter);
		trans.transform(center);

		Vector3f extent = set.extend;
		Vector3f v1 = set.localHalfExtents;
		v1.sub(localAabbMax, localAabbMin);
		v1.scale(0.5f);

		v1.x += margin;
		v1.y += margin;
		v1.z += margin;

		extent.x = (absoluteMat.m00 * v1.x + absoluteMat.m01 * v1.y + absoluteMat.m02 * v1.z);
		extent.y = (absoluteMat.m10 * v1.x + absoluteMat.m11 * v1.y + absoluteMat.m12 * v1.z);
		extent.z = (absoluteMat.m20 * v1.x + absoluteMat.m21 * v1.y + absoluteMat.m22 * v1.z);

		aabbMinOut.sub(center, extent);
		aabbMaxOut.add(center, extent);
	}

	public int traverse(SegmentData data, SegmentDataTraverseInterface ic, boolean solid) {
		int c = 0;
		for (int i = 0; i < 8; i++) {
			c += traverse(i, 0, ic, data, solid);
		}
		return c;
	}

	private int traverse(int index, int level, SegmentDataTraverseInterface ic,
	                     SegmentData sData, boolean solid) {
		int c = 0;
		int nodeIndex = getIndex(index, level);
		int count = sizes[nodeIndex];//(level == 0 && full[nodeIndex-1]) ? 256 : (data[nodeIndex] & 0xFF);

		//		System.err.println("Count "+count+" at "+getStart(0, nodeIndex)+", "+ getStart(1, nodeIndex)+", "+ getStart(2, nodeIndex)+"; level: "+level);
		if (count > 0) {

			if (level < MAX_DEEPNESS) {
				int childIndex = index * 8;
				for (int i = 0; i < 8; i++) {
					c += traverse(childIndex + i, level + 1, ic, sData, solid);
				}
			} else {
				c += traverseLeafSolid(index, level, ic, sData);
			}
		}
		return c;
	}

	public int compress(SegmentData data, DataOutputStream stream, FastByteArrayOutputStream bOut) throws IOException {
		int c = 0;
		bOut.position(2);
		for (int i = 0; i < 8; i++) {
			c += compress(i, 0, data, stream, false);

		}
		long prePos = bOut.position() - 2;
		bOut.position(0);

		assert ((prePos) / 3 < Short.MAX_VALUE);
		stream.writeShort((short) ((prePos) / 3));

		bOut.position(prePos + 2);
		for (int i = 0; i < 8; i++) {
			compress(i, 0, data, stream, true);
		}
		return c;
	}

	private int compress(int index, int level,
	                     SegmentData sData, DataOutputStream stream, boolean blockData) throws IOException {
		int c = 0;
		int nodeIndex = getIndex(index, level);

		int count = sizes[nodeIndex];//(level == 0 && full[nodeIndex-1]) ? 256 : (data[nodeIndex] & 0xFF);
		if (count > 0) {

			if (level < MAX_DEEPNESS) {
				int childIndex = index * 8;
				for (int i = 0; i < 8; i++) {
					c += compress(childIndex + i, level + 1, sData, stream, blockData);
				}
			} else {
				c += compressLeaf(index, level, sData, stream, blockData);
			}
		}
		return c;
	}

	private int compressLeaf(int index, int level, SegmentData sData, DataOutputStream stream, boolean blockData) throws IOException {
		int nodeIndex = getIndex(index, level);
		int leafIndex = (sizes[nodeIndex]);
		byte[][] bs = ArrayOctreeTraverse.tMap[leafIndex];
		ArrayOctree.testHashSet.add(leafIndex);
		byte x = getStart(0, nodeIndex);
		byte y = getStart(1, nodeIndex);
		byte z = getStart(2, nodeIndex);

		if (!blockData) {
			assert (nodeIndex < Short.MAX_VALUE);
			stream.writeShort(nodeIndex);
			assert (leafIndex < 256);
			stream.writeByte(leafIndex);
		} else {

			for (int i = 0; i < bs.length; i++) {
				int infoIndex = SegmentData.getInfoIndex((byte) (x + bs[i][0] + SegmentData.SEG_HALF), (byte) (y + bs[i][1] + SegmentData.SEG_HALF), (byte) (z + bs[i][2] + SegmentData.SEG_HALF));
				sData.writeSingle(infoIndex, stream);
			}
		}
		return bs.length;
	}

	public int compressInterleaf(SegmentData data, DataOutputStream stream) throws IOException {
		int c = 0;
		for (int i = 0; i < 8; i++) {
			c += compressInterleaf(i, 0, data, stream);
		}
		return c;
	}

	private int compressInterleaf(int index, int level,
	                              SegmentData sData, DataOutputStream stream) throws IOException {
		int c = 0;
		int nodeIndex = getIndex(index, level);

		int count = sizes[nodeIndex];//(level == 0 && full[nodeIndex-1]) ? 256 : (data[nodeIndex] & 0xFF);
		if (count > 0) {

			if (level < MAX_DEEPNESS) {
				int childIndex = index * 8;
				for (int i = 0; i < 8; i++) {
					c += compressInterleaf(childIndex + i, level + 1, sData, stream);
				}
			} else {
				c += compressLeafInterleaf(index, level, sData, stream);
			}
		}
		return c;
	}

	private int compressLeafInterleaf(int index, int level, SegmentData sData, DataOutputStream stream) throws IOException {
		int nodeIndex = getIndex(index, level);
		int leafIndex = sizes[nodeIndex];//(data[nodeIndex] & 0xFF);
		byte[][] bs = ArrayOctreeTraverse.tMap[leafIndex];
		ArrayOctree.testHashSet.add(leafIndex);
		byte x = getStart(0, nodeIndex);
		byte y = getStart(1, nodeIndex);
		byte z = getStart(2, nodeIndex);

		assert (nodeIndex < Short.MAX_VALUE);
		stream.writeShort(nodeIndex);
		assert (leafIndex < 256);
		stream.writeByte(leafIndex);

		for (int i = 0; i < bs.length; i++) {
			int infoIndex = SegmentData.getInfoIndex((byte) (x + bs[i][0] + SegmentData.SEG_HALF), (byte) (y + bs[i][1] + SegmentData.SEG_HALF), (byte) (z + bs[i][2] + SegmentData.SEG_HALF));
			sData.writeSingle(infoIndex, stream);
		}
		return bs.length;
	}

	private int traverseLeafSolid(int index, int level,
	                              SegmentDataTraverseInterface ic, SegmentData sData) {
		int nodeIndex = getIndex(index, level);

		//		byte[][] bs = ArrayOctreeTraverse.absoluteMap[(data[nodeIndex] & 0xFF)];
		//
		//		for(int i = 0; i < bs.length; i++){
		//			byte x = bs[i][0];
		//			byte y = bs[i][1];
		//			byte z = bs[i][2];
		//			ic.handle(x,y,z, nodeIndex, i);
		//		}
		//		return bs.length;
		return 0;
	}

}
