package org.schema.game.common.controller;

import java.io.IOException;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.controller.GameClientController;
import org.schema.game.client.controller.element.world.ClientSegmentProvider;
import org.schema.game.common.controller.elements.InventoryMap;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.elements.ManagerContainer.ManualMouseEvent;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SendableTypes;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.player.GenericProvider;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.inventory.InventoryHolder;
import org.schema.game.common.data.world.RemoteSegment;
import org.schema.game.common.data.world.Segment;
import org.schema.game.network.objects.BitsetResponse;
import org.schema.game.network.objects.NetworkSegmentProvider;
import org.schema.game.network.objects.remote.RemoteBitset;
import org.schema.game.network.objects.remote.RemoteControlStructure;
import org.schema.game.network.objects.remote.RemoteInventory;
import org.schema.game.network.objects.remote.RemoteInventoryBuffer;
import org.schema.game.network.objects.remote.RemoteManualMouseEvent;
import org.schema.game.network.objects.remote.TextBlockPair;
import org.schema.game.server.controller.GameServerController;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.SendableType;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.objects.NetworkObject;
import org.schema.schine.network.server.ServerMessage;

import com.googlecode.javaewah.EWAHCompressedBitmap;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class SendableSegmentProvider extends GenericProvider<SendableSegmentController> {

	private final Vector3i posTmp = new Vector3i();
	
	private NetworkSegmentProvider networkSegmentProvider;
	private boolean controlElementMapRequestReceived = false;
	private boolean flagServerSendInventories = true;
	private LongOpenHashSet requestedTextBlocks = new LongOpenHashSet();

	private LongOpenHashSet changedTextBoxLongRangeIndices = new LongOpenHashSet();
	

	public SendableSegmentProvider(StateInterface state) {
		super(state);

	}
	@Override
	public SendableType getSendableType() {
		return SendableTypes.SENDABLE_SEGMENT_PROVIDER;
	}
	private void answerSegmentSignatureRequest(long index4, long timestamp) {
		final GameServerState gs = (GameServerState) state;

		try {
//			long index4 = remoteLongArray.getTransientArray()[0];
			long index = ElementCollection.getPosIndexFrom4(index4);
			
			short size = (short) ElementCollection.getType(index4);
//			long timestamp = remoteLongArray.getTransientArray()[1];
			NetworkSegmentProvider sc = networkSegmentProvider;
			ElementCollection.getPosFromIndex(index, posTmp);
			int segmentState = getSegmentController().getSegmentBuffer().getSegmentState(posTmp.x, posTmp.y, posTmp.z);

			if (segmentState >= 0) {
				RemoteSegment segmentFromCache = (RemoteSegment) getSegmentController().getSegmentFromCache(posTmp.x, posTmp.y, posTmp.z);
				synchronized (networkSegmentProvider) {
					((GameServerState) state).getController();
					GameServerController.handleSegmentRequest(sc, segmentFromCache, index, timestamp, size);
				}
			} else if (segmentState == SegmentBufferOctree.EMPTY) {

				synchronized (networkSegmentProvider) {
					((GameServerState) state).getController().handleEmpty(sc, getSegmentController(), posTmp, index, timestamp);
				}
			} else {

				// schedule request for segment that is not yet in buffer
				((GameServerState) state).getController().scheduleSegmentRequest(getSegmentController(), new Vector3i(posTmp), sc, timestamp, size, false, false);

			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("[SendableSegmentProvider] Exception catched for ID: " + getSegmentController() + "; if null, the segmentcontroller has probably been removed (id for both: " + getId() + ")");
		}
	}


	@Override
	public NetworkSegmentProvider getNetworkObject() {
		return networkSegmentProvider;
	}



	

	@Override
	public void newNetworkObject() {
		//		assert(getSegmentController() != null);
		networkSegmentProvider = new NetworkSegmentProvider(state, this);
		if (getSegmentController() instanceof ManagedSegmentController<?> && (((ManagedSegmentController<?>) getSegmentController()).getManagerContainer() instanceof InventoryHolder)) {
			networkSegmentProvider.invetoryBuffer = 
					new RemoteInventoryBuffer(
							((ManagedSegmentController<?>) getSegmentController()).getManagerContainer(), networkSegmentProvider);
		} else {
			networkSegmentProvider.invetoryBuffer = 
					new RemoteInventoryBuffer(null, networkSegmentProvider);
		}
	}

	@Override
	public void updateFromNetworkObject(NetworkObject o, int senderId) {
		super.updateFromNetworkObject(o, senderId);
		SendableSegmentController sg = getSegmentController();
		if (sg == null) {
			System.err.println("[SendableSegmentProvider] no longer updating: missing segment controller: " + getId() + ": " + getState());
			return;
		}

		//		handleReceivedSegmentBuffers((NetworkSegmentProvider)o);
		//		handleReceivedSignatures((NetworkSegmentProvider)o);
		setId(networkSegmentProvider.id.get());

		if (!isOnServer()) {
			((ClientSegmentProvider) getSegmentController().getSegmentProvider()).receivedFromNT(this, ((NetworkSegmentProvider) o));

		}

		for(int i = 0; i < networkSegmentProvider.messagesToBlocks.getReceiveBuffer().size(); i++){
			ServerMessage serverMessage = networkSegmentProvider.messagesToBlocks.getReceiveBuffer().get(i).get();
			getSegmentController().receivedBlockMessages.enqueue(serverMessage);
		}
		
		getSegmentController().handleReceivedModifications((NetworkSegmentProvider) o);
		getSegmentController().handleReceivedBlockActivations((NetworkSegmentProvider) o);

		
		
		if (isOnServer()) {

			if (((NetworkSegmentProvider) o).segmentBufferRequestBuffer.getReceiveBuffer().size() > 0) {
				synchronized (((GameServerState) state).getSegmentRequests()) {
					for (int i = 0; i < ((NetworkSegmentProvider) o).segmentBufferRequestBuffer.getReceiveBuffer().size(); i++) {
						answerSegmentBufferRequest(((NetworkSegmentProvider) o).segmentBufferRequestBuffer.getReceiveBuffer().get(i));
					}
				}
			}

			if (((NetworkSegmentProvider) o).segmentClientToServerCombinedRequestBuffer.getReceiveBuffer().size() > 0) {
				synchronized (((GameServerState) state).getSegmentRequests()) {
					for (int i = 0; i < ((NetworkSegmentProvider) o).segmentClientToServerCombinedRequestBuffer.getReceiveBuffer().size(); i+=2) {
						long index4 = ((NetworkSegmentProvider) o).segmentClientToServerCombinedRequestBuffer.getReceiveBuffer().get(i).longValue();
						long timeStamp = ((NetworkSegmentProvider) o).segmentClientToServerCombinedRequestBuffer.getReceiveBuffer().get(i+1).longValue();
						answerSegmentSignatureRequest(index4, timeStamp);
					}
				}
			}
		}
		if (isOnServer() && controlElementMapRequestReceived && !networkSegmentProvider.requestedInitialControlMap.get()) {
			//server reset the flag
			controlElementMapRequestReceived = false;
		}
		if (isOnServer() && !controlElementMapRequestReceived && networkSegmentProvider.requestedInitialControlMap.get()) {
			networkSegmentProvider.initialControlMap.add(new RemoteControlStructure(this, isOnServer()));

			controlElementMapRequestReceived = true;
		}

		if (isOnServer() && getSegmentController() instanceof ManagedSegmentController<?> && (((ManagedSegmentController<?>) getSegmentController()).getManagerContainer() instanceof InventoryHolder)) {
			ManagerContainer<?> ih = (((ManagedSegmentController<?>) getSegmentController()).getManagerContainer());
			if (networkSegmentProvider.inventoryDetailRequests.getReceiveBuffer().size() > 0) {
				ih.handleInventoryDetailRequests(networkSegmentProvider, networkSegmentProvider.inventoryDetailRequests);
			}
			
		}
		if (!isOnServer() && getSegmentController() instanceof ManagedSegmentController<?> && (((ManagedSegmentController<?>) getSegmentController()).getManagerContainer() instanceof InventoryHolder)) {
			ManagerContainer<?> ih = (((ManagedSegmentController<?>) getSegmentController()).getManagerContainer());
			if (networkSegmentProvider.inventoryDetailAnswers.getReceiveBuffer().size() > 0) {
				ih.handleInventoryDetailAnswer(networkSegmentProvider, networkSegmentProvider.inventoryDetailAnswers);
			}
		}
		if (getSegmentController() instanceof ManagedSegmentController<?>){
			ManagerContainer<?> ih = (((ManagedSegmentController<?>) getSegmentController()).getManagerContainer());
			ObjectArrayList<RemoteManualMouseEvent> b = networkSegmentProvider.manualMouseEventBuffer.getReceiveBuffer();
			for(int i = 0; i < b.size(); i++){
				
				ManualMouseEvent mme = b.get(i).get();
				
				ih.triggeredManualMouseEvent(mme);
			}
		}
		if (!isOnServer() && getSegmentController() instanceof ManagedSegmentController<?> && (((ManagedSegmentController<?>) getSegmentController()).getManagerContainer() instanceof InventoryHolder)) {
			ManagerContainer<?> ih = (((ManagedSegmentController<?>) getSegmentController()).getManagerContainer());
			if (networkSegmentProvider.invetoryBuffer.getReceiveBuffer().size() > 0) {
				System.err.println("[CLIENT] RECEIVED INITIAL INVETORY LIST FOR " + getSegmentController());
				ih.handleInventoryFromNT(networkSegmentProvider.invetoryBuffer, null, null, null, null, null);
				ih.handleInventoryReceivedNT();
			}
		}

		//this buffer contains responses from the server to the client, and change requests from the client to the server
		for (int i = 0; i < ((NetworkSegmentProvider) o).textBlockResponsesAndChangeRequests.getReceiveBuffer().size(); i++) {
			synchronized (getSegmentController().receivedTextBlocks) {
				TextBlockPair textBlockPair = ((NetworkSegmentProvider) o).textBlockResponsesAndChangeRequests.getReceiveBuffer().get(i).get();
				getSegmentController().receivedTextBlocks.enqueue(textBlockPair);

			}
		}

		for (int i = 0; i < ((NetworkSegmentProvider) o).textBlockChangeInLongRange.getReceiveBuffer().size(); i++) {
			long index = ((NetworkSegmentProvider) o).textBlockChangeInLongRange.getReceiveBuffer().getLong(i);
			synchronized (getSegmentController().textBlockChangeInLongRange) {
				
			}
		}
		for (int i = 0; i < ((NetworkSegmentProvider) o).textBlockRequests.getReceiveBuffer().size(); i++) {
			long index = ((NetworkSegmentProvider) o).textBlockRequests.getReceiveBuffer().getLong(i);
			synchronized (getSegmentController().receivedTextBlockRequests) {
				TextBlockPair textBlockPair = new TextBlockPair();
				textBlockPair.provider = this;
				textBlockPair.block = index;
				getSegmentController().receivedTextBlockRequests.enqueue(textBlockPair);
				/*
				 * a reqeust for this block has been made. this covers the case
				 * of server chaning and caching a text box sent client comes
				 * close and goes away, then server chaning again not sending
				 * the change since it's still cached with this, the cache is
				 * cleared when the client comes ever close and one changed
				 * block is requested
				 */
				getSegmentController().addFlagRemoveCachedTextBoxes(this);
				
			}
		}

		if (isOnServer() && ((NetworkSegmentProvider) o).signalDelete.get()) {
			setMarkedForDeleteVolatile(true);
		}

	}

	@Override
	public void updateLocal(Timer timer) {
		super.updateLocal(timer);
		if (isConnectionReady() && flagServerSendInventories) {
//						System.err.println("[SERVER] PRIVATELY SENDING ALL SERVER INVETORIES FOR "+getSegmentController());
			InventoryHolder ih = (((ManagedSegmentController<?>) getSegmentController()).getManagerContainer());
			InventoryMap inventories = ih.getInventories();
			synchronized (inventories) {
				for (Inventory i : inventories.values()) {
					networkSegmentProvider.invetoryBuffer.add(new RemoteInventory(i, ih, true, onServer));
				}
			}
			flagServerSendInventories = false;

		}
	}
	

	public void requestCurrentControlMap() {
		getSegmentController().getControlElementMap().setFlagRequested(true);
		networkSegmentProvider.requestedInitialControlMap.forceClientUpdates();
		networkSegmentProvider.requestedInitialControlMap.set(true, true);
	}

	public void resetControlMapRequest() {
		getSegmentController().getControlElementMap().setFlagRequested(false);
		networkSegmentProvider.requestedInitialControlMap.forceClientUpdates();
		networkSegmentProvider.requestedInitialControlMap.set(false, true);
	}

	public void requestCurrentInventories() {
		if (getSegmentController() instanceof ManagedSegmentController<?> && (((ManagedSegmentController<?>) getSegmentController()).getManagerContainer() instanceof InventoryHolder)) {
			//			InventoryHolder ih = ((InventoryHolder)((ManagedSegmentController<?>)getSegmentController()).getManagerContainer());
			try {
				((GameClientController) getState().getController()).requestInvetoriesUnblocked(getSegmentController().getId());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			//			System.err.println("[CLIENT] CANNOT REQUEST INVETORIES FOR "+getSegmentController()+": not an invetory holder");
		}
	}

	public void sendServerInventories() {
		if (isConnectionReady()) {
			flagServerSendInventories = true;
			if (isOnServer()) {
				((GameServerState) getState()).scheduleUpdate(this);
			}
		}
		//		System.err.println("SEND SERVER INVENTORIES");
	}

	public void setConnectionReady() {
		networkSegmentProvider.connectionReady.set(true);
	}

	public void answerSegmentBufferRequest(long index) {
		final GameServerState gs = (GameServerState) state;

		try {
			NetworkSegmentProvider sc = networkSegmentProvider;
			ElementCollection.getPosFromIndex(index, posTmp);

			//			System.err.println("REQUESTING POS: "+posTmp);

			int segmentState = getSegmentController().getSegmentBuffer().getSegmentState(posTmp.x, posTmp.y, posTmp.z);

			long bufferIndexFromAbsolute = SegmentBufferManager.getBufferIndexFromAbsolute(posTmp);
			if (segmentState >= 0) {
				EWAHCompressedBitmap bitMap = getSegmentController().getSegmentProvider().getSegmentDataIO().requestSignature(posTmp.x, posTmp.y, posTmp.z);
				if (bitMap != null) {
					bitMap = getSegmentController().getSegmentBuffer().applyBitMap(bufferIndexFromAbsolute, bitMap);
				}
				if (bitMap != null) {
					getSegmentController().getSegmentBuffer().insertFromBitset(posTmp, bufferIndexFromAbsolute, bitMap, new SegmentBufferIteratorEmptyInterface() {
						@Override
						public boolean handle(Segment s, long lastChanged) {
							return false;
						}

						@Override
						public boolean handleEmpty(int posX, int posY, int posZ, long lastChanged) {
							return false;
						}
					});
				}
				BitsetResponse r = new BitsetResponse(bufferIndexFromAbsolute, bitMap, new Vector3i(posTmp));
				synchronized (networkSegmentProvider) {
					networkSegmentProvider.segmentBufferAwnserBuffer.add(new RemoteBitset(r, networkSegmentProvider));
				}
			} else if (segmentState == SegmentBufferOctree.EMPTY) {
				EWAHCompressedBitmap bitMap = getSegmentController().getSegmentProvider().getSegmentDataIO().requestSignature(posTmp.x, posTmp.y, posTmp.z);
				if (bitMap != null) {
					bitMap = getSegmentController().getSegmentBuffer().applyBitMap(bufferIndexFromAbsolute, bitMap);
				}
				if (bitMap != null) {
					getSegmentController().getSegmentBuffer().insertFromBitset(posTmp, bufferIndexFromAbsolute, bitMap, new SegmentBufferIteratorEmptyInterface() {
						@Override
						public boolean handle(Segment s, long lastChanged) {
							return false;
						}

						@Override
						public boolean handleEmpty(int posX, int posY, int posZ, long lastChanged) {
							return false;
						}
					});
				}
				BitsetResponse r = new BitsetResponse(bufferIndexFromAbsolute, bitMap, new Vector3i(posTmp));
				synchronized (networkSegmentProvider) {
					networkSegmentProvider.segmentBufferAwnserBuffer.add(new RemoteBitset(r, networkSegmentProvider));
				}
			} else {
				// schedule request for segment that is not yet in buffer
				((GameServerState) state).getController().scheduleSegmentRequest(getSegmentController(), new Vector3i(posTmp), sc, -1, (short) -2, true, false);

			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("[SendableSegmentProvider] Exception catched for ID: " + getSegmentController() + "; if null, the segmentcontroller has probably been removed (id for both: " + getId() + ")");
		}
	}

	public void clientTextBlockRequest(long blockIndex) {
		assert (!isOnServer());
		synchronized (requestedTextBlocks) {
			if (!requestedTextBlocks.contains(blockIndex)) {
				requestedTextBlocks.add(blockIndex);
				synchronized (networkSegmentProvider.getState()) {
					boolean needsSynch = !networkSegmentProvider.getState().isSynched();
					if (needsSynch) {
						networkSegmentProvider.getState().setSynched();
					}
					networkSegmentProvider.textBlockRequests.add(blockIndex);

					if (needsSynch) {
						networkSegmentProvider.getState().setUnsynched();
					}
				}
			}
		}
	}

	/**
	 * @return the requestedTextBlocks
	 */
	public LongOpenHashSet getRequestedTextBlocks() {
		return requestedTextBlocks;
	}

	/**
	 * @param requestedTextBlocks the requestedTextBlocks to set
	 */
	public void setRequestedTextBlocks(LongOpenHashSet requestedTextBlocks) {
		this.requestedTextBlocks = requestedTextBlocks;
	}

	public void sendTextBoxCacheClearIfNotSentYet(long textBlockIndex) {
		assert(isOnServer());
		
		if(!changedTextBoxLongRangeIndices.contains(textBlockIndex)){
			//only send textbox change notification once until
			//this entity is in client range again
			networkSegmentProvider.textBlockChangeInLongRange.add(textBlockIndex);
			changedTextBoxLongRangeIndices.add(textBlockIndex);
		}
	}

	public void clearChangedTextBoxLongRangeIndices() {
		changedTextBoxLongRangeIndices.clear();
	}

	

	@Override
	public boolean isPrivateNetworkObject(){
		return true;
	}
}
