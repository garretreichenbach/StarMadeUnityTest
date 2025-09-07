package org.schema.game.common.controller;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;

public class SegmentAABBCalculator extends Thread {
	public final ObjectLinkedOpenHashSet<Segment> queue = new ObjectLinkedOpenHashSet<>();
	public Int2IntOpenHashMap inQueueMap = new Int2IntOpenHashMap();
	private boolean shutdown;

	public SegmentAABBCalculator() {
		super("SegmentAABBCalculator");
		setPriority(3);
	}

	public void enqueue(SegmentProvider segmentProvider, Segment s) {
		synchronized(queue) {
			boolean add = queue.add(s);
			if(add) {
				inQueueMap.addTo(segmentProvider.getSegmentController().getId(), 1);
			}
			queue.notify();
		}
	}

	@Override
	public void run() {
		while(!shutdown) {
			Segment seg;
			synchronized(queue) {
				while(queue.isEmpty()) {
					try {
						queue.wait();
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
					if(shutdown) {
						return;
					}
				}
				if(shutdown) {
					return;
				}
				seg = queue.removeFirst();
			}
			Vector3b min = new Vector3b();
			Vector3b max = new Vector3b();
			if(seg == null) {
				continue;
			}
			SegmentData segmentData = seg.getSegmentData();
			if(segmentData != null && segmentData.getSegment() != null) {
				segmentData.restructBB(min, max);
				segmentData.getMin().set(min);
				segmentData.getMax().set(max);
			}
			boolean redoAABB;
			synchronized(queue) {
				inQueueMap.addTo(seg.getSegmentController().getId(), -1);
				redoAABB = inQueueMap.get(seg.getSegmentController().getId()) <= 0;
				if(redoAABB) {
					inQueueMap.remove(seg.getSegmentController().getId());
				}
			}
			// Check if the queue is empty or if the AABB needs to be recalculated
			if(queue.isEmpty() || redoAABB) {
				// If the segment data is not null
				if(segmentData != null) {
					// Synchronize on the state of the segment controller
					synchronized(seg.getSegmentController().getState()) {
						// Recalculate the bounding box of the segment data
						seg.getSegmentController().getSegmentBuffer().restructBBFast(segmentData);
					}
					// If the segment is empty
				} else if(seg.isEmpty()) {
					// Recalculate the bounding box of the segment based on its position
					seg.getSegmentController().getSegmentBuffer().restructBBFastOnRemove(seg.pos);
				}
			}
			try {
				Thread.sleep(10);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void shutdown() {
		shutdown = true;
		synchronized(queue) {
			queue.notify();
		}
	}
}
