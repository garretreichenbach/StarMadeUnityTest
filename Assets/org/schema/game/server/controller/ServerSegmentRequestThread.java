package org.schema.game.server.controller;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.schema.game.common.controller.SegmentOutOfBoundsException;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;

public class ServerSegmentRequestThread extends Thread {

	private GameServerState state;
	private ExecutorService executor;
	private boolean shutdown;
	public static int num;
	public ServerSegmentRequestThread(GameServerState state) {
		super("ServerSegmentRequestThread");
		this.state = state;
		int threads = ServerConfig.CHUNK_REQUEST_THREAD_POOL_SIZE_TOTAL.getInt();
		executor = Executors.newFixedThreadPool(threads, r -> new Thread(r, "ServerSegmentGeneratorThread_"+(num++)));
		//preheat pool
		for(int i = 0; i < threads; i++) {
			executor.execute(() -> {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
		}
		
		System.err.println("[SERVER] Request Thread pool size is: " + ServerConfig.CHUNK_REQUEST_THREAD_POOL_SIZE_TOTAL.getInt());
		setDaemon(true);
	}

	private ServerSegmentRequest getReq() throws InterruptedException {

		synchronized (state.getSegmentRequests()) {
			while (state.getSegmentRequests().isEmpty()) {
				state.getSegmentRequests().wait(5000);
				if(shutdown){
					return null;
				}
			}
			return state.getSegmentRequests().dequeue();
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		try {
			while (!shutdown) {
				final ServerSegmentRequest serverSegmentRequest = getReq();
				
				if(shutdown){
					return;
				}
				synchronized(state){
					if(!state.getLocalAndRemoteObjectContainer().getLocalObjects().containsKey(serverSegmentRequest.getSegmentController().getId())){
						if(serverSegmentRequest.highPrio) {
							synchronized (state.getSegmentRequests()) {
								System.err.println("[SERVER] Segment Controller doesn't exist yet for high prio chunk request "+serverSegmentRequest.getSegmentController());
								//requeue high priority requests
								state.getSegmentRequests().enqueue(serverSegmentRequest);
							}
						}
						continue;
					}
				}
				executor.execute(() -> {
					try {
//							setPriority(NORM_PRIORITY-1);
						state.getController().handleSegmentRequest(serverSegmentRequest);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (SegmentOutOfBoundsException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				});

				//					Thread.sleep(2);

			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assert (false) : "Thread stopped";
	}

	public void shutdown() {
		shutdown = true;
		synchronized (state.getSegmentRequests()) {
			state.getSegmentRequests().notifyAll();
		}
	}

}
