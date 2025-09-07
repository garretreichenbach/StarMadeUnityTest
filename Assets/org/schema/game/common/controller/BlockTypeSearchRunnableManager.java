package org.schema.game.common.controller;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import org.schema.schine.graphicsengine.core.Timer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class BlockTypeSearchRunnableManager {

	private final SegmentController c;
	
	private final List<BlockTypeSearchRunnable> running = new ObjectArrayList<BlockTypeSearchRunnable>();
	public static final int THREADS = 5;
	private static final ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(THREADS, r -> new Thread(r, "BlockTypeSearchThread"));
	static {
		//preheat
		for(int i = 0; i < THREADS; i++) {
			threadPool.execute(() -> {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
		}
	}
	public BlockTypeSearchRunnableManager(SegmentController c) {
		super();
		this.c = c;
	}

	public interface BlockTypeSearchProgressCallback{
		public void onDone();
	}
	public BlockTypeSearchRunnable searchByBlock(BlockTypeSearchProgressCallback p, short ... types){
		BlockTypeSearchRunnable r = new BlockTypeSearchRunnable(c, p, new BlockTypeSearchMeshCreator(c), false, types);
		
		running.add(r);
		
		threadPool.execute(r);
		
		return r;
	}
	
	
	public void update(Timer timer){
		if(!running.isEmpty()){
			for(int i = 0; i < running.size(); i++){
				BlockTypeSearchRunnable r = running.get(i);
				if(r.isDone()){
					r.executeSynchAfterDone();
					r.cleanUpAfterDone();
					running.remove(i);
					i--;
				}
			}
		}
	}
}
