package org.schema.game.common.controller;

import com.bulletphysics.linearmath.Transform;
import com.googlecode.javaewah.EWAHCompressedBitmap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.SegmentRetrieveCallback;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.RemoteSegment;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.forms.BoundingBox;

public class SegmentBufferManager implements SegmentBufferInterface {

	public static final int DIMENSION = 16;
	public static final int DIMENSIONxDIMENSION = DIMENSION * DIMENSION;
	public static final int DIMENSION_HALF = DIMENSION / 2;
	public static final boolean activatePushBack = false;
	static final long maskY = ((1l << (42l - 21l)) - 1l) << 21l;
	static final long maskZ = ((1l << (63l - 42l)) - 1l) << 42l;
	private static final int MAX_BUFFERED_CLIENT = 64;

	public static final int DENSITY_MAP_BIT_SCALE = 2;
	public static final int DENSITY_MAP_SCALE = 1 << DENSITY_MAP_BIT_SCALE;
	public static final int DENSITY_MAP_SIZE = (Segment.DIM >> DENSITY_MAP_BIT_SCALE) * DIMENSION + 1;
	public static final int DENSITY_MAP_SIZE_P2 = DENSITY_MAP_SIZE * DENSITY_MAP_SIZE;
	
	//	private TreeMap<Long, SegmentBufferInterface> buffer;
	private static final int MAX_BUFFERED_SERVER = 32;
	private static final int MAX_COORDINATE = 1 << 20;
	private final Long2ObjectOpenHashMap<SegmentBufferInterface> buffer = new Long2ObjectOpenHashMap<SegmentBufferInterface>();
	private final Vector3i fromBuffer = new Vector3i();
	private final Vector3i toBuffer = new Vector3i();
	public int lastIteration = 0;
	public int lastDoneIteration = 0;
	private SegmentController segmentController;
	private BoundingBox boundingBox;
	private int totalSize;

	private long lastInteraction;
	private int freeSegmentData;
	private int allocatedSegmentData;
	private int writingThreads;
	private long lastSegmentLoadChange;

	//	@SuppressWarnings("unused")
	//	private static long calculateIndexFromCoordinates(Vector3i p) {
	//		return calculateIndexFromCoordinates(p.x, p.y, p.z);
	//	}
	private boolean untouched = true;
	private Vector3b sample = new Vector3b();
	private Vector3i outSegPos = new Vector3i();
	private Vector3i absPos = new Vector3i();
	private long lastBufferChanged;
	private long lastBufferSaved;
	private int expectedNonEmptySegmentsFromLoad = 1;
	
	
	private int nonEmptyInitialLoad;
	private int nonEmptySize;
	
	public SegmentBufferManager(SegmentController segmentController) {
		this.segmentController = segmentController;
		this.boundingBox = new BoundingBox();

		lastBufferChanged = System.currentTimeMillis();
		lastBufferSaved = System.currentTimeMillis();
	}

	private static long calculateIndexFromCoordinates(int x, int y, int z) {

		assert (x < MAX_COORDINATE) : x;
		assert (y < MAX_COORDINATE) : y;
		assert (z < MAX_COORDINATE) : z;

		long zl = (((long) z << 42) & maskZ);
		long yl = (((long) y << 21) & maskY);
		long xl = x;

		long c = xl + yl + zl;

		return c;
	}

	public static int getBufferCoordAbsolute(int x) {
		return ByteUtil.divU16(ByteUtil.divUSeg(x) + DIMENSION_HALF);
	}
	public static long getBufferIndexFromAbsolute(int x, int y, int z) {

		x = getBufferCoordAbsolute(x);
		y = getBufferCoordAbsolute(y);
		z = getBufferCoordAbsolute(z);

		return calculateIndexFromCoordinates(x, y, z);
	}

	public static long getBufferIndexFromAbsolute(Vector3i pos) {
		return getBufferIndexFromAbsolute(pos.x, pos.y, pos.z);
	}

	public static void main(String[] args) {

		for (int z = -64; z < 64; z += SegmentData.SEG) {
			for (int y = -64; y < 64; y += SegmentData.SEG) {
				for (int x = -64; x < 64; x += SegmentData.SEG) {
					int xA = x;
					int yA = y;
					int zA = z;

					int xD = getBufferCoordAbsolute(x) * DIMENSION;
					int yD = getBufferCoordAbsolute(y) * DIMENSION;
					int zD = getBufferCoordAbsolute(z) * DIMENSION;

					final Vector3i start = new Vector3i(xD, yD, zD);
					final Vector3i end = new Vector3i(xD, yD, zD);
					end.add(DIMENSION, DIMENSION, DIMENSION);

					long index = getBufferIndexFromAbsolute(xA, yA, zA);

					System.err.println(xA + ", " + yA + ", " + zA + " -> " + index + " ----> " + xD + ", " + yD + ", " + zD + " ;;; " +
							(getBufferCoordAbsolute(x) - DIMENSION_HALF) + ", " +
							(getBufferCoordAbsolute(y) - DIMENSION_HALF) + ", " +
							(getBufferCoordAbsolute(z) - DIMENSION_HALF));
				}
			}
		}

	}

	public static long getBufferIndexFromSegmentIndex(long index) {
		return getBufferIndexFromAbsolute(ElementCollection.getPosX(index), ElementCollection.getPosY(index), ElementCollection.getPosZ(index));
	}

	@Override
	public void addImmediate(Segment s) {
		synchronized (buffer) {
			if(((RemoteSegment)s).buildMetaData != null){
				System.err.println("Segment didnt use meta data for "+s.pos+" on "+ segmentController);
				SegmentProvider.freeSegmentDataMetaData(((RemoteSegment)s).buildMetaData);
				((RemoteSegment)s).buildMetaData = null;
			}
			getBuffer(s.pos, true).addImmediate(s);
			assert (getBuffer(s.pos, false) != null);
			assert (containsKey(s.pos) && ((isEmpty(s.pos) && s.isEmpty()) || get(s.pos) != null)) : s.pos + "; " + containsKey(s.pos);
			segmentController.getSegmentProvider().onAddedToBuffer((RemoteSegment)s);
		}
	}

	@Override
	public int clear(boolean threadedClear) {
		segmentController.hadAtLeastOneElement = false;
		final SegmentBufferInterface[] bufferCopy;
		synchronized (buffer) {
			bufferCopy = new SegmentBufferInterface[buffer.size()];
			int i = 0;
			for (SegmentBufferInterface m : buffer.values()) {
				bufferCopy[i] = m;
				i++;
			}
			buffer.clear();
			totalSize = 0;
			nonEmptySize = 0;
			segmentController.hadAtLeastOneElement = false;
		}
		
		if (!segmentController.getState().getConnectionThreadPool().isTerminating() && !segmentController.getState().getConnectionThreadPool().isTerminated()) {
			//			segmentController.getState().getThreadPool().execute(new Runnable() {
			//				@Override
			//				public void run() {

			int i = 0;
			//					System.err.println("["+segmentController.getState()+"] CLEARING SG BUFFER MANAGER hash("+this.hashCode()+") manBufferSize "+bufferCopy.size()+" / totsize "+size());
			for (SegmentBufferInterface s : bufferCopy) {
				synchronized (segmentController.getState()) {
					synchronized (segmentController.getState().getLocalAndRemoteObjectContainer().getLocalObjects()) {
						long t = System.currentTimeMillis();
						i += ((SegmentBuffer) s).clearFast();
						long took = System.currentTimeMillis() - t;
						if (took > 20) {
							System.err.println("[" + segmentController.getState() + "] WARNING: Segment Clear of " + s + "(" + segmentController + ": " + segmentController.getState() + ") took " + took + " ms");
						}
					}
				}
			}

			segmentController.getElementClassCountMap().resetAll();
			segmentController.resetTotalElements();
			
			//					System.err.println(segmentController.getState()+" FINISHED CLEARING SG BUFFER MANAGER hash("+this.hashCode()+") manBufferSize "+bufferCopy.size()+" / totsize "+size()+"; cleared: "+i);
			//				}
			//			});
		}

		return 0;

	}

	@Override
	public boolean containsIndex(long index) {
		return containsKey(ElementCollection.getPosX(index), ElementCollection.getPosY(index), ElementCollection.getPosZ(index));
	}

	@Override
	public boolean containsKey(int x, int y, int z) {

		SegmentBufferInterface subBuffer = getBuffer(x, y, z, false);
		return subBuffer != null && subBuffer.containsKey(x, y, z);
	}

	@Override
	public boolean containsKey(Vector3i key) {
		return containsKey(key.x, key.y, key.z);
	}

	@Override
	public boolean containsValue(Segment value) {

		SegmentBufferInterface subBuffer = getBuffer(value.pos, false);
		return subBuffer != null ? subBuffer.containsValue(value) : false;

	}

	@Override
	public boolean existsPointUnsave(Vector3i posMod) {

		SegmentBufferInterface subBuffer = getBuffer(posMod, false);
		if (subBuffer == null) {
			return false;
		}
		return subBuffer.existsPointUnsave(posMod);

	}
	@Override
	public boolean existsPointUnsave(long index){
		SegmentBufferInterface subBuffer = getBuffer(ElementCollection.getPosX(index), ElementCollection.getPosY(index), ElementCollection.getPosZ(index), false);
		if (subBuffer == null) {
			return false;
		}
		return subBuffer.existsPointUnsave(index);
	}
	@Override
	public boolean existsPointUnsave(int x, int y, int z){
		SegmentBufferInterface subBuffer = getBuffer(x,y,z, false);
		if (subBuffer == null) {
			return false;
		}
		return subBuffer.existsPointUnsave(x,y,z);
	}
	@Override
	public Segment get(int x, int y, int z) {

		SegmentBufferInterface subBuffer = getBuffer(x, y, z, false);
		if (subBuffer != null) {
			return subBuffer.get(x, y, z);
		}
		return null;
	}

	@Override
	public Segment get(long index) {
		return get(ElementCollection.getPosX(index), ElementCollection.getPosY(index), ElementCollection.getPosZ(index));
	}

	@Override
	public Segment get(Vector3i key) {
		return get(key.x, key.y, key.z);
	}

	@Override
	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	@Override
	public final SegmentBuffer getBuffer(Vector3i pos) {
		return (SegmentBuffer) getBuffer(pos, false);
	}

	@Override
	public long getLastInteraction() {
		return lastInteraction;
	}

	@Override
	public long getLastSegmentLoadChanged() {
		return this.lastSegmentLoadChange;
	}

	@Override
	public SegmentPiece getPointUnsave(int posModX, int posModY, int posModZ) {
		SegmentBufferInterface subBuffer = getBuffer(posModX, posModY, posModZ, false);
		if (subBuffer == null) {
			return null;
		}
		return subBuffer.getPointUnsave(posModX, posModY, posModZ);
	}

	@Override
	public SegmentPiece getPointUnsave(int posModX, int posModY, int posModZ, SegmentPiece piece) {
		SegmentBufferInterface subBuffer = getBuffer(posModX, posModY, posModZ, false);
		if (subBuffer == null) {
			return null;
		}
		return subBuffer.getPointUnsave(posModX, posModY, posModZ, piece);
	}

	@Override
	public SegmentPiece getPointUnsave(long posMod) {
		SegmentBufferInterface subBuffer = getBuffer(
				ElementCollection.getPosX(posMod), 
				ElementCollection.getPosY(posMod), 
				ElementCollection.getPosZ(posMod), 
				false);
		if (subBuffer == null) {
			return null;
		}
		return subBuffer.getPointUnsave(posMod);
	}

	@Override
	public SegmentPiece getPointUnsave(long posMod,
	                                   SegmentPiece piece) {

		SegmentBufferInterface subBuffer = getBuffer(
				ElementCollection.getPosX(posMod), 
				ElementCollection.getPosY(posMod), 
				ElementCollection.getPosZ(posMod), 
				false);
		if (subBuffer == null) {
			return null;
		}
		return subBuffer.getPointUnsave(posMod, piece);
	}

	@Override
	public SegmentPiece getPointUnsave(Vector3i posMod) {

		SegmentBufferInterface subBuffer = getBuffer(posMod, false);
		if (subBuffer == null) {
			return null;
		}
		return subBuffer.getPointUnsave(posMod);

	}

	@Override
	public SegmentPiece getPointUnsave(Vector3i posMod,
	                                   SegmentPiece piece) {

		SegmentBufferInterface subBuffer = getBuffer(posMod);
		if (subBuffer == null) {
			return null;
		}
		return subBuffer.getPointUnsave(posMod, piece);
	}

	@Override
	public SegmentController getSegmentController() {
		return segmentController;
	}

	@Override
	public int getTotalNonEmptySize() {
		return nonEmptySize;
	}

	@Override
	public int getTotalSize() {
		return totalSize;
	}

	@Override
	public boolean handle(Vector3i cachPos,
	                      SegmentBufferIteratorInterface iteratorImpl, long lastChanged) {

		SegmentBufferInterface subBuffer = getBuffer(cachPos, false);

		if (subBuffer != null) {
			return subBuffer.handle(cachPos, iteratorImpl, lastChanged);
		} else {
			return true;
		}
	}

	@Override
	public boolean handleNonEmpty(int x, int y, int z,
	                              SegmentBufferIteratorInterface iteratorImpl, long lastChanged) {
		lastIteration++;
		SegmentBufferInterface subBuffer = getBuffer(x, y, z, false);

		if (subBuffer != null && !subBuffer.isEmpty()) {
			lastDoneIteration++;
			return subBuffer.handleNonEmpty(x, y, z, iteratorImpl, lastChanged);
		} else {
			return true;
		}
	}
	@Override
	public void onSegmentBecameEmpty(Segment segment) {
		SegmentBufferInterface subBuffer = getBuffer(segment.pos.x, segment.pos.y, segment.pos.z, false);
		if(subBuffer != null){
			subBuffer.onSegmentBecameEmpty(segment);
		}
	}
	@Override
	public boolean handleNonEmpty(Vector3i cachPos,
	                              SegmentBufferIteratorInterface iteratorImpl, long lastChanged) {
		return handleNonEmpty(cachPos.x, cachPos.y, cachPos.z, iteratorImpl, lastChanged);
	}

	@Override
	public void incActive(int i, Segment s) {
		SegmentBufferInterface subBuffer = getBuffer(s.pos, false);
		if (subBuffer != null) {
			subBuffer.incActive(i, s);
		}
	}

	@Override
	public boolean isEmpty() {
		return buffer.isEmpty();
	}

	/**
	 * @return the untouched
	 */
	@Override
	public boolean isUntouched() {
		return untouched;
	}

	@Override
	public void iterateIntersecting(SegmentBufferInterface otherSegmentBuffer,
	                                Transform otherBufferInverse,
	                                SegmentBufferIntersectionInterface handler, SegmentBufferIntersectionVariables v,
	                                Vector3i from, Vector3i to, Vector3i fromOther, Vector3i toOther) {

		//		v.reset();
		//
		//		otherSegmentBuffer.iterateOverNonEmptyElementRange(new SegmentBufferIteratorInterface() {
		//
		//			@Override
		//			public boolean handle(Segment s, long lastChanged) {
		//						//				return false;
		//			}
		//		}, fromOther, toOther, false);

	}

	//	@Override
	//	public boolean iterateOverElementRange(SegmentBufferIteratorInterface iteratorImpl, Vector3i from, Vector3i to, boolean synch){
	//
	//
	//		Vector3i cachPos = new Vector3i();
	//
	//		if(synch){
	//			synchronized(buffer){
	//				for(int x = from.x; x < to.x; x+=SegmentData.SEG){
	//					for(int y = from.y; y < to.y; y+=SegmentData.SEG){
	//						for(int z = from.z; z < to.z; z+=SegmentData.SEG){
	//							cachPos.set(x,y,z);
	//							boolean handle = handle(cachPos, iteratorImpl);
	//							if(!handle){
	//								return handle;
	//							}
	//						}
	//					}
	//				}
	//			}
	//		}else{
	//			for(int x = from.x; x < to.x; x+=SegmentData.SEG){
	//				for(int y = from.y; y < to.y; y+=SegmentData.SEG){
	//					for(int z = from.z; z < to.z; z+=SegmentData.SEG){
	//						cachPos.set(x,y,z);
	//						boolean handle = handle(cachPos, iteratorImpl);
	//						if(!handle){
	//							return handle;
	//						}
	//					}
	//				}
	//			}
	//		}
	//		return true;
	//	}
	@Override
	public boolean iterateOverEveryElement(SegmentBufferIteratorEmptyInterface iteratorImpl, boolean synch) {
		if (synch) {
			synchronized (buffer) {
				for (SegmentBufferInterface sb : buffer.values()) {
					boolean handle = sb.iterateOverEveryElement(iteratorImpl, synch);
					if (!handle) {
						//iteration preemptively interrupted
						return handle;
					}
				}
			}
		} else {
			for (SegmentBufferInterface sb : buffer.values()) {
				boolean handle = sb.iterateOverEveryElement(iteratorImpl, synch);
				if (!handle) {
					//iteration preemptively interrupted
					return handle;
				}
			}
		}
		//full set has been iterated
		return true;
	}

	@Override
	public boolean iterateOverNonEmptyElement(
			SegmentBufferIteratorInterface iteratorImpl, boolean synch) {
		if (synch) {
			synchronized (buffer) {
				for (SegmentBufferInterface sb : buffer.values()) {
					boolean handle = sb.iterateOverNonEmptyElement(iteratorImpl, synch);
					if (!handle) {
						//iteration preemptively interrupted
						return handle;
					}
				}
			}
		} else {
			for (SegmentBufferInterface sb : buffer.values()) {
				boolean handle = sb.iterateOverNonEmptyElement(iteratorImpl, synch);
				if (!handle) {
					//iteration preemptively interrupted
					return handle;
				}
			}
		}
		//full set has been iterated
		return true;

	}

	@Override
	public boolean iterateOverEveryChangedElement(SegmentBufferIteratorEmptyInterface iteratorImpl, boolean synch) {
		final long time = segmentController.getUpdateTime();
		if (synch) {
			synchronized (buffer) {
				for (SegmentBufferInterface sb : buffer.values()) {
					System.err.println(sb.getSegmentController()+" : BUFFER CHANGED: "+(sb.getLastBufferChanged() > sb.getLastBufferSaved())+"; "+((SegmentBuffer)sb).getRegionStartBlock()+"; "+((SegmentBuffer)sb).getRegionEndBlock()+"; LastChanged "+sb.getLastBufferChanged()+"; LastSaved "+sb.getLastBufferSaved());
					if (sb.getLastBufferChanged() > sb.getLastBufferSaved()) {
						boolean handle = sb.iterateOverEveryElement(iteratorImpl, synch);
						if (!handle) {
							//iteration preemptively interrupted
							return handle;
						}
						sb.setLastBufferSaved(time);
					}
				}
			}
		} else {
			for (SegmentBufferInterface sb : buffer.values()) {
				if (sb.getLastBufferChanged() > sb.getLastBufferSaved()) {
					boolean handle = sb.iterateOverEveryElement(iteratorImpl, synch);
					if (!handle) {
						//iteration preemptively interrupted
						return handle;
					}
					sb.setLastBufferSaved(time);
				}
			}
		}
		//full set has been iterated
		return true;
	}

	@Override
	public boolean iterateOverNonEmptyElementRange(SegmentBufferIteratorInterface iteratorImpl,
	                                               int fromX, int fromY, int fromZ,
	                                               int toX, int toY, int toZ,
	                                               boolean synch) {

		int tries = 0;
		if (synch) {
			synchronized (buffer) {
				iterateOverNonEmptyElementRangeInternal(iteratorImpl, fromX, fromY, fromZ, toX, toY, toZ, synch);
			}
		} else {
			iterateOverNonEmptyElementRangeInternal(iteratorImpl, fromX, fromY, fromZ, toX, toY, toZ, synch);
		}

		return true;
	}

	@Override
	public void onAddedElement(short newType, int oldSize, byte x, byte y, byte z,
	                           Segment segment, long time, byte orientation) {
		//don't update revalidating segments (means that they are not yet added)
//		if (!getSegmentController().isOnServer() && segment.getSegmentData() != null && !segment.getSegmentData().isRevalidating()) {
//			((SendableSegmentController)getSegmentController()).getBlockProcessor().markNeighbors(segment);
//			
//		}

		
	}

	@Override
	public void onRemovedElementClient(short oldType, int oldSize, byte x, byte y, byte z,
	                                   Segment segment, long time) {
//		if(!getSegmentController().isOnServer() && ElementKeyMap.isValidType(oldType) && ElementKeyMap.getInfoFast(oldType).isLightSource()) {
//			((SendableSegmentController)getSegmentController()).getBlockProcessor().markNeighbors(segment);
//		}

		//no reason to deligate as there is currently nothing to do

		//		SegmentBufferInterface subBuffer = getBuffer(segment.pos,false);
		//		if(subBuffer != null){
		//			subBuffer.onRemovedElement(oldType, oldSize, x,y,z, segment);
		//		}else{
		//			//			System.err.println("REMOVE Exception: WARNING NO SEGMENT BUFFER AT "+out);
		//		}

	}

	@Override
	public Segment removeImmediate(Vector3i pos, boolean aabbUpdate) {

		SegmentBufferInterface subBuffer = getBuffer(pos, false);
		System.err.println("[SEGMENTBUFFERMANAGER] " + segmentController.getState() + " " + segmentController + " REMOVING IMMEDIATE: " + pos + " -> " + pos);
		if (subBuffer == null) {
			return null;
		}

		Segment removeImmediate = subBuffer.removeImmediate(pos, aabbUpdate);
		return removeImmediate;
	}

	@Override
	public void restructBB() {
		if (segmentController.getTotalElements() == 0 || totalSize == 0) {
			boundingBox.reset();
			boundingBox.min.set(0, 0, 0);
			boundingBox.max.set(0, 0, 0);
			return;
		}
		synchronized (boundingBox) {
			boundingBox.reset();
			for (SegmentBufferInterface s : buffer.values()) {
				s.restructBB();
			}
		}
	}
	public void restructBBFastSynch(SegmentData data) {
		
	}
	@Override
	public void restructBBFastOnRemove(Vector3i pos) {
		synchronized(buffer){
			SegmentBufferInterface bb = getBuffer(pos, false);
			
			if (bb != null) {
				if (
						bb.getBoundingBox().min.x == boundingBox.min.x ||
						bb.getBoundingBox().min.y == boundingBox.min.y ||
						bb.getBoundingBox().min.z == boundingBox.min.z ||
						bb.getBoundingBox().max.x == boundingBox.max.x ||
						bb.getBoundingBox().max.y == boundingBox.max.y ||
						bb.getBoundingBox().max.z == boundingBox.max.z
						) {
					//				System.err.println("RESTRUCT: "+segmentController.getState()+" "+data.getSegment().pos+" : "+getBoundingBox());
					//buffer is on edge
					bb.restructBBFastOnRemove(pos);
					//				System.err.println("RESTRUCT: "+data.getSegment().pos+" BEFORE: "+getBoundingBox());
				} else {
					//				System.err.println("RESTRUCT: "+segmentController.getState()+" "+data.getSegment().pos+" : "+getBoundingBox());
					//nothing to do since the buffer is in the middle somewhere
				}
				
			} else {
				System.err.println("RESTRUCT BB NULL: " + segmentController.getState() + " " + pos + " : " + boundingBox);
			}
		}
	}
	@Override
	public void restructBBFast(SegmentData data) {
		if(data == null){
			try {
				throw new Exception("WARNING: data null");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
		synchronized(buffer){
			Segment segment = data.getSegment();
			if(segment == null){
				try {
					throw new Exception("WARNING: segment null");
				} catch (Exception e) {
					e.printStackTrace();
				}
				return;
			}
			SegmentBufferInterface bb = getBuffer(data.getSegment().pos, false);
	
			if (bb != null) {
				if (
						bb.getBoundingBox().min.x == boundingBox.min.x ||
								bb.getBoundingBox().min.y == boundingBox.min.y ||
								bb.getBoundingBox().min.z == boundingBox.min.z ||
								bb.getBoundingBox().max.x == boundingBox.max.x ||
								bb.getBoundingBox().max.y == boundingBox.max.y ||
								bb.getBoundingBox().max.z == boundingBox.max.z
						) {
					//				System.err.println("RESTRUCT: "+segmentController.getState()+" "+data.getSegment().pos+" : "+getBoundingBox());
					//buffer is on edge
					bb.restructBBFast(data);
					//				System.err.println("RESTRUCT: "+data.getSegment().pos+" BEFORE: "+getBoundingBox());
				} else {
					//				System.err.println("RESTRUCT: "+segmentController.getState()+" "+data.getSegment().pos+" : "+getBoundingBox());
					//nothing to do since the buffer is in the middle somewhere
				}
	
			} else {
				System.err.println("RESTRUCT BB NULL: " + segmentController.getState() + " " + data.getSegment().pos + " : " + boundingBox);
			}
		}
	}

	@Override
	public void setLastInteraction(long lastInteraction, Vector3i segPos) {
		this.lastInteraction = lastInteraction;
		SegmentBufferInterface subBuffer = getBuffer(segPos, false);

		if (subBuffer != null) {
			subBuffer.setLastInteraction(lastInteraction, segPos);
		}
	}

	@Override
	public int size() {
		return buffer.size();
	}

	@Override
	public void update() {
		checkCleanUp();
	}

	@Override
	public void updateBB(Segment s) {

		SegmentBufferInterface bb = getBuffer(s.pos, false);
		if (bb != null) {
			//			System.err.println("UPDATE BB "+s.pos);
			bb.updateBB(s);
		} else {
			//			System.err.println("ERROR CANNOT UPDATE UPDATE BB "+s.pos);
		}

	}

	@Override
	public void updateLastSegmentLoadChanged(long time) {
		this.lastSegmentLoadChange = time;
	}

	@Override
	public void updateNumber() {
		freeSegmentData = segmentController.getSegmentProvider().getCountOfFree();

		allocatedSegmentData = nonEmptySize;

		if (segmentController.isOnServer()) {
			GameServerState.allocatedSegmentData += allocatedSegmentData;
		} else {
			//			System.err.println("CLIENT UPDATE ALLOCATED: "+getSegmentController()+"; "+allocatedSegmentData);
			GameClientState.allocatedSegmentData += allocatedSegmentData;
		}
	}

	@Override
	public boolean iterateOverNonEmptyElementRange(
			SegmentBufferIteratorInterface handler, Vector3i from,
			Vector3i to, boolean synch) {
		return iterateOverNonEmptyElementRange(handler, from.x, from.y, from.z, to.x, to.y, to.z, synch);
	}

	@Override
	public long getLastChanged(Vector3i pos) {
		SegmentBufferInterface subBuffer = getBuffer(pos, false);
		if (subBuffer != null) {
			return subBuffer.getLastChanged(pos);
		}
		return 0;
	}

	@Override
	public void setLastChanged(Vector3i pos, long lastChanged) {
		SegmentBufferInterface subBuffer = getBuffer(pos, true);
		if (subBuffer != null) {
			subBuffer.setLastChanged(pos, lastChanged);
		} else {
			assert (false) : pos + "; " + segmentController;
		}
//		if(lastChanged > lastBufferSaved && !(getSegmentController() instanceof TransientSegmentController)){
//			try{
//			throw new NullPointerException(" "+lastChanged+"; "+lastBufferSaved);
//			}catch(Exception e){
//				e.printStackTrace();
//			}
//		}
		lastBufferChanged = Math.max(lastBufferChanged, lastChanged);
	}

	@Override
	public void get(Vector3i pos, SegmentRetrieveCallback callback) {
		get(pos.x, pos.y, pos.z, callback);
	}

	@Override
	public void get(int x, int y, int z, SegmentRetrieveCallback callback) {
		SegmentBufferInterface subBuffer = getBuffer(x, y, z, false);
		if (subBuffer != null) {
			subBuffer.get(x, y, z, callback);
		} else {
			callback.segment = null;
			callback.state = SegmentBufferOctree.NOTHING;
			callback.pos.set(x, y, z);
			callback.abspos.set(x >> 4, y >> 4, z >> 4);
		}

	}

	@Override
	public int getSegmentState(Vector3i pos) {
		return getSegmentState(pos.x, pos.y, pos.z);
	}

	@Override
	public int getSegmentState(int x, int y, int z) {
		SegmentBufferInterface subBuffer = getBuffer(x, y, z, false);
		if (subBuffer != null) {
			return subBuffer.getSegmentState(x, y, z);
		} else {
			return SegmentBufferOctree.NOTHING;
		}

	}
	@Override
	public int getSegmentState(long segmentPos) {
		int x = ElementCollection.getPosX(segmentPos);
		int y = ElementCollection.getPosY(segmentPos);
		int z = ElementCollection.getPosZ(segmentPos);
		return getSegmentState(x, y, z);
		
	}

	@Override
	public void setEmpty(Vector3i pos) {
		setEmpty(pos.x, pos.y, pos.z);
	}

	@Override
	public int setEmpty(int x, int y, int z) {
		SegmentBufferInterface subBuffer = getBuffer(x, y, z, false);
		if (subBuffer != null) {
			return subBuffer.setEmpty(x, y, z);
		} else {
			return SegmentBufferOctree.NOTHING;
		}

	}

	@Override
	public EWAHCompressedBitmap applyBitMap(long segBufferIndex, EWAHCompressedBitmap bitMap) {
		SegmentBufferInterface returnBuffer;
		synchronized (buffer) {
			returnBuffer = buffer.get(segBufferIndex);
		}
		if (returnBuffer != null) {
			return returnBuffer.applyBitMap(segBufferIndex, bitMap);
		}
		return null;
	}

	@Override
	public void insertFromBitset(Vector3i pos, long segmentBufferIndex,
	                             EWAHCompressedBitmap bitmap, SegmentBufferIteratorEmptyInterface callback) {
		SegmentBufferInterface buf;
		synchronized (buffer) {
			buf = getBuffer(pos, true);
		}
		assert (buf != null);
		assert (bitmap != null);
		buf.insertFromBitset(pos, segmentBufferIndex, bitmap, callback);

	}

	/**
	 * @return the lastBufferSaved
	 */
	@Override
	public long getLastBufferSaved() {
		return lastBufferSaved;
	}

	/**
	 * @return the bufferLastChanged
	 */
	@Override
	public long getLastBufferChanged() {
		return lastBufferChanged;
	}

	/**
	 * @param lastBufferSaved the lastBufferSaved to set
	 */
	@Override
	public void setLastBufferSaved(long lastBufferSaved) {
		this.lastBufferSaved = lastBufferSaved;
	}

	@Override
	public final void setSegmentController(SegmentController segmentController) {
		this.segmentController = segmentController;
	}

	public void checkCleanUp() {
		if (writingThreads > 5) {
			return;
		}
		if (activatePushBack) {
			synchronized (buffer) {
				if (segmentController.isOnServer()) {
					if (size() > MAX_BUFFERED_SERVER) {
						SegmentBufferInterface oldest = null;
						for (SegmentBufferInterface b : buffer.values()) {
							if (oldest == null || b.getLastInteraction() < oldest.getLastInteraction()) {
								oldest = b;
							}
						}
						if (oldest != null) {
							pushBackRegion(((SegmentBuffer) oldest).getRegionStart());
						}
					}
				} else {
					if (size() > MAX_BUFFERED_CLIENT) {

						for (SegmentBufferInterface b : buffer.values()) {
							if (System.currentTimeMillis() - ((SegmentBuffer) b).getCreationTime() > 10000 && !((SegmentBuffer) b).isActive()) {
								pushBackRegion(((SegmentBuffer) b).getRegionStart());
								return;
							}
						}
					}
				}
			}
		}

	}

	public void dec() {
		totalSize = Math.max(0, totalSize - 1);
	}

	public void decNonEmpty() {

		nonEmptySize = Math.max(0, nonEmptySize - 1);
	}

	public final Long2ObjectOpenHashMap<SegmentBufferInterface> getBuffer() {
		return buffer;
	}

	public final SegmentBufferInterface getBuffer(int x, int y, int z, boolean autoAdd) {
		long index = getBufferIndexFromAbsolute(x, y, z);

		SegmentBufferInterface returnBuffer = buffer.get(index);

		if (autoAdd && returnBuffer == null) {
			synchronized (buffer) {
				returnBuffer = buffer.get(index);
				//check a second time so it wont get added by blocking threads
				if (returnBuffer == null) {

					/*
					 * calculate position in top level buffer
					 */
					int ox = getBufferCoordAbsolute(x) * DIMENSION;
					int oy = getBufferCoordAbsolute(y) * DIMENSION;
					int oz = getBufferCoordAbsolute(z) * DIMENSION;

					final Vector3i start = new Vector3i(ox - DIMENSION_HALF, oy - DIMENSION_HALF, oz - DIMENSION_HALF);
					final Vector3i end = new Vector3i(ox - DIMENSION_HALF, oy - DIMENSION_HALF, oz - DIMENSION_HALF);
					end.add(DIMENSION, DIMENSION, DIMENSION);
					returnBuffer = new SegmentBuffer(segmentController, start, end, this);

					//						System.err.println(getSegmentController().getState()+": "+getSegmentController()+" ADDING NEW SEGMENT BUFFER FROM REQUEST: "+x+", "+y+", "+z+": s->e "+start+" -> "+end);

					buffer.put(index, returnBuffer);
					untouched = false;
					assert (buffer.get(index) != null);
				} else {

				}
			}
		}
		if (autoAdd && segmentController.isOnServer() && returnBuffer == null) {
			throw new RuntimeException("Critical: Autoadding SegmentBuffer failed for " + this);
		}
		return returnBuffer;

	}

	public final SegmentBufferInterface getBuffer(long index) {
		return buffer.get(index);
	}

	public final SegmentBufferInterface getBuffer(Vector3i pos, boolean autoAdd) {
		return getBuffer(pos.x, pos.y, pos.z, autoAdd);
	}

	/**
	 * @return the freeSegmentData
	 */
	public int getFreeSegmentData() {
		return freeSegmentData;
	}

	public long getLatestChangedInArea(Vector3i from, Vector3i to, boolean synch) {

		fromBuffer.x = ByteUtil.divU16(ByteUtil.divUSeg(from.x));
		fromBuffer.y = ByteUtil.divU16(ByteUtil.divUSeg(from.y));
		fromBuffer.z = ByteUtil.divU16(ByteUtil.divUSeg(from.z));

		toBuffer.x = ByteUtil.divU16(ByteUtil.divUSeg(to.x));
		toBuffer.y = ByteUtil.divU16(ByteUtil.divUSeg(to.y));
		toBuffer.z = ByteUtil.divU16(ByteUtil.divUSeg(to.z));

		if(fromBuffer.equals(toBuffer)) {
			return lastSegmentLoadChange;
		}
		
		toBuffer.x++;
		toBuffer.y++;
		toBuffer.z++;

		if (fromBuffer.x == toBuffer.x) {
			toBuffer.x++;
		}
		if (fromBuffer.y == toBuffer.y) {
			toBuffer.y++;
		}
		if (fromBuffer.z == toBuffer.z) {
			toBuffer.z++;
		}
		//		System.err.println("CHECKING: "+from+" -> "+to+" .-.-.-. "+fromBuffer+" -> "+toBuffer+": ");
		long max = 0;
		if (synch) {
			synchronized (buffer) {

				for (int x = fromBuffer.x; x < toBuffer.x; x++) {
					for (int y = fromBuffer.y; y < toBuffer.y; y++) {
						for (int z = fromBuffer.z; z < toBuffer.z; z++) {

							SegmentBufferInterface segmentBufferInterface = buffer.get(calculateIndexFromCoordinates(x, y, z));

							if (segmentBufferInterface != null) {
								max = Math.max(max, segmentBufferInterface.getLastSegmentLoadChanged());
							}
						}
					}
				}
			}
		} else {

			for (int x = fromBuffer.x; x < toBuffer.x; x += SegmentData.SEG) {
				for (int y = fromBuffer.y; y < toBuffer.y; y += SegmentData.SEG) {
					for (int z = fromBuffer.z; z < toBuffer.z; z += SegmentData.SEG) {

						SegmentBufferInterface segmentBufferInterface = buffer.get(calculateIndexFromCoordinates(x, y, z));

						if (segmentBufferInterface != null) {
							max = Math.max(max, segmentBufferInterface.getLastSegmentLoadChanged());
						}
					}
				}
			}
		}
		return max;
	}

	public NeighboringBlockCollection getNeighborCollection(Vector3i from) {
		SegmentPiece p = getPointUnsave(from);

		if (p != null && p.getType() != Element.TYPE_NONE) {
			NeighboringBlockCollection n = new NeighboringBlockCollection();
			if ((ElementKeyMap.getInfo(p.getType()).isRailTrack())) {
				System.err.println("[BULK] checking for orientation too");
				n.setOrientation(p.getOrientation());
				n.searchWithOrient(p, this);
			} else {
				n.search(p, this);
			}
			return n;
		}
		return null;
	}

//	private void markNeighborForMeshUpdateIfNecessary(byte x, byte y, byte z, Segment segment, Vector3b sample) {
//		Segment neighboringSegment = segmentController.getNeighboringSegment(sample, segment, outSegPos);
//
//		if (neighboringSegment != null && neighboringSegment != segment) {
//			neighboringSegment.dataChanged(true);
//		}
//	}
//
//	public void markNeighborsForMeshUpdateIfNecessary(byte x, byte y, byte z, Segment segment) {
//
//		if (!getSegmentController().isOnServer() && segment.getSegmentData() != null && !segment.getSegmentData().isRevalidating()) {
//			segment.getAbsoluteElemPos(x, y, z, absPos);
//
//			byte dist = 15;
//			for (int zDist = -1; zDist < 2; zDist++) {
//				for (int yDist = -1; yDist < 2; yDist++) {
//					for (int xDist = -1; xDist < 2; xDist++) {
//						if (!(xDist == 0 && yDist == 0 && zDist == 0)) {
//							sample.set((byte) (x + xDist * dist), (byte) (y + yDist * dist), (byte) (z + zDist * dist));
//							markNeighborForMeshUpdateIfNecessary(x, y, z, segment, sample);
//						}
//					}
//				}
//
//			}
//		}
//	}

	public void inc() {
		totalSize++;
	}

	public void incNonEmpty() {
		nonEmptySize++;
	}

	public boolean isEmpty(Vector3i pos) {
		SegmentBuffer b = getBuffer(pos);
		return b == null || b.isEmpty() || b.getSegmentState(pos) == SegmentBufferOctree.EMPTY;
	}
	

	private boolean iterateOverNonEmptyElementRangeInternal(SegmentBufferIteratorInterface iteratorImpl,
	                                                        int fromX, int fromY, int fromZ,
	                                                        int toX, int toY, int toZ,
	                                                        boolean synch) {
		int fX = ByteUtil.divU16(ByteUtil.divUSeg(fromX) + DIMENSION_HALF);
		int fY = ByteUtil.divU16(ByteUtil.divUSeg(fromY) + DIMENSION_HALF);
		int fZ = ByteUtil.divU16(ByteUtil.divUSeg(fromZ) + DIMENSION_HALF);

		int tX = (ByteUtil.divU16(ByteUtil.divUSeg(toX) + DIMENSION_HALF)) + 1;
		int tY = (ByteUtil.divU16(ByteUtil.divUSeg(toY) + DIMENSION_HALF)) + 1;
		int tZ = (ByteUtil.divU16(ByteUtil.divUSeg(toZ) + DIMENSION_HALF)) + 1;
		for (int x = fX; x < tX; x++) {
			for (int y = fY; y < tY; y++) {
				for (int z = fZ; z < tZ; z++) {
					long bufferIndex = calculateIndexFromCoordinates(x, y, z);
					SegmentBufferInterface b = buffer.get(bufferIndex);
					if (b != null) {
						int xMin = ((x) * DIMENSION - DIMENSION_HALF) * SegmentData.SEG;
						int yMin = ((y) * DIMENSION - DIMENSION_HALF) * SegmentData.SEG;
						int zMin = ((z) * DIMENSION - DIMENSION_HALF) * SegmentData.SEG;

						int xMax = (((x + 1) * DIMENSION - DIMENSION_HALF) * SegmentData.SEG) - 1;
						int yMax = (((y + 1) * DIMENSION - DIMENSION_HALF) * SegmentData.SEG) - 1;
						int zMax = (((z + 1) * DIMENSION - DIMENSION_HALF) * SegmentData.SEG) - 1;

						boolean handle = b.iterateOverNonEmptyElementRange(iteratorImpl,
								Math.max(fromX, xMin),
								Math.max(fromY, yMin),
								Math.max(fromZ, zMin),

								Math.min(toX, xMax),
								Math.min(toY, yMax),
								Math.min(toZ, zMax),
								synch);
						if (!handle) {
							return handle;
						}
					}
				}
			}
		}
		return true;
	}

	public void pushBackRegion(Vector3i regionPos) {
		if (!activatePushBack) {
			return;
		}
		//		SegmentBufferInterface region = null;
		//		synchronized(buffer){
		//			region = buffer.remove(regionPos);
		//		}
		//
		//		if(region != null){
		//			final SegmentBuffer r = ((SegmentBuffer)region);
		//			this.writingThreads++;
		//			if(!getSegmentController().isOnServer()){
		//				System.err.println("##[CLEANUP] CLEANING UP SEGMENT BUFFER REGION "+regionPos+" ("+region.size()+") "+getSegmentController()+";;; "+getSegmentController().getState());
		//			}
		//			//				((SegmentBuffer)region).writeAndClearSynchronized();
		//
		//			getSegmentController().getState().getThreadPool().execute(new Runnable(){
		//				@Override
		//				public void run() {
		//					r.writeAndClearSynchronized();
		//					writingThreads--;
		//				}
		//			});
		//			//
		//		}
	}

	public void updateBB(BoundingBox bb) {
		boundingBox.expand(bb.min, bb.max);
		segmentController.aabbRecalcFlag();
	}

	public void incNonEmptyInitial() {
		nonEmptyInitialLoad++;
	}

	/**
	 * @param expectedNonEmptySegmentsFromLoad the expectedNonEmptySegmentsFromLoad to set
	 */
	public void setExpectedNonEmptySegmentsFromLoad(
			int expectedNonEmptySegmentsFromLoad) {
		this.expectedNonEmptySegmentsFromLoad = expectedNonEmptySegmentsFromLoad;
	}
	public int getExpectedNonEmptySegmentsFromLoad(){
		return expectedNonEmptySegmentsFromLoad;
	}
	@Override
	public boolean isFullyLoaded() {
		//		if(!loaded && getSegmentController().isOnServer()){
//			System.err.println("EE "+getSegmentController()+" :: "+nonEmptyInitialLoad+" :: "+expectedNonEmptySegmentsFromLoad);
//		}
		return true;
	}

	@Override
	public float getLoadPercent() {
		float percent = nonEmptySize / (float) expectedNonEmptySegmentsFromLoad;
		if(percent == Float.POSITIVE_INFINITY) return 0;
		else return percent;
	}
}
