package org.schema.game.client.controller.element.world;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.io.SegmentDataFileUtils;
import org.schema.game.common.data.world.RemoteSegment;

public class LocalSegmentRetriever extends Thread {

	private ArrayList<LocalSegmentRequest> queue = new ArrayList<LocalSegmentRequest>();
	public static final int THREADS = 5;
	private ExecutorService executor = Executors.newFixedThreadPool(THREADS);

	public LocalSegmentRetriever() {
		super("LocalSegmentRetriever");
		this.setDaemon(true);
		
		
		for(int i = 0; i < THREADS; i++) {
			executor.execute(() -> {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
		}
	}

	public void enqueue(LocalSegmentRequest s) {
		synchronized (queue) {
			//			System.err.println("ADDING TO LOCAL QUEUE: "+s.segment.getIndex()+"; "+s+"; "+s.provider.getSegmentController());
			assert (!queue.contains(s));
			queue.add(s);
			queue.notify();
		}
	}

	public LocalSegmentRequest getNextInQueue() {
		synchronized (queue) {
			while (queue.isEmpty()) {
				try {
					queue.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return queue.remove(0);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {

		while (true) {
			final LocalSegmentRequest request = getNextInQueue();

			executor.execute(() -> {
									RemoteSegment requested = request.segment;
				// request last changed from server
				int inDB = SegmentDataFileUtils.READ_NO_DATA;
				boolean corrupt = false;
				try {
					// check with database
					inDB = request.provider.requestFromLocalDB(requested);
				} catch (IOException e) {
					e.printStackTrace();
					corrupt = true;
					inDB = SegmentDataFileUtils.READ_NO_DATA;
					System.err.println("[CLIENT] Exception: Local cache is corrupted: REQUESTING COMPLETE PURGE");
					((GameClientState) request.segment.getSegmentController().getState()).setDbPurgeRequested(true);
				}

				if (corrupt || inDB == SegmentDataFileUtils.READ_NO_DATA) {
					// something went wrong reading client DB
					// re-request from server
					if (corrupt) {
						System.err.println("[CLIENT][SEGMENTPROVIDER][WARNING] Requesting corrupt segment!! " + requested.pos + ": " + requested.getSegmentController());
					} else {
						System.err.println("[CLIENT][SEGMENTPROVIDER][WARNING] VersionOK but no data in DB! Re-Requesting segment from server!! " + requested.pos + ": " + requested.getSegmentController());
					}
					request.provider.enqueueSynched(requested);
				} else {
					if (inDB == SegmentDataFileUtils.READ_EMPTY && requested.pos.x == 0 && requested.pos.y == 0 && requested.pos.z == 0) {
						System.err.println("[CLIENT][DB-READ] WARNING 0,0,0 on " + request.provider.getSegmentController() + " IS EMPTY");
					}

					request.provider.received(requested, requested.getIndex());
					//						synchronized(request.provider.getReceivedSegments()){
					//							request.provider.getReceivedSegments().add(requested);
					//						}

					// segment read from local db
					// ready to add to the buffer
					//						handleReceivedSegment(requested);
				}
			});

		}
	}
}
