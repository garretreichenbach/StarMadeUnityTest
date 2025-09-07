package org.schema.game.common.controller;

import api.listener.events.block.SegmentPieceModifyOnClientEvent;
import api.mod.StarLoader;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.dynamics.RigidBody;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.shards.ShardDrawer;
import org.schema.game.common.controller.damage.DamageDealerType;
import org.schema.game.common.data.*;
import org.schema.game.common.data.VoidSegmentPiece.VoidSegmentPiecePool;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.physics.PhysicsExt;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.world.*;
import org.schema.game.network.objects.NetworkSegmentProvider;
import org.schema.game.network.objects.remote.RemoteSegmentPiece;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.PlayerNotFountException;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.server.ServerMessage;

import javax.vecmath.Vector3f;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

public class BlockProcessor {
	private final SendableSegmentController con;
	
	
	
	private final List<VoidSegmentPiece> delayedSegmentMods = new ObjectArrayList<VoidSegmentPiece>();
	private final ObjectArrayFIFOQueue<BlockBulkSerialization> delayedBulkMods = new ObjectArrayFIFOQueue<BlockBulkSerialization>();
	private final Object2ObjectOpenHashMap<PlayerState, IntOpenHashSet> inventoryMods = new Object2ObjectOpenHashMap<PlayerState, IntOpenHashSet>();
	private final ObjectArrayList<Segment> emptySegments = new ObjectArrayList<Segment>();
	private final ObjectArrayList<Segment> segmentsAABBUpdateNeeded = new ObjectArrayList<Segment>();
	private final Long2ObjectOpenHashMap<RemoteSegment> segmentsChanged = new Long2ObjectOpenHashMap<RemoteSegment>();
	private final ObjectOpenHashSet<RemoteSegment> markNeighborForChanged = new ObjectOpenHashSet<RemoteSegment>();
	public final Long2ObjectOpenHashMap<LongArrayList> connectionsToAddFromPaste = new Long2ObjectOpenHashMap<LongArrayList>();
	public final Long2ObjectOpenHashMap<String> textToAddFromPaste = new Long2ObjectOpenHashMap<String>();

	private final SegmentRetrieveCallback callBackTmp = new SegmentRetrieveCallback();
	private final PieceListProvider plProvider;
	private final SegmentChangeContainer pieceMap;
	
	final SegmentBlockProcessor segBlockProc;
	
	private final SegmentPiece current = new SegmentPiece();


	private final VoidSegmentPiecePool receivedPiecePool;
	
	
	private SegmentPiece tmpPiece = new SegmentPiece();



	private final static VoidSegmentPiecePool serverPool = new VoidSegmentPiecePool();
	private final static VoidSegmentPiecePool clientPool = new VoidSegmentPiecePool();



	protected static ThreadLocal<PieceListProvider> plProviderTL = new ThreadLocal<PieceListProvider>() {
		@Override
		protected PieceListProvider initialValue() {
			return new PieceListProvider();
		}
	};
	protected static ThreadLocal<SegmentChangeContainer> sccTL = new ThreadLocal<SegmentChangeContainer>() {
		@Override
		protected SegmentChangeContainer initialValue() {
			return new SegmentChangeContainer();
		}
	};
	private static class PieceListProvider{
		private final List<SegmentPiece> sg = new ObjectArrayList<SegmentPiece>();
		private final List<SegmentPiece> addwd = new ObjectArrayList<SegmentPiece>();
		private final List<PieceList> pl = new ObjectArrayList<PieceList>();
		
		private final LongArrayList connectionsFrom = new LongArrayList();
		private final LongArrayList connectionsTo = new LongArrayList();
		private final ShortArrayList oldTypes = new ShortArrayList();
		
		public SegmentPiece getPiece() {
			SegmentPiece p;
			if(sg.isEmpty()) {
				p = new SegmentPiece();
			}else {
				p = sg.remove(sg.size()-1);
			}
			
			addwd.add(p);
			return p;
		}
		public void freeAllPieces() {
			for(SegmentPiece p : addwd) {
				freePiece(p);
			}
			addwd.clear();
			
			
		}
		public void clearProcessingData() {
			connectionsFrom.clear();
			connectionsTo.clear();
			oldTypes.clear();
		}
		private void freePiece(SegmentPiece pl) {
			pl.reset();
			this.sg.add(pl);
		}
		public PieceList get() {
			if(pl.isEmpty()) {
				return new PieceList();
			}else {
				return pl.remove(pl.size()-1);
			}
		}
		
		public void free(PieceList pl) {
			pl.clear();
			this.pl.add(pl);
		}
	}
	private static class SegmentChangeContainer extends Long2ObjectOpenHashMap<PieceList>{
		private static final long serialVersionUID = -802243669446537813L;
		
		private Long2ObjectOpenHashMap<SegmentPiece> controllers = new Long2ObjectOpenHashMap<SegmentPiece>();
		
		public void clear() {
			super.clear();
			controllers.clear();
		}
	}
	public static class PieceList extends ObjectArrayList<VoidSegmentPiece>{
		private static final long serialVersionUID = 2296926034557584508L;
		public RemoteSegment segment;
		public long absIndex = Long.MIN_VALUE;
		public int x = Integer.MIN_VALUE;
		public int y = Integer.MIN_VALUE;
		public int z = Integer.MIN_VALUE;
		private boolean newSegment;
		public int blocksModOrAdd;
		public int blocksRemoved;
		public SegmentData segmentData;
		
		public void clear() {
			super.clear();
			newSegment = false;
			segment = null;
			absIndex = Long.MIN_VALUE;
			x = Integer.MIN_VALUE;
			y = Integer.MIN_VALUE;
			z = Integer.MIN_VALUE;
			blocksRemoved = 0;
			blocksModOrAdd = 0;
			segmentData = null;
		}

		@Override
		public boolean add(VoidSegmentPiece k) {
			return super.add(k);
		}
		
		
	}
	
	public BlockProcessor(SendableSegmentController con) {
		this.con = con;
		this.pieceMap = sccTL.get();
		this.plProvider = plProviderTL.get();
		
		if(isOnServer()) {
			receivedPiecePool = serverPool;
			segBlockProc = new SegmentBlockProcessorServer();
		}else {
			receivedPiecePool = clientPool;
			segBlockProc = new SegmentBlockProcessorClient();
		}
	}
	private List<PieceList> locked = new ObjectArrayList<PieceList>();
	public void handleDelayedMods() {
		boolean touched = false;
		getState().getDebugTimer().start(con, "SendableSegmentController", "DelayedMods", "Total");
		getState().getDebugTimer().setMeta(con, "SendableSegmentController", "DelayedMods", "Total", delayedSegmentMods.size()+" Blocks");
		if (!delayedSegmentMods.isEmpty()) {
//			if(Keyboard.isCreated() && Keyboard.isKeyDown(GLFW.GLFW_KEY_L)) {
//				System.err.println("++++ON PROCESS "+delayedSegmentMods.size()+" blocks");
//				for(int i = 0; i < delayedSegmentMods.size(); i++) {
//					System.err.println("BLOCK "+i+": "+delayedSegmentMods.get(i)+"; "+delayedSegmentMods.get(i).getData());
//				}
//			}
			if (isOnServer()) {
				con.onBlockSinglePlacedOnServer();
			}
			if (isAccessible()) {
				getState().getDebugTimer().start(con, "SendableSegmentController", "DelayedMods", "PreProcess");
				final SegmentChangeContainer pieceMap = preProcess(delayedSegmentMods);
				
				getState().getDebugTimer().end(con, "SendableSegmentController", "DelayedMods", "PreProcess");
				
				getState().getDebugTimer().start(con, "SendableSegmentController", "DelayedMods", "Aquire");
				boolean allLocked = true;
				for (PieceList e : pieceMap.values()) {
					boolean lockable = aquireSegmentLock(e);
					if(!lockable) {
						allLocked = false;
						break;
					}else {
						locked.add(e);
					}
				}
				getState().getDebugTimer().end(con, "SendableSegmentController", "DelayedMods", "Aquire");
				if(!allLocked) {
					//cannot process since not all locks have been aquired
					//delayed mods isn't cleared and will be tried to be processed next update
					
					//unlock all segments that have already been locked
					for(PieceList e : locked) {
						
						if(e.segmentData != null) {
							e.segmentData.rwl.writeLock().unlock();
						}
						plProvider.free(e);
					}
					locked.clear();
					
					
					
					getState().getDebugTimer().start(con, "SendableSegmentController", "DelayedMods", "ClearBuffers");
					clearBuffers();
					getState().getDebugTimer().end(con, "SendableSegmentController", "DelayedMods", "ClearBuffers");
					
//					if(Keyboard.isCreated() && Keyboard.isKeyDown(GLFW.GLFW_KEY_L)) {
//						System.err.println("####NOT PROCESSING "+delayedSegmentMods.size()+" blocks");
//						for(int i = 0; i < delayedSegmentMods.size(); i++) {
//							System.err.println("BLOCK "+i+": "+delayedSegmentMods.get(i)+"; "+delayedSegmentMods.get(i).getData());
//						}
//					}
					return;
				}else {
					//process normally. no need to store locked segments any longer
					locked.clear();
				}
//				if(Keyboard.isCreated() && Keyboard.isKeyDown(GLFW.GLFW_KEY_L)) {
//					System.err.println("!!!!PROCESSING "+delayedSegmentMods.size()+" blocks");
//					for(int i = 0; i < delayedSegmentMods.size(); i++) {
//						System.err.println("BLOCK "+i+": "+delayedSegmentMods.get(i)+"; "+delayedSegmentMods.get(i).getData());
//					}
//				}
				
				getState().getDebugTimer().start(con, "SendableSegmentController", "DelayedMods", "Handle");
				for (PieceList e : pieceMap.values()) {
					final String vSt = String.valueOf(e.absIndex);
					getState().getDebugTimer().start(con, "SendableSegmentController", "DelayedMods", "Handle", vSt);
					
					handleSegment(e);
					segmentsChanged.put(ElementCollection.getIndex(e.segment.pos), e.segment);
					if (e.segment.isEmpty() && e.segment.getSegmentData() != null) {
						emptySegments.add(e.segment);
					}
					
					
					if (segBlockProc.removes > 0 ) {
						segmentsAABBUpdateNeeded.add(e.segment);
					}
					if(segBlockProc.total > 0) {
						touched = true;
					}
					segBlockProc.reset();
					plProvider.free(e);
					
					getState().getDebugTimer().end(con, "SendableSegmentController", "DelayedMods", "Handle", vSt);
				}
				getState().getDebugTimer().end(con, "SendableSegmentController", "DelayedMods", "Handle");
				
				getState().getDebugTimer().start(con, "SendableSegmentController", "DelayedMods", "FreeEmpty");
				freeEmptySegs();
				getState().getDebugTimer().end(con, "SendableSegmentController", "DelayedMods", "FreeEmpty");
	
				
				getState().getDebugTimer().start(con, "SendableSegmentController", "DelayedMods", "MarkNeighbor");
				for(RemoteSegment s : markNeighborForChanged) {
					for(int z = -1; z <= 1; z++) {
						for(int y = -1; y <= 1; y++) {
							for(int x = -1; x <= 1; x++) {
								int posX = s.pos.x + x * Segment.DIM;
								int posY = s.pos.y + y * Segment.DIM;
								int posZ = s.pos.z + z * Segment.DIM;
								
								long index = ElementCollection.getIndex(posX, posY, posZ);
								
								if(!segmentsChanged.containsKey(index)) {
									con.getSegmentBuffer().get(posX, posY, posZ, callBackTmp);
									if(callBackTmp.segment != null) {
										segmentsChanged.put(index, (RemoteSegment) callBackTmp.segment);
									}
								}
							}
						}
					}
				}
				getState().getDebugTimer().end(con, "SendableSegmentController", "DelayedMods", "MarkNeighbor");
				
				getState().getDebugTimer().start(con, "SendableSegmentController", "DelayedMods", "UpdateAABB");
				updateAABBs();
				getState().getDebugTimer().end(con, "SendableSegmentController", "DelayedMods", "UpdateAABB");
				
				
			} else {
				//dont do delayed updates for unreachable objects
			}
			getState().getDebugTimer().start(con, "SendableSegmentController", "DelayedMods", "SendInventoryMods");
			sendInventoryModifications();
			getState().getDebugTimer().end(con, "SendableSegmentController", "DelayedMods", "SendInventoryMods");
			if(touched) {
				con.setAllTouched(true);
			}
		}
		getState().getDebugTimer().start(con, "SendableSegmentController", "DelayedMods", "ClearBuffers");
		clearDelayedSegmentMods();
		clearBuffers();
		getState().getDebugTimer().end(con, "SendableSegmentController", "DelayedMods", "ClearBuffers");
		
		if(!segmentsChanged.isEmpty()) {
			getState().getDebugTimer().start(con, "SendableSegmentController", "DelayedMods", "UpdateTimestamps");
			for(RemoteSegment s : segmentsChanged.values()) {
				s.dataChanged(true);
				s.setLastChanged(getState().getUpdateTime());
			}
			segmentsChanged.clear();
			getState().getDebugTimer().end(con, "SendableSegmentController", "DelayedMods", "UpdateTimestamps");
		}
		getState().getDebugTimer().end(con, "SendableSegmentController", "DelayedMods", "Total");
	}

	private boolean isAccessible() {
		return isOnServer() || ((GameClientState) getState()).getCurrentSectorEntities().containsKey(con.getId());
	}
	
	private SegmentChangeContainer preProcess(List<VoidSegmentPiece> m) {
		final int size = m.size();
		for(int i = 0; i < size; i++) {
			final VoidSegmentPiece p = m.get(i);
			
			if(p.controllerPos != Long.MIN_VALUE) {
				long controllerIndex = p.controllerPos;
				SegmentPiece controllerPiece = pieceMap.controllers.get(controllerIndex);
				if(controllerPiece == null) {
					controllerPiece = con.getSegmentBuffer().getPointUnsave(controllerIndex, plProvider.getPiece());
					pieceMap.controllers.put(controllerIndex, controllerPiece);
				}
				
				
			}
			
			long index = p.getSegmentAbsoluteIndex();
			
			PieceList pl = pieceMap.get(index);
			if(pl == null) {
				pl = plProvider.get();
				pl.absIndex = index;
				pl.x = ByteUtil.divUSeg(p.voidPos.x) * Segment.DIM;
				pl.y = ByteUtil.divUSeg(p.voidPos.y) * Segment.DIM;
				pl.z = ByteUtil.divUSeg(p.voidPos.z) * Segment.DIM;
				
				
				con.getSegmentBuffer().get(pl.x, pl.y, pl.z, callBackTmp);
				
				
				if(isOnServer() || callBackTmp.state != SegmentBufferOctree.NOTHING || p.forceClientSegmentAdd) {
					//dont process blocks in unavailable segments on client if they aren't forced
					if(callBackTmp.state < 0) {
						pl.segment = isOnServer() ? new RemoteSegment(con) : new DrawableRemoteSegment(con);
						pl.segment.setPos(pl.x, pl.y, pl.z );
						System.err.println("[BLOCKPROCESSOR] NEW SEGMENT FROM ADDED PIECE: "+con.getState()+"; "+pl.segment.pos);
						pl.newSegment = true;
					}else {
						assert(callBackTmp.segment != null);
						pl.segment = (RemoteSegment) callBackTmp.segment;
					}
					pieceMap.put(index, pl);
				}
			}
			if(ElementKeyMap.isValidType(p.getType())) {
				pl.blocksModOrAdd++;
			}else {
				pl.blocksRemoved++;
			}
			pl.add(p);
		}
		
		
		
		return pieceMap;
	}
	
	private void clearDelayedSegmentMods() {
		final int size = delayedSegmentMods.size();
		for(int i = 0; i < size; i++) {
			receivedPiecePool.free(delayedSegmentMods.get(i));
		}
		delayedSegmentMods.clear();
	}
	private void clearBuffers() {
		inventoryMods.clear();
		segmentsAABBUpdateNeeded.clear();
		emptySegments.clear();
		pieceMap.clear();
		plProvider.clearProcessingData();
		plProvider.freeAllPieces();
	}

	private void sendInventoryModifications() {
		for (Entry<PlayerState, IntOpenHashSet> e : inventoryMods.entrySet()) {
			e.getKey().sendInventoryModification(e.getValue(), Long.MIN_VALUE);
		}
	}

	private void updateAABBs() {
		if (!segmentsAABBUpdateNeeded.isEmpty()) {
			for (Segment s : segmentsAABBUpdateNeeded) {
				con.getSegmentProvider().enqueueAABBChange(s);
			}
		}
		
	}

	private void freeEmptySegs() {
		final int size = emptySegments.size();
		for (int i = 0; i < size; i++) {
			Segment s = emptySegments.get(i);
			SegmentData segmentData = s.getSegmentData();
			if (segmentData != null) {
				if (!isOnServer()) {
					((GameClientState) getState()).getWorldDrawer().getFlareDrawerManager().clearSegment((DrawableRemoteSegment) s);
				}
				try {
					segmentData.rwl.writeLock().lock();
					//we can add fast since the segment is already empty
					//so no operation has to be done
					assert (segmentData.getSegment() == null || segmentData.getSegment().isEmpty());
					assert (segmentData.getSize() == 0);
					con.getSegmentProvider().addToFreeSegmentDataFast(segmentData);
				} finally {
					segmentData.rwl.writeLock().unlock();
				}
			}
		}
		
	}
	private boolean aquireSegmentLock(PieceList e) {
		
		String vSt = String.valueOf(e.absIndex);
		
		
		if (e.blocksModOrAdd > 0 && e.segment.isEmpty()) {
			getState().getDebugTimer().start(con, "SendableSegmentController", "DelayedMods", "Aquire", vSt, "assign");
			SegmentData mine = con.getSegmentProvider().getFreeSegmentData();
			mine.assignData(e.segment);
			getState().getDebugTimer().end(con, "SendableSegmentController", "DelayedMods", "Aquire", vSt, "assign");
		}
		
		
		final int size = e.size();
		
		final SegmentData segmentData = e.segment.getSegmentData();
		if(segmentData == null) {
			assert(e.blocksModOrAdd == 0);
			assert(e.blocksRemoved == size);
			//special case when client remove all the blocks of a segment through build actions. The server then sends confirm blocks to the same client
			plProvider.connectionsFrom.clear();
			plProvider.connectionsTo.clear();
			plProvider.oldTypes.clear();
			System.err.println("[BLOCKPROCESSOR] Segment already empty. nothing to do");
			assert(e.segmentData == null);
			return true;
		}
		e.segmentData = segmentData;
		getState().getDebugTimer().start(con, "SendableSegmentController", "DelayedMods", "Aquire", vSt, "lock");
		boolean lock = segmentData.rwl.writeLock().tryLock();
		getState().getDebugTimer().end(con, "SendableSegmentController", "DelayedMods", "Aquire", vSt, "lock");
		return lock;
	}
	private void handleSegment(PieceList e) {
		
		String vSt = String.valueOf(e.absIndex);
		if(e.segmentData == null) {
			return; //no need to process. was already processed
		}
		
		getState().getDebugTimer().start(con, "SendableSegmentController", "DelayedMods", "Handle", vSt, "handleSeg");

		
		final int size = e.size();
		
		final SegmentData segmentData = e.segmentData;
		
		try {
			if(e.blocksModOrAdd > 0) {
				getState().getDebugTimer().start(con, "SendableSegmentController", "DelayedMods", "Handle", vSt, "onBlockAddedHandled");
				con.onBlockAddedHandled();
				getState().getDebugTimer().end(con, "SendableSegmentController", "DelayedMods", "Handle", vSt, "onBlockAddedHandled");
			}
			for(int i = 0; i < size; i++) {
				
				getState().getDebugTimer().start(con, "SendableSegmentController", "DelayedMods", "Handle", vSt, "BLOCK"+i);
				VoidSegmentPiece segmentPiece = e.get(i);
				
				assert(segmentData != null):e.segment+"; "+e.blocksModOrAdd+"; "+e.size();
				current.setByReference(e.segment, (byte)ByteUtil.modUSeg(segmentPiece.voidPos.x), (byte)ByteUtil.modUSeg(segmentPiece.voidPos.y), (byte)ByteUtil.modUSeg(segmentPiece.voidPos.z));
				
				segmentPiece.setSegment(current.getSegment());
				segmentPiece.x = current.x;
				segmentPiece.y = current.y;
				segmentPiece.z = current.z;
				
				final short oldType = current.getType();

				//INSERTED CODE
				SegmentPieceModifyOnClientEvent event = new SegmentPieceModifyOnClientEvent(segmentPiece, current);
				StarLoader.fireEvent(SegmentPieceModifyOnClientEvent.class, event, this.isOnServer());
				if(event.isCanceled()){
					continue;
				}
				///

				try {
					getState().getDebugTimer().start(con, "SendableSegmentController", "DelayedMods", "Handle", vSt, "BLOCK"+i+"HandleChange");
					if(segmentPiece.onlyHitpointsChanged && (segmentPiece.isAlive() || current.getType() == ElementKeyMap.CORE_ID)){
						//insert type since only hitpoints changed
						int hitPoints = segmentPiece.getHitpointsByte();
						segmentPiece.setDataByReference(current.getData());
						segmentPiece.setHitpointsByte(hitPoints);
						segmentPiece.onlyHitpointsChanged = false;
					}else if(segmentPiece.onlyActiveChanged){
						//insert type since only active changed
						boolean act = segmentPiece.isActive();
						segmentPiece.setDataByReference(current.getData());
						segmentPiece.setActive(act);
						segmentPiece.onlyHitpointsChanged = false;
					}
					
					
					if (!isOnServer() && segmentPiece.getType() == Element.TYPE_NONE && segmentPiece.isDead()) {
						
						
						float propSpawn = 1f / ((float)Math.max(1, ShardDrawer.shardsAddedFromNTBlocks));
						
						if(Math.random() < propSpawn) {
							Vector3f f = new Vector3f();
							f.set(segmentPiece.voidPos.x, segmentPiece.voidPos.y, segmentPiece.voidPos.z);
							f.x -= SegmentData.SEG_HALF;
							f.y -= SegmentData.SEG_HALF;
							f.z -= SegmentData.SEG_HALF;
							((GameClientState) getState()).getWorldDrawer().getShards().voronoiBBShatterDelayed(
									(PhysicsExt) ((GameClientState) getState()).getPhysics(), f, 
									current.getType(), con, con.getGravity().source);
							ShardDrawer.shardsAddedFromNTBlocks++;
						}
						con.onBlockKill(current, null);
					}else{
						//if there was no block there previously, we were repairing or adding a block otherwise
						int damage = (current.getType() != 0 ? current.getHitpointsFull() : 0) - segmentPiece.getHitpointsFull();
						if(damage > 0) con.onBlockDamage(current.getAbsoluteIndex(), current.getType(), damage, DamageDealerType.GENERAL, null);
					}
					getState().getDebugTimer().end(con, "SendableSegmentController", "DelayedMods", "Handle", vSt, "BLOCK"+i+"HandleChange");
					assert(segmentPiece.getSegment() != null);
					
					getState().getDebugTimer().start(con, "SendableSegmentController", "DelayedMods", "Handle", vSt, "BLOCK"+i+"ProcessPiece");
					
					boolean synched = false;
					boolean handledOk = 
							 processPiece(segmentPiece, segmentPiece.senderId, segmentPiece.controllerPos, oldType, inventoryMods, synched, emptySegments, segmentsAABBUpdateNeeded, con.getUpdateTime());
					getState().getDebugTimer().end(con, "SendableSegmentController", "DelayedMods", "Handle", vSt, "BLOCK"+i+"ProcessPiece");
					
					if (isOnServer()) {
						if (!handledOk) {
							//sending actual value to update the clients
							SegmentPiece p = new SegmentPiece(current);
							if (p.isDead()) {
								//to not spawn debris
								p.setHitpointsByte(1);
							}
							//need to duplicate piece, since the original is going to be freed
							SegmentPiece cp = new SegmentPiece(p);
							con.sendBlockMod(new RemoteSegmentPiece(cp, isOnServer()));
						} else {
							//sending new value since update succeded
							segmentPiece.forceClientSegmentAdd = e.newSegment;
							
							//need to duplicate piece, since the original is going to be freed
							SegmentPiece cp = new SegmentPiece(segmentPiece);
							cp.forceClientSegmentAdd = e.newSegment;
							RemoteSegmentPiece remoteSegmentPiece = new RemoteSegmentPiece(cp, isOnServer());
							con.sendBlockMod(remoteSegmentPiece);
						}
					}
					
					if (handledOk) {
						if (segmentPiece.getType() != Element.TYPE_NONE && segmentPiece.getType() != oldType) {
							
							plProvider.oldTypes.add(oldType);
							
							if (ElementKeyMap.getInfoFast(segmentPiece.getType()).getControlledBy().contains(ElementKeyMap.CORE_ID) && con instanceof Ship) {
								
								plProvider.connectionsFrom.add(ElementCollection.getIndex(Ship.core));
								plProvider.connectionsTo.add(segmentPiece.getAbsoluteIndex());
							} else if (segmentPiece.controllerPos != Long.MIN_VALUE) {
								
								plProvider.connectionsFrom.add(segmentPiece.controllerPos);
								plProvider.connectionsTo.add(segmentPiece.getAbsoluteIndex());
								
								
							}
						}
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}		
				getState().getDebugTimer().end(con, "SendableSegmentController", "DelayedMods", "Handle", vSt, "BLOCK"+i);
			}
			
			if (e.newSegment) {
				getState().getDebugTimer().start(con, "SendableSegmentController", "DelayedMods", "Handle", vSt, "addImmediate");
				con.getSegmentBuffer().addImmediate(e.segment);
				con.getSegmentBuffer().updateBB(e.segment);
				getState().getDebugTimer().end(con, "SendableSegmentController", "DelayedMods", "Handle", vSt, "addImmediate");
			}
			
			getState().getDebugTimer().start(con, "SendableSegmentController", "DelayedMods", "Handle", vSt, "controllerUpdate");
			final int pSize = plProvider.connectionsFrom.size();
			for(int i = 0; i < pSize; i++) {
				long from = plProvider.connectionsFrom.getLong(i);
				long to = plProvider.connectionsTo.getLong(i);
				short oldType = plProvider.oldTypes.getShort(i);
				
				SegmentPiece controllerPiece = pieceMap.controllers.get(from);
				if(controllerPiece == null && from == ElementCollection.getIndex(Ship.core)) {
					controllerPiece = con.getSegmentBuffer().getPointUnsave(from);
					pieceMap.controllers.put(from, controllerPiece);
				}
				if (controllerPiece != null) {
					if (ElementKeyMap.isValidType(controllerPiece.getType())) {
						try {
							if(!isOnServer()){
								//this is a rail replaced with a different block type
								//we need to remove all links to this block first
								getState().getDebugTimer().start(con, "SendableSegmentController", "DelayedMods", "Handle", vSt, "removedControlledFromAll"+i);
								con.getControlElementMap().removeControlledFromAll(current.getAbsoluteIndex(), oldType, false);
								getState().getDebugTimer().end(con, "SendableSegmentController", "DelayedMods", "Handle", vSt, "removedControlledFromAll"+i);
							}
							con.setCurrentBlockController(controllerPiece, tmpPiece, to);
						} catch (CannotBeControlledException ex) {
							ex.printStackTrace();
						}
					} else {
						System.err.println("Exception: Client sent controller, that is type 0: " + controllerPiece + " for " + ElementCollection.getPosFromIndex(to, new Vector3i()));
					}
				} else {
					System.err.println("WARNING: NOT CONNECTION " + ElementCollection.getPosFromIndex(from, new Vector3i()) + " to " + ElementCollection.getPosFromIndex(to, new Vector3i()));
				}
			}
			plProvider.connectionsFrom.clear();
			plProvider.connectionsTo.clear();
			plProvider.oldTypes.clear();
			getState().getDebugTimer().end(con, "SendableSegmentController", "DelayedMods", "Handle", vSt, "controllerUpdate");
		}finally {
			if(segmentData != null) {
				getState().getDebugTimer().start(con, "SendableSegmentController", "DelayedMods", "Handle", vSt, "unlock");
				segmentData.rwl.writeLock().unlock();
				getState().getDebugTimer().end(con, "SendableSegmentController", "DelayedMods", "Handle", vSt, "unlock");
			}
		}
		getState().getDebugTimer().end(con, "SendableSegmentController", "DelayedMods", "Handle", String.valueOf(e.absIndex), "handleSeg");
	}
	private boolean processPiece(VoidSegmentPiece segmentPiece, int senderId, long currentController, final short oldType, Object2ObjectOpenHashMap<PlayerState, IntOpenHashSet> inventoryMod, boolean synched, List<Segment> emptySegments, List<Segment> segemntsAABBUPdateNeeded, long time) throws InterruptedException, IOException {
		if (this instanceof TransientSegmentController) {
			((TransientSegmentController) this).setTouched(true, false);
		}
		
		boolean handledOk = segBlockProc.handleSegmentPiece(segmentPiece.getSegment(), segmentPiece, oldType, senderId, inventoryMod, synched, time);

		if (handledOk) {
			long index = ElementCollection.getIndex(segmentPiece.voidPos);
			LongArrayList longArrayList = connectionsToAddFromPaste.remove(index);
			if (segmentPiece.getType() != Element.TYPE_NONE) {
				if (longArrayList != null) {
					for (int i = 0; i < longArrayList.size(); i++) {
						long controllerIndex = longArrayList.getLong(i);
						con.getControlElementMap().addControllerForElement(controllerIndex, index, segmentPiece.getType());
					}
				}
				String text = textToAddFromPaste.remove(index);
				if (text != null) {
					con.getTextMap().put(ElementCollection.getIndex4(segmentPiece.voidPos, segmentPiece.getOrientation()), text);
				}
			}
		}
		
		return handledOk;
	}
	public void handleDelayedBuklkMods() {
		while (!delayedBulkMods.isEmpty()) {
			BlockBulkSerialization b = delayedBulkMods.dequeue();

			handleReceivedBulk(b);

		}
	}
	private void handleReceivedBulk(BlockBulkSerialization b) {
		long time = System.currentTimeMillis();

		for (Entry<Long, ByteArrayList> e : b.buffer.entrySet()) {
			long segmentPos = e.getKey();
			ByteArrayList changed = e.getValue();

			con.getSegmentBuffer().get(
					ElementCollection.getPosX(segmentPos),
					ElementCollection.getPosY(segmentPos),
					ElementCollection.getPosZ(segmentPos),
					callBackTmp);

			if (callBackTmp.state == 1) {
				Segment segment = callBackTmp.segment;
				
				try{
					
					segment.getSegmentData().checkWritable();
				}catch(SegmentDataWriteException ee){
					SegmentDataWriteException.replaceData(segment);
				}
				try{
				int p = 0;
				if (changed.size() == 1) {
					assert (changed.get(0) == (byte) 0);
					//size 1 means the whole segment got emptied
					//other blocks are at least 3
					try {
						segment.getSegmentData().reset(time);
					} catch (SegmentDataWriteException e1) {
						throw new RuntimeException("Chunk should be normal here", e1);
					}

				} else {
					boolean deleted = false;
					while (p < changed.size()) {
						

						byte x = (byte) (changed.get(p) & 255);
						byte y = (byte) (changed.get(p + 1) & 255);
						byte z = (byte) (changed.get(p + 2) & 255);
						int hitPoints = changed.get(p + 3) & 255;


						int infoIndex = SegmentData.getInfoIndex(x, y, z);
						short typeOld = segment.getSegmentData().getType(infoIndex);
						if (hitPoints > 0 || typeOld == ElementKeyMap.CORE_ID) {
							((RemoteSegment) segment).getSegmentData().setHitpointsByte(infoIndex, hitPoints);
						} else {
							deleted = true;
							((RemoteSegment) segment).getSegmentData().setInfoElement(x, y, z, (short) 0, false, infoIndex, time);

							if (((GameClientState) getState()).getWorldDrawer() != null && ((x % 2 == 0 && y % 2 == 0 && z % 2 == 0) || Math.random() > 0.9)) {
								Vector3f f = new Vector3f();
								segment.getAbsoluteElemPos(x, y, z, f);
								f.x -= SegmentData.SEG_HALF;
								f.y -= SegmentData.SEG_HALF;
								f.z -= SegmentData.SEG_HALF;
								((GameClientState) getState()).getWorldDrawer().getShards()
								.voronoiBBShatterDelayed((PhysicsExt) ((GameClientState) getState()).getPhysics(), f, typeOld, con, con.getGravity().source);
							}

						}

						p += 4;
					}

					if (deleted) {
						if (((RemoteSegment) segment).getSegmentData().getSize() == 0) {
							SegmentData segmentData = ((RemoteSegment) segment).getSegmentData();
							con.getSegmentProvider().addToFreeSegmentDataFast(segmentData);
						}
					}

					((RemoteSegment) segment).dataChanged(deleted);

				}
				}catch(SegmentDataWriteException ee){
					throw new RuntimeException("Should be already changed before this", ee);
				}
			}
			BlockBulkSerialization.freeBufferClient(changed);

			CollisionObject pObject = con.getPhysicsDataContainer().getObject();
			if (pObject != null) {
				pObject.activate(true);
				((RigidBody) pObject).applyGravity();
			}
		}
	}
	private StateInterface getState() {
		return con.getState();
	}

	public boolean isOnServer() {
		return con.isOnServer();
	}

	public void received(BlockBulkSerialization blockBulkSerialization) {
		delayedBulkMods.enqueue(blockBulkSerialization);
	}

	public void receivedMods(NetworkSegmentProvider s) {
		for (int i = 0; i < s.modificationBuffer.getReceiveBuffer().size(); i++) {
			VoidSegmentPiece segmentPiece = receivedPiecePool.get();
			segmentPiece.setByValue((VoidSegmentPiece)s.modificationBuffer.getReceiveBuffer().get(i).get());
			assert(!con.isOnServer() || segmentPiece.senderId != 0);
			delayedSegmentMods.add((VoidSegmentPiece) segmentPiece);
			
			
		}
		for (int i = 0; i < s.killBuffer.getReceiveBuffer().size(); i+=4) {
			short x  = s.killBuffer.getReceiveBuffer().getShort(i);
			short y  = s.killBuffer.getReceiveBuffer().getShort(i+1);
			short z  = s.killBuffer.getReceiveBuffer().getShort(i+2);
			short hp = s.killBuffer.getReceiveBuffer().getShort(i+3);
			
			VoidSegmentPiece p = receivedPiecePool.get();
			p.voidPos.set(x,y,z);
			p.onlyHitpointsChanged = true;
			p.setType((short)0);
			p.setHitpointsByte(hp);
			delayedSegmentMods.add(p);
		}
	
		for (int i = 0; i < s.activeChangedTrueBuffer.getReceiveBuffer().size(); i+=3) {
			short x = s.activeChangedTrueBuffer.getReceiveBuffer().getShort(i);
			short y = s.activeChangedTrueBuffer.getReceiveBuffer().getShort(i+1);
			short z = s.activeChangedTrueBuffer.getReceiveBuffer().getShort(i+2);
			boolean act = true;
			
			VoidSegmentPiece p = receivedPiecePool.get();
			p.voidPos.set(x,y,z);
			p.setActive(act);
			p.onlyActiveChanged = true;
			delayedSegmentMods.add(p);
		}
		for (int i = 0; i < s.activeChangedFalseBuffer.getReceiveBuffer().size(); i+=3) {
			short x = s.activeChangedFalseBuffer.getReceiveBuffer().getShort(i);
			short y = s.activeChangedFalseBuffer.getReceiveBuffer().getShort(i+1);
			short z = s.activeChangedFalseBuffer.getReceiveBuffer().getShort(i+2);
			boolean act = false;
			
			VoidSegmentPiece p = receivedPiecePool.get();
			p.voidPos.set(x,y,z);
			p.setActive(act);
			p.onlyActiveChanged = true;
			delayedSegmentMods.add(p);
		}
		for (int i = 0; i < s.salvageBuffer.getReceiveBuffer().size(); i+=3) {
			short x = s.salvageBuffer.getReceiveBuffer().getShort(i);
			short y = s.salvageBuffer.getReceiveBuffer().getShort(i+1);
			short z = s.salvageBuffer.getReceiveBuffer().getShort(i+2);
			
			VoidSegmentPiece p = receivedPiecePool.get();
			p.voidPos.set(x,y,z);
			p.setType((short)0);
			p.setHitpointsByte(1);
			delayedSegmentMods.add(p);
		}
		for (int i = 0; i < s.modificationBulkBuffer.getReceiveBuffer().size(); i++) {
			received(s.modificationBulkBuffer.getReceiveBuffer().get(i).get());
		}		
	}
	
	
	
	public abstract class SegmentBlockProcessor{
		
		protected int removes;
		protected int changes;
		protected int adds;
		protected int total;
		
		public void reset() {
			removes = 0;
			changes = 0;
			adds = 0;
			total = 0;
		}

		public abstract boolean checkPermission(SegmentPiece segmentPiece, short type, int senderId);
		
		public boolean handleSegmentPiece(Segment segment, SegmentPiece segmentPiece, final short lastType, int senderId, Object2ObjectOpenHashMap<PlayerState, IntOpenHashSet> inventoryMod, boolean synched, long time) throws InterruptedException {

			assert(segment != null):segmentPiece+"; "+con;
			
			boolean wasDataNull = false;
			
			short type = segmentPiece.getType();
			if(ElementKeyMap.isValidType(type) && ElementKeyMap.isValidType(ElementKeyMap.getInfoFast(type).getSourceReference())){
				type = (short) ElementKeyMap.getInfoFast(type).getSourceReference();
			}
			if(senderId != 0 && !checkPermission(segmentPiece, type, senderId)) {
				return false;
			}
			

			int changeId;
			try{
				changeId = segment.getSegmentData().applySegmentData(segmentPiece.x, segmentPiece.y, segmentPiece.z, segmentPiece.getData(), 0, synched, segmentPiece.getAbsoluteIndex(), false, true, time);
			}catch(SegmentDataWriteException ee){
				SegmentDataWriteException.replaceData(segment);
				try {
					changeId = segment.getSegmentData().applySegmentData(segmentPiece.x, segmentPiece.y, segmentPiece.z, segmentPiece.getData(), 0, synched, segmentPiece.getAbsoluteIndex(), false, true, time);
				} catch (SegmentDataWriteException e) {
					throw new RuntimeException(e);
				}
			}
			switch(changeId) {
				case(SegmentData.PIECE_ADDED): onPieceAdded(segment, segmentPiece, type, lastType, senderId, inventoryMod); break;
				case(SegmentData.PIECE_ACTIVE_CHANGED): onPieceActiveChanged(segment, segmentPiece, lastType, senderId, inventoryMod); break;
				case(SegmentData.PIECE_REMOVED): onPieceRemoved(segment, segmentPiece, lastType, senderId, inventoryMod); break;
				case(SegmentData.PIECE_CHANGED): break;
				case(SegmentData.PIECE_UNCHANGED): break;
				default: throw new RuntimeException("unknown change id: "+changeId);
			}
			
			checkSurround(segment, segmentPiece, type);
			
			return true;
		}

		protected abstract void checkSurround(Segment segment, SegmentPiece segmentPiece, short type);

		protected abstract void onPieceRemoved(Segment segment, SegmentPiece segmentPiece, short lastType, int senderId, Object2ObjectOpenHashMap<PlayerState, IntOpenHashSet> inventoryMod);
		

		protected abstract void onPieceActiveChanged(Segment segment, SegmentPiece segmentPiece, short lastType, int senderId, Object2ObjectOpenHashMap<PlayerState, IntOpenHashSet> inventoryMod);


		protected void onPieceAdded(Segment segment, SegmentPiece segmentPiece, short type, short lastType, int senderId, Object2ObjectOpenHashMap<PlayerState, IntOpenHashSet> inventoryMod) {
			adds++;
			total++;
		}

		
	}
	public void onBlockChanged(RemoteSegment seg) {
		segmentsChanged.put(ElementCollection.getIndex(seg.pos), seg);
	}
	public class SegmentBlockProcessorServer extends SegmentBlockProcessor{
		
		private final GameServerState state = (GameServerState)con.getState();
		
		
		
		@Override
		public boolean handleSegmentPiece(Segment segment, SegmentPiece segmentPiece, short lastType, int senderId,
				Object2ObjectOpenHashMap<PlayerState, IntOpenHashSet> inventoryMod, boolean synched, long time)
				throws InterruptedException {
			
			if (con.getLastModifierId() != senderId) {
				con.setLastModifierChanged(true);
				con.setLastModifierId(senderId);
			}
			
			return super.handleSegmentPiece(segment, segmentPiece, lastType, senderId, inventoryMod, synched, time);
		}


		@Override
		protected void onPieceAdded(Segment segment, SegmentPiece segmentPiece, short type, short lastType, int senderId, Object2ObjectOpenHashMap<PlayerState, IntOpenHashSet> inventoryMod) {
			super.onPieceAdded(segment, segmentPiece, type, lastType, senderId, inventoryMod);
			if (type == ElementKeyMap.FACTION_BLOCK) {
				System.err.println("[SERVER] FACTION BLOCK ADDED TO " + this + "; resetting faction!");
				con.railController.resetFactionForEntitiesWithoutFactionBlock(con.getFactionId());
			}

			try {
				short typeT = segmentPiece.getType();
				if(ElementKeyMap.isValidType(typeT)){
					
					if(ElementKeyMap.isValidType(ElementKeyMap.getInfoFast(typeT).getSourceReference())){
						typeT = (short) ElementKeyMap.getInfoFast(typeT).getSourceReference();
					}
					
					con.getHpController().onManualAddBlock(ElementKeyMap.getInfo(typeT));
				}
				if(!segmentPiece.getSegmentController().isVirtualBlueprint()){
					PlayerState player = state.getPlayerFromStateId(senderId);
					IntOpenHashSet mods = inventoryMod.get(player);
					if (mods == null) {
						mods = new IntOpenHashSet();
						inventoryMod.put(player, mods);
					}
					int buildSlot = player.getSelectedBuildSlot();
					if (player.getInventory(null).getType(buildSlot) == typeT) {
						player.getInventory(null).inc(buildSlot, typeT, -1);
						mods.add(buildSlot);
					} else {
						player.getInventory(null).decreaseBatch(typeT, 1, mods);
					}
				}

			} catch (PlayerNotFountException e1) {
				e1.printStackTrace();
			}
			
		}
		

		@Override
		public boolean checkPermission(SegmentPiece segmentPiece, short type, int senderId) {
			assert(senderId != 0):"sender "+senderId+" was server but also received on server";
			BoundingBox bb = con.getSegmentBuffer().getBoundingBox();
			try {
				PlayerState player = state.getPlayerFromStateId(senderId);
				if(type != Element.TYPE_NONE && state.getGameConfig().isHasMaxDim() && !state.getGameConfig().isOk(bb, con)){
					player.sendServerMessagePlayerError(Lng.astr(
							"Server doesn't allow ship's dimension\nShip: %s\nAllowed: %s\nCan't add any more blocks until ship is reduced to allowed size.", 
							bb.toStringSize(-2), //account for extra size
							state.getGameConfig().toStringAllowedSize(con)));
					return false;
				}
				boolean admin = state.isAdmin(player.getName());
				int slotCount;
				int overallCount;
				
				if (type != Element.TYPE_NONE &&
						(slotCount = player.getInventory(null).getCount(player.getSelectedBuildSlot(), type)) <= 0 &&
						(overallCount = player.getInventory(null).getOverallQuantity(type)) <= 0) {
					System.err.println("[SERVER] place-before: Player " + player + " doesnt have enough in the slot he wants to build with: " + segmentPiece +"; slot "+slotCount+"; overall "+overallCount);
					if (!admin || !Segment.ALLOW_ADMIN_OVERRIDE) {
						return false;
					} else {
						System.err.println("[SERVER] overwritten by admin rights");
					}
				}
				if (!admin && !con.allowedToEdit(player)) {
					/*
					 * decrease type from inventory if it was tried to be deleted to
					 * prevent exploit
					 */

					Object[] error = Lng.astr("SERVER-WARNING: %s\ntried to edit\n%s\n(faction access denied)", player.getName(),  con.toNiceString());
					System.err.println(error);
					if (System.currentTimeMillis() - GameServerState.lastBCMessage > 2000) {
						state.getController().broadcastMessage(error, ServerMessage.MESSAGE_TYPE_ERROR);
					}
					return false;
				}
				
				
				
				/*
				 * check if player actually has this type in the inventory
				 */
				if (ElementKeyMap.isValidType(type)&& !segmentPiece.getSegmentController().isVirtualBlueprint()) { //on server
					int firstSlot = player.getInventory(null).getFirstSlot(type, true);
					if (firstSlot == Inventory.FREE_RET) {
						System.err.println("[SERVER] place-after: Player " + player + " doesnt have enough in the slot he wants to build with: " + segmentPiece);
						if (!admin || !Segment.ALLOW_ADMIN_OVERRIDE) {
							return false;
						} else {
							System.err.println("[SERVER] overwritten by admin rights");
						}
					}
				}
				
			} catch (PlayerNotFountException e) {
				e.printStackTrace();
				Object[] error = Lng.astr("SERVER-WARNING: unknown(%s)\ntried to edit\n%s\n(faction access denied)",  senderId,  con.toNiceString());
				System.err.printf("SERVER-WARNING: unknown(%s)\ntried to edit\n%s\n(faction access denied)\n",  senderId,  con.toNiceString());
				if (System.currentTimeMillis() - GameServerState.lastBCMessage > 2000) {

					state.getController().broadcastMessage(error, ServerMessage.MESSAGE_TYPE_ERROR);
				}
				return false;
			}
			
			
			
			return true;
		}


		@Override
		protected void checkSurround(Segment segment, SegmentPiece segmentPiece, short type) {
		}


		@Override
		protected void onPieceActiveChanged(Segment segment, SegmentPiece segmentPiece, short lastType, int senderId,
				Object2ObjectOpenHashMap<PlayerState, IntOpenHashSet> inventoryMod) {
			changes++;
			total++;			
		}


		@Override
		protected void onPieceRemoved(Segment segment, SegmentPiece segmentPiece, short lastType, int senderId, Object2ObjectOpenHashMap<PlayerState, IntOpenHashSet> inventoryMod) {
			
			removes++;
			total++;
			if (ElementKeyMap.isValidType(lastType)) {
					
				if(ElementKeyMap.isValidType(ElementKeyMap.getInfoFast(lastType).getSourceReference())){
					lastType = (short) ElementKeyMap.getInfoFast(lastType).getSourceReference();
				}
				
				con.getHpController().onManualRemoveBlock(ElementKeyMap.getInfo(lastType));
			}
			if(!con.isVirtualBlueprint() && senderId != 0){
				try {
					PlayerState player = state.getPlayerFromStateId(senderId);
					IntOpenHashSet mods = inventoryMod.get(player);
					if (mods == null) {
						mods = new IntOpenHashSet();
						inventoryMod.put(player, mods);
					}
					short typeGotten = lastType;
					if (con.isScrap()) {
						if (Universe.getRandom().nextFloat() > 0.5f) {
							typeGotten = ElementKeyMap.SCRAP_ALLOYS;
						} else {
							typeGotten = ElementKeyMap.SCRAP_COMPOSITE;
						}
					}

					mods.add(player.getInventory().incExistingOrNextFreeSlot(typeGotten, 1));

					if (ServerConfig.ENABLE_BREAK_OFF.isOn()) {
						((EditableSendableSegmentController) con).checkCore(segmentPiece);
					}
				} catch (PlayerNotFountException e1) {
					e1.printStackTrace();
				}
			}
			
		}
	}
	public class SegmentBlockProcessorClient extends SegmentBlockProcessor{

		@Override
		public boolean checkPermission(SegmentPiece segmentPiece, short type, int senderId) {
			return true;
		}

		@Override
		public boolean handleSegmentPiece(Segment segment, SegmentPiece segmentPiece, short lastType, int senderId,
				Object2ObjectOpenHashMap<PlayerState, IntOpenHashSet> inventoryMod, boolean synched, long time)
				throws InterruptedException {
			boolean ok = super.handleSegmentPiece(segment, segmentPiece, lastType, senderId, inventoryMod, synched, time);
			
			if(ok) {
				markNeighbors(segment);
			}
			return ok;
		}

		@Override
		protected void checkSurround(Segment segment, SegmentPiece segmentPiece, short type) {
			if (!segment.isEmpty()) {
				if (!ElementInformation.isAlwaysPhysical(segmentPiece.getType()) && segmentPiece.isEdgeOfSegment()) {
					byte sideFlags = segmentPiece.getCornerSegmentsDiffs();
					int c = 0;
					byte x = 0;
					byte y = 0;
					byte z = 0;
					for (int i = 0; i < 6; i++) {
						if ((Element.SIDE_FLAG[i] & sideFlags) == Element.SIDE_FLAG[i]) {

							segment.setChangedSurround(Element.DIRECTIONSb[i].x, Element.DIRECTIONSb[i].y, Element.DIRECTIONSb[i].z);
							c++;
							x += Element.DIRECTIONSb[i].x;
							y += Element.DIRECTIONSb[i].y;
							z += Element.DIRECTIONSb[i].z;

						}
					}
					if (c > 2) {
						// a direct corner
						// its 1/-1 for every coordinate, so we have to mark the rest
						segment.setChangedSurround(x, y, 0);
						segment.setChangedSurround(x, 0, z);
						segment.setChangedSurround(0, y, z);
						segment.setChangedSurround(x, y, z);
					} else if (c > 1) {
						//at least 2 coordinates are extreme
						segment.setChangedSurround(x, y, z);
					}
				}
			}				
		}

		@Override
		protected void onPieceActiveChanged(Segment segment, SegmentPiece segmentPiece, short lastType, int senderId,
				Object2ObjectOpenHashMap<PlayerState, IntOpenHashSet> inventoryMod) {
				changes++;
				total++;
				assert (segmentPiece.getType() != 0);
				//only add to activated delegation if this type actually need to be deligated
				if (con instanceof ManagedSegmentController<?> &&
						((ManagedSegmentController<?>) con).getManagerContainer()
								.getTypesThatNeedActivation().contains(segmentPiece.getType()) ||
						ElementKeyMap.getInfo(segmentPiece.getType()).isController()) {
					SegmentPiece blockActivationPiece = new SegmentPiece(segmentPiece);
					//needs to be instanced to not reset on the end of processing
					con.getNeedsActiveUpdateClient().enqueue(blockActivationPiece);

				}
		}

		@Override
		protected void onPieceRemoved(Segment segment, SegmentPiece segmentPiece, short lastType, int senderId,
				Object2ObjectOpenHashMap<PlayerState, IntOpenHashSet> inventoryMod) {
			removes++;
			total++;			
		}
		
	}

	public void markNeighbors(Segment segment) {
		markNeighborForChanged.add((RemoteSegment)segment);
	}
	
}
