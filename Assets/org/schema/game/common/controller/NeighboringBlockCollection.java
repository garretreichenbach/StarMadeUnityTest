package org.schema.game.common.controller;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.element.ElementKeyMap;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;

public class NeighboringBlockCollection {

	private final LongArrayList result = new LongArrayList();
	private final ObjectArrayFIFOQueue<SegmentPiece> openList = new ObjectArrayFIFOQueue<SegmentPiece>();
	private final LongOpenHashSet closedSet = new LongOpenHashSet();
	private short type;
	private byte orientation = -1;

	/**
	 * @return the result
	 */
	public LongArrayList getResult() {
		return result;
	}

	/**
	 * @return the type
	 */
	public short getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(short type) {
		this.type = type;
	}

	public void searchWithOrient(SegmentPiece p, SegmentBufferManager segmentBufferManager) {
		type = p.getType();

		if (type == Element.TYPE_NONE || (ElementKeyMap.getInfo(type).getControlledBy().isEmpty() && !ElementKeyMap.getInfo(type).isRailTrack())) {
			return;
		}
		openList.enqueue(p);
		Vector3i pos = new Vector3i();
		Vector3i posTest = new Vector3i();
		SegmentPiece probe = new SegmentPiece();
		while (!openList.isEmpty()) {

			SegmentPiece dequeue = openList.dequeue();
			long current = dequeue.getAbsoluteIndex();
			closedSet.add(current);
			if (dequeue.getType() == type && dequeue.getOrientation() == orientation) {
				result.add(current);
				for (int i = 0; i < 6; i++) {
					dequeue.getAbsolutePos(pos);
					pos.add(Element.DIRECTIONSi[i]);
					SegmentPiece g = segmentBufferManager.getPointUnsave(pos, probe);
					if (g == null || g.getType() == 0 || g.getType() != type || g.getOrientation() != orientation) {
						//dont use g.getAbsoluteIndex since it may be null
						closedSet.add(ElementCollection.getIndex(pos));
					} else {
						long absoluteIndex = g.getAbsoluteIndex();
						if (!closedSet.contains(absoluteIndex)) {
							closedSet.add(absoluteIndex);
							openList.enqueue(new SegmentPiece(g));
						}
					}
				}
			}
		}
	}

	public void search(SegmentPiece p, SegmentBufferManager segmentBufferManager) {
		type = p.getType();

		if (type == Element.TYPE_NONE || ElementKeyMap.getInfo(type).getControlledBy().isEmpty()) {
			return;
		}
		openList.enqueue(p);
		Vector3i pos = new Vector3i();
		Vector3i posTest = new Vector3i();
		SegmentPiece probe = new SegmentPiece();
		while (!openList.isEmpty()) {

			SegmentPiece dequeue = openList.dequeue();
			long current = dequeue.getAbsoluteIndex();
			closedSet.add(current);
			if (dequeue.getType() == type) {
				result.add(current);
				for (int i = 0; i < 6; i++) {
					dequeue.getAbsolutePos(pos);
					pos.add(Element.DIRECTIONSi[i]);
					SegmentPiece g = segmentBufferManager.getPointUnsave(pos, probe);
					if (g == null || g.getType() == 0 || g.getType() != type) {
						//dont use g.getAbsoluteIndex since it may be null
						closedSet.add(ElementCollection.getIndex(pos));
					} else {
						long absoluteIndex = g.getAbsoluteIndex();
						if (!closedSet.contains(absoluteIndex)) {
							closedSet.add(absoluteIndex);
							openList.enqueue(new SegmentPiece(g));
						}
					}
				}
			}
		}
	}

	/**
	 * @return the orientation
	 */
	public byte getOrientation() {
		return orientation;
	}

	/**
	 * @param orientation the orientation to set
	 */
	public void setOrientation(byte orientation) {
		this.orientation = orientation;
	}

}
