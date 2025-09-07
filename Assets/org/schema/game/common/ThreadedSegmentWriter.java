package org.schema.game.common;

import java.io.IOException;
import java.util.LinkedList;

import org.schema.game.common.data.world.RemoteSegment;

class Queue<E> {
	private LinkedList<E> queue = new LinkedList<E>();
	private boolean shutdown;

	public void shutdown(){
		shutdown = true;
		synchronized (queue) {
			queue.notify();
		}
	}
	void add(E o) {
		synchronized (queue) {
			queue.add(o);
			queue.notify();
		}
	}

	boolean contains(E o) {
		synchronized (queue) {
			return queue.contains(o);
		}
	}

	E take() throws InterruptedException {
		synchronized (queue) {
			while (queue.isEmpty()) {
				queue.wait();
				if(shutdown){
					return null;
				}
			}
			return queue.removeFirst();
		}
	}
}

public class ThreadedSegmentWriter {
	
	private boolean shutdown;

	public void shutdown(){
		shutdown = true;
		requests.shutdown();
	}
	
	private Queue<RemoteSegment> requests = new Queue<RemoteSegment>();

	public ThreadedSegmentWriter(String string) {
		Runnable service = () -> {
			while (!shutdown ) {
				try {
					RemoteSegment take = requests.take();
					if(shutdown){
						return;
					}
					realWrite(take);
				} catch (InterruptedException e) {
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		Thread thread = new Thread(service);
		thread.setDaemon(true);
		thread.setName(string + "_SEGMENT_WRITER_THREAD");
		thread.start();
	}

	public void finish(RemoteSegment segment) {
		while (segment != null && requests.contains(segment)) {
			try {
				System.err.println("WAITING TO FINISH WRITING " + segment);
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void realWrite(RemoteSegment job) throws IOException {
		// do the real work, in our case, writing to a file
		job.getSegmentController().getSegmentProvider().writeSegment(job, job.getLastChanged());

	}

	public void queueWrite(RemoteSegment rs) {
		requests.add(rs);
	}

}


