package org.schema.game.common.controller.elements;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.schema.game.common.util.FastCopyLongOpenHashSet;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class ElementCollectionCalculationThreadManager extends Thread {

	private final ObjectArrayList<ElementCollectionCalculationThreadExecution<?, ?, ?>> queue = new ObjectArrayList<ElementCollectionCalculationThreadExecution<?, ?, ?>>();
	
	
	private boolean finished;
	private ElementCollectionCalculationThreadExecution<?, ?, ?> lastQueueElement;
	private boolean onServer;
	private SizedExecutor[] executors;

	public ElementCollectionCalculationThreadManager(boolean onServer) {
		super(onServer ? "ElementCollectionCalculationThreadManagerServer" : "ElementCollectionCalculationThreadManagerClient");
		this.onServer = onServer;
		setDaemon(true);
		setPriority(4);
		
		executors = new SizedExecutor[]{
			new SizedExecutor(25, 32, onServer),	
			new SizedExecutor(15, 256, onServer),	
			new SizedExecutor(7, 1024, onServer),
			new SizedExecutor(5, -1, onServer)	
		};
		for(SizedExecutor e : executors){
			e.start();
		}
	}

	public void enqueue(ElementCollectionCalculationThreadExecution<?, ?, ?> e) {
		synchronized (queue) {
			if (!queue.contains(e)) {
				queue.add(e);
				queue.notify();
			}
		}
	}

	@Override
	public void run() {
		try{
			while (!finished) {
				ElementCollectionCalculationThreadExecution<?, ?, ?> nextQueueElement;
				synchronized (queue) {
					while (queue.isEmpty()) {
						try {
							queue.wait(5000);
						} catch (Exception e) {
							e.printStackTrace();
						}
						if(finished){
							return;
						}
					}
					nextQueueElement = queue.remove(0);
				}
				this.lastQueueElement = nextQueueElement;
				synchronized (nextQueueElement.getMan().getSegmentController().getState()) {
					if (!nextQueueElement
							.getMan().getSegmentController().getState().getLocalAndRemoteObjectContainer().getLocalObjects()
							.containsKey(nextQueueElement.getMan().getSegmentController().getId())) {
						System.err.println("[ElColManTh][" + nextQueueElement.getMan().getSegmentController().getState() + "] " + nextQueueElement.getMan() + " not executing Element Collection update because segmentController no longer exists");
						continue;
					}
				}
				for(int i = 0; i < executors.length; i++){
					if(nextQueueElement.getRawSize() <= executors[i].maxRaw || i == executors.length -1){
						executors[i].enqueuePriv(nextQueueElement);
						break;
					}
				}
			}
		}finally{
			
		}
	}
	private static class SizedExecutor extends Thread{
		private final ObjectArrayList<ElementCollectionCalculationThreadExecution<?, ?, ?>> privQueue = new ObjectArrayList<ElementCollectionCalculationThreadExecution<?, ?, ?>>();
		private final ExecutorService executor;
		private final int maxRaw;
		private final ObjectArrayFIFOQueue<ElementCollectionCalculationThread> threadDataPool 
		;
		public boolean finished;
		private boolean onServer;
		public SizedExecutor(final int size, int maxRaw, boolean onServer){
			super((onServer ? "ServerECCalcExecutor" : "ClientECCalcExecutor"+"_"+(maxRaw < 0 ? "UNLIMITED" : maxRaw)));
			this.setPriority(MIN_PRIORITY);
			this.onServer = onServer;
			threadDataPool = new ObjectArrayFIFOQueue<ElementCollectionCalculationThread>(size);
			executor = Executors.newFixedThreadPool(size);
			for (int i = 0; i < size; i++) {
				threadDataPool.enqueue(new ElementCollectionCalculationThread(maxRaw, onServer));
				executor.execute(() -> {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				});
			}
			this.maxRaw = maxRaw;
			
		}
		
		@Override
		public void run(){
			try{
				while(!finished){
					ElementCollectionCalculationThreadExecution<?, ?, ?> nextQueueElement;
					synchronized (privQueue) {
						while (privQueue.isEmpty()) {
							try {
								privQueue.wait(20000);
							} catch (Exception e) {
								e.printStackTrace();
							}
							if(finished){
								return;
							}
						}
						nextQueueElement = privQueue.remove(0);
					}
					ElementCollectionCalculationThread thr;
					synchronized (threadDataPool) {
						while (threadDataPool.isEmpty()) {
							try {
								threadDataPool.wait(20000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							if(finished){
								return;
							}
						}
						thr = threadDataPool.dequeue();
					}
//					if(!onServer) {
//						System.err.println("PREPARED");
//					}
//					try {
//						Thread.sleep(1500);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//					if(!onServer) {
//						System.err.println("INITIALIZING");
//					}
					thr.init(nextQueueElement, this);
					
//					if(!onServer) {
//						System.err.println("EXECUTING");
//					}
//					try {
//						Thread.sleep(1500);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
					executor.execute(thr);
					
//					Thread t = new Thread(thr);
//					t.start();
				}
			}finally{
				executor.shutdown();
			}
		}
		public void enqueuePriv(ElementCollectionCalculationThreadExecution<?, ?, ?> e) {
			synchronized (privQueue) {
				if (!privQueue.contains(e)) {
					privQueue.add(e);
					privQueue.notify();
				}
			}
		}
		public void addBackToPool(
				ElementCollectionCalculationThread finsishedThreadData) {
			synchronized (threadDataPool) {
				threadDataPool.enqueue(finsishedThreadData);
				threadDataPool.notify();
			}
		}
		public void onStop() {
			synchronized(threadDataPool){
				threadDataPool.notify();
			}
			
			try{
				executor.shutdownNow();
			}catch(Exception e){
				e.printStackTrace();
			}			
		}
	}
	


	private static class ElementCollectionCalculationThread implements Runnable, CollectionCalculationCallback {
		private final LongArrayList rawCollection;
		private final FastCopyLongOpenHashSet closedCollection;
		private final LongArrayList openCollection;
		ElementCollectionCalculationThreadExecution<?, ?, ?> nextQueueElement;
		private SizedExecutor sizedExecutor;
		private int expected;
		private final FastCopyLongOpenHashSet totalSet;
		private boolean onServer;

		public ElementCollectionCalculationThread(int expected, boolean onServer) {
			super();
			if(expected <= 0){
				expected = 1024;
			}
			this.onServer = onServer;
			rawCollection = new LongArrayList(expected);
			closedCollection = new FastCopyLongOpenHashSet(expected);
			totalSet = new FastCopyLongOpenHashSet(expected);
			openCollection = new LongArrayList(expected);
			
			this.expected = expected;
		}

		public void init(ElementCollectionCalculationThreadExecution<?, ?, ?> nextQueueElement, SizedExecutor sizedExecutor) {
			this.nextQueueElement = nextQueueElement;
			this.sizedExecutor = sizedExecutor;
		}

		@Override
		public void callback(ElementCollectionCalculationThreadExecution<?, ?, ?> thread) {

		}

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			
			try {
			
				//wait for regular update cycle to fill closed list
				boolean sucessPrepare = nextQueueElement.getMan().flagPrepareUpdate(closedCollection);
				
				if (sucessPrepare) {
					
					totalSet.deepApplianceCopy(closedCollection);
					rawCollection.addAll(closedCollection);
	
					//update preparation is finished
					nextQueueElement.initialize(closedCollection, totalSet, rawCollection, openCollection, this);
	
					nextQueueElement.process();
	
					
					nextQueueElement.flagUpdateFinished();
				} else {
					nextQueueElement.getMan().updateInProgress = Long.MIN_VALUE;
				}
			
			

			}finally {
				sizedExecutor.addBackToPool(this);
				
				totalSet.clear();

				//clear open list
				openCollection.clear();

				//clear closed list
				closedCollection.clear();

				//clear raw collection
				rawCollection.clear();

				openCollection.trim(expected);
				closedCollection.trim(expected);
				rawCollection.trim(expected);
				
			}
		}
	}
	public int getSize(){
		return queue.size();
	}
	public ElementCollectionCalculationThreadExecution<?, ?, ?> getLastQueueElement(){
		return lastQueueElement;
	}
	public void onStop() {
		finished = true;
		for(SizedExecutor e : executors) {
			e.finished = true;
		}
		synchronized(queue){
			queue.notify();
		}
		for(SizedExecutor s : executors){
			s.onStop();
		}
		
	}

}
