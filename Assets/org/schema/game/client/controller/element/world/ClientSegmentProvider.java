package org.schema.game.client.controller.element.world;

import java.io.DataInputStream;
import java.io.IOException;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.GameClientController;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.SegmentAABBCalculator;
import org.schema.game.common.controller.SegmentBufferIteratorEmptyInterface;
import org.schema.game.common.controller.SegmentBufferManager;
import org.schema.game.common.controller.SegmentBufferOctree;
import org.schema.game.common.controller.SegmentProvider;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.SendableSegmentProvider;
import org.schema.game.common.controller.Vector3iSegment;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.world.DeserializationException;
import org.schema.game.common.data.world.DrawableRemoteSegment;
import org.schema.game.common.data.world.RemoteSegment;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.network.objects.BitsetResponse;
import org.schema.game.network.objects.NetworkSegmentProvider;
import org.schema.schine.graphicsengine.core.Controller;

import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;

public class ClientSegmentProvider extends SegmentProvider {
	private final static byte BUFFER_REQUESTED = 2;
	private final static byte BUFFER_RECEIVED = 3;
	private final static LocalSegmentRetriever localSegmentRetriever = new LocalSegmentRetriever();
	private final static SegmentAABBCalculator segmentAABBCalculator = new SegmentAABBCalculator();
	public static RemoteSegment dummySegment;

	static {
		localSegmentRetriever.start();
		segmentAABBCalculator.start();
	}

	public final Long2ObjectOpenHashMap<RemoteSegment> inRequest = new Long2ObjectOpenHashMap<RemoteSegment>();
	private final LongArrayList requestLocalDiskSignatureQueue = new LongArrayList();
	private final LongArrayList localSignatureList = new LongArrayList();
	private final ShortArrayList localSignatureSizeList = new ShortArrayList();
	private final LongArrayList localTimeStampReceived = new LongArrayList();
	private final LongArrayList segmentsReceived = new LongArrayList();
	private final LongArrayList localOkReceived = new LongArrayList();
	private final Long2ByteOpenHashMap segmentBufferStatus = new Long2ByteOpenHashMap();
	private final Vector3f relativePlayerPosition = new Vector3f();
	private final GameClientState state;
	private final SegmentRequester requester;
	private final ObjectArrayList<BitsetResponse> receivedSegmentBufferBitset = new ObjectArrayList<BitsetResponse>();
	private SendableSegmentProvider sendableSegmentProvider;
	private boolean segmentProviderReady;
	private boolean inventoriesRequested;
	private boolean controlMapRequested;
	private LongArrayFIFOQueue segmentBufferRequests = new LongArrayFIFOQueue();

	private boolean dimChanged;

	private ObjectArrayList<SegStatus> segmentBufferChangeQueueForThread = new ObjectArrayList<SegStatus>();

	private final Long2ByteOpenHashMap thrededSegmentBufferStatus = new Long2ByteOpenHashMap();
	private final LongOpenHashSet threadedSegmentBufferRequests = new LongOpenHashSet();
	public boolean registeredContent;
	private Vector3i registeredMin = new Vector3i();
	private Vector3i registeredMax = new Vector3i();
	public short registerId = -1;
	private LongOpenHashSet receivedIndices = new LongOpenHashSet();

	public ClientSegmentProvider(SendableSegmentController controller) {
		super(controller);
		segmentBufferStatus.defaultReturnValue((byte) 0);

		this.state = (GameClientState) controller.getState();

		this.requester = new SegmentRequester(this);
	}
	private class SegStatus{
		public SegStatus(long index, byte state) {
			super();
		}
		
	}
	public void enqueueHightPrio(int x, int y, int z, boolean force) {
		GameClientController controller = ((GameClientState)getSegmentController().getState()).getController();
		controller.getCreatorThreadController().clientQueueManager.requestPriorizied(this, ElementCollection.getIndex(x, y, z));
	}
	private void changeSegmentBufferStatusMainThread(long index, byte state){
		segmentBufferStatus.put(index, state);
		segmentBufferChangeQueueForThread.add(new SegStatus(index, state));
	}
	/**
	 * called from main thread. synched under 'this'
	 */
	private void addReceivedSegmentBuffers(final SegmentQueueManager m) {

		for (int i = 0; i < receivedSegmentBufferBitset.size(); i++) {
			
			BitsetResponse r = receivedSegmentBufferBitset.get(i);
			changeSegmentBufferStatusMainThread(r.segmentBufferIndex, BUFFER_RECEIVED);
			if (r.data) {
				synchronized(receivedIndices){
					getSegmentController().getSegmentBuffer().insertFromBitset(r.pos, r.segmentBufferIndex, r.bitmap, new SegmentBufferIteratorEmptyInterface() {
	
						
	
						@Override
						public boolean handle(Segment s, long lastChanged) {
							return true;
						}
	
						@Override
						public boolean handleEmpty(int posX, int posY, int posZ, long lastChanged) {
	//							System.err.println("HEMPTY: "+posX+"; "+posY+"; "+posZ);
							long index = ElementCollection.getIndex(posX, posY, posZ);
							receivedIndices.add(index);
							m.flagAlreadyReceived(ClientSegmentProvider.this, index);
							return true;
						}
					});
				}
			}
		}
		receivedSegmentBufferBitset.clear();
	}

	private void addReceived() {
		long time = System.currentTimeMillis();
		for (int i = 0; i < segmentsReceived.size(); i++) {
			long index = segmentsReceived.getLong(i);
			assert (inRequest.containsKey(index));
			RemoteSegment s = inRequest.get(index);
			if (s != null) {
				
				synchronized(receivedIndices){
					receivedIndices.add(index);
				}
				
				addWithNextUpdate(s);
			} else {
				System.err.println("Exception: CRITICAL: inrequest did not contain segment: " + index);
			}
		}
		segmentsReceived.clear();

		long taken = System.currentTimeMillis() - time;

		if (taken > 30) {
			System.err.println("[CLIENT] WARNING: Segment Provider add received update took: " + taken + "ms");
		}
	}


	@Override
	protected void finishAllWritingJobsFor(RemoteSegment segment) {
		((GameClientState) segment.getSegmentController().getState()).getThreadedSegmentWriter().finish(segment);

	}

	@Override
	public void onAddRequestedSegment(Segment s) {
		synchronized (this) {
			assert (((RemoteSegment) s).requestStatus == RemoteSegment.SEGMENT_ADDED);
			long index = s.getIndex();

			//			try{
			//				throw new NullPointerException("REMOVING FROM REUQEST: (ADDED) "+index+"; "+getSegmentController());
			//			}catch(Exception e){
			//				e.printStackTrace();
			//			}
			inRequest.remove(index);
			requester.onAddedSegment(s);
		}
	}

	@Override
	protected boolean readyToRequestElements() {
		return isSegmentProviderReady();
	}


	/* (non-Javadoc)
	 * @see org.schema.game.common.controller.SegmentProvider#update()
	 */
	@Override
	public void update(final SegmentQueueManager m) {
		
		//this method is called from the main update thread
		
		if (isSegmentProviderReady()) {

			long time = System.currentTimeMillis();
			if (dimChanged) {
				dimChanged = false;
			}

				

			synchronized (this) {
				while (!segmentBufferRequests.isEmpty()) {
					requestSegmentBuffer(segmentBufferRequests.dequeueLong());
				}
				updatePlayerPositionRelativeToController();
				addReceivedSegmentBuffers(m);
				requestSignatures();
				requestValidLocal();
				addReceived();
			}
			requestCurrentInventories();
			requestCurrentControlMap();
			long took = System.currentTimeMillis() - time;
			if (took > 30) {
				System.err.println("[CLIENT] WARNING ClientSegmentProvider QUEUE UPDATES TOOK " + took);
			}
			super.update(m);
		}
	}

	@Override
	public void updateThreaded() {
		try {
			//read local timestamp and add to send queue
			requestLocalTimestamp();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void writeToDiskQueued(Segment s) throws IOException {
		final RemoteSegment rs = (RemoteSegment) s;
		((GameClientState) getSegmentController().getState()).getThreadedSegmentWriter().queueWrite(rs);

	}

	@Override
	public void clearRequestedBuffers() {
		synchronized (this) {
			clearBuffers();
			resetRegisterAABB();
			((GameClientState)getSegmentController().getState()).getController()
			.getCreatorThreadController().clientQueueManager
			.onClearSegmentBuffer(this);
		}
	}
	public void clearBuffers(){
		synchronized (this) {
			super.clearRequestedBuffers();
			inRequest.clear();
			requestLocalDiskSignatureQueue.clear();
			localSignatureList.clear();
			localTimeStampReceived.clear();
			segmentsReceived.clear();
			localOkReceived.clear();
			segmentBufferStatus.clear();
			threadedSegmentBufferRequests.clear();
			thrededSegmentBufferStatus.clear();
			segmentBufferChangeQueueForThread.clear();
			receivedIndices.clear();
		}
	}
	public void resetRegisterAABB(){
		synchronized (this) {
			registeredMin.set(Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE);
			registeredMax.set(Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE);
		}
	}
	public void decode(int x, int y, int z, long index, int len,
	                   DataInputStream tmpClientBuffer) throws IOException, DeserializationException {

		RemoteSegment selSeg;
		synchronized (this) {
			selSeg = inRequest.get(index);

		}
		boolean ignoreName = true;
		if (selSeg == null) {
			//this can happen when object hust unloaded and server still sends some leftover requests

			//this can also happen on re-request since the original request will be discarded
			System.err.println("[REQUEST][WARNING] Exception " + getSegmentController() + " the received segment " + ElementCollection.getPosFromIndex(index, new Vector3i()) + " was not requested or request was already satisfied -> skipping stream");
			// dump the data to a dummy segment since we dont need it
			ClientSegmentProvider.dummySegment.deserialize(tmpClientBuffer, len, ignoreName, false, segmentController.getState().getUpdateTime());
			return;
		}
		assert (selSeg != null);
		boolean needsReset = false;
		if (selSeg.getSegmentData() == null) {
//			SegmentData segmentData;
//			segmentData = getFreeSegmentData();
//			segmentData.assignData(selSeg);
		} else {
			//reset is done in deserialize
			//selSeg.getSegmentData().reset();
			needsReset = true;
		}

		selSeg.deserialize(tmpClientBuffer, len, ignoreName, needsReset, segmentController.getState().getUpdateTime());

		//add to received segments so it can be initialized and added to buffer
		//in the next local Update

		assert (selSeg != null);

		received(selSeg, index);

	}
	private void requestClient(RemoteSegment s) {
		
		long index = s.getIndex();
		synchronized (this) {
			s.requestStatus = RemoteSegment.NEW_REQUEST;
			RemoteSegment requesting = inRequest.put(index, s);
			if (requesting != null) {
				System.err.println("[CLIENT][PROVIDER] WARNING: " + getSegmentController() + ": " + requesting.getIndex() + " -> " + requesting + " already in queue");
			}
		}
		
		assert (!requestLocalDiskSignatureQueue.contains(index));
		requestLocalDiskSignatureQueue.add(index);
	}

	public void enqueueSynched(RemoteSegment requested) {
		((GameClientState)getSegmentController().getState()).getController()
				.getCreatorThreadController().clientQueueManager
				.requestPriorizied(this, requested);
	}

	public boolean existsOrIsInRequest(long index) {
		//FIXME this is possibly not synched:
		try {
			if (getSegmentController().getSegmentBuffer().containsIndex(index)) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		synchronized (this) {
			if (inRequest.containsKey(index)) {
				return true;
			}
		}
		
		return false;
	}
	public boolean existsOrIsInRequest(int x, int y, int z) {
		
		//FIXME this is possibly not synched:
		try {
			if (getSegmentController().getSegmentBuffer().getSegmentState(x, y, z) != SegmentBufferOctree.NOTHING) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		synchronized (this) {
			long index = ElementCollection.getIndex(x, y, z);
			if (inRequest.containsKey(index)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @return the relativePlayerPosition
	 */
	public Vector3f getRelativePlayerPosition() {
		return relativePlayerPosition;
	}

	/**
	 * @return the sendableSegmentProvider
	 */
	public SendableSegmentProvider getSendableSegmentProvider() {
		return sendableSegmentProvider;
	}

	/**
	 * @param sendableSegmentProvider the sendableSegmentProvider to set
	 */
	public void setSendableSegmentProvider(SendableSegmentProvider sendableSegmentProvider) {
		this.sendableSegmentProvider = sendableSegmentProvider;
	}

	/**
	 * called from main thread. synched under 'this'
	 * @param s
	 * @return
	 */
	private boolean isOkToRequest(RemoteSegment s) {
		SegmentBufferManager m = (SegmentBufferManager) getSegmentController().getSegmentBuffer();
		long segmentBufferIndex = SegmentBufferManager.getBufferIndexFromAbsolute(s.pos);

		byte status = segmentBufferStatus.get(segmentBufferIndex);
		if (status == 0) {
			changeSegmentBufferStatusMainThread(segmentBufferIndex, BUFFER_REQUESTED);
			sendableSegmentProvider.getNetworkObject().segmentBufferRequestBuffer.add(ElementCollection.getIndex(s.pos));
		}

		return true;
	}

	/**
	 * called in main thread. synched under 'this'
	 * @param segmentIndex
	 * @return
	 */
	private void requestSegmentBuffer(long segmentIndex) {
		SegmentBufferManager m = (SegmentBufferManager) getSegmentController().getSegmentBuffer();
		long segmentBufferIndex = SegmentBufferManager.getBufferIndexFromSegmentIndex(segmentIndex);

		if(segmentBufferStatus.get(segmentBufferIndex) == 0){
			changeSegmentBufferStatusMainThread(segmentBufferIndex, BUFFER_REQUESTED);
			sendableSegmentProvider.getNetworkObject().segmentBufferRequestBuffer.add(segmentIndex);
		}else{
			
		}
	}

	public boolean isSegmentProviderReady() {
		if (!segmentProviderReady) {
			segmentProviderReady = sendableSegmentProvider != null
					&& sendableSegmentProvider.isConnectionReady();
		}
		return segmentProviderReady;
	}

	/**
	 * called from main thread. synched under 'this'
	 * @param index
	 * @param s
	 */
	private void onNotOkToRequest(long index, RemoteSegment s) {
		System.err.println("NOT OK: REMOVE FROM INREQUEST");
		//this doesnt need to be requested
		inRequest.remove(index);
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
	public void received(RemoteSegment selSeg, long index) {
		if(selSeg.getSegmentData() != null){
			SegmentProvider.buildRevalidationIndex(selSeg, segmentController.isStatic(), true); //types are always prevalidatd on server already
			if(selSeg.getSegmentData() != null) {
				selSeg.getSegmentData().setNeedsRevalidate(true);
			}
		}
		
		synchronized (this) {
			selSeg.requestStatus = RemoteSegment.SEGMENT_RECEIVED;
			segmentsReceived.add(index);
		}
		
		
	}

	public void receivedFromNT(
			SendableSegmentProvider p,
			NetworkSegmentProvider o) {
		assert (p == sendableSegmentProvider);

		if (o.signatureEmptyBuffer.getReceiveBuffer().size() > 0) {
			synchronized (this) {
				for (int i = 0; i < o.signatureEmptyBuffer.getReceiveBuffer().size(); i++) {
					long sig = o.signatureEmptyBuffer.getReceiveBuffer().getLong(i);
					//					System.err.println("RECEIVED EMPTY SIG: "+ElementCollection.getPosFromIndex(sig, new Vector3i()));
					//add empty
					//					assert(inRequest.containsKey(sig));
					if (inRequest.containsKey(sig)) {
						RemoteSegment s = inRequest.get(sig);
						if (s.getSegmentData() != null) {
							getSegmentController().getSegmentProvider().addToFreeSegmentData(s.getSegmentData());
						}
						s.setSize(0);
						//					System.err.println("Successfully Received empty segment "+s);
						synchronized(receivedIndices){
							receivedIndices.add(s.getIndex());
						}
						addWithNextUpdate(s);
					} else {
						//possible when client has just unloaded and cleared the buffers
						//only to be catched here, since everything else is not triggered when object is unloaded
					}
				}
			}
		}

		if (o.segmentBufferAwnserBuffer.getReceiveBuffer().size() > 0) {
			synchronized (this) {
				for (int i = 0; i < o.segmentBufferAwnserBuffer.getReceiveBuffer().size(); i++) {
					BitsetResponse sig = o.segmentBufferAwnserBuffer.getReceiveBuffer().get(i).get();
					receivedSegmentBufferBitset.add(sig);
				}
			}
		}

		if (o.signatureOkBuffer.getReceiveBuffer().size() > 0) {
			synchronized (this) {
				for (int i = 0; i < o.signatureOkBuffer.getReceiveBuffer().size(); i++) {
					long sig = o.signatureOkBuffer.getReceiveBuffer().getLong(i);
					//					System.err.println("RECEIVED OK SIG: "+ElementCollection.getPosFromIndex(sig, new Vector3i()));
					//add local
					if (inRequest.containsKey(sig)) {
						localOkReceived.add(sig);
					} else {
						//possible when client has just unloaded and cleared the buffers
						//only to be catched here, since everything else is not triggered when object is unloaded
					}
				}
			}
		}
	}

	public void requestCurrentControlMap() {
		if (!controlMapRequested) {
			//			System.err.println("[CLIENT] PRIVATLY REQUESTING INVENTORIES FOR "+this);
			if (isSegmentProviderReady()) {
				sendableSegmentProvider.requestCurrentControlMap();
				controlMapRequested = true;
			}
		}
	}

	public void resetCurrentCopntrolMap() {

		if (isSegmentProviderReady()) {
			synchronized (getSegmentController().getState()) {
				getSegmentController().getState().setSynched();
				try {
					controlMapRequested = false;
					sendableSegmentProvider.resetControlMapRequest();
				} finally {
					getSegmentController().getState().setUnsynched();
				}
			}
		}
	}

	/**
	 * @return the sendableSegmentProvider
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void requestCurrentInventories() {
		if (!inventoriesRequested ) {
			//			System.err.println("[CLIENT] PRIVATLY REQUESTING INVENTORIES FOR "+this);
			if (isSegmentProviderReady()) {
				sendableSegmentProvider.requestCurrentInventories();
				inventoriesRequested = true;
			}
		}
	}

	int requestFromLocalDB(RemoteSegment requested) throws IOException, DeserializationException {
		//		System.err.println("REQUEST FROM LOCAL: "+requested.pos+" -> "+ElementCollection.getIndex(requested.pos)+"; "+getSegmentController());
		return getSegmentDataIO().request(requested.pos.x, requested.pos.y, requested.pos.z, requested);
	}

	/**
	 * happens threaded
	 *
	 * @throws IOException
	 */
	private void requestLocalTimestamp() throws IOException {
		if (!requestLocalDiskSignatureQueue.isEmpty()) {
			
			Vector3i v = new Vector3i();
			//request desynchronized (long part)
			for (int i = 0; i < requestLocalDiskSignatureQueue.size(); i++) {
				long index = requestLocalDiskSignatureQueue.getLong(i);
				ElementCollection.getPosFromIndex(index, v);
				int size = getSegmentDataIO().getSize(v.x, v.y, v.z);
				//add size into encoding
				localSignatureList.add(getSegmentDataIO().getTimeStamp(v.x, v.y, v.z));
				localSignatureSizeList.add((short) size);

			}

			//apply synchronized (fast part)
			synchronized (this) {
				final long currentTime = System.currentTimeMillis();
				for (int i = 0; i < requestLocalDiskSignatureQueue.size(); i++) {
					long index = requestLocalDiskSignatureQueue.getLong(i);
					long timestamp = localSignatureList.getLong(i);
					short size = localSignatureSizeList.getShort(i);

					assert (inRequest.containsKey(index)) : this.getSegmentController() + ": " + index + "->" + ElementCollection.getPosFromIndex(index, v);
					RemoteSegment s = inRequest.get(index);
					s.requestStatus = RemoteSegment.LOCAL_SIGNATURE_RECEIVED;

					if(timestamp > currentTime){
						System.err.println("[CLIENT][IO] WARNING Client Header last changed from the future future "+getSegmentController()+"; "+System.currentTimeMillis()+"; "+timestamp);
						timestamp = currentTime-1000;
					}
					s.lastLocalTimeStamp = timestamp;
					s.lastSize = size;

					localTimeStampReceived.add(index);
				}
			}
			localSignatureSizeList.clear();
			localSignatureList.clear();
			requestLocalDiskSignatureQueue.clear();
		}

	}

	/**
	 * called from main thread. synched under 'this'
	 */
	private void requestSignatures() {
		for (int i = 0; i < localTimeStampReceived.size(); i++) {
			long index = localTimeStampReceived.getLong(i);
			assert (inRequest.containsKey(index));
			RemoteSegment s = inRequest.get(index);
			if (isOkToRequest(s)) {

				s.requestStatus = RemoteSegment.SEGMENT_REQUESTED;
//				RemoteLongArray remoteLongArray = new RemoteLongArray(2, false);
//				remoteLongArray.set(0, ElementCollection.getIndex4(index, s.lastSize));
//				remoteLongArray.set(1, s.lastLocalTimeStamp);
//				//request signature (combined with local timestamp)
//				getSendableSegmentProvider().getNetworkObject().segmentClientToServerCombinedRequestBuffer.add(remoteLongArray);
				
				sendableSegmentProvider.getNetworkObject().segmentClientToServerCombinedRequestBuffer.add(ElementCollection.getIndex4(index, s.lastSize));
				sendableSegmentProvider.getNetworkObject().segmentClientToServerCombinedRequestBuffer.add(s.lastLocalTimeStamp);

			} else {
				onNotOkToRequest(index, s);
			}
		}

		localTimeStampReceived.clear();
	}

	private void requestValidLocal() {
		for (int i = 0; i < localOkReceived.size(); i++) {
			long index = localOkReceived.getLong(i);
			assert (inRequest.containsKey(index)) : index + " -> " + ElementCollection.getPosFromIndex(index, new Vector3i()) + "; " + inRequest + "; " + getSegmentController();
			RemoteSegment s = inRequest.get(index);
			localSegmentRetriever.enqueue(new LocalSegmentRequest(this, s));
		}
		localOkReceived.clear();
	}

	private void updatePlayerPositionRelativeToController() {
		relativePlayerPosition.set(Controller.getCamera().getWorldTransform().origin);
		getSegmentController().getClientTransformInverse().transform(relativePlayerPosition);
	}
	@Override
	public void onAddedToBuffer(RemoteSegment s) {
//		try {
//			throw new Exception("ADDED: "+s);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	/**
	 * this is done threaded
	 *
	 * @param index
	 * @return
	 * @throws IOException
	 */
	protected void doRequest(long index)
			throws IOException {

		
		synchronized(receivedIndices){
			if(receivedIndices.contains(index)){
				return;
			}
		}
		
		//NT REREQUEST CHANGE
		RemoteSegment segment;
		synchronized (segmentController.getSegmentBuffer()) {
			segment = (RemoteSegment) segmentController.getSegmentBuffer().get(index);
		}
		
		if (segment != null) {
			System.err.println("[CLIENT] WARNING: tried to re-request segment: " + segment + " on MinMaxPos[" + segmentController.getMinPos()+"; "+segmentController.getMaxPos()+"] "+segmentController+"; ");
			return;
		} else {
			Vector3i pos = ElementCollection.getPosFromIndex(index, new Vector3i());

			//		System.err.println("REQUESTING: "+index+" -> "+pos);
			assert ((ElementCollection.getPosX(index)) % SegmentData.SEG == 0) : "Invalid request: "
					+ pos;
			assert ((ElementCollection.getPosY(index)) % SegmentData.SEG == 0) : "Invalid request: "
					+ pos;
			assert ((ElementCollection.getPosZ(index)) % SegmentData.SEG == 0) : "Invalid request: "
					+ pos;
			if (segmentController.isOnServer()) {
				segment = new RemoteSegment(segmentController);
				segment.setPos(index);
			} else {
				segment = new DrawableRemoteSegment(segmentController);
				segment.setPos(index);
			}
		}
		requestClient(segment);
		return;
	}

	public void flagDimChange() {
		this.dimChanged = true;
	}

	@Override
	public void freeValidationList(FastValidationContainer fastValidationIdex) {
		freeFastValidationList(fastValidationIdex);		
	}

	@Override
	public SegmentAABBCalculator getSegmentAABBCalculator() {
		return segmentAABBCalculator;
	}
	private static LongArrayList grp = new LongArrayList(2048);
	public void registerContent(SegmentQueueManager clientQueueManager) {
		
		//in case of first iteration or if minmax changed, add additional segments
		Vector3iSegment min = getSegmentController().getMinPos();
		Vector3iSegment max = getSegmentController().getMaxPos();
		if(!min.equals(registeredMin) || !max.equals(registeredMax)){
			synchronized(grp){
				for(int z = min.z; z <= max.z; z++){
					for(int y = min.y; y <= max.y; y++){
						for(int x = min.x; x <= max.x; x++){
							
							if(x >= registeredMin.x && x < registeredMax.x &&
									y >= registeredMin.y && y < registeredMax.y &&
									z >= registeredMin.z && z < registeredMax.z							){
								//part of the previous bounding box
							}else{
								//wasnt registered yet
								grp.add(ElementCollection.getIndex(x*Segment.DIM,y*Segment.DIM,z*Segment.DIM));
							}
						}
					}
				}
//				System.err.println("[CLIENTREQ] Registered "+grp.size()+" segment for "+getSegmentController());
				clientQueueManager.registerSegment(this, grp);
				grp.clear();
			}
			registeredMin.set(min);
			registeredMax.set(max);
		}
	}
	
	


}
