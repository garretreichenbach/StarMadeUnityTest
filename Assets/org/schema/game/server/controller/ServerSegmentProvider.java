package org.schema.game.server.controller;

import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.element.world.FastValidationContainer;
import org.schema.game.common.controller.*;
import org.schema.game.common.controller.generator.CreatorThread;
import org.schema.game.common.controller.io.SegmentDataFileUtils;
import org.schema.game.common.data.SegmentDataLock;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.world.*;
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructure;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ServerSegmentProvider extends SegmentProvider {

	public static final Object GENERAL_GENERATOR_LOCK = new Object();
	public static final Object PLANET_GENERATOR_LOCK = new Object();
	public static final Object ASTEROID_GENERATOR_LOCK = new Object();
	public static final Object STATION_GENERATOR_LOCK = new Object();
	public final Object GRID_LOCK = new Object(); //per segmentController
	public static int dbJobs;
	private final GeneratorGridLock gridLock = new GeneratorGridLock();
	private Map<Vector3i, SegmentDataLock> waitingRequests;
	public static int predeteminedEmpty;
	public static int firstStages;
	private final static SegmentAABBCalculator segmentAABBCalculator = new SegmentAABBCalculator();
	static{
		segmentAABBCalculator.start();
	}
	public ServerSegmentProvider(SendableSegmentController controller) {
		super(controller);
		waitingRequests = new HashMap<Vector3i, SegmentDataLock>();
		TerrainStructure.readWriteConfig();
	}

	@Override
	protected void finishAllWritingJobsFor(RemoteSegment segment) {
		((GameServerState) segment.getSegmentController().getState()).getThreadedSegmentWriter().finish(segment);
	}

	@Override
	public void onAddRequestedSegment(Segment s) {
	}

	@Override
	protected boolean readyToRequestElements() {
		return true;
	}

	public void enqueueHightPrio(int x, int y, int z, boolean force) {
		
		System.err.println("[SERVER) ENQUEUE HIGH PRIORITY CHUNK: "+x+", "+y+", "+z+" for "+segmentController);
		((GameServerState) segmentController.getState()).getController()
		.scheduleSegmentRequest(getSegmentController(), new Vector3i(x, y, z), null, -1, (short)-1, false, true);
		
	}
	@Override
	public void writeToDiskQueued(Segment s) throws IOException {
		final RemoteSegment rs = (RemoteSegment) s;
		((GameServerState) rs.getSegmentController().getState()).getThreadedSegmentWriter().queueWrite(rs);
	}

	private void generate(RemoteSegment ws, RequestData requestData, boolean force) {
		boolean predictEmpty = false;
		if(!force){
			predictEmpty = getSegmentController().getCreatorThread().predictEmpty(ws.pos);
		}

		if(predictEmpty) {
			if (ws.getSegmentData() != null) {
				purgeSegmentData(ws, ws.getSegmentData(), false);
			}
			ws.setSize(0);

		} else {
			if (ws.getSegmentData() == null || !(ws.getSegmentData() instanceof SegmentData4Byte)) {
				SegmentData freeSegmentData = getSegmentController().getSegmentProvider().getFreeSegmentData();
				freeSegmentData.assignData(ws);
			}else{
				System.err.println("WARNING. Generating in already existing element: "+ws);
			}
			ws.setSize(0);
			ws.getSegmentData().setNeedsRevalidate(false);

			getSegmentController().getCreatorThread().onNoExistingSegmentFound(ws, requestData);

			if (!ws.getSegmentData().isBlockAddedForced()) {
				purgeSegmentData(ws, ws.getSegmentData(), true);
			} else if (ws.getSegmentData() != null) {
				ws.getSegmentData().setNeedsRevalidate(true);
				ws.getSegmentData().setBlockAddedForced(false);
			}
			
		}
	}


	public RemoteSegment addToBufferIfNecessary(RemoteSegment handleElement, int x, int y, int z) {

		
		assert (handleElement.pos.equals(x, y, z)) : getSegmentController() + "; " + handleElement.pos + "; " + x + ", " + y + ", " + z;
		synchronized (getSegmentController().getSegmentBuffer()) {

			Segment segment = segmentController.getSegmentBuffer().get(x, y, z);
			if (segment != null) {
				//				try{
				//					throw new SegmentAlreadyAddedException("[SERVER] A segment for "+getSegmentController()+": "+handleElement.pos.x+", "+handleElement.pos.y+", "+handleElement.pos.z+" was requested multiple times (debug exception; will not crash server)");
				//				}catch(Exception e){
				//					e.printStackTrace();
				//				}
//				System.err.println("[SERVER] A segment for " + getSegmentController() + ": " + handleElement.pos.x + ", " + handleElement.pos.y + ", " + handleElement.pos.z + " was requested multiple times (debug exception; will not crash server)");
				return (RemoteSegment) segment;
			}
		}
		addSegmentToBuffer(handleElement);
		return handleElement;
	}

//	public Segment requestImmediate(int x, int y, int z) throws IOException, SegmentOutOfBoundsException {
//		assert(getSegmentController().getCreatorThread() != null):getSegmentController()+"; "+x+", "+y+", "+z+"; "+segmentController;
//		RequestData allocateTerrainData = (getSegmentController().getCreatorThread()).allocateRequestData(x, y, z);
//		RemoteSegment handleElement = doRequest(x, y, z, allocateTerrainData);
//		(getSegmentController().getCreatorThread()).freeRequestData(allocateTerrainData, x, y, z);
//
//		addToBufferIfNecessary(handleElement, x, y, z);
//
//		return handleElement;
//	}

	protected RemoteSegment createSegment(long index){
		int x = ElementCollection.getPosX(index);
		int y = ElementCollection.getPosY(index);
		int z = ElementCollection.getPosZ(index);

		assert ((x) % SegmentData.SEG == 0) : "Invalid request: " + x + ", " + y + ", " + z;
		assert ((y) % SegmentData.SEG == 0) : "Invalid request: " + x + ", " + y + ", " + z;
		assert ((z) % SegmentData.SEG == 0) : "Invalid request: " + x + ", " + y + ", " + z;

		RemoteSegment segment;
		
		synchronized (getSegmentController().getSegmentBuffer()) {
			segment = (RemoteSegment) segmentController.getSegmentBuffer().get(index);
		}
		if (segment != null) {
			segment.needsGeneration = false;
			//segment already existed. will not be generated and returned as is
			return segment;
		} else {
			segment = new RemoteSegment(segmentController);
			segment.setPos(x, y, z);
		}
		synchronized (getSegmentController().getSegmentBuffer()) {
			if (getSegmentController().getSegmentBuffer().containsKey(segment.pos)) {
				//empty
				segment.needsGeneration = false;
				return segment;
			}
		}
		segment.needsGeneration = true;
		return segment;
	}
	public RemoteSegment doRequest(long index, RequestData requestData)
			throws IOException {
		
		RemoteSegment segment = createSegment(index);
		if(!segment.needsGeneration){
			return segment;
		}
		int dbReturnCode = requestSegmentFromDatabase(segment);
		requestSegmentGeneration(segment, dbReturnCode, requestData);
		if (dbReturnCode == SegmentDataFileUtils.READ_EMPTY) {
			/*
			 * read segment was empty. purge data (if allocated)
			 */
			if (segment.getSegmentData() != null) {
				purgeSegmentData(segment, segment.getSegmentData(), false);
			}
			segment.setSize(0);
		}
		return segment;
	}
	
	/**
	 * 
	 * Gridded request
	 * 
	 * STEP 0: Check if chunk can be loaded from disk
	 * 
	 * STEP 1: calculate 3x3x3 of chunks needed with the requested one in the
	 * center
	 * 
	 * STEP 2: lock ALL needed chunks at the same time to avoid deadlock danger
	 * 
	 * STEP 3: For ALL needed chunks: Load from cache or generate basic raw
	 * terrain (stage 1) and place it in cache
	 * 
	 * STEP 4: Copy raw data from center chunk into real chunk and 
	 * generate structures for requested chunk (stage 2)
	 * 
	 * STEP 5: unlock ALL locked chunks
	 */
	public RemoteSegment doRequestStaged(long index, RequestDataIcoPlanet requestData)
			throws IOException {
		
		RemoteSegment segment = createSegment(index);
		
		// STEP 0: Check if chunk can be loaded from disk
		int dbReturnCode = requestSegmentFromDatabase(segment);
		
		if(dbReturnCode == SegmentDataFileUtils.READ_NO_DATA){
			//STEP 1: calculate 3x3x3 of chunks needed with the requested one in the center

			long[] indices = new long[27];
			int c = 0;
			for(int z = -Segment.DIM; z <Segment.DIM+1; z+=Segment.DIM){
				for(int y = -Segment.DIM; y <Segment.DIM+1; y+=Segment.DIM){
					for(int x = -Segment.DIM; x <Segment.DIM+1; x+=Segment.DIM){
						int xPos = segment.pos.x+x;
						int yPos = segment.pos.y+y;
						int zPos = segment.pos.z+z;
						long srIndex = ElementCollection.getIndex(xPos, yPos, zPos);
						indices[c] = srIndex;
						c++;
					}
				}
			}
//			System.err.println("GENERATION -> "+getSegmentController()+" -> STEP 1 completed. grid calculated");
			//STEP 2: lock ALL needed chunks at the same time to avoid deadlock danger
			synchronized(GRID_LOCK){
				while(gridLock.isGridLocked(indices)){
					try {
						GRID_LOCK.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				gridLock.lockGrid(indices);
			}
//			System.err.println("GENERATION -> "+getSegmentController()+" -> STEP 2 completed. all Locked");
			//STEP 3: For ALL needed chunks: Load from cache or generate 
			//basic raw terrain (stage 1) and place it in cache
			TerrainChunkCacheElement[] caches = requestData.rawChunks;
			Vector3i position = new Vector3i();
			for(int i = 0; i < indices.length; i++){
				long gridChunkIndex = indices[i];
				TerrainChunkCacheElement fromCache = gridLock.getFromCache(gridChunkIndex);
				
				position.set(
						ElementCollection.getPosX(indices[i]), 
						ElementCollection.getPosY(indices[i]), 
						ElementCollection.getPosZ(indices[i]));
				if(fromCache == null){
					fromCache = new TerrainChunkCacheElement();
					RemoteSegment preData = new RemoteSegment(getSegmentController());
					
					preData.setPos(position.x, position.y, position.z);
					fromCache.preData = preData;
					
					//generate first stage
					if(!getSegmentController().getCreatorThread().predictFirstStageEmpty(position)){
						
						SegmentData freeSegmentData = getSegmentController().getSegmentProvider().getFreeSegmentData();
						freeSegmentData.assignData(preData);
						
						firstStages++;
						
						getSegmentController().getCreatorThread().doFirstStage(preData, fromCache, requestData);
						assert(fromCache.preData.getSegmentData() instanceof SegmentData4Byte):"Must be 4 byte";
						if (!fromCache.preData.getSegmentData().isBlockAddedForced()) {
							//no need to keep around empty data
							getSegmentDataManager()
							.addToFreeSegmentData(fromCache.preData.getSegmentData(), true, true);
							fromCache.preData.setSegmentData(null);
						}
					}else{
						fromCache.placeAir();
						predeteminedEmpty++;
					}
					gridLock.putInCache(gridChunkIndex, fromCache);
				}else{
				}
				caches[i] = fromCache;
			}
			
//			System.err.println("GENERATION -> "+getSegmentController()+" -> STEP 3 completed. cache created");

			//STEP 4: Copy raw data from center chunk into real chunk and 
			//generate structures for requested chunk (stage 2)
			generate(segment, requestData, true);
			
			
			
			//STEP 5: unlock ALL locked chunks
			synchronized(GRID_LOCK){
				gridLock.unlockGrid(indices);
				GRID_LOCK.notifyAll();
			}
		}
		
		if (dbReturnCode == SegmentDataFileUtils.READ_EMPTY) {
			/*
			 * read segment was empty. purge data (if allocated)
			 */
			if (segment.getSegmentData() != null) {
				purgeSegmentData(segment, segment.getSegmentData(), false);
			}
			segment.setSize(0);
		}
		return segment;
	}


	public int requestSegmentFromDatabase(RemoteSegment ws) throws IOException {

		int dbReturnCode = SegmentDataFileUtils.READ_NO_DATA;
		/*
		 * request segment from server database first
		 */
		try {
			dbReturnCode = getSegmentDataIO().request(ws.pos.x, ws.pos.y, ws.pos.z, ws);
		} catch (IOException e) {
			e.printStackTrace();
			((GameServerState) getSegmentController().getState()).getController().broadcastMessage(Lng.astr("ERROR\nA chunk has failed to load on\n%s",  ws.getSegmentController()), ServerMessage.MESSAGE_TYPE_ERROR);
			System.err.println(e.getClass().getSimpleName() + " WARNING: COULD NOT READ " + ws.pos + ": " + ws + ", " + ws.getSegmentController() + ":  RECREATING -- In case of EOF");
			dbReturnCode = SegmentDataFileUtils.READ_NO_DATA;
		}
		
		return dbReturnCode;
	}
	/**
	 * Can happen threaded. It is locked depending on generator implementation ranging
	 * for fully parallel to strictly sequential
	 * 
	 * Reads from disk and checks if segemnt existed. If not it will generate the segment.
	 * 
	 * The reurned segment of this method is not yet "in the game" it will purely create the
	 * segment data. Revalidation will come after
	 *
	 * @param requestData
	 */
	public void requestSegmentGeneration(RemoteSegment ws, int dbReturnCode, RequestData requestData) throws IOException {
		
		
		if (dbReturnCode == SegmentDataFileUtils.READ_NO_DATA) {
			/*
			 * Segment has to be generated according to the servers algorithm
			 */
			if (getSegmentController() instanceof Planet || getSegmentController() instanceof PlanetIco) {
				generate(ws, requestData, false);
			} else {
				if (getSegmentController().getCreatorThread().isConcurrent() == CreatorThread.FULL_CONCURRENT) {
					//generate paralell
					generate(ws, requestData, false);
				} else if (getSegmentController().getCreatorThread().isConcurrent() == CreatorThread.LOCAL_CONCURRENT) {
					synchronized (getSegmentController().getCreatorThread()) {
						//Synchronize on a local level only
						//creator thread has buffer values
						generate(ws, requestData, false);
					}
				} else {
					synchronized (GENERAL_GENERATOR_LOCK) {
						//generate synchronized (shared buffers)
						//static buffer values used
						generate(ws, requestData, false);
					}
				}
			}
			ws.setLastChanged(System.currentTimeMillis());
		}

	}
	private static ObjectArrayFIFOQueue<FastValidationContainer> fastValidationPool = new ObjectArrayFIFOQueue<FastValidationContainer>();
	
	public static void freeFastValidationList(FastValidationContainer l){
		synchronized(fastValidationPool){
			fastValidationPool.enqueue(l);
		}
	}
	public static FastValidationContainer getFastValidationList(){
		synchronized(fastValidationPool){
			if(!fastValidationPool.isEmpty()){
				return fastValidationPool.dequeue();
			}
		}
		return new FastValidationContainer();
	}
	@Override
	public void freeValidationList(FastValidationContainer fastValidationIdex) {
		freeFastValidationList(fastValidationIdex);		
	}
	@Override
	public SegmentAABBCalculator getSegmentAABBCalculator() {
		return segmentAABBCalculator;
	}

}
