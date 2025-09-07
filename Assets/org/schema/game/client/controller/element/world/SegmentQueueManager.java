package org.schema.game.client.controller.element.world;

import java.io.IOException;
import java.util.List;

import javax.vecmath.Vector3f;

import org.schema.common.util.linAlg.Vector3fTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.controller.CreatorThreadController;
import org.schema.game.common.controller.SegmentProvider;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.world.RemoteSegment;

import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongComparator;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;

public class SegmentQueueManager {
	
	private static final int REQUESTS_PER_UPDATE = 256;

	private final GameClientState state;
	
	private final SegmentManager man = new SegmentManager();
	private boolean shutdown;
	public SegmentQueueManager(GameClientState state) {
		super();
		this.state = state;
		Thread t = new Thread(man, "NSegmentRequestManager");
		t.start();
	}
	public void executeSynchend(CreatorThreadController c){
		assert(state.isSynched());
		man.updateTransformationBuffer(c);
		
		synchronized(removedSegs){
			for(short id : removedSegs){
				c.register.onRemovedFromQueue(id);
			}
			removedSegs.clear();
		}
	}
	
	
	
	
	private final LongArrayList queueChunk = new LongArrayList(REQUESTS_PER_UPDATE);
	private Short2ObjectOpenHashMap<ClientSegmentProvider> cpy = new Short2ObjectOpenHashMap<ClientSegmentProvider>();
	private final ShortArrayList removedSegs = new ShortArrayList();
	private final Long2LongOpenHashMap highPrio = new Long2LongOpenHashMap(); 
	private final Long2LongOpenHashMap highPrioFullfilled = new Long2LongOpenHashMap();
	public void doActualRequests(CreatorThreadController c) {
		getQueued(queueChunk, REQUESTS_PER_UPDATE);
		cpy.clear();
		c.register.getCopy(cpy); //create copy from synched set to minimize monitor time
		
		
		for(long l : queueChunk){
			short id = (short)ElementCollection.getType(l);
			long pos = ElementCollection.getPosIndexFrom4(l);
			ClientSegmentProvider clientSegmentProvider = cpy.get(id);
			if(clientSegmentProvider != null){
			try {
				boolean added = clientSegmentProvider.addInProgress(pos);
				if(added){
					clientSegmentProvider.doRequest(pos);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			}else{
				System.err.println("[CLIENT] Requester not found: "+id);
			}
		}
		queueChunk.clear();
	}
	
	private class SegmentManager implements Runnable{
		public Short2ObjectOpenHashMap<LongOpenHashSet> segments = new Short2ObjectOpenHashMap<LongOpenHashSet>();
		public Long2FloatOpenHashMap distances = new Long2FloatOpenHashMap();
		public long[] tempListA = new long[1024];
		public long[] tempListB = new long[1024];
		
		public LongArrayList enqA = new LongArrayList();
		public LongArrayList enqB = new LongArrayList();
		public LongArrayList currentUsedUpList = enqA;
		
		private Short2ObjectOpenHashMap<LongOpenHashSet> usedUpMap = new Short2ObjectOpenHashMap<LongOpenHashSet>();
		
		public ArrayQueue queue;
		
		public Short2ObjectOpenHashMap<LongOpenHashSet> segmentsToAdd = new Short2ObjectOpenHashMap<LongOpenHashSet>();
		public ShortArrayList segmentsToRemove = new ShortArrayList();
		
		private Short2ObjectOpenHashMap<Vector3f> inversePositions = new Short2ObjectOpenHashMap<Vector3f>();
		private Short2ObjectOpenHashMap<Vector3f> inversePositionsCpy = new Short2ObjectOpenHashMap<Vector3f>();
		private Short2ObjectOpenHashMap<ClientSegmentProvider> tmpCpy = new Short2ObjectOpenHashMap<ClientSegmentProvider>();
		private Object queueSwitch = new Object();
		private final LongOpenHashSet highPrioFullFiledLcl = new LongOpenHashSet();
		private int waitToSort;
		public void registerSegment(ClientSegmentProvider clientSegmentProvider, LongCollection indices) {
			synchronized(segmentsToAdd){
				LongOpenHashSet l = segmentsToAdd.get(clientSegmentProvider.registerId);
				if(l == null){
					l = new LongOpenHashSet(indices.size());
					segmentsToAdd.put(clientSegmentProvider.registerId, l);
				}
				l.addAll(indices);
			}
		}


		@Override
		public void run() {
			while(!shutdown){
				//alternating lists to remove queued segments, so that no synch monitor is needed
				LongArrayList usedUp = currentUsedUpList;
				if(currentUsedUpList == enqA){
					currentUsedUpList = enqB; 
				}else{
					currentUsedUpList = enqA;
				}
				int qSize = usedUp.size();
				//put used up into map
				for(int i = 0; i < usedUp.size(); i++){
					long l = usedUp.get(i);
					short id = (short) ElementCollection.getType(l);
					LongOpenHashSet set = usedUpMap.get(id);
					if(set != null){
						set.add(l);
					}
				}
				usedUp.clear();		
				
				int dSize = distances.size();
				
				int dSizeBef = distances.size();
				final int usedUpSize = usedUp.size();
				//remove used up from current queue
				for(LongOpenHashSet e : usedUpMap.values()){
					LongIterator li = e.iterator();
					while(li.hasNext()){
						boolean removed = distances.keySet().remove(li.nextLong());
						if(removed){
							li.remove();
						}
					}
				}
				int dSizeAft = distances.size();
//				if(usedUp.size() > 0){
//					System.err.println("USED UP: "+usedUp.size()+"; DISTANCES REDUCED BY "+(dSizeBef-dSizeAft));
//				}
//				if(dSize > 0){
//					System.err.println("CLEARING "+qSize+"; "+dSize+" -> "+distances.size());
//				}
				
				//put in segments of newly registered entities (keep monitor time low)
				if(segmentsToAdd.size() > 0){
					ShortArrayList copyAdd = new ShortArrayList();
					synchronized(segmentsToAdd){
						
						segments.putAll(segmentsToAdd);
						copyAdd.addAll(segmentsToAdd.keySet());
						segmentsToAdd.clear();
					}
					for(short s : copyAdd){
						if(!usedUpMap.containsKey(s)){
							usedUpMap.put(s, new LongOpenHashSet());
						}
						LongOpenHashSet usedUpAlready = usedUpMap.get(s);
						LongOpenHashSet addedIndices = segments.get(s);
						for(long l : addedIndices){
							long s4 = ElementCollection.getIndex4(l, s);
							//check if newly registered segments are already satisfied by sign request
							if(!usedUpAlready.contains(s4)){
								distances.put(s4, 0.0000001f);
							}
						}
					}
				}
				
				//removesegments of unregistered entities (keep monitor time low)
				if(segmentsToRemove.size() > 0){
					ShortArrayList copyRem = new ShortArrayList();
					synchronized(segmentsToRemove){
						copyRem.addAll(segmentsToRemove);
						
						segmentsToRemove.clear();
					}
					for(short s : copyRem){
						usedUpMap.remove(s);
						
						LongOpenHashSet ls = segments.remove(s);
						/*
						 * it's possible that this is null, because the clean is
						 * called twice. Once on client unload, and once on
						 * server unload. Will not cause additional lag 
						 */
						if(ls != null){
							int rem = 0;
							//remove all distance records for segments of this entity
							for(long l : ls){
								long s4 = ElementCollection.getIndex4(l, s);
								float remove = distances.remove(s4);
								if(remove != 0.0000001f){
									rem++;
								}
							}
//							assert(false):"unregistering id: "+s+": "+ls.size()+"::: Removed: "+rem;
						}
						synchronized(removedSegs){
							removedSegs.add(s);
						}
					}
					
				}
				if(distances.size() < 100000 || waitToSort > 10){
					updateDistances(distances);
				}
				
				long[] toSort;
				//select the array that is currently not in use
				if(queue == null || queue.queue == tempListB){
					
					int ln = tempListA.length;
					while(ln < distances.size()){
						ln *= 2;
					}
					if(ln > tempListA.length){
						tempListA = new long[ln];
					}
					toSort = tempListA;
				}else{
					int ln = tempListB.length;
					while(ln < distances.size()){
						ln *= 2;
					}
					if(ln > tempListB.length){
						tempListB = new long[ln];
					}
					toSort = tempListB;
				}
				int i = 0;
				for(long l : distances.keySet()){
					toSort[i++] = l;
				}
				//sort array by distance
				if(i < 100000 || waitToSort > 10){
					waitToSort = 0;
					LongArrays.quickSort(toSort, 0, i, comp);
				}
				
				ArrayQueue q = new ArrayQueue();
				q.queue = toSort;
				q.queuePointer = 0;
				q.queueSize = i;
				synchronized(queueSwitch){
					if(q.queueSize > 1000){
						System.err.println("[SEGMENTMANAGER] QUEUE SIZE CURRENTLY "+q.queueSize);
					}
					queue = q;
				}
				
				if(!highPrioFullfilled.isEmpty()){
					//remove fullfilled high prio requests if they havent been called for a while
					long time = System.currentTimeMillis();
					synchronized(highPrio){
						ObjectIterator<it.unimi.dsi.fastutil.longs.Long2LongMap.Entry> iterator = highPrioFullfilled.long2LongEntrySet().iterator();
						while(iterator.hasNext()){
							it.unimi.dsi.fastutil.longs.Long2LongMap.Entry e = iterator.next();
							long last = e.getLongValue();
							if(time - last > 20000){
								iterator.remove();
							}
						}
					}
				}
				if(!highPrio.isEmpty()){
					//remove fullfilled high prio requests if they havent been called for a while
					long time = System.currentTimeMillis();
					synchronized(highPrio){
						ObjectIterator<it.unimi.dsi.fastutil.longs.Long2LongMap.Entry> iterator = highPrio.long2LongEntrySet().iterator();
						while(iterator.hasNext()){
							it.unimi.dsi.fastutil.longs.Long2LongMap.Entry e = iterator.next();
							long last = e.getLongValue();
							if(time - last > 60000){
								iterator.remove();
							}
						}
					}
				}
				
				try {
					Thread.sleep(1000);
					waitToSort++;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
//				System.err.println("QUEUE_SET: distances "+distances.size()+"; segments "+segments.size()+"; poses: "+inversePositions.size());
			}
		}
		private final Vector3f posTmp = new Vector3f();
		private final LongOpenHashSet highPrioCpy = new LongOpenHashSet(); 
		private void updateDistances(Long2FloatOpenHashMap d) {
			inversePositionsCpy.clear();
			highPrioFullFiledLcl.clear();
			synchronized(inversePositions){
				inversePositionsCpy.putAll(inversePositions);
			}
			highPrioCpy.clear();
			synchronized(highPrio){
				highPrioCpy.addAll(highPrio.values());
			}
			for(long l : d.keySet()){
				if(highPrioCpy.contains(l)){
					//high prio -> closest distance
					d.put(l, 0f);
					highPrioFullFiledLcl.add(l);
				}else{
					Vector3f inv = inversePositionsCpy.get((short)ElementCollection.getType(l));
					if(inv != null){
						d.put(l, Vector3fTools.diffLengthSquared(inv, ElementCollection.getPosFromIndex(l, posTmp)));
					}else{
						d.put(l, 10000000f);
					}
				}
			}
			long time = System.currentTimeMillis();
			synchronized(highPrio){
				for(long l : highPrioFullFiledLcl){
					highPrioFullfilled.put(l, time);
				}
				for(long l : highPrioFullfilled.keySet()){
					highPrio.remove(l);
				}
			}
		}
		
		private final List<Vector3f> posPool = new ObjectArrayList<Vector3f>();
		/**
		 * updates all relative positions for entities
		 * @param c
		 */
		private void updateTransformationBuffer(CreatorThreadController c) {
			assert(state.isSynched());
			tmpCpy.clear();
			c.register.getCopy(tmpCpy);
			
			synchronized(inversePositions){
				ObjectCollection<Vector3f> st = inversePositions.values();
				posPool.addAll(st);
				inversePositions.clear();
				
				for(ClientSegmentProvider p : tmpCpy.values()){
					Vector3f pos = inversePositions.get(p.registerId);
					
					if(pos == null){
						Vector3f nPos;
						if(posPool.isEmpty()){
							nPos = new Vector3f();
						}else{
							nPos = posPool.remove(posPool.size()-1);
						}
						nPos.set(p.getRelativePlayerPosition());
						inversePositions.put(p.registerId, nPos);
					}else{
						pos.set(p.getRelativePlayerPosition());
					}
				}
			}
		}
		private final DistLongComp comp = new DistLongComp();
		
		private class DistLongComp implements LongComparator{
			@Override
			public int compare(Long o1, Long o2) {
				throw new RuntimeException("do not use this method because it's slow");
			}

			@Override
			public int compare(long a, long b) {
				return Float.compare(distances.get(a), distances.get(b));
			}
			
		}
		
		
		//retrieve a chunk of the sorted queue
		public void getQueued(LongArrayList out, int amount){
			ArrayQueue q = queue;
			if(q != null){
				int qP = q.queuePointer;
				int qS = q.queueSize;
				
				int c = 0;
				for(int i = q.queuePointer; i < q.queueSize && c < amount; i++){
					out.add(q.queue[i]);
					currentUsedUpList.add(q.queue[i]);
					c++;
				}
				q.queuePointer+=c;
//				if(q.queueSize > 0){
//					System.err.println("QUEUE: "+qP+" / "+q.queueSize+" -> "+q.queuePointer+"; Done: "+c);
//				}
			}
		}


		
	}
	private class ArrayQueue{
		public long[] queue;
		public int queuePointer = 0;
		public int queueSize = 0;
	}
	public void flagAlreadyReceived(ClientSegmentProvider p, long index) {
		long index4 = ElementCollection.getIndex4(index, p.registerId);
		man.currentUsedUpList.add(index4);
	}
	public void registerSegment(ClientSegmentProvider clientSegmentProvider, LongCollection segments) {
		man.registerSegment(clientSegmentProvider, segments);
	}
	public void onUnregister(ClientSegmentProvider c, short registerId) {
		man.segmentsToRemove.add(registerId);
	}
	public void getQueued(LongArrayList out, int amount){
		man.getQueued(out, amount);
	}
	public boolean isShutdown() {
		return shutdown;
	}


	public void setShutdown(boolean shutdown) {
		this.shutdown = shutdown;
	}
	public void requestPriorizied(ClientSegmentProvider p, RemoteSegment requested) {
		throw new RuntimeException("Prio Request not possible");
	}
	public void requestPriorizied(SegmentProvider segmentProvider, long index) {
		
		long bid = ElementCollection.getIndex4(index, ((ClientSegmentProvider)segmentProvider).registerId);
		synchronized(highPrio){
			if(!highPrioFullfilled.containsKey(bid) && !highPrio.containsKey(bid)){
				highPrio.put(bid, System.currentTimeMillis());
			}
		}
		
	}
	/**
	 * clears the buffers so that item can be re-requested in necessary
	 * @param clientSegmentProvider
	 */
	public void onClearSegmentBuffer(ClientSegmentProvider clientSegmentProvider) {
		onUnregister(clientSegmentProvider, clientSegmentProvider.registerId);
	}
	
}
