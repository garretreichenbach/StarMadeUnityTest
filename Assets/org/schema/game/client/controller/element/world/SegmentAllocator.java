package org.schema.game.client.controller.element.world;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentBuffer;
import org.schema.game.common.controller.SegmentDataManager;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;

public class SegmentAllocator {

	private final ArrayList<Vector3i> queue;
	private final Set<Vector3i> createBuffer;
	private final ArrayList<Segment> readySegments;
	private final ArrayList<Segment> overwrittenSegments;
	Vector3i simpleToAbsolute3i = new Vector3i();
	private SegmentBuffer segmentBuffer;

	public SegmentAllocator(SegmentBuffer segmentBuffer) {
		this.segmentBuffer = segmentBuffer;
		this.readySegments = new ArrayList<Segment>();
		this.overwrittenSegments = new ArrayList<Segment>();

		queue = new ArrayList<Vector3i>();
		createBuffer = new HashSet<Vector3i>();
	}

	public void addSegmentBuffered(Segment seg) {
		//		System.err.println("adding seg "+seg);
		readySegments.add(seg);
	}

	public void buffer(int x, int y, int z) {
		//		v.set(x, y , z);
		simpleToAbsolute3i.set((x * SegmentData.SEG), (y * SegmentData.SEG), (z * SegmentData.SEG));

		if (!isInQueue(simpleToAbsolute3i) && !isCreating(simpleToAbsolute3i) && !segmentBuffer.containsKey(simpleToAbsolute3i)) {
			synchronized (queue) {
				//				System.err.println("adding to buffer "+v+", buffered ");
				Vector3i queueElement = new Vector3i(simpleToAbsolute3i);
				queue.add(queueElement);
				synchronized (createBuffer) {
					createBuffer.add(queueElement);
					if (queue.size() > SegmentDataManager.MAX_BUFFERED_DATA) {
						createBuffer.remove(queue.remove(0));
					}
				}
				queue.notify();

			}
		}
	}

	public void discard(Vector3i posSimple) {
		synchronized (createBuffer) {
			createBuffer.remove(posSimple);
			queue.remove(posSimple);
		}
	}

	public Vector3i getNextQueueElement() {
		synchronized (queue) {
			return queue.remove(queue.size() - 1);
		}

	}

	boolean isCreating(Vector3i pos) {
		return createBuffer.contains(pos);
	}

	boolean isInQueue(Vector3i pos) {
		return queue.contains(pos);
	}

	public boolean isQueueEmpty() {
		return queue.isEmpty();
	}

	public void overrideSegment(Segment s) {
		overwrittenSegments.add(s);
	}

	public void removeSegmentBuffered(Vector3i key) {
		Segment s = segmentBuffer.get(key);
		if (s != null) {
			//stuff goes on: collisions etc
			overwrittenSegments.add(s);
		}
	}

	//	public void waitForInput() throws InterruptedException {
	//		synchronized (queue) {
	//			queue.wait();
	//		}
	//	}

}
