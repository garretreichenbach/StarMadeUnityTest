package org.schema.game.common.data.physics.octree;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3b;
import org.schema.game.common.data.world.SegmentData;

public class Octree {

	private static final byte ROOT_HALF_DIM = SegmentData.SEG_HALF;
	//	private static ThreadLocal<OctreeVariableSet> threadLocal = new ThreadLocal<OctreeVariableSet>() {
	//		@Override
	//		protected OctreeVariableSet initialValue() {
	//			return new OctreeVariableSet();
	//		}
	//	};
	//
	public static OctreeVariableSet serverSet = new OctreeVariableSet();
	public static OctreeVariableSet clientSet = new OctreeVariableSet();
	public static boolean dr;
	//	public static void initCache(boolean onServer){
	//		get(onServer).initializeCache();
	//	}
	private final OctreeNode root;
	private int occTreeCount = 0;

	public Octree(final int maxLevel, boolean onServer) {
		super();

		synchronized (get(onServer)) {

			if (get(onServer).first) {

				get(onServer).initializeCache();
				assert (get(onServer).maxLevel == 0);
				get(onServer).maxLevel = maxLevel;
				Vector3b start = new Vector3b();
				Vector3b end = new Vector3b();
				start.add((byte) (-SegmentData.SEG_HALF), (byte) (-SegmentData.SEG_HALF), (byte) (-SegmentData.SEG_HALF));
				end.add((byte)(SegmentData.SEG_HALF), (byte)(SegmentData.SEG_HALF), (byte)(SegmentData.SEG_HALF));
				System.err.println("[OCTREE] Building Octree");
				root = new OctreeNode(start, end, 0, (byte) 0, maxLevel, onServer);
				buildOctree();
				get(onServer).first = false;
				if (onServer) {
					System.err.println((onServer ? "[SERVER]" : "[CLIENT]") + "[OCTREE] NODES: " + OctreeVariableSet.nodes);
				}
			} else {
				assert (get(onServer).maxLevel == maxLevel);
				root = new OctreeNode(0, (byte) 0, maxLevel, onServer);
				buildOctree();
			}
		}
	}

	//	public static void clearCache(StateInterface state, boolean onServer){
	//		get(onServer).clearCache(state);
	//	}
	public static void dr(boolean dr, boolean onServer) {
		get(onServer).dr = dr;
	}

	public static OctreeVariableSet get(boolean onServer) {
		return onServer ? serverSet : clientSet;
	}

	//	public static int getCacheSize(boolean onServer){
	//		return get(onServer).getCacheSize();
	//	}
	//	public static int getCacheTaken(boolean onServer){
	//		return get(onServer).getChacheUsedSize();
	//	}
	public static int getTreeCacheIndex(byte x, byte y, byte z) {
		return z * SegmentData.SEG_TIMES_SEG + y * SegmentData.SEG + x;
	}

	private void buildOctree() {
		if (root.getMaxLevel() > 0) {

			occTreeCount += root.split(0, 0);
		} else {
		}
	}

	public void delete(byte x, byte y, byte z) {
		int treeCacheIndex = getTreeCacheIndex(x, y, z);
		if (!root.getSet().treeCache[treeCacheIndex].initialized) {
			root.delete((byte) (x - ROOT_HALF_DIM), (byte) (y - ROOT_HALF_DIM), (byte) (z - ROOT_HALF_DIM), root.getSet().treeCache[treeCacheIndex], 0);
			root.getSet().treeCache[treeCacheIndex].initialized = true;
		} else {
			root.insertCached(root.getSet().treeCache[treeCacheIndex], 0);
		}

	}

	public void drawOctree(Vector3f offset) {
		root.drawOctree(offset, true);
	}

	public int getOccTreeCount() {
		return occTreeCount;
	}

	public OctreeNode getRoot() {
		return root;
	}

	public void insert(byte x, byte y, byte z) {
		int treeCacheIndex = getTreeCacheIndex(x, y, z);

		if (!root.getSet().treeCache[treeCacheIndex].initialized) {
			root.insert((byte) (x - ROOT_HALF_DIM), (byte) (y - ROOT_HALF_DIM), (byte) (z - ROOT_HALF_DIM), root.getSet().treeCache[treeCacheIndex], 0);
			root.getSet().treeCache[treeCacheIndex].initialized = true;
		} else {
			root.insertCached(root.getSet().treeCache[treeCacheIndex], 0);
		}

	}

	public void reset() {
		root.reset();

	}

}
