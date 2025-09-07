package org.schema.game.common.controller;

import com.bulletphysics.linearmath.Transform;
import com.googlecode.javaewah.EWAHCompressedBitmap;
import com.googlecode.javaewah.IntIteratorOverIteratingRLW;
import com.googlecode.javaewah.IteratingRLW;
import org.schema.common.util.ByteUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.SegmentRetrieveCallback;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.world.DrawableRemoteSegment;
import org.schema.game.common.data.world.RemoteSegment;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.forms.BoundingBox;

import javax.vecmath.Vector3f;
import java.io.IOException;
import java.util.List;

public class SegmentBuffer implements SegmentBufferInterface {
	public final static int BUFFER_LENGTH = SegmentBufferManager.DIMENSION * SegmentBufferManager.DIMENSION * SegmentBufferManager.DIMENSION;
	public final Vector3i regionStart = new Vector3i();
	public final Vector3i regionEnd = new Vector3i();
	public final Vector3i regionBlockStart = new Vector3i();
	public final Vector3i regionBlockEnd = new Vector3i();
	// private Segment[] bufferedSegments;
	private final BoundingBox boundingBox;
	private final long creationTime;
	private final SegmentBufferOctree data;
	public short aabbHelperUpdateNum;
	public boolean inViewFrustum;
	public boolean inViewFrustumFully;
	private boolean untouched = true;
	private SegmentController segmentController;
	private SegmentBufferManager manager;
	private long lastSegmentLoadChange;
	private int freeSegmentData;
	private int allocatedSegmentData;
	private int size;
	private Vector3i absPos = new Vector3i();
	private long lastInteraction;
	private int active = 0;
	private long lastBufferChanged;
	private long lastBufferSaved;
	private int sizeNonEmpty = 0;

	public SegmentBuffer(SegmentController segmentController, Vector3i start, Vector3i end, SegmentBufferManager manager) {
		lastBufferChanged = segmentController.getState().getUpdateTime() - 10000;
		lastBufferSaved = segmentController.getState().getUpdateTime() - 10000;
		this.segmentController = segmentController;
		regionStart.set(start);
		regionEnd.set(end);
		regionBlockStart.set(start);
		regionBlockStart.scale(SegmentData.SEG);
		regionBlockEnd.set(end);
		regionBlockEnd.scale(SegmentData.SEG);
		// assert(checkBufferFileIntegrity());
		// bufferedSegments = new Segment[BUFFER_LENGTH];
		boundingBox = new BoundingBox();
		this.manager = manager;
		this.creationTime = System.currentTimeMillis();
		data = new SegmentBufferOctree(this);
		if(segmentController.isOnServer()) {
			// this.setHasSignature(true);
			// String segFile = segmentController.getSegmentProvider().getSegmentDataIO().getSegFile(start.x, start.y, start.z, segmentController);
			// File f = new FileExt(segFile);
			// if(f.exists()){
			// try {
			// SegmentRegionFileHandle file = segmentController.getSegmentProvider().getSegmentDataIO().getManager().getFile(segFile);
			// file.calculateSignature(start, end, signature);
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
			// }
		}
	}

	public static int getIndex(int x, int y, int z, Vector3i start) {
		int nX = ByteUtil.divUSeg(x);
		int nY = ByteUtil.divUSeg(y);
		int nZ = ByteUtil.divUSeg(z);
		int nnX = (nX) - start.x;
		int nnY = (nY) - start.y;
		int nnZ = (nZ) - start.z;
		// nnX = (nnX < 0 ? nnX +1 : nnX);
		// nnY = (nnY < 0 ? nnY +1 : nnY);
		// nnZ = (nnZ < 0 ? nnZ +1 : nnZ);
		int index = nnZ * SegmentBufferManager.DIMENSIONxDIMENSION + nnY * SegmentBufferManager.DIMENSION + nnX;
		assert (index < BUFFER_LENGTH && index >= 0) : ("PUTTING " + index + "/" + BUFFER_LENGTH + "; " + "(" + x + "; " + y + "; " + z + "): divUSeg(" + nX + ", " + nY + ", " + nZ + "): minusStart(" + nnX + ", " + nnY + ", " + nnZ + "):   ActualStart" + start);
		return index;
	}

	public static int getIndexAbsolute(int x, int y, int z) {
		int nX = ByteUtil.divUSeg(x);
		int nY = ByteUtil.divUSeg(y);
		int nZ = ByteUtil.divUSeg(z);
		int nnX = (nX);
		int nnY = (nY);
		int nnZ = (nZ);
		// nnX = (nnX < 0 ? nnX +1 : nnX);
		// nnY = (nnY < 0 ? nnY +1 : nnY);
		// nnZ = (nnZ < 0 ? nnZ +1 : nnZ);
		int index = nnZ * SegmentBufferManager.DIMENSIONxDIMENSION + nnY * SegmentBufferManager.DIMENSION + nnX;
		assert (index < BUFFER_LENGTH && index >= 0) : ("PUTTING " + index + "/" + BUFFER_LENGTH + "; " + "(" + x + "; " + y + "; " + z + "): divUSeg(" + nX + ", " + nY + ", " + nZ + "): -Start(" + nnX + ", " + nnY + ", " + nnZ + "):   ");
		return index;
	}

	public int clearFast() {
		// System.err.println("CLEARING SEGMENT BUFFER  hash("+hashCode()+")");
		final Vector3i tmp = new Vector3i();
		data.iterateOverNonEmptyElement((s, lastChanged) -> {
			Segment segment = removeImmediate(s, false);
			SegmentData oldData = segment.getSegmentData();
			final SegmentData segmentData = segment.getSegmentData();
			if(segmentData != null) {
				segmentData.rwl.writeLock().lock();
			}
			try {
				segmentController.getSegmentProvider().purgeSegmentData(segment, oldData, true);
				tmp.x++;
			} finally {
				if(segmentData != null) {
					segmentData.rwl.writeLock().unlock();
				}
			}
			return true;
		});
		data.clear();
		boundingBox.reset();
		return tmp.x;
	}	@Override
	public void addImmediate(Segment s) {
		synchronized(this) {
			if(((RemoteSegment) s).lastLocalTimeStamp > 0) {
				((RemoteSegment) s).setLastChanged(((RemoteSegment) s).lastLocalTimeStamp);
			}
			untouched = false;
			// remove any old segment on that pos cleanly
			assert (s.isEmpty() || s.getSegmentData().getSegment() == s);
			// if(getSegmentController().isOnServer()){
			// System.err.println("---> ADDING ELEMENT IMMEDIATE:   "+regionStart+" "+ hashCode()+"; "+s.pos+" "+getSegmentController()+"; "+getSegmentController().getState());
			// }
			int state = getSegmentState(s.pos);
			if(state == SegmentBufferOctree.EMPTY && (state == SegmentBufferOctree.EMPTY) == s.isEmpty()) {
			} else {
				// put new segment in place
				Segment lastOnThatPosition = put(s.pos, s);
				if(lastOnThatPosition == null) {
					size++;
					manager.inc();
					if(!s.isEmpty()) {
						sizeNonEmpty++;
						manager.incNonEmpty();
						manager.incNonEmptyInitial();
						updateBB(s);
					}
					segmentController.onSegmentAddedSynchronized(s);
				} else {
					if(lastOnThatPosition.isEmpty() && !s.isEmpty()) {
						sizeNonEmpty++;
						// manager.incNonEmpty();
					} else if(!lastOnThatPosition.isEmpty() && s.isEmpty()) {
						sizeNonEmpty--;
						// manager.decNonEmpty();
					}
					// if(lastOnThatPosition != s){
					// System.err.println("[BUFFER] WARNING "+getSegmentController()+" NOT Overwriting "+lastOnThatPosition+" with "+s+"; "+s.isRerequesting());
					// }
				}
				// System.err.println("############################ADDED SEGMENT!!!!!!!!!!!!!!!!!!!!!! "+size+" ---> "+sizeNonEmpty+" ;; "+s);
			}
			assert (getSegmentState(s.pos) != SegmentBufferOctree.NOTHING);
			assert (!s.isEmpty() || getSegmentState(s.pos) == SegmentBufferOctree.EMPTY);
			assert (s.isEmpty() || getSegmentState(s.pos) >= 0);
			if(!segmentController.isOnServer()) {
				DrawableRemoteSegment ds = (DrawableRemoteSegment) s;
				ds.requestState = DrawableRemoteSegment.RequestState.JUST_ADDED;
				ds.requestStatus = RemoteSegment.SEGMENT_ADDED;
			}
			// updateSurroundingAdd(s);
			updateLastSegmentLoadChanged(s.getSegmentController().getState().getUpdateTime());
			// System.err.println("New bounding box on "+s.getSegmentController().getState()+": "+boundingBox);
			if(s.getSegmentController().getPhysicsDataContainer().isInitialized() && s.getSegmentController().getPhysicsDataContainer().getObject() != null) {
				s.getSegmentController().getPhysicsDataContainer().getObject().activate();
				/*AudioController.fireAudioEvent(AudioTags.GUI, AudioTags.DIALOG, AudioTags.ACTIVATE)*/
				//AudioController.fireAudioEventID(938);
			}
		}
	}

	/**
	 * @return the creationTime
	 */
	public long getCreationTime() {
		return creationTime;
	}	@Override
	public int clear(boolean threadedClear) {
		synchronized(this) {
			final Vector3i tmp = new Vector3i();
			// System.err.println("CLEARING SEGMENT BUFFER  hash("+hashCode()+")");
			data.iterateOverNonEmptyElement((s, lastChanged) -> {
				Segment segment = removeImmediate(s, false);
				SegmentData oldData = segment.getSegmentData();
				SegmentData segmentData = segment.getSegmentData();
				if(segmentData != null) {
					segmentData.rwl.writeLock().lock();
				}
				try {
					segmentController.getSegmentProvider().purgeSegmentData(segment, oldData, false);
					tmp.x++;
				} finally {
					if(segmentData != null) {
						segmentData.rwl.writeLock().unlock();
					}
				}
				return true;
			});
			data.clear();
			boundingBox.reset();
			return tmp.x;
		}
	}

	/**
	 * @return the freeSegmentData
	 */
	public int getFreeSegmentData() {
		return freeSegmentData;
	}	@Override
	public boolean containsIndex(long index) {
		return containsKey(ElementCollection.getPosX(index), ElementCollection.getPosY(index), ElementCollection.getPosZ(index));
	}

	public Vector3i getRegionStart() {
		return regionStart;
	}	@Override
	public boolean containsKey(int x, int y, int z) {
		return data.contains(x, y, z);
	}

	public Vector3i getRegionStartBlock() {
		return regionBlockStart;
	}	@Override
	public boolean containsKey(Vector3i key) {
		return data.contains(key);
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active > 0;
	}	@Override
	public boolean containsValue(Segment value) {
		return value.equals(get(value.pos));
	}

	@Override
	public String toString() {
		return "[" + segmentController + "(" + regionStart + " - " + regionEnd + ")]";
	}	/**
	 * provided point segmentpos + elempos wthout HALFSIZE
	 *
	 * @param posMod
	 *
	 * @return true is exists. false if not, or if segment is not buffered (segment is requested)
	 * @throws InterruptedException
	 */
	@Override
	public boolean existsPointUnsave(Vector3i posMod) {
		return existsPointUnsave(posMod.x, posMod.y, posMod.z);
	}

	public Vector3i getRegionEndBlock() {
		return regionBlockEnd;
	}	@Override
	public boolean existsPointUnsave(long l) {
		return existsPointUnsave(ElementCollection.getPosX(l), ElementCollection.getPosY(l), ElementCollection.getPosZ(l));
	}

	// public void iterateOverUnloadedElements(
	// SegmentBufferIteratorEmptyInterface segmentBufferIteratorInterface) {
	//
	// data.iterateOverUnloaded(segmentBufferIteratorInterface);
	//
	// }
	// public void iterateOverUnloadedAndNonemptyElements(
	// SegmentBufferIteratorEmptyInterface segmentBufferIteratorInterface) {
	//
	// data.iterateOverUnloaded(segmentBufferIteratorInterface);
	//
	// }
	public boolean isInViewFrustum(Vector3f minOut, Vector3f maxOut, Vector3f minOutC, Vector3f maxOutC) {
		Transform t = segmentController.getWorldTransformOnClient();
		minOut.set(regionBlockStart.x - SegmentData.SEG_HALF, regionBlockStart.y - SegmentData.SEG_HALF, regionBlockStart.z - SegmentData.SEG_HALF);
		maxOut.set(regionBlockEnd.x - SegmentData.SEG_HALF, regionBlockEnd.y - SegmentData.SEG_HALF, regionBlockEnd.z - SegmentData.SEG_HALF);
		DrawableRemoteSegment.transformAabb(minOut, maxOut, 0, t, minOutC, maxOutC);
		return Controller.getCamera().isAABBInFrustum(minOutC, maxOutC);
	}	@Override
	public boolean existsPointUnsave(int x, int y, int z) {
		byte elemIndexTmpX = (byte) ByteUtil.modUSeg(x);
		byte elemIndexTmpY = (byte) ByteUtil.modUSeg(y);
		byte elemIndexTmpZ = (byte) ByteUtil.modUSeg(z);
		int segIndexTmpX = ByteUtil.divUSeg(x);
		int segIndexTmpY = ByteUtil.divUSeg(y);
		int segIndexTmpZ = ByteUtil.divUSeg(z);
		segIndexTmpX *= SegmentData.SEG;
		segIndexTmpY *= SegmentData.SEG;
		segIndexTmpZ *= SegmentData.SEG;
		if(containsKey(segIndexTmpX, segIndexTmpY, segIndexTmpZ)) {
			int sState = getSegmentState(segIndexTmpX, segIndexTmpY, segIndexTmpZ);
			if(sState >= 0) {
				Segment segment = segmentController.getSegmentBuffer().get(segIndexTmpX, segIndexTmpY, segIndexTmpZ);
				return segment.getSegmentData().contains(elemIndexTmpX, elemIndexTmpY, elemIndexTmpZ);
			} else {
				return false;
			}
		}
		return false;
	}

	public boolean isFullyInViewFrustum(Vector3f minOut, Vector3f maxOut, Vector3f minOutC, Vector3f maxOutC) {
		Transform t = segmentController.getWorldTransformOnClient();
		minOut.set(regionBlockStart.x - SegmentData.SEG_HALF, regionBlockStart.y - SegmentData.SEG_HALF, regionBlockStart.z - SegmentData.SEG_HALF);
		maxOut.set(regionBlockEnd.x - SegmentData.SEG_HALF, regionBlockEnd.y - SegmentData.SEG_HALF, regionBlockEnd.z - SegmentData.SEG_HALF);
		DrawableRemoteSegment.transformAabb(minOut, maxOut, 0, t, minOutC, maxOutC);
		return Controller.getCamera().isAABBFullyInFrustum(minOutC, maxOutC);
	}	@Override
	public Segment get(int x, int y, int z) {
		// bufferedSegments[getIndex(x, y, z)];
		return data.getSegment(x, y, z, regionStart);
	}

	public void setLastChanged(long t) {
		lastBufferChanged = t;
	}	@Override
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
	public SegmentBuffer getBuffer(Vector3i pos) {
		return this;
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
		return getPointUnsave(posModX, posModY, posModZ, new SegmentPiece());
	}

	@Override
	public SegmentPiece getPointUnsave(int posModX, int posModY, int posModZ, SegmentPiece piece) {
		return getPointUnsave(posModX, posModY, posModZ, piece, 0);
	}

	public SegmentPiece getPointUnsave(int posModX, int posModY, int posModZ, SegmentPiece piece, int iter) {
		byte elemIndexTmpX = (byte) ByteUtil.modUSeg(posModX);
		byte elemIndexTmpY = (byte) ByteUtil.modUSeg(posModY);
		byte elemIndexTmpZ = (byte) ByteUtil.modUSeg(posModZ);
		int segIndexTmpX = ByteUtil.divUSeg(posModX);
		int segIndexTmpY = ByteUtil.divUSeg(posModY);
		int segIndexTmpZ = ByteUtil.divUSeg(posModZ);
		segIndexTmpX *= SegmentData.SEG;
		segIndexTmpY *= SegmentData.SEG;
		segIndexTmpZ *= SegmentData.SEG;
		boolean empty = false;
		int segmentState = data.getSegmentState(segIndexTmpX, segIndexTmpY, segIndexTmpZ);
		// Segment segment = segmentController.getSegmentBuffer().get(segIndexTmpX, segIndexTmpY, segIndexTmpZ);
		if(segmentState >= -1) {
			if(segmentState >= 0) {
				piece.populate(data.getSegment(segIndexTmpX, segIndexTmpY, segIndexTmpZ, regionStart), elemIndexTmpX, elemIndexTmpY, elemIndexTmpZ);
				return piece;
			} else {
				// doesn't reall happen too often
				empty = true;
				piece.reset();
				piece.setSegment(data.getSegment(segIndexTmpX, segIndexTmpY, segIndexTmpZ, regionStart));
				piece.setPos(elemIndexTmpX, elemIndexTmpY, elemIndexTmpZ);
				// no need to add in this situation as it already is registered empty
				// adding empty wouldnt add anything
				if(!piece.getSegment().isEmpty()) {
					addImmediate(piece.getSegment());
				}
				return piece;
				// System.err.println("EMPTY: "+segment.isEmpty()+"; segData: "+segment.getSegmentData()+"; "+segment.pos);
			}
		}
		return null;
	}

	@Override
	public SegmentPiece getPointUnsave(long posMod) {
		return getPointUnsave(posMod, new SegmentPiece());
	}

	@Override
	public SegmentPiece getPointUnsave(long posMod, SegmentPiece piece) {
		return getPointUnsave(ElementCollection.getPosX(posMod), ElementCollection.getPosY(posMod), ElementCollection.getPosZ(posMod), piece);
	}

	/**
	 * provided point segmentpos + elempos wthout HALFSIZE
	 *
	 * @param posMod
	 * @param autoRequest
	 *
	 * @return true is exists. false if not, or if segment is not buffered (segment is requested)
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Override
	public SegmentPiece getPointUnsave(Vector3i posMod) {
		return getPointUnsave(posMod, new SegmentPiece());
	}

	@Override
	public SegmentPiece getPointUnsave(Vector3i posMod, SegmentPiece piece) {
		return getPointUnsave(posMod.x, posMod.y, posMod.z, piece);
	}

	@Override
	public SegmentController getSegmentController() {
		return segmentController;
	}

	@Override
	public int getTotalNonEmptySize() {
		return sizeNonEmpty;
	}

	@Override
	public int getTotalSize() {
		return size();
	}

	@Override
	public boolean handle(Vector3i cachPos, SegmentBufferIteratorInterface iteratorImpl, long lastChanged) {
		Segment segment = get(cachPos);
		if(segment != null) {
			boolean handle = iteratorImpl.handle(segment, lastChanged);
			if(!handle) {
				return handle;
			}
		}
		return true;
	}

	@Override
	public boolean handleNonEmpty(int x, int y, int z, SegmentBufferIteratorInterface iteratorImpl, long lastChanged) {
		Segment segment = get(x, y, z);
		if(segment != null && !segment.isEmpty()) {
			boolean handle = iteratorImpl.handle(segment, lastChanged);
			if(!handle) {
				return handle;
			}
		}
		return true;
	}

	@Override
	public boolean handleNonEmpty(Vector3i cachPos, SegmentBufferIteratorInterface iteratorImpl, long lastChanged) {
		return handleNonEmpty(cachPos.x, cachPos.y, cachPos.z, iteratorImpl, lastChanged);
	}

	/**
	 * @param active the active to set
	 */
	@Override
	public void incActive(int active, Segment s) {
		this.active += active;
	}

	@Override
	public boolean isEmpty() {
		return size <= 0;
	}

	@Override
	public boolean isUntouched() {
		return untouched;
	}

	@Override
	public void iterateIntersecting(SegmentBufferInterface otherSegmentBuffer, Transform otherBufferInverse, SegmentBufferIntersectionInterface handler, SegmentBufferIntersectionVariables v, Vector3i from, Vector3i to, Vector3i fromOther, Vector3i toOther) {
	}

	@Override
	public boolean iterateOverEveryElement(SegmentBufferIteratorEmptyInterface iteratorImpl, boolean synch) {
		if(synch) {
			synchronized(this) {
				data.iterateOverEveryElement(iteratorImpl);
			}
		} else {
			data.iterateOverEveryElement(iteratorImpl);
		}
		// the whole set has been iterated
		return true;
	}

	@Override
	public boolean iterateOverNonEmptyElement(SegmentBufferIteratorInterface iteratorImpl, boolean synch) {
		if(synch) {
			synchronized(this) {
				data.iterateOverNonEmptyElement(iteratorImpl);
			}
		} else {
			data.iterateOverNonEmptyElement(iteratorImpl);
		}
		// the whole set has been iterated
		return true;
	}

	@Override
	public boolean iterateOverEveryChangedElement(SegmentBufferIteratorEmptyInterface iteratorImpl, boolean synch) {
		if(synch) {
			synchronized(this) {
				data.iterateOverEveryElement(iteratorImpl);
			}
		} else {
			data.iterateOverEveryElement(iteratorImpl);
		}
		// the whole set has been iterated
		return true;
	}

	@Override
	public boolean iterateOverNonEmptyElementRange(SegmentBufferIteratorInterface iteratorImpl, int fromX, int fromY, int fromZ, int toX, int toY, int toZ, boolean synch) {
		if(sizeNonEmpty == 0) {
			return true;
		}
		assert (fromX >= regionBlockStart.x) : fromX + " / " + regionBlockStart;
		assert (fromY >= regionBlockStart.y) : fromY + " / " + regionBlockStart;
		assert (fromZ >= regionBlockStart.z) : fromZ + " / " + regionBlockStart;
		assert (toX < regionBlockEnd.x) : toX + " / " + regionBlockEnd;
		assert (toY < regionBlockEnd.y) : toY + " / " + regionBlockEnd;
		assert (toZ < regionBlockEnd.z) : toZ + " / " + regionBlockEnd;
		if(synch) {
			synchronized(this) {
				data.iterateOverNonEmptyElementRange(iteratorImpl, fromX, fromY, fromZ, toX, toY, toZ);
			}
		} else {
			data.iterateOverNonEmptyElementRange(iteratorImpl, fromX, fromY, fromZ, toX, toY, toZ);
		}
		return true;
	}

	@Override
	public void onAddedElement(short newType, int oldSize, byte x, byte y, byte z, Segment segment, long time, byte orientation) {
		// if(oldSize == 0){
		// sizeNonEmpty++;
		// manager.incNonEmpty();
		// updateBB(segment);
		// }
		// markNeighborsForMeshUpdateIfNecessary(x,y,z, segment);
	}

	@Override
	public void onRemovedElementClient(short oldType, int oldSize, byte x, byte y, byte z, Segment segment, long time) {
		// if(oldSize == 1){
		// manager.decNonEmpty();
		// sizeNonEmpty--;
		// restructBB();
		// }
		// markNeighborsForMeshUpdateIfNecessary(x,y,z, segment);
	}

	@Override
	public Segment removeImmediate(Vector3i pos, boolean aabbUpdate) {
		synchronized(this) {
			Segment s = removeSeg(pos);
			return removeImmediate(s, aabbUpdate);
		}
	}

	@Override
	public void restructBB() {
		boundingBox.reset();
		iterateOverNonEmptyElement((s, lastChanged) -> {
			updateBB(s);
			return true;
		}, false);
	}

	@Override
	public void restructBBFastOnRemove(Vector3i pos) {
		float xMin;
		float yMin;
		float zMin;
		float xMax;
		float yMax;
		float zMax;
		xMin = (pos.x) - Element.BLOCK_SIZE;
		yMin = (pos.y) - Element.BLOCK_SIZE;
		zMin = (pos.z) - Element.BLOCK_SIZE;
		xMax = (pos.x) + Element.BLOCK_SIZE;
		yMax = (pos.y) + Element.BLOCK_SIZE;
		zMax = (pos.z) + Element.BLOCK_SIZE;
		// using a wider range (16) is ok,
		// since on remove this method is only invoked
		// for blocks at the borders of segmentData
		if(xMin + SegmentData.SEG > boundingBox.min.x || yMin + SegmentData.SEG > boundingBox.min.y || zMin + SegmentData.SEG > boundingBox.min.z || xMax - SegmentData.SEG < boundingBox.max.x - 1 || yMax - SegmentData.SEG < boundingBox.max.y - 1 || zMax - SegmentData.SEG < boundingBox.max.z - 1) {
			// this segmentData is on the edge
			// restruction needed
			// System.err.println("["+(getSegmentController().isOnServer() ? "SERVER" : "CLIENT")+"][SEGMENTBUFFER] EDGE UPDATE: RESTRUCTION OF AABB REQUIRED "+getSegmentController()+": ("+xMin+", "+yMin+", "+zMin+"), ("+xMax+", "+yMax+", "+zMax+") ---- "+getBoundingBox().min+", "+getBoundingBox().max);
			manager.restructBB();
		}
	}

	/**
	 * This method is used to recalculate the bounding box (AABB) of a segment in an efficient manner.
	 * It checks if the segment is on the edge of the segment data, and if so, it triggers a full recalculation of the bounding box.
	 * If the segment is somewhere in the middle of the segment data, no recalculation is needed.
	 *
	 * @param segmentData The segment data for which the bounding box needs to be recalculated.
	 */
	@Override
	public void restructBBFast(SegmentData segmentData) {
		// Get the segment from the segment data
		Segment s = segmentData.getSegment();
		float xMin;
		float yMin;
		float zMin;
		float xMax;
		float yMax;
		float zMax;
		// If the segment is not empty, calculate the min and max coordinates for x, y, and z
		if(!s.isEmpty()) {
			xMin = (s.pos.x + s.getSegmentData().getMin().x - SegmentData.SEG_HALF) - Element.BLOCK_SIZE;
			yMin = (s.pos.y + s.getSegmentData().getMin().y - SegmentData.SEG_HALF) - Element.BLOCK_SIZE;
			zMin = (s.pos.z + s.getSegmentData().getMin().z - SegmentData.SEG_HALF) - Element.BLOCK_SIZE;
			xMax = (s.pos.x + s.getSegmentData().getMax().x - SegmentData.SEG_HALF) + Element.BLOCK_SIZE;
			yMax = (s.pos.y + s.getSegmentData().getMax().y - SegmentData.SEG_HALF) + Element.BLOCK_SIZE;
			zMax = (s.pos.z + s.getSegmentData().getMax().z - SegmentData.SEG_HALF) + Element.BLOCK_SIZE;
		} else {
			// If the segment is empty, set the min and max coordinates for x, y, and z to the segment position
			xMin = (s.pos.x) - Element.BLOCK_SIZE;
			yMin = (s.pos.y) - Element.BLOCK_SIZE;
			zMin = (s.pos.z) - Element.BLOCK_SIZE;
			xMax = (s.pos.x) + Element.BLOCK_SIZE;
			yMax = (s.pos.y) + Element.BLOCK_SIZE;
			zMax = (s.pos.z) + Element.BLOCK_SIZE;
		}
		// Check if the segment is on the edge of the segment data
		if(xMin + SegmentData.SEG > boundingBox.min.x || yMin + SegmentData.SEG > boundingBox.min.y || zMin + SegmentData.SEG > boundingBox.min.z || xMax - SegmentData.SEG < boundingBox.max.x - 1 || yMax - SegmentData.SEG < boundingBox.max.y - 1 || zMax - SegmentData.SEG < boundingBox.max.z - 1) {
			// If the segment is on the edge, trigger a full recalculation of the bounding box
			manager.restructBB();
		} // If the segment is somewhere in the middle of the segment data, no recalculation is needed
	}

	@Override
	public void setLastInteraction(long lastInteraction, Vector3i segPos) {
		this.lastInteraction = lastInteraction;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void update() {
	}

	@Override
	public boolean isFullyLoaded() {
		throw new IllegalArgumentException("This function is not implemented for this buffer");
	}

	@Override
	public float getLoadPercent() {
		throw new IllegalArgumentException("This function is not implemented for this buffer");
	}

	@Override
	public void updateBB(Segment s) {
		if(!s.isEmpty()) {
			assert (s.getSegmentData().isBBValid()) : "Invalid BB: " + s + "; " + s.getSize() + " " + s.getSegmentData().getSize() + "; " + s.getSegmentData().getMin() + "; " + s.getSegmentData().getMax() + "; " + s.getSegmentData().isRevalidating();
			// Element.HALF_SIZE for margin
			float xMin = (s.pos.x + s.getSegmentData().getMin().x - SegmentData.SEG_HALF) - Element.BLOCK_SIZE;
			float yMin = (s.pos.y + s.getSegmentData().getMin().y - SegmentData.SEG_HALF) - Element.BLOCK_SIZE;
			float zMin = (s.pos.z + s.getSegmentData().getMin().z - SegmentData.SEG_HALF) - Element.BLOCK_SIZE;
			float xMax = (s.pos.x + s.getSegmentData().getMax().x - SegmentData.SEG_HALF) + Element.BLOCK_SIZE;
			float yMax = (s.pos.y + s.getSegmentData().getMax().y - SegmentData.SEG_HALF) + Element.BLOCK_SIZE;
			float zMax = (s.pos.z + s.getSegmentData().getMax().z - SegmentData.SEG_HALF) + Element.BLOCK_SIZE;
			boundingBox.expand(xMin, yMin, zMin, xMax, yMax, zMax);
			if(segmentController.getPhysicsDataContainer().isInitialized()) {
				segmentController.setFlagSegmentBufferAABBUpdate(true);
				// force activation (even when static object) so the Physics internal AABB will update
				// if the physics are not initialized. the AABB will be updated when adding the object
				// getSegmentController().getPhysicsDataContainer().getObject().activate(true);
			}
		}
		if(boundingBox.isInitialized()) {
			manager.updateBB(boundingBox);
		}
		segmentController.aabbRecalcFlag();
	}

	@Override
	public void updateLastSegmentLoadChanged(long time) {
		this.lastSegmentLoadChange = time;
		manager.updateLastSegmentLoadChanged(time);
	}

	@Override
	public void updateNumber() {
		freeSegmentData = segmentController.getSegmentProvider().getCountOfFree();
		allocatedSegmentData = sizeNonEmpty;
		if(segmentController.isOnServer()) {
			GameServerState.allocatedSegmentData += allocatedSegmentData;
		} else {
			GameClientState.allocatedSegmentData += allocatedSegmentData;
		}
	}

	// public void writeAndClearSynchronized(){
	// // write to database if necessary
	// iterateOverEveryElement(new SegmentBufferIteratorEmptyInterface() {
	//
	// @Override
	// public boolean handle(Segment s, long lastChanged) {
	// try {
	//
	// getSegmentController().getSegmentProvider().getSegmentDataIO().write((RemoteSegment)s, lastChanged, false);
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// return true;
	// }
	//
	// @Override
	// public boolean handleEmpty(int posX, int posY, int posZ, long lastChanged) {
	// try {
	// getSegmentController().getSegmentProvider().getSegmentDataIO().writeEmpty(posX, posY, posZ, getSegmentController(), lastChanged, false);
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// return false;
	// }
	// }, true);
	//
	//
	//
	// synchronized(this){
	// data.iterateOverNonEmptyElement(new SegmentBufferIteratorInterface() {
	//
	// @Override
	// public boolean handle(Segment s, long lastChanged) {
	// Segment segment = removeImmediate(s, false);
	// SegmentData oldData = segment.getSegmentData();
	// segmentController.getSegmentProvider().purgeSegmentData(segment, oldData, false);
	// }
	// return true;
	// }
	// });
	// }
	// }
	@Override
	public boolean iterateOverNonEmptyElementRange(SegmentBufferIteratorInterface handler, Vector3i from, Vector3i to, boolean synch) {
		return iterateOverNonEmptyElementRange(handler, from.x, from.y, from.z, to.x, to.y, to.z, synch);
	}

	@Override
	public long getLastChanged(Vector3i pos) {
		return data.getLastChanged(pos);
	}

	@Override
	public void setLastChanged(Vector3i pos, long lastChanged) {
		data.setLastChanged(pos, lastChanged);
		// try {
		// if(getSegmentController().isOnServer()) {
		//
		// throw new Exception(regionBlockStart+"; "+regionBlockEnd+"; --> "+getSegmentController()+" "+lastChanged);
		// }
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		lastBufferChanged = Math.max(lastBufferChanged, lastChanged);
	}

	@Override
	public int getSegmentState(long segmentPos) {
		int x = ElementCollection.getPosX(segmentPos);
		int y = ElementCollection.getPosY(segmentPos);
		int z = ElementCollection.getPosZ(segmentPos);
		return getSegmentState(x, y, z);
	}

	@Override
	public void get(Vector3i segPos, SegmentRetrieveCallback callback) {
		data.getSegment(segPos.x, segPos.y, segPos.z, callback);
	}

	@Override
	public void get(int x, int y, int z, SegmentRetrieveCallback callback) {
		data.getSegment(x, y, z, callback);
	}

	@Override
	public int getSegmentState(Vector3i pos) {
		return data.getSegmentState(pos.x, pos.y, pos.z);
	}

	// private void updateSurroundingAdd(Segment s){
	//
	// p.set(s.pos);
	// for(int i = 0; i < 6; i++){
	// segmentController.getNeighborSegmentPos(p, i, posOutTmp);
	// Segment neighbor = getSegmentController().getSegmentBuffer().get(posOutTmp);
	// if(neighbor != null){
	// s.setSurrounding(s.getSurrounding() + 1);
	// neighbor.setSurrounding(neighbor.getSurrounding() + 1);
	// }
	// }
	//
	// }
	//
	// private void updateSurroundingRemove(Segment s){
	// Vector3i p = manager.getPool().get();
	// Vector3i posOutTmp = manager.getPool().get();
	// try{
	// s.setSurrounding(0);
	// p.set(s.pos);
	// for(int i = 0; i < 6; i++){
	// segmentController.getNeighborSegmentPos(p, i, posOutTmp);
	// Segment neighbor = getSegmentController().getSegmentBuffer().get(posOutTmp);
	// if(neighbor != null){
	// neighbor.setSurrounding(neighbor.getSurrounding() - 1);
	// }
	// }
	// }finally{
	// manager.getPool().release(p);
	// manager.getPool().release(posOutTmp);
	// }
	// }
	@Override
	public int getSegmentState(int x, int y, int z) {
		return data.getSegmentState(x, y, z);
	}

	@Override
	public void setEmpty(Vector3i pos) {
		setEmpty(pos.x, pos.y, pos.z);
	}

	@Override
	public int setEmpty(int x, int y, int z) {
		data.setEmpty(x, y, z);
		return 0;
	}

	@Override
	public EWAHCompressedBitmap applyBitMap(long segBufferIndex, EWAHCompressedBitmap bitMap) {
		return data.applyBitMap(bitMap);
	}

	@Override
	public void insertFromBitset(Vector3i pos, long segmentBufferIndex, EWAHCompressedBitmap bitmap, SegmentBufferIteratorEmptyInterface callback) {
		IteratingRLW dd = bitmap.getIteratingRLW();
		IntIteratorOverIteratingRLW it = new IntIteratorOverIteratingRLW(dd);
		while(it.hasNext()) {
			addIndex(it.next(), callback);
		}
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
		// try {
		// if(getSegmentController().isOnServer()) {
		// throw new Exception("MMMM "+getSegmentController()+" "+getRegionStart()+"; "+regionEnd+" "+lastBufferSaved);
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		this.lastBufferSaved = lastBufferSaved;
	}

	@Override
	public void setSegmentController(SegmentController buffer) {
		segmentController = buffer;
	}













	public Segment put(int x, int y, int z, Segment s) {
		Segment old = data.getSegment(x, y, z, regionStart);
		data.insert(s);
		return old;
	}

	private Segment put(Vector3i key, Segment s) {
		return put(key.x, key.y, key.z, s);
	}

	@Override
	public void onSegmentBecameEmpty(Segment segment) {
		sizeNonEmpty--;
		manager.decNonEmpty();
	}

	private Segment removeImmediate(Segment s, boolean aabbUpdate) {
		if(s != null) {
			manager.dec();
			size--;
			if(!s.isEmpty()) {
				sizeNonEmpty--;
				manager.decNonEmpty();
				// updateSurroundingRemove(s);
				if(s instanceof DrawableRemoteSegment) {
					List<DrawableRemoteSegment> removedSegments = ((GameClientState) segmentController.getState()).getWorldDrawer().getSegmentDrawer().getRemovedSegments();
					synchronized(removedSegments) {
						removedSegments.add((DrawableRemoteSegment) s);
					}
				}
				// if(s.getSegmentData() != null){
				// getSegmentController().dec(s.getSegmentData());
				// }
				if(aabbUpdate) {
					restructBB();
				}
			}
			updateLastSegmentLoadChanged(s.getSegmentController().getState().getUpdateTime());
		}
		return s;
	}

	public Segment removeSeg(int x, int y, int z) {
		Segment old = data.getSegment(x, y, z, regionStart);
		data.remove(x, y, z);
		return old;
	}

	private Segment removeSeg(Vector3i key) {
		return removeSeg(key.x, key.y, key.z);
	}





	private void addIndex(long p, SegmentBufferIteratorEmptyInterface callback) {
		long orig = p;
		int z = (int) (p / SegmentBufferManager.DIMENSIONxDIMENSION);
		p -= z * (long) SegmentBufferManager.DIMENSIONxDIMENSION;
		int y = (int) (p / SegmentBufferManager.DIMENSION);
		p -= y * (long) SegmentBufferManager.DIMENSION;
		int x = (int) p;
		x *= SegmentData.SEG;
		y *= SegmentData.SEG;
		z *= SegmentData.SEG;
		RemoteSegment emptySeg;
		if(segmentController.isOnServer()) {
			emptySeg = new RemoteSegment(segmentController);
		} else {
			emptySeg = new DrawableRemoteSegment(segmentController);
		}
		emptySeg.setPos(x + regionBlockStart.x, y + regionBlockStart.y, z + regionBlockStart.z);
		data.setLastChanged(emptySeg.pos, 1);
		callback.handleEmpty(emptySeg.pos.x, emptySeg.pos.y, emptySeg.pos.z, emptySeg.getLastChanged());
		addImmediate(emptySeg);
	}






}
