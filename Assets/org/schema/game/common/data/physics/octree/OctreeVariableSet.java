package org.schema.game.common.data.physics.octree;

import java.nio.ByteBuffer;
import java.util.HashMap;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import org.lwjgl.system.MemoryUtil;
import org.schema.common.util.linAlg.Vector3b;

public class OctreeVariableSet {
	public static final int MAX_IDS = 4097;
	public static Vector3f localHalfExtend = new Vector3f();
	public static Vector3f[] localHalfExtends = new Vector3f[4];
	public static Vector3f[] localCentersAdd = new Vector3f[4];
	public static int nodes;

	static {
		int a = 8;
		for (int i = 0; i < localHalfExtends.length; i++) {
			Vector3f localAabbMin = new Vector3f(-a, -a, -a);
			Vector3f localAabbMax = new Vector3f(a, a, a);

			localCentersAdd[i] = new Vector3f();
			localHalfExtends[i] = new Vector3f();
			localHalfExtends[i].sub(localAabbMax, localAabbMin);
			localHalfExtends[i].scale(0.5f);

			localHalfExtends[i].x += 0.1f;
			localHalfExtends[i].y += 0.1f;
			localHalfExtends[i].z += 0.1f;

			localCentersAdd[i].set(a, a, a);
			a /= 2;
		}
	}

	public final Vector3f aabbHalfExtent = new Vector3f();
	public final Vector3f aabbCenter = new Vector3f();
	public final Vector3f source = new Vector3f();
	public final Vector3f target = new Vector3f();
	public final Vector3f r = new Vector3f();
	public final Vector3f hitNormal = new Vector3f();
	public final Vector3f closest = new Vector3f();
	public boolean dr;
	public TreeCache[] treeCache = new TreeCache[4096];
	public HashMap<OctreeLevel, Short> map = new HashMap<OctreeLevel, Short>();
	public ByteBuffer mapV = MemoryUtil.memAlloc(2340 * 3);
	public int maxLevel;
	public Vector3f localHalfExtents = new Vector3f();
	public Vector3f localCenter = new Vector3f();
	public Matrix3f abs_b = new Matrix3f();
	public Vector3f center = new Vector3f();

	//	public Vector3b[] mapV = new Vector3b[2340];
	public Vector3f extend = new Vector3f();
	public Vector3f tmpAB = new Vector3f();
	public boolean debug;
	boolean first = true;
	Vector3b min = new Vector3b();
	Vector3b max = new Vector3b();
	Vector3b minTest = new Vector3b();
	Vector3b maxTest = new Vector3b();
	Vector3f tmpMin = new Vector3f();
	Vector3f tmpMax = new Vector3f();
	Vector3f tmpMinOut = new Vector3f();
	Vector3f tmpMaxOut = new Vector3f();
	Vector3f tmpMin2 = new Vector3f();
	Vector3f tmpMax2 = new Vector3f();
	Vector3f tmpMinOut2 = new Vector3f();
	Vector3f tmpMaxOut2 = new Vector3f();
	Vector3f tmpDistTest = new Vector3f();
	float[] param = new float[1];
	Vector3f normal = new Vector3f();
	short gen = 0;
	//	private float[][] cachePool = new float[4096][6];
	//	private float[][] cachePoolExtend;
	//	private int cachePointer;
	//	private boolean signalCacheOverflow;
	//	private boolean cacheExtensionRunning;
	//	private boolean cacheExtensionFinished;
	private boolean cacheInitialized;
	private OctreeLevel tmp = new OctreeLevel();

	public OctreeVariableSet() {
	}

	//	public void clearCache(StateInterface s){
	//
	//		if(signalCacheOverflow){
	//			if(cacheExtensionFinished){
	//				cachePool = cachePoolExtend;
	//				cacheExtensionFinished = false;
	//				cacheExtensionRunning = false;
	//				signalCacheOverflow = false;
	//			}else if(!cacheExtensionRunning){
	//				System.err.println("EXTENDING CACHE octTree to "+cachePool.length*2);
	//				cacheExtensionRunning = true;
	//				Thread t = new Thread(new Runnable() {
	//					@Override
	//					public void run() {
	//						cachePoolExtend = new float[cachePool.length*2][6];
	//						for(int i = 0; i < cachePool.length*2; i++){
	//							if(i < cachePool.length){
	//								cachePoolExtend[i] = cachePool[i];
	//							}
	//						}
	//						cacheExtensionFinished = true;
	//					}
	//				});
	//				t.start();
	//			}
	//		}
	//		cachePointer = 0;
	//	}
	public void get(short id, Vector3b out) {
		int index = id * 3;
		out.set(mapV.get(index), mapV.get(index + 1), mapV.get(index + 2));
	}

	public void get(short id, Vector3f out) {
		int index = id * 3;
		out.set(mapV.get(index), mapV.get(index + 1), mapV.get(index + 2));
	}

	public short getId(byte level, int index, int typeid) {

		tmp.level = level;
		tmp.index = index;
		tmp.id = typeid;
		short id = map.get(tmp);
		return id;
	}

	public byte getX(short id) {
		return mapV.get(id * 3);
	}

	public byte getY(short id) {
		return mapV.get(id * 3 + 1);
	}

	public byte getZ(short id) {
		return mapV.get(id * 3 + 2);
	}

	//	public int getCacheSize(){
	//		return cachePool.length;
	//	}
	//	public int getChacheUsedSize(){
	//		return cachePointer;
	//	}
	//	public float[] getFromCache(){
	//		if(cachePointer < cachePool.length){
	//			float[] vector3f = cachePool[cachePointer++];
	//			return vector3f;
	//		}else{
	//			float[] vector3f = new float[6];
	//			signalCacheOverflow = true;
	//			return vector3f;
	//		}
	//
	//	}
	//	public float[] getFromCache(IntersectionCallback intersectionCall) {
	//		long t = System.nanoTime();
	//
	//
	//		if(cachePointer < cachePool.length){
	//			float[] vector3f = cachePool[cachePointer++];
	//			intersectionCall.aabbRetrieve += (System.nanoTime() - t);
	//			return vector3f;
	//		}else{
	//			float[] vector3f = new float[6];
	//			signalCacheOverflow = true;
	//			intersectionCall.aabbRetrieve += (System.nanoTime() - t);
	//			return vector3f;
	//		}
	//
	//	}
	public void initializeCache() {
		if (!cacheInitialized) {

			for (int i = 0; i < treeCache.length; i++) {
				treeCache[i] = new TreeCache();
			}
			cacheInitialized = true;
		}
	}

	public short put(byte level, int index, int id, Vector3b val) {
		OctreeLevel key = new OctreeLevel();
		key.level = level;
		key.index = index;
		key.id = id;
		assert (!map.containsKey(key)) : level + "; " + index + "; " + id + ": " + map;
		map.put(key, gen);
		mapV.put(gen * 3 + 0, val.x);
		mapV.put(gen * 3 + 1, val.y);
		mapV.put(gen * 3 + 2, val.z);
		short realid = gen;
		gen++;

		return realid;
	}

}
