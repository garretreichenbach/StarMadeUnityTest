package org.schema.game.common.controller;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.SegmentRetrieveCallback;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;
import org.schema.schine.graphicsengine.forms.BoundingBox;

import com.bulletphysics.linearmath.Transform;
import com.googlecode.javaewah.EWAHCompressedBitmap;

public interface SegmentBufferInterface {
	public void addImmediate(Segment s);

	public int clear(boolean b);

	public boolean containsIndex(long pos);

	public boolean containsKey(int x, int y, int z);

	public boolean containsKey(Vector3i key);

	public boolean containsValue(Segment value);

	public boolean existsPointUnsave(Vector3i posMod);
	public boolean existsPointUnsave(long l);
	public boolean existsPointUnsave(int x, int y, int z);
	public Segment get(int x, int y, int z);

	public Segment get(long pos);

	public Segment get(Vector3i key);

	public BoundingBox getBoundingBox();

	public SegmentBuffer getBuffer(Vector3i pos);

	public long getLastInteraction();

	public long getLastSegmentLoadChanged();

	public SegmentPiece getPointUnsave(int posModX, int posModY, int posModZ);

	public SegmentPiece getPointUnsave(int posModX, int posModY, int posModZ, SegmentPiece piece);

	public SegmentPiece getPointUnsave(long posMod);

	public SegmentPiece getPointUnsave(long posMod, SegmentPiece piece);

	public SegmentPiece getPointUnsave(Vector3i posMod);

	public SegmentPiece getPointUnsave(Vector3i posMod, SegmentPiece piece);

	public SegmentController getSegmentController();

	public void setSegmentController(SegmentController buffer);

	public int getTotalNonEmptySize();

	public int getTotalSize();

	boolean handle(Vector3i cachPos, SegmentBufferIteratorInterface iteratorImpl, long lastChanged);

	public boolean handleNonEmpty(int x, int y, int z,
	                              SegmentBufferIteratorInterface iteratorImpl, long lastChanged);

	public boolean handleNonEmpty(Vector3i cachPos,
	                              SegmentBufferIteratorInterface iteratorImpl, long lastChanged);

	public void incActive(int i, Segment s);

	public boolean isEmpty();

	public void setEmpty(Vector3i pos);

	public boolean isUntouched();

	public void iterateIntersecting(SegmentBufferInterface otherSegmentBuffer,
	                                Transform otherBufferInverse, SegmentBufferIntersectionInterface handler, SegmentBufferIntersectionVariables v,
	                                Vector3i from, Vector3i to, Vector3i fromOther, Vector3i toOther);

	//	public boolean iterateOverElementRange(SegmentBufferIteratorInterface iteratorImpl, Vector3i from, Vector3i to, boolean synch);
	public boolean iterateOverEveryElement(
			SegmentBufferIteratorEmptyInterface iteratorImpl, boolean synch);

	boolean iterateOverNonEmptyElement(SegmentBufferIteratorInterface iteratorImpl, boolean synch);

	public boolean iterateOverEveryChangedElement(SegmentBufferIteratorEmptyInterface iteratorImpl, boolean synch);

	public boolean iterateOverNonEmptyElementRange(SegmentBufferIteratorInterface iteratorImpl,
	                                               int fromX, int fromY, int fromZ,
	                                               int toX, int toY, int toZ,
	                                               boolean synch);

	public void onAddedElement(short newType, int oldSize, byte x, byte y, byte z, Segment segment, long time, byte orientation);

	public void onRemovedElementClient(short oldType, int oldSize, byte x, byte y, byte z, Segment segment, long time);

	public Segment removeImmediate(Vector3i pos, boolean aabbUpdate);

	public void restructBB();

	public void restructBBFast(SegmentData segmentData);

	public void setLastInteraction(long lastInteraction, Vector3i segPos);

	public int size();

	public void update();

	public void updateBB(Segment s);

	public void updateLastSegmentLoadChanged(long time);

	public void updateNumber();

	public boolean iterateOverNonEmptyElementRange(
			SegmentBufferIteratorInterface handler, Vector3i from,
			Vector3i toA, boolean synch);

	public long getLastChanged(Vector3i pos);

	public void setLastChanged(Vector3i pos, long lastChanged);

	public void get(Vector3i segPos, SegmentRetrieveCallback callback);

	public void get(int x, int y, int z, SegmentRetrieveCallback callback);

	public int getSegmentState(Vector3i pos);

	public int getSegmentState(int x, int y, int z);

	public int getSegmentState(long index);
	
	public int setEmpty(int x, int y, int z);

	public EWAHCompressedBitmap applyBitMap(long segBufferIndex, EWAHCompressedBitmap bitMap);

	public void insertFromBitset(Vector3i pos, long segmentBufferIndex,
	                             EWAHCompressedBitmap bitmap, SegmentBufferIteratorEmptyInterface segmentBufferIteratorEmptyInterface);

	public long getLastBufferSaved();

	public void setLastBufferSaved(long lastBufferSaved);

	public long getLastBufferChanged();

	boolean isFullyLoaded();

	public void restructBBFastOnRemove(Vector3i pos);

	public void onSegmentBecameEmpty(Segment segment);

	float getLoadPercent();
}
