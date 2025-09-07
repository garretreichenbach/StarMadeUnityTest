package org.schema.schine.network.common;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

public interface ConnectionRunnable extends Runnable{

	public Thread getThread();
	public void setThread(Thread thread);
	
	public static ThreadPoolExecutor createPool() {
		ThreadPoolExecutor e = (ThreadPoolExecutor) Executors.newCachedThreadPool( );
		 e.setThreadFactory(r -> {
			 Thread thread = new Thread(r);
			 thread.setDaemon(true); //all connection threads are daemons

			 //unfortunately r is not the actual runnable but a worker
//					assert(r instanceof ConnectionRunnable):r;
//					if(r instanceof ConnectionRunnable) {
//						((ConnectionRunnable)r).setThread(thread);
//					}
			 return thread;
		 });
		return e;
	}
}
