package org.schema.game.common.controller;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.element.world.FastValidationContainer;
import org.schema.game.client.controller.element.world.SegmentQueueManager;
import org.schema.game.client.data.SegmentManagerInterface;
import org.schema.game.common.controller.io.SegmentDataIO16;
import org.schema.game.common.controller.io.SegmentDataIOInterface;
import org.schema.game.common.controller.io.SegmentDataIONew;
import org.schema.game.common.data.SegmentRetrieveCallback;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.world.RemoteSegment;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentData4Byte;
import org.schema.schine.network.server.ServerState;

import java.io.IOException;
import java.util.List;

public abstract class SegmentProvider {
	private final LongOpenHashSet inProg = new LongOpenHashSet();
	private final SegmentDataManager segmentDataManager;
	private final SegmentDataIOInterface segmentDataIO;
	private final RemoteSegment buffer;
	public long lastCheck;
	protected SendableSegmentController segmentController;
	protected Object lock = new Object();
	long lastAliveCheck = 0;
	private ObjectArrayList<Segment> readySegments;
	private Vector3i requestHelper = new Vector3i();
	
	public abstract SegmentAABBCalculator getSegmentAABBCalculator();
	

	private Vector3i tmpPos = new Vector3i();
	private final SegmentRetrieveCallback tmpCallback = new SegmentRetrieveCallback();

	public SegmentProvider(SendableSegmentController controller) {
		this.segmentController = controller;

		this.buffer = new RemoteSegment(controller);
		buffer.setSegmentData(new SegmentData4Byte(!controller.isOnServer()));
		readySegments = new ObjectArrayList<Segment>();
		if(ByteUtil.Chunk32){
			segmentDataIO = new SegmentDataIONew(controller,
					segmentController.getState() instanceof ServerState);
		}else{
			segmentDataIO = new SegmentDataIO16(controller,
					segmentController.getState() instanceof ServerState);
		}
		segmentDataManager = ((SegmentManagerInterface) segmentController
				.getState()).getSegmentDataManager();
	}

	private void addReadySegments() {
		int size = 0;
		if (!readySegments.isEmpty()) {
			synchronized (this) {
				size = readySegments.size();
				for (int i = 0; i < size; i++) {
					Segment seg = readySegments.get(i);
					
					segmentController.getSegmentBuffer().get(
							seg.pos.x, seg.pos.y, seg.pos.z, tmpCallback);
					if(tmpCallback.segment == null || tmpCallback.segment.getSegmentData() == null ||  seg.getSegmentData() == null || tmpCallback.segment.getSegmentData() != seg.getSegmentData()){
//						System.err.println("CB: "+seg.pos+" "+segmentController+" ;"+segmentController.getState());
						addSegmentToBuffer((RemoteSegment) seg);
					}
					
				}
				readySegments.clear();
			}
		}
	}

	private static List<SegmentDataMetaData> metaPool = new ObjectArrayList<SegmentDataMetaData>();
	public static void freeSegmentDataMetaData(SegmentDataMetaData p){
		synchronized(metaPool){
			if(metaPool.size() < 64){
				metaPool.add(p);
			}
		}
	}
	private static SegmentDataMetaData getSegmentDataMetaData(){
		synchronized(metaPool){
			if(metaPool.isEmpty()){
				return new SegmentDataMetaData();
			}else{
				SegmentDataMetaData remove = metaPool.remove(metaPool.size()-1);
				remove.check();
				return remove;
			}
		}
	}
	
	public static void buildRevalidationIndex(RemoteSegment seg, boolean staticElement, boolean prevalidatedTypes){
		
		if(seg.getSegmentData() != null && !seg.getSegmentData().revalidatedOnce){
			if(seg.buildMetaData != null){
				try {
					throw new Exception("Seg meta data already existed: "+seg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			seg.buildMetaData = getSegmentDataMetaData();
			seg.getSegmentData().revalidateDataMeta(System.currentTimeMillis(), staticElement, prevalidatedTypes, seg.buildMetaData);
		}
		
		
	}

	//	private LongOpenHashSet checkSet = new LongOpenHashSet();
	public void addSegmentToBuffer(RemoteSegment s) {
		assert (s != null);

		if (s == null) {
			try {
				throw new IllegalArgumentException("SEGMENT IS NULL");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}

		s.setRevalidating(true);

		assert (s.isEmpty() || s.getSegmentData() != null);

		//		if(s.getSegmentController().isOnServer() && s.getSegmentController() instanceof Ship){
		//			assert(!checkSet.contains(ElementCollection.getIndex(s.pos)));
		//			checkSet.add(ElementCollection.getIndex(s.pos));
		//		}
		long t = System.currentTimeMillis();
		
		SegmentData segmentData = s.getSegmentData();
		if (segmentData != null) {
			if (segmentData.needsRevalidate()) {
				Segment segment = segmentController.getSegmentBuffer().get(s.pos);
				if(segment != null && !segment.isEmpty() && segment.getSegmentData() != null){
					System.err.println("[SEGMENTPROVIDER] Unvalidating already existing Segment :: "+segment+" and replacing with new");
					segment.getSegmentData().unvalidateData(t);
				}
				if(s.buildMetaData != null){
					SegmentDataMetaData buildMetaData = s.buildMetaData;
					s.buildMetaData = null;
					segmentData.revalidateMeta(buildMetaData);
					freeSegmentDataMetaData(buildMetaData);
					
				}else{
					segmentData.revalidateData(t, segmentController.isStatic());
				}
			}else{
				System.err.println(segmentController.getState()+" [SEGMENT][WARNING] UNVALIDATED SEGMENT ADDED: "+segmentData+"; "+ segmentController);
				assert(false):"UNVALIDATED SEGMENTS SHOULD NEVER BE ADDED";
//				if(getSegmentController().isOnServer()){
//					assert(false):segmentData+"; "+getSegmentController();
//				}
			}
		}

		long tookMan = System.currentTimeMillis() - t;
		if (!segmentController.isOnServer() && tookMan > 30) {
			System.err.println("[CLIENT] " + segmentController + " revalidating this segment " + s + " took " + tookMan);
		}
		t = System.currentTimeMillis();

		s.setRevalidating(false);
		s.dataChanged(true);
		
		segmentController.getSegmentBuffer().addImmediate(s);

		//		tookMan = System.currentTimeMillis() - t;
		//		if(!segmentController.isOnServer() && tookMan > 10){
		//			System.err.println("[CLIENT] "+segmentController+" addImmidiate "+s+" took "+tookMan);
		//		}
		//		t = System.currentTimeMillis();
		removeInProgress(s.getIndex());

		//		tookMan = System.currentTimeMillis() - t;
		//		if(!segmentController.isOnServer() && tookMan > 10){
		//			System.err.println("[CLIENT] "+segmentController+" inprogress remove "+s+" took "+tookMan);
		//		}
		//		t = System.currentTimeMillis();

		onAddRequestedSegment(s);

		tookMan = System.currentTimeMillis() - t;
		if (!segmentController.isOnServer() && tookMan > 30) {
			System.err.println("[CLIENT] " + segmentController + " onAddRequestedSegment(s)/addImmediate/inProgress.remove " + s + " took " + tookMan);
		}

	}

	private void addToFreeSegmentData(SegmentData segmentData, boolean fast) {
		if (segmentData.getSegment() != null) {
			segmentController.getSegmentBuffer().setEmpty(segmentData.getSegment().pos);
		}
		synchronized (segmentDataManager) {

			segmentDataManager.addToFreeSegmentData(segmentData, true, fast);
		}

	}

	public void addToFreeSegmentData(SegmentData segmentData) {
		addToFreeSegmentData(segmentData, false);

	}

	public void addToFreeSegmentDataFast(SegmentData segmentData) {
		addToFreeSegmentData(segmentData, true);

	}

	/**
	 * used by client to add segments synchronized
	 *
	 * @param segment
	 */
	public void addWithNextUpdate(RemoteSegment segment) {
		
		synchronized (this) {
			assert(segment.getSegmentData() == null || segment.getSegmentData().needsRevalidate()):"Make sure segments flag revalidation";
			readySegments.add(segment);
		}
	}


	public void checkTimeoutSegmentInProgress() {
	}

	public void writeSegment(RemoteSegment rs, long lastChanged) throws IOException {
		{
			synchronized (segmentDataIO) {
				synchronized (rs.writingToDiskLock) {
					//					if(!rs.getSegmentController().isOnServer()){
					//						System.err.println("CLIENT WRITING TO DISK "+rs.pos);
					//					}
					// write to database if necessary (if server version is
					// newer)
					segmentDataIO.write(rs, lastChanged, false, false);

				}
			}
		}

	}


	public abstract void enqueueHightPrio(int x, int y, int z, boolean force) ;

	protected abstract void finishAllWritingJobsFor(RemoteSegment segment);

	public int getCountOfFree() {
		return segmentDataManager.sizeFree();
	}

	public SegmentData getFreeSegmentData() {
		synchronized (segmentDataManager) {
			return segmentDataManager.getFreeSegmentData();
		}
	}


	public SendableSegmentController getSegmentController() {
		return segmentController;
	}

	/**
	 * @return the segmentDataIO
	 */
	public SegmentDataIOInterface getSegmentDataIO() {
		return segmentDataIO;
	}

	public boolean isInBound(Vector3i pos) {
		return segmentController.isInbound(
				Segment.getSegmentIndexFromSegmentElement(pos.x, pos.y, pos.z, tmpPos));

	}

	public abstract void onAddRequestedSegment(Segment s);

	public void purgeDB() {
	}

	public void purgeSegmentData(Segment s, SegmentData segmentData, boolean fast) {
		assert (segmentData != null);
		segmentDataManager.addToFreeSegmentData(segmentData, true, fast);
		s.setSegmentData(null);
	}

	protected abstract boolean readyToRequestElements();

	public void releaseFileHandles() throws IOException {
		segmentDataIO.releaseFileHandles();
	}





	private boolean aliveCheck() {
		if (lastAliveCheck > 10000) {
			lastAliveCheck = 0;
			return segmentController.getCreatorThread().isAlive();
		} else {
			lastAliveCheck++;
		}
		return true;
	}

	public void update(SegmentQueueManager m) {

		assert (aliveCheck());

		addReadySegments();
	}

	/**
	 * @return true, if New Segments Have Been Queued
	 */
	public void updateThreaded() {
	}

	public abstract void writeToDiskQueued(final Segment s) throws IOException;

	/**
	 * @return the buffer
	 */
	public RemoteSegment getBuffer() {
		return buffer;
	}

	public abstract void freeValidationList(FastValidationContainer fastValidationIdex);

	public SegmentDataManager getSegmentDataManager() {
		return segmentDataManager;
	}

	public void enqueueAABBChange(Segment s) {
		getSegmentAABBCalculator().enqueue(this, s);

	}
	public boolean removeInProgress(long index) {
		synchronized (inProg) {
			return inProg.remove(index);
		}
		
	}
	public boolean isInProgress(long index) {
		synchronized (inProg) {
			return inProg.contains(index);
		}
	}
	public boolean isInProgress(int x, int y, int z) {
		return isInProgress(ElementCollection.getIndex(x, y, z));
	}
	public boolean addInProgress(int x, int y, int z) {
		return addInProgress(ElementCollection.getIndex(x, y, z));
	}
	public boolean removeInProgress(int x, int y, int z) {
		return removeInProgress(ElementCollection.getIndex(x, y, z));
	}
	public boolean addInProgress(long index) {
		synchronized (inProg) {
			return inProg.add(index);
		}
	}

	public void clearRequestedBuffers() {
		synchronized (inProg) {
			inProg.clear();
		}
	}

	public void onAddedToBuffer(RemoteSegment s) {
	}

}
