package org.schema.game.common.controller.generator;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.element.world.ClientSegmentProvider;
import org.schema.game.client.controller.element.world.SegmentQueueManager;
import org.schema.game.common.controller.CreatorThreadControlInterface;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.world.RemoteSegment;
import org.schema.game.common.data.world.Segment;
import org.schema.game.server.controller.RequestData;
import org.schema.game.server.controller.RequestDataIcoPlanet;
import org.schema.game.server.controller.TerrainChunkCacheElement;

import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;

public abstract class CreatorThread {

	public static final int NO_CONCURRENT = 0;
	public static final int LOCAL_CONCURRENT = 1;
	public static final int FULL_CONCURRENT = 2;
	public static ObjectArrayFIFOQueue<RandomRequestData> randomPool = new ObjectArrayFIFOQueue<RandomRequestData>();
	private final SegmentController segmentController;
	private boolean active = true;
	private boolean alive = true;

	public CreatorThread(SegmentController world) {
		super();
		this.segmentController = world;
		//		this.setName(segmentController+"_CREATOR");
		((CreatorThreadControlInterface) this.segmentController.getState().getController()).getCreatorThreadController().addCreatorThread(segmentController);

	}


	public SegmentController getSegmentController() {
		return segmentController;
	}

	public boolean isAlive() {
		return alive;
	}

	public abstract int isConcurrent();

	public abstract int loadFromDatabase(Segment ws);

	public abstract void onNoExistingSegmentFound(Segment ws, RequestData requestData);

	public abstract boolean predictEmpty(Vector3i pos);

	public boolean requestQueueHandle(SegmentQueueManager clientQueueManager) {
		if (active) {
			((ClientSegmentProvider) segmentController.getSegmentProvider()).updateThreaded();
//			return ((ClientSegmentProvider) getSegmentController().getSegmentProvider()).handleNextQueueElement(clientQueueManager);
		} else {
			segmentController.getSegmentProvider().updateThreaded();
		}
		return false;
	}

	public void terminate() {
		active = false;
	}

	public RequestData allocateRequestData(int x, int y, int z) {
		synchronized (randomPool) {
			if (randomPool.isEmpty()) {
				return new RandomRequestData();
			} else {
				return randomPool.dequeue();
			}
		}
	}

	public void freeRequestData(RequestData data, int x, int y, int z) {
		synchronized (randomPool) {
			randomPool.enqueue((RandomRequestData) data);
		}
	}

	public void doFirstStage(RemoteSegment preData,
			TerrainChunkCacheElement fromCache, RequestDataIcoPlanet requestData) {
		
	}

	public boolean predictFirstStageEmpty(Vector3i position) {
		return false;
	}

}
