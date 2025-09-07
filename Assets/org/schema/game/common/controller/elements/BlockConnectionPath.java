package org.schema.game.common.controller.elements;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class BlockConnectionPath {
	private final Long2LongOpenHashMap backing = new Long2LongOpenHashMap();
	private boolean changed = true;
	private final LongArrayList startPoints = new LongArrayList();
	private final ObjectArrayList<LongArrayList> paths = new ObjectArrayList<LongArrayList> ();
	private final ManagerContainer<?> man;
	private final LongOpenHashSet blocks = new LongOpenHashSet();
	
	public BlockConnectionPath(ManagerContainer<?> managerContainer) {
		this.man = managerContainer;
	}
	public void remove(long a){
		changed = true;
		backing.remove(a);
		this.man.getPowerInterface().flagStabilizerPathCalc();
	}
	
	public void put(long a, long b){
		changed = true;
		
		backing.put(a, b);
		this.man.getPowerInterface().flagStabilizerPathCalc();
	}
	
	public boolean containsKey(long l) {
		return backing.containsKey(l);
	}
	
	public long get(long l) {
		return backing.get(l);
	}
	
	public void recalc() {
		if(changed){
			//get Start Points
			this.startPoints.clear();
			paths.clear();
			LongSet keySet = backing.keySet();
			LongOpenHashSet singles = new LongOpenHashSet(blocks);
			LongOpenHashSet vals = new LongOpenHashSet(backing.values());
			for(long key : keySet){
				singles.remove(key);
				singles.remove(backing.get(key));
				if(!vals.contains(key)){
					//if no blocks connects to this key
					startPoints.add(key);
				}
			}
			startPoints.addAll(singles);
			//check for loops
			
			for(int i = 0; i < startPoints.size(); i++){
				vals.clear();
				LongArrayList path = new LongArrayList();
				long start = startPoints.getLong(i);
				path.add(start);
				while(backing.containsKey(path.getLong(path.size()-1))){
					
					long next = backing.get(path.getLong(path.size()-1));
					
					if(vals.contains(next)){
						break;
					}else{
						vals.add(next);
						path.add(next);
					}
				}
				paths.add(path);
			}
			
			
			changed = false;
		}
	}
	public ObjectArrayList<LongArrayList> getPaths() {
		recalc();
		return paths;
	}
	public void removeBlock(long absIndex) {
		changed = true;
		//single block unconnected
		blocks.remove(absIndex);
	
		this.man.getPowerInterface().flagStabilizerPathCalc();
	}
	public void addBlock(long absIndex) {
		changed = true;
		//single block unconnected
		blocks.add(absIndex);
	
		this.man.getPowerInterface().flagStabilizerPathCalc();
	}
}
