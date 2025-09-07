package org.schema.game.common.data.world;

import java.nio.ByteBuffer;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.SegmentDrawer;
import org.schema.game.client.view.cubes.CubeData;
import org.schema.game.client.view.cubes.CubeMeshBufferContainer;
import org.schema.game.client.view.cubes.CubeMeshBufferContainerPool;
import org.schema.game.client.view.cubes.cubedyn.LODCubeMeshManagerBulkOptimized;
import org.schema.game.client.view.cubes.cubedyn.VBOCell;
import org.schema.game.common.controller.SegmentBuffer;
import org.schema.game.common.controller.SegmentController;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.network.objects.container.TransformTimed;

import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.Transform;

import it.unimi.dsi.fastutil.longs.LongArrayList;

public class DrawableRemoteSegment extends RemoteSegment {

	private static Matrix3f abs_b = new Matrix3f();
	private static Vector3f center = new Vector3f();
	private static Vector3f extent = new Vector3f();
	private static Vector3f tmp = new Vector3f();
	private static Vector3f localHalfExtents = new Vector3f();
	private static Vector3f localCenter = new Vector3f();
	public SegmentBuffer segmentBufferAABBHelperSorting;
	public SegmentBuffer segmentBufferAABBHelper;
	public long lastDrawn = -1;
	public Object cubeMeshLock = new Object();
	public long occlusionFailTime;
	public boolean occlusionFailed;

	public boolean exceptDockingBlock;
	public boolean exceptDockingBlockMarked;
	
	//	private int cacheDateCameraDist;
	//	private float cachedCameraDistance;
	public RequestState requestState = RequestState.INACTIVE;
	public int requestedRecursive = 0;
	public boolean inLightingQueue;
	public int sortingId;
	public long lastLightingUpdateTime;
	public float camDist;
	public float lastSegmentDistSquared;
	public boolean cachedFrustumSet;
	public boolean cachedFrustum;
	private boolean needsMeshUpdate;
	private boolean needsVisUpdate = true;
	private boolean inUpdate = false;

	private boolean hasVisibleElements = true;
	private CubeMeshBufferContainer currentBufferContainer;
	private CubeData nextCubeMesh;
	private CubeData currentCubeMesh;
	private boolean active;
	private int sortingSerial;
//	private Transform latAABBCheck = new Transform();
	private Vector3f minAABBChached = new Vector3f();
	private Vector3f maxAABBChached = new Vector3f();
	private short frustumCacheNum;
	private boolean inFrustumCache = false;
	private float percentageDrawn = 1;
	public int lightTries;
	public LongArrayList onEdge;
	private long clientTransformCacheTime = Long.MIN_VALUE;
	
	
	public LODMeshData LODMeshLock = new LODMeshData();

	public static class LODMeshData{
		public long updateBasedOn;
		public long lastRequest;
		public boolean queued;
		public final ByteBuffer[] queuedBuffer = new ByteBuffer[2];
		public int[][] bufferPosAndSize = new int[1][2];
		public final VBOCell currentVBOCell[] = new VBOCell[2];
		public boolean updateFlag;
		


		public void cleanUp(LODCubeMeshManagerBulkOptimized man) {
			for(int i = 0; i < currentVBOCell.length; i++ ) {
				if (currentVBOCell[i] != null) {
					currentVBOCell[i].released();
					currentVBOCell[i] = null;
				}
			}
		}
	}
	
	//	public DrawableRemoteSegment replaced;
	public DrawableRemoteSegment(SegmentController segmentController) {
		super(segmentController);

	}

	public static int[] decodeAmbient(byte input, int[] output) {
		int bMin = input - Byte.MIN_VALUE;
		//1
		//2

		//4
		//8

		//16
		//32

		//64
		//128
		output[0] = 3 & bMin;
		output[1] = 3 & (bMin >> 2);
		output[2] = 3 & (bMin >> 4);
		output[3] = 3 & (bMin >> 6);
		return output;
	}

	public static byte encodeAmbient(int[] input) {
		//1
		//2

		//4
		//8

		//16
		//32

		//64
		//128
		int one = (input[0]);
		int two = (4 * input[1]);
		int three = (16 * input[2]);
		int four = (64 * input[3]);
		return (byte) (Byte.MIN_VALUE + (one + two + three + four));
	}

	public static void transformAabb(Vector3f localAabbMin, Vector3f localAabbMax, float margin, Transform trans, Vector3f aabbMinOut, Vector3f aabbMaxOut) {
		assert (localAabbMin.x <= localAabbMax.x);
		assert (localAabbMin.y <= localAabbMax.y);
		assert (localAabbMin.z <= localAabbMax.z);

		localHalfExtents.sub(localAabbMax, localAabbMin);
		localHalfExtents.scale(0.5f);

		localHalfExtents.x += margin;
		localHalfExtents.y += margin;
		localHalfExtents.z += margin;

		localCenter.add(localAabbMax, localAabbMin);
		localCenter.scale(0.5f);

		abs_b.set(trans.basis);
		MatrixUtil.absolute(abs_b);

		center.set(localCenter);
		trans.transform(center);

		abs_b.getRow(0, tmp);
		extent.x = tmp.dot(localHalfExtents);
		abs_b.getRow(1, tmp);
		extent.y = tmp.dot(localHalfExtents);
		abs_b.getRow(2, tmp);
		extent.z = tmp.dot(localHalfExtents);

		aabbMinOut.sub(center, extent);
		aabbMaxOut.add(center, extent);
	}

	public void applyCurrent() {
		synchronized (cubeMeshLock) {

			CubeData old = currentCubeMesh;

			assert (currentCubeMesh != nextCubeMesh);
			// current <- next
			currentCubeMesh = nextCubeMesh;
			// release current
			SegmentDrawer.dataPool.release(old);

			nextCubeMesh = null;

		}
	}
	public void keepOld() {
		synchronized (cubeMeshLock) {
			
			CubeData old = currentCubeMesh;
			
			// release new
			if(nextCubeMesh != null){
				SegmentDrawer.dataPool.release(nextCubeMesh);
			}
			
			nextCubeMesh = null;
			
		}
	}

	@Override
	public void dataChanged(boolean addedDeleted) {
		super.dataChanged(addedDeleted);
		setNeedsMeshUpdate(true);
		this.LODMeshLock.updateFlag = true;
		if(!((GameClientState) getSegmentController().getState()).isPassive()){
			((GameClientState) getSegmentController().getState()).getWorldDrawer().getBuildModeDrawer().flagUpdate();
		}

	}

	public void disposeAll() {
		synchronized (cubeMeshLock) {
			disposeCurrent();
			disposeNext();
		}
	}

	public void disposeCurrent() {
		synchronized (cubeMeshLock) {
			SegmentDrawer.dataPool.release(currentCubeMesh);
			currentCubeMesh = null;
		}
	}

	public void disposeNext() {
		synchronized (cubeMeshLock) {
			SegmentDrawer.dataPool.release(nextCubeMesh);
			nextCubeMesh = null;
		}
	}

	public void getAABBClient(Vector3f minOut, Vector3f maxOut, Vector3f posOut) {
		TransformTimed t = getSegmentController().getWorldTransformOnClient();
		if (t.lastChanged != clientTransformCacheTime) {
			minOut.set(pos.x, pos.y, pos.z);
			maxOut.set(minOut);
			maxOut.x += SegmentData.SEG_HALF;
			maxOut.y += SegmentData.SEG_HALF;
			maxOut.z += SegmentData.SEG_HALF;

			minOut.x -= SegmentData.SEG_HALF;
			minOut.y -= SegmentData.SEG_HALF;
			minOut.z -= SegmentData.SEG_HALF;

			transformAabb(minOut, maxOut, 0, t, minAABBChached, maxAABBChached);
			clientTransformCacheTime = t.lastChanged;
		}
		minOut.set(minAABBChached);
		maxOut.set(maxAABBChached);
		posOut.set(
				minOut.x + (maxOut.x - minOut.x) * 0.5f,
				minOut.y + (maxOut.y - minOut.y) * 0.5f,
				minOut.z + (maxOut.z - minOut.z) * 0.5f);
	}

	//	public float getCameraDistance(){
	//		if(getSegmentController().getState().getUpdateNumber() == cacheDateCameraDist){
	//			return cachedCameraDistance;
	//		}else{
	//			tV = getWorldPosition(tV);
	//			tV.sub(Controller.getCamera().getPos());
	//			cacheDateCameraDist = getSegmentController().getState().getUpdateNumber();
	//			cachedCameraDistance = tV.length();
	//			return cachedCameraDistance;
	//		}
	//	}
	public CubeMeshBufferContainer getContainerFromPool()  {
		assert (currentBufferContainer == null);

		CubeMeshBufferContainer cubeMeshBufferContainer = CubeMeshBufferContainerPool.get();
		this.currentBufferContainer = cubeMeshBufferContainer;
		return cubeMeshBufferContainer;
	}

	/**
	 * @return the currentBufferContainer
	 */
	public CubeMeshBufferContainer getCurrentBufferContainer() {
		return currentBufferContainer;
	}

	/**
	 * @return the currentCubeMesh
	 */
	public CubeData getCurrentCubeMesh() {
		return currentCubeMesh;
	}

	/**
	 * @param currentCubeMesh the currentCubeMesh to set
	 */
	public void setCurrentCubeMesh(CubeData currentCubeMesh) {
		this.currentCubeMesh = currentCubeMesh;
	}

	/**
	 * @return the nextCubeMesh
	 */
	public CubeData getNextCubeMesh() {
		return nextCubeMesh;
	}

	/**
	 * @param nextCubeMesh the nextCubeMesh to set
	 */
	public void setNextCubeMesh(CubeData nextCubeMesh) {
		this.nextCubeMesh = nextCubeMesh;
	}

	public Vector3i getPos() {
		return pos;
	}

	public int getSortingSerial() {
		return sortingSerial;
	}

	public void setSortingSerial(int sortingSerial) {
		this.sortingSerial = sortingSerial;
	}


	/**
	 * @return the hasVisibleElements
	 */
	public boolean hasVisibleElements() {
		return hasVisibleElements;
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		if (this.active != active) {
			getSegmentController().getSegmentBuffer().incActive(active ? 1 : -1, this);
		}
		this.active = active;
	}

	/**
	 * @return the inUpdate
	 */
	public boolean isInUpdate() {
		return inUpdate;
	}

	/**
	 * @param inUpdate the inUpdate to set
	 * @param string
	 */
	public void setInUpdate(boolean inUpdate) {
		//		System.err.println("SET TO "+("SET TO "+inUpdate+" "+pos)+" FROM "+string);
		//		try{
		//			throw new NullPointerException("SET TO "+inUpdate+" "+pos);
		//		}catch (Exception e) {
		//			e.printStackTrace();
		//		}
		this.inUpdate = inUpdate;
	}

	/**
	 * @return the needsVisUpdate
	 */
	public boolean isNeedsVisUpdate() {
		return needsVisUpdate;
	}

	/**
	 * @param needsVisUpdate the needsVisUpdate to set
	 */
	public void setNeedsVisUpdate(boolean needsVisUpdate) {
		this.needsVisUpdate = needsVisUpdate;
	}

	public boolean needsMeshUpdate() {
		boolean d = needsMeshUpdate || (getSegmentController().percentageDrawn != this.percentageDrawn );
		this.percentageDrawn = getSegmentController().percentageDrawn;
		return d;
	}

	public void releaseContainerFromPool() {
		if (currentBufferContainer != null) {
			CubeMeshBufferContainerPool.release(currentBufferContainer);
			currentBufferContainer = null;
		}
	}

	/**
	 * @param hasVisibleElements the hasVisibleElements to set
	 */
	public void setHasVisibleElements(boolean hasVisibleElements) {
		this.hasVisibleElements = hasVisibleElements;
	}

	public void setNeedsMeshUpdate(boolean needsMeshUpdate) {

		if (needsMeshUpdate) {
			
			//reset for next lighting update
			hasVisibleElements = true;
		}
		
		this.needsMeshUpdate = needsMeshUpdate;
	}

	/**
	 * WARNING: must calc the AABB BEFORE calling this
	 *
	 * @param s
	 * @return
	 */
	public boolean isInViewFrustum(short updateNum) {

		if (frustumCacheNum == updateNum) {
			return inFrustumCache;
		} else {
			inFrustumCache = Controller.getCamera().isAABBInFrustum(minAABBChached, maxAABBChached);
			frustumCacheNum = updateNum;
			return inFrustumCache;
		}

	}

	public SegmentBuffer getSegmentBufferRegion() {
		return getSegmentController().getSegmentBuffer().getBuffer(pos);
	}

	public enum RequestState {
		INACTIVE(1),
		JUST_ADDED(1),
		ALL_REQUESTS_DONE(0),
		TOO_FAR(0),
		INVALID(0);

		public int code;

		private RequestState(int i) {
			code = i;
		}
	}
}
