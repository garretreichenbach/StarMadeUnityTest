package org.schema.game.server.data.simulation.npc.geo;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.schema.common.util.LogInterface;
import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.InventoryMap;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.game.common.data.world.SystemRange;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.data.blueprintnw.BlueprintClassification;
import org.schema.game.server.data.blueprintnw.BlueprintEntry;
import org.schema.game.server.data.simulation.npc.geo.NPCEntityContingent.NPCEntitySpecification;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class NPCEntityContainer implements LogInterface{
	private static final byte CC_VERSION = 0;
	private final Object2ObjectOpenHashMap<String, List<NPCEntity>> counts = new Object2ObjectOpenHashMap<String, List<NPCEntity>>();
	private final Long2ObjectOpenHashMap<NPCEntity> entities = new Long2ObjectOpenHashMap<NPCEntity>();
	private final NPCEntityContingent contingent;
	private Object2IntOpenHashMap<BlueprintClassification> byClassification = new Object2IntOpenHashMap<BlueprintClassification>();
	private Object2IntOpenHashMap<EntityType> byType = new Object2IntOpenHashMap<EntityType>();
	private final List<NPCEntity> lostEntities = new ObjectArrayList<NPCEntity>();
	private final ElementCountMap lostResServer = new ElementCountMap();
	
	
	public NPCEntity getSpanwed(long dbId){
		return entities.get(dbId);
	}
	public Collection<NPCEntity> getSpanwed(){
		return entities.values();
	}
	public class NPCEntity{
		private static final byte STAG_VERSION = 0;
		long entityId = Long.MIN_VALUE;
		long fleetId = Long.MIN_VALUE;
		private final String bbName;
		public BlueprintClassification c;
		public NPCEntity(String name) {
			this.bbName = name;
		}
		private Tag toTag(){
			return new Tag(Type.STRUCT, null,
					new Tag[]{
					new Tag(Type.BYTE, null, STAG_VERSION),	
					new Tag(Type.LONG, null, entityId),	
					new Tag(Type.LONG, null, fleetId),	
					new Tag(Type.BYTE, null, (byte)c.ordinal()),	
					FinishTag.INST}
					);
		}
		private void fromTag(Tag tag){
			Tag[] t = tag.getStruct();
			byte version = t[0].getByte();
			entityId = t[1].getLong();
			fleetId = t[2].getLong();
			c = BlueprintClassification.values()[t[3].getByte()];
		}
		@Override
		public String toString() {
			return "NPCEntity [sys= "+contingent.system.system+" c=" + c.name() + ", bbName=" + bbName + ", entityId="
					+ entityId + ", fleetId=" + fleetId + "]";
		}
		public SegmentController getLoaded(NPCSystem system) {
			Sendable sendable = system.state.getLocalAndRemoteObjectContainer().getDbObjects().get(entityId);
			
			if(sendable != null && sendable instanceof SegmentController){
				return ((SegmentController)sendable);
			}
			return null;
		}
		
		
	}
	public NPCEntityContainer(NPCEntityContingent contingent) {
		this.contingent = contingent;
	}
	
	
	private static Tag[] getTag(Collection<NPCEntity> entities){
		Tag[] t = new Tag[entities.size()+1];
		t[t.length-1] = FinishTag.INST;
		int i = 0;
		for(NPCEntity npcShip : entities){
			t[i] = new Tag(Type.STRUCT, null,
					new Tag[]{
					new Tag(Type.STRING, null, npcShip.bbName),	
					npcShip.toTag(),	
					FinishTag.INST
			}
			);
			i++;
		}
		return t;
	}
	
	public Tag toTag(){
		
		
		
		return new Tag(Type.STRUCT, null,
				new Tag[]{
				new Tag(Type.BYTE, null, CC_VERSION),	
				new Tag(Type.STRUCT, null, getTag(entities.values())),	
				new Tag(Type.STRUCT, null, getTag(lostEntities)),
				FinishTag.INST}
				);
	}
	public void fromTag(Tag tag){
		clear();
		Tag[] t = (Tag[])tag.getValue();
		byte version = t[0].getByte();
		
		{
			Tag[] ec = t[1].getStruct();
			
			for(int i = 0; i < ec.length-1; i++){
				Tag[] pair = ec[i].getStruct();
				
				String name = pair[0].getString();
				NPCEntity ent = new NPCEntity(name);
				ent.fromTag(pair[1]);
				
				addEnt(ent);
			}
		}
		{
			Tag[] ec = t[2].getStruct();
			for(int i = 0; i < ec.length-1; i++){
				Tag[] pair = ec[i].getStruct();
				
				String name = pair[0].getString();
				NPCEntity ent = new NPCEntity(name);
				ent.fromTag(pair[1]);
				
				addToLost(ent, false, false);
			}
		}
	}
	
	@Override
	public String toString(){
		StringBuffer b = new StringBuffer();
		
		for(NPCEntity e : entities.values()){
			b.append(e+"\n");
		}
		
		return b.toString();
	}
	public Object2ObjectOpenHashMap<String, List<NPCEntity>> getCounts() {
		return counts;
	}
	public int getSpanwed(String bbName) {
		List<NPCEntity> list = counts.get(bbName);
		return list == null ? 0 : list.size();
	}
	public boolean remove(long dbId, SegmentController entity, boolean lost) {
		NPCEntity ent = entities.get(dbId);
		
		if(ent != null){
			if(ent.c.type == EntityType.SPACE_STATION && entity != null && contingent.system instanceof NPCSystem){
				
				NPCSystem sys = (NPCSystem)contingent.system;
				
				sys.getState().getFactionManager()
				.getNpcFactionNews().lostStation(sys.getFactionId(), entity.getRealName());
				
			}
			boolean removeEnt = removeEnt(ent);
			if(removeEnt){
				if(lost){
					ElementCountMap cargo = null;
					if(entity != null && entity instanceof ManagedSegmentController<?>){
						ManagerContainer<?> man = ((ManagedSegmentController<?>)entity).getManagerContainer();
						InventoryMap inventories = man.getInventories();
						
						for(Inventory inv : inventories.inventoriesList){
							if(cargo == null){
								cargo = new ElementCountMap();
							}
							inv.addTo(cargo);
						}
						
					}
					addToLost(ent, true, true);
					
					
					((NPCSystem)contingent.system).lostEntity(ent.c, ent.bbName, cargo, dbId, entity);
				}
			}
			
			assert(removeEnt);
			return removeEnt;
		}else{
			log("Entity to remove not found: "+dbId, LogLevel.ERROR);
			return false;
		}
	}
	public void loseRandomResources() {
		
		List<BlueprintEntry> readBluePrints = ((NPCSystem)contingent.system).getFaction().getConfig().getPreset().blueprintController.readBluePrints();
		
		Random r = new Random();
		BlueprintEntry blueprint = readBluePrints.get(r.nextInt(readBluePrints.size()));
		ElementCountMap bbCounts = blueprint.getElementCountMapWithChilds();
		lostResServer.add(bbCounts);
		calcStatus(true, true);
	}
	private void addToLost(NPCEntity ent, boolean verbose, boolean onStatusChanged) {
		lostEntities.add(ent);
		
		if(contingent.system instanceof NPCSystem){
			BlueprintEntry blueprint;
			try {
				
				blueprint = ((NPCSystem)contingent.system).getFaction().getConfig().getPreset().blueprintController.getBlueprint(ent.bbName);
				ElementCountMap bbCounts = blueprint.getElementCountMapWithChilds();
				
				if(verbose){
					log("Lost entity ("+ent.c.name()+") BBType: "+ent.bbName+"; Ship: "+bbCounts.getTotalAmount(), LogLevel.NORMAL);
				}
				lostResServer.add(bbCounts);
				
			
				calcStatus(verbose, onStatusChanged);
				
				
				((NPCSystem)contingent.system).setChangedNT();
			} catch (EntityNotFountException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	private void calcStatus(boolean verbose, boolean onStatusChanged){
		double lost = lostResServer.getTotalAmount();
		double value = contingent.contingentValueServer.getTotalAmount();
		double statBef = contingent.system.status;
		contingent.system.status =   Math.max(0, 1.0-lost/value);
		
		if(onStatusChanged && statBef != contingent.system.status){
			((NPCSystem)contingent.system).onStatusChanged(statBef);
		}
		if(verbose){
			log("SystemStatus "+contingent.system.status+"; Lost: "+lost+"; Value: "+value, LogLevel.NORMAL);
		}
	}
	@Override
	public void log(String m, LogLevel lvl) {
		contingent.log(m, lvl);
	}
	public void spawn(NPCEntitySpecification spec, long dbId) {
		
		NPCEntity ent = new NPCEntity(spec.bbName);
		ent.entityId = dbId;
		ent.c = spec.c;
		
		addEnt(ent);
	}
	private boolean removeEnt(NPCEntity ent){
		List<NPCEntity> list = counts.get(ent.bbName);
		if(list == null){
			return false;
		}
		for(int i = 0; i < list.size(); i++){
			if(list.get(i).entityId == ent.entityId){
				list.remove(i);
				break;
			}
		}
		if(list.isEmpty()){
			counts.remove(ent.bbName);
		}
		if(contingent.system instanceof NPCSystem){
			((NPCSystem)contingent.system).structure.spawnedEntitiesPerSystem.remove(ent.entityId);
		}
		entities.remove(ent.entityId);
		byClassification.addTo(ent.c, -1);
		byType.addTo(ent.c.type, -1);
		return true;
	}
	public void addEnt(NPCEntity ent){
		if(!entities.containsKey(ent.entityId)){
		
			List<NPCEntity> list = counts.get(ent.bbName);
			if(list == null){
				list = new ObjectArrayList();
				counts.put(ent.bbName, list);
			}
			list.add(ent);
			
			entities.put(ent.entityId, ent);
			
			if(contingent.system instanceof NPCSystem){
				((NPCSystem)contingent.system).structure.spawnedEntitiesPerSystem.put(ent.entityId, contingent.system.system);
			}
			
			byClassification.addTo(ent.c, 1);
			byType.addTo(ent.c.type, 1);
			
			log("Spanwed in contingent: "+ent, LogLevel.NORMAL);
		}else{
			log("ERROR: Tried to add entity to spawned contingent twice: "+ent, LogLevel.ERROR);
		}
	}
	public int getSpawnedCountByClassification(BlueprintClassification t){
		return byClassification.getInt(t);
	}
	public int getSpawnedCountByType(EntityType t){
		return byType.getInt(t);
	}
	public void deserialize(DataInput b) throws IOException {
		//serialization is done on Tag
		
//		clear();
//		final int size = b.readInt();
//		
//		for(int i = 0; i < size; i++){
//			String name = b.readUTF();
//			
//			NPCEntity ent = new NPCEntity(name);
//			ent.fromTag(Tag.deserializeNT(b));
//			
//			addEnt(ent);
//		}
//		
//		final int lostsize = b.readInt();
//		
//		for(int i = 0; i < lostsize; i++){
//			String name = b.readUTF();
//			
//			NPCEntity ent = new NPCEntity(name);
//			ent.fromTag(Tag.deserializeNT(b));
//			addToLost(ent, false);
//		}
		
	}
	public void serialize(DataOutput b) throws IOException {
		//serialization is done on Tag
		
		
//		b.writeInt(entities.size());
//		
//		for(NPCEntity e : entities.values()){
//			b.writeUTF(e.bbName);
//			e.toTag().serializeNT(b);
//		}
//		
//		b.writeInt(getLostEntities().size());
//		
//		for(NPCEntity e : getLostEntities()){
//			b.writeUTF(e.bbName);
//			e.toTag().serializeNT(b);
//		}
		
	}
	public void clear() {
		if(contingent.system instanceof NPCSystem){
			for(NPCEntity ent : entities.values()){
				((NPCSystem)contingent.system).structure.spawnedEntitiesPerSystem.remove(ent.entityId);
			}
		}
		
		counts.clear();
		entities.clear();
		byClassification.clear();
		byType.clear();
		
		lostResServer.resetAll();
		lostEntities.clear();
	}
	public List<NPCEntity> getLostEntities() {
		return lostEntities;
	}
	public void onRemovedCleanUp(NPCSystem system) {
		for(NPCEntity e : getSpanwed()){
			SegmentController loaded = e.getLoaded(system);
			if(loaded != null){
				//delete when unloaded
				loaded.railController.markForPermanentDelete(true);
			}else{
				try {
					system.state.getDatabaseIndex().getTableManager().getEntityTable().removeSegmentController(e.entityId);
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			
		}
	}
	public void resupply(NPCSystem system, long time) {
		Inventory inventory = system.getFaction().getInventory();
		if(lostResServer.getExistingTypeCount() > 0){
			IntOpenHashSet set = new IntOpenHashSet();
			double statbef = system.status;
			for(short type : ElementKeyMap.typeList()){
				int needed = lostResServer.get(type);
				if(needed > 0){
					int available = inventory.getOverallQuantity(type);
					if(available >= needed){
						set.add(inventory.incExistingOrNextFreeSlotWithoutException(type, -needed));
						lostResServer.reset(type);
					}else{
						set.add(inventory.incExistingOrNextFreeSlotWithoutException(type, -available));
						lostResServer.inc(type, -available);
					}
				}
			}
			system.getFaction().setChangedNT();
			if(set.size() > 0){
				inventory.sendAllWithExtraSlots(set);
			}
			calcStatus(true, false);
			
			
			log("Resupplied status: "+statbef+" -> "+system.status, LogLevel.DEBUG);
			if(system.status > system.getFaction().getConfig().abandonSystemOnStatus && 
					system.status < system.getFaction().getConfig().abandonSystemOnStatusAfterResupply
					){
				log("System status not sufficient after resupply: "+system.status+" / "+system.getFaction().getConfig().abandonSystemOnStatusAfterResupply+"; Abandoning", LogLevel.NORMAL);
				system.abandon();
			}else if(lostResServer.getExistingTypeCount() == 0){
				log("Lost resources replenished completely. Resetting all structures to full status (Deleting stations and resetting sectors)", LogLevel.NORMAL);
				//completely resupplied
				SystemRange r = SystemRange.get(system.system);
				system.getState().getDatabaseIndex().getTableManager().getEntityTable().resetSectorsAndDeleteStations(r, system.getFactionId(), system, system.getFaction().getTradeNode().getEntityDBId());
			}
		}
		
	}
	
		
}
