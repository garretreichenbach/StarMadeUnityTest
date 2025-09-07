package org.schema.game.common.data.physics.octree;

import java.util.Arrays;

import org.schema.common.util.linAlg.Vector3b;
import org.schema.game.common.data.world.SegmentData;

public class ArrayOctreeTraverse {

	public static byte[][][] tMap = new byte[256][][];
	public static byte[][][][] absoluteMap;
	public static int[][][] tIndexMap;
	public static final byte[][][] tAABBMap = new byte[256][2][3];

	public static void create() {
		Vector3b min = new Vector3b();
		Vector3b max = new Vector3b();
		for (int index = 0; index < 256; index++) {

			int size = 0;
			for (byte z = 0; z < 2; z++) {
				for (byte y = 0; y < 2; y++) {
					for (byte x = 0; x < 2; x++) {
						int i = (z % 2) * 4 + (y % 2) * 2 + (x % 2);

						int mask = (1 << i);
						if ((index & mask) == mask) {
							size++;
						}
					}
				}
			}
			tMap[index] = new byte[size][];
			//			absoluteMap[index] = new byte[size][];
			//			tIndexMap[index] = new int[size];

			int c = 0;
			//			System.err.println("AAAA: ---------------");
			for (byte z = 0; z < 2; z++) {
				for (byte y = 0; y < 2; y++) {
					for (byte x = 0; x < 2; x++) {

						int i = (z % 2) * 4 + (y % 2) * 2 + (x % 2);
						int mask = (1 << i);
						if ((index & mask) == mask) {
							tMap[index][c] = new byte[3];
							tMap[index][c][0] = x;
							tMap[index][c][1] = y;
							tMap[index][c][2] = z;
							c++;
						}

					}
				}
			}
			
			//create custom AABB for max level, so it's possible to compare smaller areas
			
			tAABBMap[index][0][0] = Byte.MAX_VALUE;
			tAABBMap[index][0][1] = Byte.MAX_VALUE;
			tAABBMap[index][0][2] = Byte.MAX_VALUE;
			
			tAABBMap[index][1][0] = Byte.MIN_VALUE;
			tAABBMap[index][1][1] = Byte.MIN_VALUE;
			tAABBMap[index][1][2] = Byte.MIN_VALUE;
			
			for(int i = 0; i < size; i++){
				byte x = tMap[index][i][0];
				byte y = tMap[index][i][1];
				byte z = tMap[index][i][2];
				
				tAABBMap[index][0][0] = (byte) Math.min(tAABBMap[index][0][0], x); 
				tAABBMap[index][0][1] = (byte) Math.min(tAABBMap[index][0][1], y); 
				tAABBMap[index][0][2] = (byte) Math.min(tAABBMap[index][0][2], z); 
				
				tAABBMap[index][1][0] = (byte) Math.max(tAABBMap[index][1][0], x+1); 
				tAABBMap[index][1][1] = (byte) Math.max(tAABBMap[index][1][1], y+1); 
				tAABBMap[index][1][2] = (byte) Math.max(tAABBMap[index][1][2], z+1); 
				
//				System.err.println(Arrays.toString(tAABBMap[index][0])+"; "+Arrays.toString(tAABBMap[index][1]));
			}
		}
		tIndexMap = new int[ArrayOctree.NODES][256][];
		absoluteMap = new byte[ArrayOctree.NODES][256][][];
		int c = 0;
		for (byte z = 0; z < SegmentData.SEG; z += 2) {
			for (byte y = 0; y < SegmentData.SEG; y += 2) {
				for (byte x = 0; x < SegmentData.SEG; x += 2) {
					int nodeIndex = ArrayOctree.getIndex(c, ArrayOctree.MAX_DEEPNESS);
					//					int nodeIndex = ArrayOctreeGenerator.getNodeIndex(x, y, z, ArrayOctree.MAX_DEEPNESS);
					//					assert(nodeIndex == nodeIndex2);
					ArrayOctree.getBox(nodeIndex, min, max);
					//					min.set(x,y,z);
					//					max.set((byte)(x+2),(byte)(y+2),(byte)(z+2));
					for (int i = 0; i < 256; i++) {
						tIndexMap[nodeIndex][i] = new int[tMap[i].length];
						absoluteMap[nodeIndex][i] = new byte[tMap[i].length][3];

						for (int m = 0; m < tMap[i].length; m++) {
							absoluteMap[nodeIndex][i][m][0] = (byte) ((min.x + tMap[i][m][0] + SegmentData.SEG_HALF));
							absoluteMap[nodeIndex][i][m][1] = (byte) ((min.y + tMap[i][m][1] + SegmentData.SEG_HALF));
							absoluteMap[nodeIndex][i][m][2] = (byte) ((min.z + tMap[i][m][2] + SegmentData.SEG_HALF));

							int infoIndex = SegmentData.getInfoIndex(absoluteMap[nodeIndex][i][m][0], absoluteMap[nodeIndex][i][m][1], absoluteMap[nodeIndex][i][m][2]);
							tIndexMap[nodeIndex][i][m] = infoIndex;
						}
					}

					c++;
				}
			}
		}
		absoluteMap = null;
	}

	public static void main(String[] asd) {
		long t = System.currentTimeMillis();

		//		System.err.println(Arrays.toString(tMap)+": "+(System.currentTimeMillis() - t));

		for (int index = 0; index < 256; index++) {

			System.err.println("############## " + index + " ##############");
			for (int x = 0; x < tMap[index].length; x++) {
				System.err.println(Arrays.toString(tMap[index][x]));
			}
		}
	}

	public static int traverseExisting(SegmentData data, SegmentDataTraverseInterface ic) {
		return 0; //data.getOctree().traverse(data, ic, true);
	}

	public static int traverseEmpty(SegmentData data, SegmentDataTraverseInterface ic) {
		return 0;//data.getOctree().traverse(data, ic, false);
	}
}
