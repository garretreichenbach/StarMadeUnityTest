package org.schema.game.client.view.cubes;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.world.DrawableRemoteSegment;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class CubeDataPool {
	public final int POOL_SIZE;
	private final ObjectArrayList<CubeData> pool;
	private final ObjectArrayList<CubeData> taken;
	private int quota;

	private int overQuota;

	public CubeDataPool() {
		POOL_SIZE = EngineSettings.G_MAX_SEGMENTSDRAWN.getInt() + 10;
		pool = new ObjectArrayList<CubeData>(EngineSettings.G_MAX_SEGMENTSDRAWN.getInt());
		taken = new ObjectArrayList<CubeData>(EngineSettings.G_MAX_SEGMENTSDRAWN.getInt());
		//		POOL_SIZE = 100;
		//		pool = new ArrayList<CubeData>(100);
		quota = POOL_SIZE;
	}

	public void checkPoolSize() {
		//		synchronized (pool) {
		//			if(POOL_SIZE != EngineSettings.G_MAX_SEGMENTSDRAWN.getInt()+10){
		//				System.err.println("POOL SIZE CHANGED "+POOL_SIZE+" -----------> "+EngineSettings.G_MAX_SEGMENTSDRAWN.getInt());
		//				for(CubeData mesh : pool){
		//					((DrawableRemoteSegment)mesh.lastTouched).disposeAll();
		//				}
		//				pool.clear();
		//				POOL_SIZE = EngineSettings.G_MAX_SEGMENTSDRAWN.getInt()+10;
		//				quota = POOL_SIZE;
		//			}
		//		}
	}

	public void cleanUp(int sortingSerial) {
		synchronized (pool) {
			boolean changed = false;
			for (int i = 0; i < taken.size(); i++) {
				DrawableRemoteSegment s = (taken.get(i).lastTouched);
				if (s != null && s.getSortingSerial() < sortingSerial - 5000 && !s.isInUpdate()) {
					if (!pool.contains(taken.get(i))) {
						//						boolean empty = pool.isEmpty();
						s.releaseContainerFromPool();
						s.disposeAll();
						s.setActive(false);
						System.err.println("WARNING: Exception: cleaned up stuck mesh! " + s + "; " + s.getSegmentController() + "; SegSector: " + s.getSegmentController().getSectorId() + "; CurrentSector " + ((GameClientState) s.getSegmentController().getState()).getCurrentSectorId());
						i--;
						changed = true;
					}
				} else if (s == null) {
					System.err.println("WARNING: Exception: null mesh! ");
				}
			}

		}

	}

	public void cleanUpGL() {
		synchronized (pool) {
			for (CubeData d : pool) {
				if (d.cubeMesh != null) {
					d.cubeMesh.cleanUp();
				}
				d.lastTouched = null;
			}
			pool.clear();
		}

	}

	private CubeData getDataInstance()  {
		synchronized (pool) {
			if (pool.isEmpty() && quota > 0) {
				CubeData cubeOptOptMesh = new CubeData();
				quota--;
				taken.add(cubeOptOptMesh);
				return cubeOptOptMesh;
			}
			while (pool.isEmpty()) {
				//				System.err.println("POOL IS EMPTY");
				try {
					pool.wait(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (pool.isEmpty()) {
					overQuota++;
					CubeData cubeOptOptMesh = new CubeData();
					taken.add(cubeOptOptMesh);
					System.err.println("[CUBEMESH] WARNING: overquota " + (POOL_SIZE + overQuota));
					return cubeOptOptMesh;
				}

			}
			CubeData remove = pool.remove(0);
			taken.add(remove);
			remove.generated = false;
			assert (pool.size() <= POOL_SIZE);

			return remove;
		}
	}

	public CubeData getMesh(DrawableRemoteSegment e)  {
		synchronized (pool) {
			CubeData cubeOptOptMesh = getDataInstance();
			cubeOptOptMesh.lastTouched = e;
			assert (cubeOptOptMesh.lastTouched != null);
			return cubeOptOptMesh;
		}
	}

	public boolean isFree(CubeData oldMesh) {
		return pool.contains(oldMesh);
	}

	public void release(CubeData c) {
		synchronized (pool) {
			if (c != null) {
				c.released();
				c.lastTouched = null;
				if (!pool.contains(c)) {
					//					boolean empty = pool.isEmpty();
					pool.add(c);
					taken.remove(c);
					pool.notify();
					//					if(empty){
					//						System.err.println("POOL RELEASED : PSIZE "+pool.size());
					//					}
					//					System.err.println("RELEASED : "+pool.size());
				}
			}
		}

	}

	public int size() {
		return pool.size();
	}

	public String stats() {
		return pool.size() + " / " + taken.size() + " / " + (POOL_SIZE + overQuota);
	}

}
