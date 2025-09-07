package org.schema.game.server.data.simulation.npc;

import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.server.data.GameServerState;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class NPCFactionMiningProcessor {
	public final NPCFaction faction;
	public final GameServerState state;
	public NPCFactionMiningProcessor(NPCFaction faction, GameServerState state) {
		super();
		this.faction = faction;
		this.state = state;
	}
	
	
	public void processMining(double multiplicator){
		IntOpenHashSet inventoryMod = new IntOpenHashSet();
		Inventory inventory = faction.getInventory();
		for(int i = 0; i < ElementKeyMap.resources.length; i++){
			short type = ElementKeyMap.resources[i];
			if(!ElementKeyMap.getInfo(type).deprecated){
				int count = (int) Math.max(0, faction.structure.totalResources[i]*multiplicator);
				int slot = inventory.incExistingOrNextFreeSlot(type, count);
				inventoryMod.add(slot);
			}
		}
		inventory.sendInventoryModification(inventoryMod);
		
	}
	public void processProduction(double multiplicator){
		
	}
	
}
