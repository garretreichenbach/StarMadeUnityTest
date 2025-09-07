package org.schema.game.common.controller;

import java.util.List;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.BlockTypeSearchRunnableManager.BlockTypeSearchProgressCallback;
import org.schema.game.common.data.element.ElementCollection;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentData;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;

public class BlockTypeSearchRunnable implements Runnable, SegmentBufferIteratorInterface{

	public static interface BlockTypeSearchCallback{
		public void handleThreaded(LongList result);

		public void executeAfterDone();
	}
	
	public final ShortOpenHashSet searchFor = new ShortOpenHashSet();
	public final BlockTypeSearchCallback callback;
	public final SegmentController c;
	public List<SegmentData> datas;
	private boolean done;
	private boolean useIndex4;
	private BlockTypeSearchProgressCallback p;
	
	
	
	public BlockTypeSearchRunnable(SegmentController c, BlockTypeSearchProgressCallback p, BlockTypeSearchCallback callback, boolean useIndex4, short ... types) {
		super();
		this.c = c;
		this.callback = callback;
		this.useIndex4 = useIndex4;
		this.p = p;
		for(short s : types){
			searchFor.add(s);
		}
	}




	@Override
	public void run() {
		datas = new ObjectArrayList<SegmentData>(c.getSegmentBuffer().getTotalNonEmptySize());
		c.getSegmentBuffer().iterateOverNonEmptyElement(this, true);
		
		
		LongList result = new LongArrayList(1024);
		for(SegmentData s : datas){
			Segment segment = s.getSegment();
			if(segment != null){
				Vector3i pos = segment.pos;
				int i = 0;
				for(short z = 0; z < Segment.DIM; z++){
					for(short y = 0; y < Segment.DIM; y++){
						for(short x = 0; x < Segment.DIM; x++){
							short type = s.getType(i);
							if(searchFor.contains(type)){
								if(useIndex4){
									result.add(ElementCollection.getIndex4((short)(pos.x+x), (short)(pos.y+y), (short)(pos.z+z), type));
								}else{
									result.add(ElementCollection.getIndex(pos.x+x, pos.y+y, pos.z+z));
								}
							}
							i++;
						}
					}
				}
			}
		}
		
		System.err.println("[CLIENT] Block Type Search. Found Blocks: "+result.size());
		callback.handleThreaded(result);
		
		done = true;
		
	}




	@Override
	public boolean handle(Segment s, long lastChanged) {
		SegmentData segmentData = s.getSegmentData();
		if(segmentData != null){
			datas.add(segmentData);
		}
		return true;
	}




	public boolean isDone() {
		return done;
	}




	public void executeSynchAfterDone() {
		callback.executeAfterDone();
	}




	public void cleanUpAfterDone() {
		searchFor.clear();
		datas.clear();
		
		p.onDone();
	}




}
