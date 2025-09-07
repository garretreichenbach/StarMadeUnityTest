package org.schema.game.common.data.player.inventory;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;


public class InventoryChangeMap extends Object2ObjectOpenHashMap<Inventory, IntOpenHashSet>{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	

	public IntOpenHashSet getInv(Inventory inventory){
		IntOpenHashSet int2IntOpenHashMap = get(inventory);
		if(int2IntOpenHashMap == null){
			int2IntOpenHashMap = new IntOpenHashSet();
			put(inventory, int2IntOpenHashMap);
		}
		
		return int2IntOpenHashMap;
	}
	
	public void sendAll(){
		for(java.util.Map.Entry<Inventory, IntOpenHashSet> a : entrySet()){
			a.getKey().sendInventoryModification(a.getValue());
		}
	}
}
