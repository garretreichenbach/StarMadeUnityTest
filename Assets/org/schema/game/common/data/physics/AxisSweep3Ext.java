package org.schema.game.common.data.physics;

import javax.vecmath.Vector3f;

import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.broadphase.OverlappingPairCache;

public class AxisSweep3Ext extends AxisSweep3 {

	public AxisSweep3Ext(Vector3f worldAabbMin, Vector3f worldAabbMax) {
		super(worldAabbMin, worldAabbMax);

	}
	//	protected static class HandleImpl extends Handle {
	//		private short minEdges0;
	//		private short minEdges1;
	//		private short minEdges2;
	//
	//		private short maxEdges0;
	//		private short maxEdges1;
	//		private short maxEdges2;
	//
	//		@Override
	//		public int getMinEdges(int edgeIndex) {
	//			switch (edgeIndex) {
	//				default:
	//				case 0: return minEdges0 & 0xFFFF;
	//				case 1: return minEdges1 & 0xFFFF;
	//				case 2: return minEdges2 & 0xFFFF;
	//			}
	//		}
	//
	//		@Override
	//		public void setMinEdges(int edgeIndex, int value) {
	//			switch (edgeIndex) {
	//				case 0: minEdges0 = (short)value; break;
	//				case 1: minEdges1 = (short)value; break;
	//				case 2: minEdges2 = (short)value; break;
	//			}
	//		}
	//
	//		@Override
	//		public int getMaxEdges(int edgeIndex) {
	//			switch (edgeIndex) {
	//				default:
	//				case 0: return maxEdges0 & 0xFFFF;
	//				case 1: return maxEdges1 & 0xFFFF;
	//				case 2: return maxEdges2 & 0xFFFF;
	//			}
	//		}
	//
	//		@Override
	//		public void setMaxEdges(int edgeIndex, int value) {
	//			switch (edgeIndex) {
	//				case 0: maxEdges0 = (short)value; break;
	//				case 1: maxEdges1 = (short)value; break;
	//				case 2: maxEdges2 = (short)value; break;
	//			}
	//		}
	//	}

	public AxisSweep3Ext(Vector3f worldAabbMin, Vector3f worldAabbMax,
	                     int maxHandles) {
		super(worldAabbMin, worldAabbMax, maxHandles);

	}

	public AxisSweep3Ext(Vector3f worldAabbMin, Vector3f worldAabbMax,
	                     int maxHandles, OverlappingPairCache pairCache) {
		super(worldAabbMin, worldAabbMax, maxHandles, pairCache);

	}

	// allocation/deallocation
	@Override
	protected int allocHandle() {

		if (firstFreeHandle == 0) {

			//pool is empty -> grow pool size
			int lastMaxHandles = maxHandles;
			Handle[] lastHandles = pHandles;
			int newSize = (pHandles.length - 1) * 2 + 1;

			System.err.println("[Physics][AXIS-SWEEP] Handle Array grows: " + lastHandles.length + " -> " + newSize);

			pHandles = new HandleImpl[newSize];
			for (int i = 0; i < lastHandles.length; i++) {
				pHandles[i] = lastHandles[i];
			}
			int newHandlesFirstIndex = lastHandles.length;

			this.maxHandles = newSize;

			// handle 0 is reserved as the null index, and is also used as the sentinel
			for (int i = newHandlesFirstIndex; i < maxHandles; i++) {
				pHandles[i] = createHandle();
				pHandles[i].setNextFree(i + 1);
			}
			pHandles[maxHandles - 1].setNextFree(0);

			firstFreeHandle = newHandlesFirstIndex;

			// allocate edge buffers
			for (int i = 0; i < 3; i++) {
				EdgeArray oldEdgeArray = pEdges[i];
				pEdges[i] = createEdgeArray(maxHandles * 2);

				((EdgeArrayImplExt) pEdges[i]).insert((EdgeArrayImplExt) oldEdgeArray);
			}
			assert (assertNonNull());
		}
		assert (firstFreeHandle != 0);
		int handle = firstFreeHandle;
		firstFreeHandle = getHandle(handle).getNextFree();
		numHandles++;

		return handle;
	}

	private boolean assertNonNull() {
		for (int i = 0; i < maxHandles; i++) {
			assert (pHandles[i] != null) : i;
		}
		return true;
	}

	//	/* (non-Javadoc)
	//	 * @see com.bulletphysics.collision.broadphase.AxisSweep3Internal#addHandle(javax.vecmath.Vector3f, javax.vecmath.Vector3f, java.lang.Object, short, short, com.bulletphysics.collision.broadphase.Dispatcher, java.lang.Object)
	//	 */
	//	@Override
	//	public int addHandle(Vector3f aabbMin, Vector3f aabbMax, Object pOwner,
	//			short collisionFilterGroup, short collisionFilterMask,
	//			Dispatcher dispatcher, Object multiSapProxy) {
	////		System.err.println("####### ADDED HANDLE: "+pOwner+" ---- "+multiSapProxy);
	//			//		return super.addHandle(aabbMin, aabbMax, pOwner, collisionFilterGroup,
	//				collisionFilterMask, dispatcher, multiSapProxy);
	//
	//
	//	}

	public void cleanUp() {
		for (int i = 0; i < pHandles.length; i++) {
			pHandles[i].clientObject = null;
			pHandles[i] = null;
		}
		for (int i = 0; i < pEdges.length; i++) {
			pEdges[i] = null;
		}
	}

	public void cleanUpReferences() {
		for (int i = 0; i < pHandles.length; i++) {
			pHandles[i].clientObject = null;
			if (pHandles[i].uniqueId != 0) {
				//				System.err.println("FREEIN "+pHandles[i].uniqueId );
				//				freeHandle(pHandles[i].uniqueId);
			}
			//never clean up handle zero
		}
	}

	@Override
	protected EdgeArray createEdgeArray(int size) {
		return new EdgeArrayImplExt(size);
	}

	protected static class EdgeArrayImplExt extends EdgeArray {
		private short[] pos;
		private short[] handle;

		public EdgeArrayImplExt(int size) {
			pos = new short[size];
			handle = new short[size];
		}

		public void insert(EdgeArrayImplExt old) {
			System.err.println("[Physics][AXIS-SWEEP] EDGE ARRAY INSERTING grow: " + old.pos.length + " -> " + pos.length);
			for (int i = 0; i < old.pos.length; i++) {
				pos[i] = old.pos[i];
				handle[i] = old.handle[i];
			}
		}

		@Override
		public void swap(int idx1, int idx2) {
			short tmpPos = pos[idx1];
			short tmpHandle = handle[idx1];

			pos[idx1] = pos[idx2];
			handle[idx1] = handle[idx2];

			pos[idx2] = tmpPos;
			handle[idx2] = tmpHandle;
		}

		@Override
		public void set(int dest, int src) {
			pos[dest] = pos[src];
			handle[dest] = handle[src];
		}

		@Override
		public int getPos(int index) {
			return pos[index] & 0xFFFF;
		}

		@Override
		public void setPos(int index, int value) {
			pos[index] = (short) value;
		}

		@Override
		public int getHandle(int index) {
			return handle[index] & 0xFFFF;
		}

		@Override
		public void setHandle(int index, int value) {
			handle[index] = (short) value;
		}
	}

}
