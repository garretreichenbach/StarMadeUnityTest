package org.schema.schine.physics;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3b;

import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

public abstract class ClosestRayCastResultExt extends ClosestRayResultCallback {

	private final Vector3b cubePos = new Vector3b();
	private Object owner;
	private Object userData;
	private IgnoreBlockRayTestInterface ignInterface;
	public final Object innerSegmentIterator;
	public boolean considerAllDockedAsOwner;

	public ClosestRayCastResultExt(Vector3f rayFromWorld, Vector3f rayToWorld) {
		super(rayFromWorld, rayToWorld);
		this.innerSegmentIterator = newInnerSegmentIterator();
	}

	public abstract Object newInnerSegmentIterator();
	
	public Object getOwner() {
		return owner;
	}

	/**
	 * @return the userData
	 */
	public Object getUserData() {
		return userData;
	}

	/**
	 * @param userData the userData to set
	 */
	public void setUserData(Object userData) {
		this.userData = userData;
	}

	/**
	 * @return true, if collision should not stop at the nearest collision but record blocks along the way
	 */
	public boolean isRecordAllBlocks() {
		assert(false);
		return false;
	}

	/**
	 * used only if isRecordAllBlocks() return true
	 *
	 * @return how many blocks after the nearest one should be recorded
	 */
	public int getBlockDeepness() {
		return 0;
	}

	/**
	 * used only if isRecordAllBlocks() return true (can be null otherwise)
	 *
	 * @return the map of id->list of recorded blocks in order of nearest first
	 */
	public Int2ObjectOpenHashMap<?> getRecordedBlocks() {
		return null;
	}

	/**
	 * @return true, if collision should use a map id->list of blocks that the collision will ignore
	 * <p/>
	 * If this is set to true, getCollidingBlocks() may not return null
	 */
	public boolean isHasCollidingBlockFilter() {
		return false;
	}

	/**
	 * only used if isHasCollidingBlockFilter() returns true. (can be null otherwise)
	 *
	 * @return a map of ID -> lists of blocks that will seen as nonsolid in the collision
	 */
	public Int2ObjectOpenHashMap<LongOpenHashSet> getCollidingBlocks() {
		return null;
	}

	/**
	 * @return true if collisions should ignore debris (shattered blocks)
	 */
	public boolean isIgnoreDebris() {
		return false;
	}
	
	public boolean isCubesOnly() {
		return false;
	}
	/**
	 * @return true if non physical blocks (e.g. flowers should be ignored by this collision)
	 */
	public boolean isIgnoereNotPhysical() {
		return false;
	}

	/**
	 * @return collision local point (x, y, z are between 0 and 15)
	 */
	public Vector3b getCubePos() {
		return cubePos;
	}

	/**
	 * @return collision segment
	 */
	public Object getSegment() {
		return null;
	}

	public void setSegment(Object segment) {
	}

	public boolean isDebug() {
		return false;
	}

	/**
	 * @return object filter (SegmentController). Will only test for that object. May be null to test for all objects
	 */
	public Object getFilter() {
		return null;
	}

	/**
	 * @return true if blocks with 0 hp should be considered in the collision
	 */
	public boolean isZeroHpPhysical() {
		return true;
	}

	/**
	 * @return true if collision should only be happening with cubes
	 */
	public boolean isOnlyCubeMeshes() {
		return false;
	}

	public boolean isDamageTest() {
		return false;
	}

	/**
	 * Will not do any hitpoint and hit normal calculation
	 * @return
	 */
	public boolean isSimpleRayTest() {
		return false;
	}

	public void setOwner(Object owner) {
		this.owner = owner;
	}

	public boolean isIgnoreBlockType(short type) {
		return ignInterface != null && ignInterface.ignoreBlock(type);
	}

	public void setIgnInterface(IgnoreBlockRayTestInterface ignInterface) {
		this.ignInterface = ignInterface;
	}

}
