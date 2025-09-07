package org.schema.schine.network.server;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class ServerEntityWriterThread extends Thread {

	private final ObjectArrayList<Runnable> queue = new ObjectArrayList<Runnable>();

	private int extra = 0;

	public boolean shutdown;

	public ServerEntityWriterThread() {
		super("ServerEntityWriterThread");
	}

	public void enqueue(Runnable runnable) {
		
		synchronized (queue) {
			if(!shutdown){
				queue.add(runnable);
			}
			queue.notifyAll();
		}
	}

	public int getActiveCount() {
		synchronized (queue) {
			if(shutdown){
				System.err.println("[SERVER] Server Write Queue: "+queue.size()+"; "+extra+"; "+queue);
				System.err.println("[SERVER] Server Write Queue current: "+next);
				queue.notifyAll();
			}
			return queue.size() + extra;
		}
	}
	public void shutdown(){
		try {
			throw new Exception("ServerEntityWriterThread Shutdown");
		} catch (Exception e) {
			e.printStackTrace();
		}
		shutdown = true;
		synchronized (queue) {
			queue.notifyAll();
		}
	}
	Runnable next;
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		try{
		while (!shutdown || queue.size() > 0) {
			
			synchronized (queue) {
				if(shutdown && queue.isEmpty()){
					return;
				}
				while (queue.isEmpty()) {
					try {
						queue.wait();
						if(shutdown && queue.isEmpty()){
							return;
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				next = queue.remove(0);
				extra++;
			}

			next.run();
			extra--;

		}
		}finally{
			synchronized (queue) {
				while(queue.size() > 0){
					queue.remove(0).run();
				}
			}
			System.err.println("[SERVER] writing queue finished");
			assert(queue.size() == 0):shutdown+"; "+queue.size()+"; "+queue;
		}
	}

}
