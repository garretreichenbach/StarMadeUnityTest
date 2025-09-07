package org.schema.game.client.view.cubes;

import org.schema.common.util.MemoryManager;
import org.schema.game.client.view.SegmentDrawer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class CubeMeshBufferContainerPool {

	private static final int POOL_SIZE = SegmentDrawer.LIGHTING_THREAD_COUNT*3;
	private static final ObjectArrayList<CubeMeshBufferContainer> pool = new ObjectArrayList<CubeMeshBufferContainer>(POOL_SIZE);
	private static boolean initialized;

	public static CubeMeshBufferContainer get() {
		synchronized (pool) {
			while (pool.isEmpty()) {
				try {
					pool.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return pool.remove(pool.size()-1);
		}
	}

	public static void initialize() {
		if (!initialized) {
			MemoryManager man = new MemoryManager();
			for (int i = 0; i < POOL_SIZE; i++) {
				pool.add(CubeMeshBufferContainer.getInstance(man));
			}
			man.allocateMemory();
			initialized = true;
		}
	}

	public static void release(CubeMeshBufferContainer c) {
		assert (!pool.contains(c));
		synchronized (pool) {
			pool.add(c);
			pool.notify();
		}
		assert (pool.size() <= POOL_SIZE);
	}

	public static int size() {
		return pool.size();
	}
}
