package org.schema.game.common.controller.generator;

import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.PlanetIco;
import org.schema.game.common.data.IcosahedronHelper;
import org.schema.game.common.data.world.*;
import org.schema.game.server.controller.*;
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructureList;
import org.schema.game.server.controller.world.factory.planet.terrain.TerrainGenerator;
import org.schema.game.server.data.ServerConfig;

public class PlanetIcoCreatorThread extends CreatorThread {

	public static final ObjectArrayFIFOQueue<RequestData> dataPool = new ObjectArrayFIFOQueue<RequestData>(ServerConfig.CHUNK_REQUEST_THREAD_POOL_SIZE_CPU.getInt());

	private TerrainGenerator terrainGenerator;

	static {
		for(int i = 0; i < ServerConfig.CHUNK_REQUEST_THREAD_POOL_SIZE_CPU.getInt(); i++) {
			dataPool.enqueue(new RequestDataIcoPlanet());
		}
	}

	@Override
	public RequestData allocateRequestData(int x, int y, int z) {
		synchronized(dataPool) {
			//lock column
			while(dataPool.isEmpty()) {
				try {
					dataPool.wait();
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
			return dataPool.dequeue();

		}
	}

	@Override
	public void freeRequestData(RequestData data, int x, int y, int z) {
		assert (data != null);
		synchronized(dataPool) {
			//unlock column
			dataPool.enqueue(data);
			dataPool.notify();
		}
	}

	public PlanetIcoCreatorThread(PlanetIco world, TerrainGenerator terrainGenerator) {
		super(world);
		assert (terrainGenerator != null);
		this.terrainGenerator = terrainGenerator;
	}

	@Override
	public int isConcurrent() {
		return FULL_CONCURRENT;
	}

	@Override
	public int loadFromDatabase(Segment ws) {
		return 0;
	}

	private static final int CENTER_INDEX = GeneratorGridLock.getGridCoordinateLocal(1, 1, 1);

	@Override
	public void onNoExistingSegmentFound(Segment w, RequestData requestData) {

		Vector3i pos = new Vector3i();
		w.getAbsoluteElemPos(0, pos);

		RequestDataIcoPlanet rqIco = (RequestDataIcoPlanet) requestData;

		TerrainChunkCacheElement centerChunkCache = rqIco.rawChunks[CENTER_INDEX];

		Segment activeSegment = centerChunkCache.preData;
		boolean activeIsPreData = true;

		if(centerChunkCache.preData == null || centerChunkCache.preData.getSegmentData() == null) {
			assert (w.getSegmentData() instanceof SegmentData4Byte) : w.getSegmentData().getClass().getSimpleName();
			//this means that the first stage returned an empty segment
			//we still need to place potential structures into this segment
			activeSegment = w;
			activeIsPreData = false;
			//centerChunkCache.placeAir();
		}
		assert (activeSegment.getSegmentData() instanceof SegmentData4Byte) : activeSegment.getSegmentData() + "; ACT_PRE " + activeIsPreData;
		//set the currentChunkCache back to center as it might have changed in
		//calculating all first stages of neighbors
		((RequestDataIcoPlanet) requestData).currentChunkCache = centerChunkCache;

		//Do final step: Create the structures
		//requestData contains surrounding raw segments
		terrainGenerator.generateStructures(activeSegment, (RequestDataIcoPlanet) requestData);

		((RemoteSegment) w).setLastChanged(System.currentTimeMillis());

		if(centerChunkCache.isEmpty()) {
			return;
		}

		GenerationElementMap generationElementMap = ((RequestDataIcoPlanet) requestData).currentChunkCache.generationElementMap;

//		System.err.println("STATUS: "+centerChunkCache.segmentBlockStatus);

		SegmentData optimisedData = null;

		if(activeSegment.getSegmentData().isBlockAddedForced()) {
			//optmize if the segment isn't empt
			if(centerChunkCache.isFullyFilledWithOneType()) {
				//the chunk is just full of one block. We can use memory friendly representation of the data
				int block = generationElementMap.getBlockDataFromList(0);

				if(!centerChunkCache.allInSide)
					optimisedData = new SegmentDataSingleSideEdge(false, block);

				else
					optimisedData = new SegmentDataSingle(false, block);

				//			System.err.println("USING 1 filled CHUNK: " + (++segFilledInstances));
				//			System.err.println("USING FILLED SEG: " + SegmentData.getTypeFromIntData(centerChunkCache.segmentBlockStatus) + ": " + (++segFilledInstances));

			} else {
				//			if (!centerChunkCache.allInSide) // Add air block for out of side
				centerChunkCache.generationElementMap.addBlock(0);

				if(generationElementMap.containsBlockIndexList.size() <= 16) {
					int[] blockTypes = new int[generationElementMap.containsBlockIndexList.size()];

					for(int i = 0; i < blockTypes.length; i++) {
						blockTypes[i] = generationElementMap.getBlockDataFromList(i);
					}

					optimisedData = new SegmentDataBitMap(false, blockTypes, activeSegment.getSegmentData());
				}
			}
		}
		// Finalise
		if(optimisedData != null) {
			optimisedData.setSize(w.getSize());

			w.getSegmentData().setBlockAddedForced(false);
			w.getSegmentController().getSegmentProvider().getSegmentDataManager()
					.addToFreeSegmentData(w.getSegmentData(), true, true);

//			System.err.println("MP: "+w.pos+" :: "+w.getSize());
			optimisedData.setBlockAddedForced(true);
			optimisedData.assignData(w);

		} else if(activeIsPreData) { // No optimised data type, copy pre data to actual data
			/*w.getSegmentController().getSegmentProvider().getSegmentDataManager()
				.addToFreeSegmentData(w.getSegmentData(), true, true);

			w.setSegmentData(centerChunkCache.preData.getSegmentData());*/

			//copy generated raw terrain data to actually used segment
			centerChunkCache.preData.getSegmentData().copyTo(w.getSegmentData());

			w.getSegmentData().setBlockAddedForced(centerChunkCache.preData.getSegmentData().isBlockAddedForced());

			w.setSize(centerChunkCache.preData.getSize());
			w.getSegmentData().setSize(centerChunkCache.preData.getSegmentData().getSize());
			w.getSegmentData().setNeedsRevalidate(true);

			//add raw terrain data back to pool since we dont need it anymore
			centerChunkCache.preData.getSegmentData().setBlockAddedForced(false);

			w.getSegmentController().getSegmentProvider().getSegmentDataManager()
					.addToFreeSegmentData(centerChunkCache.preData.getSegmentData(), true, true);
		}

		//empty segment already have their predata disposed

		if(activeIsPreData) {
			centerChunkCache.preData.setSegmentData(null);
			centerChunkCache.preData = null; //fully purged
		}
	}

	@Override
	public void doFirstStage(RemoteSegment preData,
	                         TerrainChunkCacheElement fromCache, RequestDataIcoPlanet requestData) {

		Vector3i v3i = new Vector3i();
		IcosahedronHelper.segmentLowPoint(preData.pos, v3i);

		if(terrainGenerator.isEmptyTerrain(v3i.x, v3i.y, v3i.z)) {
			// IM EMPTY
//			assert(fromCache.isEmpty());
			requestData.currentChunkCache.setStructureList(null);
			return;
		}

		IcosahedronHelper.segmentHighPoint(preData.pos, v3i);

		requestData.currentChunkCache = fromCache;

		TerrainStructureList tsl = null;

		try {
			int solidBlock = terrainGenerator.isSolidTerrain(v3i.x, v3i.y, v3i.z);
			if(solidBlock != -1) {// IM SOLID

				tsl = terrainGenerator.generateSegmentUnderground(preData, requestData, solidBlock);

			} else {
				//generate raw terrain without structures
				tsl = terrainGenerator.generateSegment(preData, requestData);
			}

			requestData.currentChunkCache.setStructureList(tsl);

		} catch(SegmentDataWriteException e) {
			throw new RuntimeException("Cannot write to this chunk type " + e.data.getClass(), e);
		}
	}

	@Override
	public boolean predictEmpty(Vector3i pos) {
		return ((PlanetIco) getSegmentController()).isInPlanetCore(pos.x, pos.y, pos.z);
	}

	private final Vector3i v3i = new Vector3i();

	@Override
	public boolean predictFirstStageEmpty(Vector3i position) {
		return predictEmpty(position);
	}
}
