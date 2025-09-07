package org.schema.game.common.data.physics.octree;

import org.schema.common.util.linAlg.Vector3b;
import org.schema.game.common.data.world.SegmentData;

public class ArrayOctreeGenerator {
	public static int getNodeIndex(int x, int y, int z, int lvl) {
		int index = ((z * SegmentData.SEG_TIMES_SEG) + (y * SegmentData.SEG) + x) * ArrayOctree.COUNT_LVLS;
		return ArrayOctree.indexBuffer[index + lvl];
	}

	public static int getNodeIndex(int infoIndex, int lvl) {
		return ArrayOctree.indexBufferIndexed[(infoIndex * ArrayOctree.COUNT_LVLS) + lvl];
	}

	public static void put(int startIndex, Vector3b start, Vector3b end) {
		int bufferIndex = startIndex * 6;
		ArrayOctree.dimBuffer[bufferIndex + 0] = start.x;
		ArrayOctree.dimBuffer[bufferIndex + 1] = start.y;
		ArrayOctree.dimBuffer[bufferIndex + 2] = start.z;
		ArrayOctree.dimBuffer[bufferIndex + 3] = end.x;
		ArrayOctree.dimBuffer[bufferIndex + 4] = end.y;
		ArrayOctree.dimBuffer[bufferIndex + 5] = end.z;

	}

	public static void putNodeIndex(int x, int y, int z, int lvl, int nodeIndex) {
		int index = ((z * SegmentData.SEG_TIMES_SEG) + (y * SegmentData.SEG) + x) * ArrayOctree.COUNT_LVLS;
		ArrayOctree.indexBuffer[index + lvl] = nodeIndex;

		ArrayOctree.indexBufferIndexed[(SegmentData.getInfoIndex(x, y, z) * ArrayOctree.COUNT_LVLS) + lvl] = nodeIndex;

		int localIndex = (z % 2) * 4 + (y % 2) * 2 + (x % 2);
		ArrayOctree.localIndexBuffer[SegmentData.getInfoIndex(x, y, z)] = localIndex;

		ArrayOctree.localIndexShiftedBuffer[SegmentData.getInfoIndex(x, y, z)] = ((short) (1 << localIndex));
	}

	public static void split(int index, int level, Vector3b start, Vector3b end, byte halfDim) {

		int nodeIndex = ArrayOctree.getIndex(index, level);
		//		System.err.println("GENERATING: "+level+": "+index+" -> "+nodeIndex+" "+start+"; "+end);

		put(nodeIndex, start, end);

		for (int z = start.z + SegmentData.SEG_HALF; z < end.z + SegmentData.SEG_HALF; z++) {
			for (int y = start.y + SegmentData.SEG_HALF; y < end.y + SegmentData.SEG_HALF; y++) {
				for (int x = start.x + SegmentData.SEG_HALF; x < end.x + SegmentData.SEG_HALF; x++) {
					putNodeIndex(x, y, z, level, nodeIndex);
				}
			}
		}

		if (level < ArrayOctree.MAX_DEEPNESS) {
			Vector3b startAA = new Vector3b(start);
			Vector3b endAA = new Vector3b(halfDim, halfDim, halfDim);
			endAA.add(startAA);

			Vector3b startAB = new Vector3b(startAA);
			Vector3b endAB = new Vector3b(endAA);
			startAB.add(halfDim, (byte) 0, (byte) 0);
			endAB.add(halfDim, (byte) 0, (byte) 0);

			Vector3b startAC = new Vector3b(startAA);
			Vector3b endAC = new Vector3b(endAA);
			startAC.add(halfDim, (byte) 0, halfDim);
			endAC.add(halfDim, (byte) 0, halfDim);

			Vector3b startAD = new Vector3b(startAA);
			Vector3b endAD = new Vector3b(endAA);
			startAD.add((byte) 0, (byte) 0, halfDim);
			endAD.add((byte) 0, (byte) 0, halfDim);

			Vector3b startBA = new Vector3b(startAA);
			Vector3b endBA = new Vector3b(endAA);
			startBA.add((byte) 0, halfDim, (byte) 0);
			endBA.add((byte) 0, halfDim, (byte) 0);

			Vector3b startBB = new Vector3b(startAA);
			Vector3b endBB = new Vector3b(endAA);
			startBB.add(halfDim, halfDim, (byte) 0);
			endBB.add(halfDim, halfDim, (byte) 0);

			Vector3b startBC = new Vector3b(startAA);
			Vector3b endBC = new Vector3b(endAA);
			startBC.add(halfDim, halfDim, halfDim);
			endBC.add(halfDim, halfDim, halfDim);

			Vector3b startBD = new Vector3b(startAA);
			Vector3b endBD = new Vector3b(endAA);
			startBD.add((byte) 0, halfDim, halfDim);
			endBD.add((byte) 0, halfDim, halfDim);

			int startIndex = index * 8;
			byte halfDimNext = (byte) (halfDim / 2);
			split(startIndex + 0, level + 1, startAA, endAA, halfDimNext);
			split(startIndex + 1, level + 1, startAB, endAB, halfDimNext);
			split(startIndex + 2, level + 1, startAC, endAC, halfDimNext);
			split(startIndex + 3, level + 1, startAD, endAD, halfDimNext);
			split(startIndex + 4, level + 1, startBA, endBA, halfDimNext);
			split(startIndex + 5, level + 1, startBB, endBB, halfDimNext);
			split(startIndex + 6, level + 1, startBC, endBC, halfDimNext);
			split(startIndex + 7, level + 1, startBD, endBD, halfDimNext);
		}

	}

	public static void splitStart(Vector3b start, Vector3b end, byte halfDim) {
		Vector3b startAA = new Vector3b(start);
		Vector3b endAA = new Vector3b(halfDim, halfDim, halfDim);
		endAA.add(startAA);

		Vector3b startAB = new Vector3b(startAA);
		Vector3b endAB = new Vector3b(endAA);
		startAB.add(halfDim, (byte) 0, (byte) 0);
		endAB.add(halfDim, (byte) 0, (byte) 0);

		Vector3b startAC = new Vector3b(startAA);
		Vector3b endAC = new Vector3b(endAA);
		startAC.add(halfDim, (byte) 0, halfDim);
		endAC.add(halfDim, (byte) 0, halfDim);

		Vector3b startAD = new Vector3b(startAA);
		Vector3b endAD = new Vector3b(endAA);
		startAD.add((byte) 0, (byte) 0, halfDim);
		endAD.add((byte) 0, (byte) 0, halfDim);

		Vector3b startBA = new Vector3b(startAA);
		Vector3b endBA = new Vector3b(endAA);
		startBA.add((byte) 0, halfDim, (byte) 0);
		endBA.add((byte) 0, halfDim, (byte) 0);

		Vector3b startBB = new Vector3b(startAA);
		Vector3b endBB = new Vector3b(endAA);
		startBB.add(halfDim, halfDim, (byte) 0);
		endBB.add(halfDim, halfDim, (byte) 0);

		Vector3b startBC = new Vector3b(startAA);
		Vector3b endBC = new Vector3b(endAA);
		startBC.add(halfDim, halfDim, halfDim);
		endBC.add(halfDim, halfDim, halfDim);

		Vector3b startBD = new Vector3b(startAA);
		Vector3b endBD = new Vector3b(endAA);
		startBD.add((byte) 0, halfDim, halfDim);
		endBD.add((byte) 0, halfDim, halfDim);

		byte halfDimNext = (byte) (halfDim / 2);
		split(0, 0, startAA, endAA, halfDimNext);
		split(1, 0, startAB, endAB, halfDimNext);
		split(2, 0, startAC, endAC, halfDimNext);
		split(3, 0, startAD, endAD, halfDimNext);
		split(4, 0, startBA, endBA, halfDimNext);
		split(5, 0, startBB, endBB, halfDimNext);
		split(6, 0, startBC, endBC, halfDimNext);
		split(7, 0, startBD, endBD, halfDimNext);
	}

}
