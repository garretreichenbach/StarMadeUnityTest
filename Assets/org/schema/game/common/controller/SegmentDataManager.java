package org.schema.game.common.controller;

import org.schema.game.client.view.GameVisibility;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SegmentData4Byte;
import org.schema.game.common.data.world.SegmentDataIntArray;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.client.ClientStateInterface;

import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;

public class SegmentDataManager {

	public static final int MAX_BUFFERED_SEGMENT_DATAS = 500;
	public static int MAX_BUFFERED_DATA;
	//	private static Vector3i[] createIterations() {
	//		Vector3i[] iterations = new Vector3i[MAX_BUFFERED_DATA/2];
	//		int i = 0;
	//		for (int xOff = -Controller.vis.getVisibleDistance(); xOff <= Controller.vis.getVisibleDistance(); xOff++) {
	//			for (int yOff = -Controller.vis.getVisibleDistance(); yOff <= Controller.vis.getVisibleDistance(); yOff++) {
	//				for (int zOff = -Controller.vis.getVisibleDistance(); zOff <= Controller.vis.getVisibleDistance(); zOff++) {
	//					iterations[i] = new Vector3i(xOff, yOff, zOff);
	//
	//					i++;
	//				}
	//			}
	//		}
	//		Arrays.sort(iterations);
	//		return iterations;
	//	}
	private ObjectArrayFIFOQueue<SegmentData> unusedSegmentData;
	private StateInterface state;
	public SegmentDataManager(StateInterface state) {
		this.state = state;
		if (Controller.vis == null) {
			Controller.vis = new GameVisibility();
			Controller.vis.recalculateVisibility();
		}
		unusedSegmentData = new ObjectArrayFIFOQueue(512);
		for (int i = 0; i < 256; i++) {
			unusedSegmentData.enqueue(new SegmentData4Byte(state instanceof ClientStateInterface));
		}

	}

	public static void makeIterations() {
		//		System.err.println("MAKING ITERATION: "+Controller.vis.getVisibleDistance());
		//		try{
		//			throw new NullPointerException();
		//		}catch(Exception e){
		//			e.printStackTrace();
		//		}
		MAX_BUFFERED_DATA = (Controller.vis.getVisibleDistance() * 2 + 1)
				* (Controller.vis.getVisibleDistance() * 2 + 1) * (Controller.vis.getVisibleDistance() * 2 + 1) * 2;
	}

	public void addToFreeSegmentData(SegmentData unused, boolean reset, boolean fast) {

		if(unused != null){

			if (!(unused instanceof SegmentData4Byte)){
				return;
			}
				

			//assert (unused instanceof SegmentDataIntArray);

			try{
			synchronized (unusedSegmentData) {
				assert((unused instanceof SegmentData4Byte)):"Currently not poolable: "+unused.getClass().getSimpleName()+"; "+unused;
				assert (unused != null);
				try {
					if (reset) {
						if (fast) {
							unused.resetFast();
						} else {
							unused.reset(state.getUpdateTime());
						}
					}
				} catch (SegmentDataWriteException e) {
					throw new RuntimeException("implemented to handle this", e);
				}
				if (unused.getSegment() != null) {
					unused.getSegment().setSegmentData(null);
				}
	
				unused.setSegment(null);
				if(unusedSegmentData.size() < 1024){
					unusedSegmentData.enqueue(unused);
				}
			}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	public SegmentData getFreeSegmentData() {
		synchronized (unusedSegmentData) {
			if (!unusedSegmentData.isEmpty()) {
				SegmentData free = unusedSegmentData.dequeue();
				assert (free instanceof SegmentData4Byte);
				assert (free != null);
				assert (free.getSize() == 0);

				return free;
			}
		}

		return new SegmentData4Byte(state instanceof ClientStateInterface);
	}

	public int sizeFree() {
		return unusedSegmentData.size();
	}

}
