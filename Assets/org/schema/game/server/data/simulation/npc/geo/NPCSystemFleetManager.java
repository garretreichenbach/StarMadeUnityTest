package org.schema.game.server.data.simulation.npc.geo;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import org.schema.common.util.LogInterface;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.EditableSendableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.data.element.Element;
import org.schema.game.common.data.fleet.Fleet;
import org.schema.game.common.data.fleet.FleetCommandTypes;
import org.schema.game.common.data.fleet.FleetManager;
import org.schema.game.common.data.fleet.FleetMember;
import org.schema.game.common.data.fleet.missions.machines.states.FleetState;
import org.schema.game.common.data.fleet.missions.machines.states.FleetState.FleetStateType;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SystemRange;
import org.schema.game.network.objects.remote.FleetCommand;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.blueprintnw.BlueprintClassification;
import org.schema.game.server.data.simulation.npc.NPCFaction;
import org.schema.game.server.data.simulation.npc.NPCFactionConfig;
import org.schema.game.server.data.simulation.npc.NPCStats;
import org.schema.game.server.data.simulation.npc.geo.NPCEntityContingent.NPCEntitySpecification;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.language.Translatable;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import java.util.*;

public class NPCSystemFleetManager implements LogInterface{

	public static final String IDPREFIX_FLEET = "NPCFLT#";
	public static final String IDPREFIX_OWNER = "NPC#";
	private final NPCSystem system;
	
	private final NPCSystemStats stats = new NPCSystemStats(); 
	private class NPCSystemStats extends NPCStats{
		public void incShipNumber() {
			super.incShipNumber((GameServerState)getFaction().getState(), getFaction().getIdFaction(), system.system);
		}

		public void incFleetNumber() {
			super.incFleetNumber((GameServerState)getFaction().getState(), getFaction().getIdFaction(), system.system);
		}

		public int getFleetCreatorNumber() {
			return super.getFleetCreatorNumber((GameServerState)getFaction().getState(), getFaction().getIdFaction(), system.system);
		}

		public int getShipCreatorNumber() {
			return super.getShipCreatorNumber((GameServerState)getFaction().getState(), getFaction().getIdFaction(), system.system);
		}
	}
	private boolean loaded;
	
	private final FleetContainer[] fleets = new FleetContainer[FleetType.values().length];  
	
	public NPCSystemFleetManager(NPCSystem system) {
		super();
		this.system = system;
		for(int i = 0; i < FleetType.values().length; i++){
			fleets[i] = new FleetContainer(FleetType.values()[i]);
		}
		
		
	}
	public String getOwnerName(){
		return IDPREFIX_OWNER+system.structure.faction.getIdFaction()+"_"+system.system.x+"_"+system.system.y+"_"+system.system.z;
	}
	public FleetContainer getFleetContainer(FleetType t){
		return fleets[t.ordinal()];
	}
	
	
	public void onAciveSystem(){
		load();
		
		spawnFleets(FleetType.DEFENDING);
		spawnFleets(FleetType.MINING);
		assignMissions();
	}
	public void onInactiveSystem(){
		unload();
	}

	private void assignPatrolMissions() {
		if(!system.isActive()){
			return;
		}
		FleetContainer c = getFleetContainer(FleetType.DEFENDING);
		ShortArrayList td = new ShortArrayList(system.stationMap.keySet());
		
		if(!td.isEmpty()){
			Random r = new Random();
			for(Fleet fl : c.available){
				Vector3i target = new Vector3i();
				system.getPosSectorFromLocalCoordinate(td.get(r.nextInt(td.size())), target);
				FleetCommand com = new FleetCommand(FleetCommandTypes.PATROL_FLEET, fl, target);
				getFleetManager().executeCommand(com);
			}
			c.recalcSelf();
		}
		log("FLEET PATROL MISSIONS ASSIGNED: "+c, LogLevel.NORMAL);
	}
	private void assignMiningMissions() {
		if(!system.isActive()){
			return;
		}
		FleetContainer c = getFleetContainer(FleetType.MINING);
		Random r = new Random();
		Set<Vector3i> sentTo = new ObjectOpenHashSet<Vector3i>(); 
		
		for(Fleet fl : c.available){
			boolean sent = false;
			for(PlayerState s : system.getState().getPlayerStatesByName().values()){
				if(!sentTo.contains(s.getCurrentSector()) && s.getCurrentSystem().equals(system.system)){
					Vector3i target = new Vector3i(s.getCurrentSector());
					Vector3i dir = new Vector3i(Element.DIRECTIONSi[r.nextInt(Element.DIRECTIONSi.length)]);
					target.add(dir);
					dir = new Vector3i(Element.DIRECTIONSi[r.nextInt(Element.DIRECTIONSi.length)]);
					target.add(dir);
					
					SystemRange rng = SystemRange.get(system.system);
					
					target.min(rng.end.x, rng.end.y, rng.end.z);
					target.max(rng.start.x, rng.start.y, rng.start.z);
					
					FleetCommand com = new FleetCommand(FleetCommandTypes.MOVE_FLEET, fl, target);
					getFleetManager().executeCommand(com);
					sentTo.add(s.getCurrentSector());
					sent = true;
				}
			}
			if(!sent){
				SystemRange rng = SystemRange.get(system.system);
				
				Vector3i target = new Vector3i(rng.start);
				target.add(1, 1, 1);
				target.add(r.nextInt(14), r.nextInt(14), r.nextInt(14));
				
				
				FleetCommand com = new FleetCommand(FleetCommandTypes.MOVE_FLEET, fl, target);
				getFleetManager().executeCommand(com);
			}
			
		}
		c.recalcSelf();
		log("FLEET MINING MISSIONS ASSIGNED: "+c, LogLevel.NORMAL);
	}
	public void onCommandPartFinished(Fleet fleet, FleetState currentState) {
		FleetType npcType = fleet.getNpcType();
		FleetContainer c = getFleetContainer(npcType);
		switch(fleet.getNpcType()){
		case ATTACKING:
			break;
		case DEFENDING:
			comPatrol(fleet, c, currentState);
			break;
		case MINING:
			comMining(fleet, c, currentState);
			break;
		case SCAVENGE:
			break;
		case TRADING:
			break;
		default:
			break;
		
		}
		
		
		
	}
	private void comMining(Fleet fleet, FleetContainer c, FleetState currentState) {
		
		for(Fleet f : c.all){
			if(f.equals(fleet) && !fleet.isEmpty()){
				c.recalcSelf();

				if(currentState.getType() != FleetStateType.MINING && system.getState().getUniverse().isSectorLoaded(fleet.getFlagShip().getSector())){
					FleetCommand com = new FleetCommand(FleetCommandTypes.MINE_IN_SECTOR, fleet);
					getFleetManager().executeCommand(com);
					log("Mining Fleet finished mining in sector. "+fleet+" now mining", LogLevel.NORMAL);
				}else{
					log("Mining Fleet finished mining in sector. Sector unloaded. move again... ", LogLevel.NORMAL);
					assignMiningMissions();
				}
				
				
				return;
			}
		}
		log("ERROR: Fleet not found "+fleet+" in ["+fleet.getNpcType().name()+"]", LogLevel.ERROR);
	}
	private void comPatrol(Fleet fleet, FleetContainer c, FleetState currentState){
		
		
		
		for(Fleet f : c.all){
			if(f.equals(fleet)){
				c.recalcSelf();
				assignPatrolMissions();
				log("Fleet finished patrol part. Reassigning (fleet: "+fleet+")", LogLevel.NORMAL);
				return;
			}
		}
		log("ERROR: Fleet not found "+fleet+" in ["+fleet.getNpcType().name()+"]", LogLevel.ERROR);
	}
	private void assignMissions() {
		assignPatrolMissions();
		assignMiningMissions();
	}

	public enum FleetType{
		
		ATTACKING(en -> {
			return Lng.str("Attacking");
		}),
		DEFENDING(en -> {
			return Lng.str("Defending");
		}),
		SCAVENGE(en -> {
			return Lng.str("Scavenging");
		}),
		MINING(en -> {
			return Lng.str("Mining");
		}),
		TRADING(en -> {
			return Lng.str("Trading");
		});
		
		private final Translatable description;
		private FleetType(Translatable description){
			this.description = description;
		}

		public String getDescription() {
			return description.getName(this);
		}
	}
	public BlueprintClassification[] getClasses(FleetType type){
		return getFaction().getConfig().getFleetClasses(type);
	}
	
	private class FleetContainer{
		private List<Fleet> onMission = new ObjectArrayList<Fleet>();
		private List<Fleet> available = new ObjectArrayList<Fleet>();
		private List<Fleet> filled = new ObjectArrayList<Fleet>();
		private List<Fleet> empty = new ObjectArrayList<Fleet>();
		private List<Fleet> all = new ObjectArrayList<Fleet>();
		private final FleetType type;
		public FleetContainer(FleetType type) {
			super();
			this.type = type;
		}
		
		public void reset(){
			onMission.clear();
			available.clear();
			filled.clear();
			empty.clear();
			all.clear();
		}
		
		public void addFleetToContainer(Fleet f){
			all.add(f);
			
			if(f.getMembers().isEmpty()){
				empty.add(f);
			}else{
				filled.add(f);
				
				if(f.getMachine() != null && f.getMachine().getFsm() != null 
						&& f.getMachine().getFsm().getCurrentState() != null &&
						(f.getMachine().getFsm().getCurrentState() instanceof FleetState) &&
						(
						((FleetState)f.getMachine().getFsm().getCurrentState()).getType() != FleetStateType.IDLING && 
						((FleetState)f.getMachine().getFsm().getCurrentState()).getType() != FleetStateType.SENTRY_IDLE &&
						((FleetState)f.getMachine().getFsm().getCurrentState()).getType() != FleetStateType.FORMATION_IDLE &&
						((FleetState)f.getMachine().getFsm().getCurrentState()).getType() != FleetStateType.FORMATION_SENTRY
						)
						){
					onMission.add(f);
				}else{
					available.add(f);
				}
			}
		}
		

		@Override
		public String toString() {
			return "FleetContainer [" + type.name() + ", all=" + all.size() + ", empty="
					+ empty.size() + ", filled=" + filled.size() + ", available=" + available.size()
					+ ", onMission=" + onMission.size() + "]";
		}

		public long spawnOne(Vector3i pos){
			log("Adding a ship to ["+type.name()+"] Fleets", LogLevel.NORMAL);
			Fleet fleet = null;
			if(!empty.isEmpty()){
				fleet = empty.get(0);
			}else{
				int min = Integer.MAX_VALUE;
				for(Fleet fl : filled){
					min = Math.min(fl.getMembers().size(), min);
				}
				for(Fleet fl : filled){
					if(fl.getMembers().size() == min){
						fleet = fl;
						break;
					}
				}
			}
			if(fleet != null){
				List<NPCEntitySpecification> ships = getContingent().getShips(fleet, getClasses(type));
				
				Random r = new Random(system.seed*pos.hashCode()*fleet.getMembers().size());
				
				Collections.shuffle(ships, r);
				
				for(NPCEntitySpecification e : ships){
					if(e.hasLeft()){
						log("Adding a ship to ["+type.name()+"] Fleets with specification: "+e, LogLevel.NORMAL);
						return spawn(e, Lng.str("[System Fleet %d] %s #%d - %s %s ", fleet.dbid, fleet.getNpcType().getDescription(),  stats.getShipCreatorNumber(), e.bbName, e.c.getName()), pos);
						//return spawn(e, "[SysFleet] "+fleet.getNpcType()+" "+fleet.dbid+" Member #"+stats.getShipCreatorNumber()+" - "+e.c.getName()+" "+e.bbName, pos);
					}
				}
			}else{
				log("ERROR: NO FLEET TO SPAWN SHIP", LogLevel.ERROR);
			}
			return -1;
		}

		private long spawn(NPCEntitySpecification spec, String name, Vector3i pos) {
			String UID = "FLTSHP_"+getFaction().getIdFaction()+"_"+system.system.x+"_"+system.system.y+"_"+system.system.z+"_"+stats.getShipCreatorNumber();
			
			long spawnInDB = system.structure.spawnInDB(getState(), spec, system, pos, UID, name);
			long fleedId = -1;
			
			if(spawnInDB > 0){
				
				if(getFleetManager().isInFleet(spawnInDB)){
					log("Ship already in Database and in a fleet: "+spawnInDB+"; At "+pos+"; UID "+UID, LogLevel.NORMAL);
					return fleedId;
				}
				log("Spawned Ship in Database: "+spawnInDB+"; At "+pos+"; UID "+UID, LogLevel.NORMAL);
			}else{
				log("ERROR: Not Spawned Ship in Database: "+spawnInDB+"; At "+pos+"; UID "+UID, LogLevel.ERROR);
			}
			if(spawnInDB > 0){
				
				if(!empty.isEmpty()){
					Fleet fleet = empty.get(0);
					getFleetManager().requestShipAdd(fleet, spawnInDB);
					fleedId = fleet.dbid;
					log("Found empty fleet for ship: "+spawnInDB+"; At "+pos+"; UID "+UID+"; -> Fleet "+fleet, LogLevel.NORMAL);
				}else{
					int min = Integer.MAX_VALUE;
					for(Fleet fl : filled){
						min = Math.min(fl.getMembers().size(), min);
					}
					for(Fleet fl : filled){
						if(fl.getMembers().size() == min){
							getFleetManager().requestShipAdd(fl, spawnInDB);
							fleedId = fl.dbid;
							log("Found non-empty fleet for ship: "+spawnInDB+"; At "+pos+"; UID "+UID+"; -> Fleet "+fl, LogLevel.NORMAL);
							break;
						}
					}
				}
				if(fleedId < 0){
					log("ERROR: No fleet found for ship: "+spawnInDB+"; At "+pos+"; UID "+UID, LogLevel.ERROR);
				}
				
				recalcSelf();
				stats.incShipNumber();
			}
			return fleedId;
		}

		private void recalcSelf() {
			List<Fleet> o = new ObjectArrayList<Fleet>(all);
			reset();
			for(Fleet f : o){
				addFleetToContainer(f);
			}
		}

		public boolean checkLost(long dbId, SegmentController entity) {
			for(Fleet f : all){
				if(f.isMember(dbId)){
					f.removeMemberByDbIdUID(dbId, false);
					if(f.isEmpty()){
						system.state.getFleetManager().removeFleet(f);
					}
					recalcSelf();
					return true;
				}
			}
			return false;
		}

		public void onSystemRemoveCleanUp() {
			for(Fleet f : all){
				List<FleetMember> mems = new ObjectArrayList<FleetMember>(f.getMembers());
				for(FleetMember m : mems){
					f.removeMemberByDbIdUID(m.entityDbId, true);
				}
				
				system.state.getFleetManager().removeFleet(f);
			}
			recalcSelf();
		}

		public boolean removeFleet(Fleet f) {
			boolean had = all.remove(f);;
			if(had){
				onMission.remove(f);
				available.remove(f);
				filled.remove(f);
				empty.remove(f);
			}
			return had; 
		}

		
		
	}
	
	public Tag toTag(){
		return new Tag(Type.STRUCT, null, new Tag[]{
			new Tag(Type.BYTE, null, (byte)0),
			FinishTag.INST
		}
				);
	}
	
	public void fromTag(Tag tag) {
		Tag[] t = tag.getStruct();
		byte version = t[0].getByte();
	}
	private NPCFaction getFaction() {
		return system.structure.faction;
	}
	public boolean isType(Fleet f, FleetType type){
		return f.getName().toLowerCase(Locale.ENGLISH).startsWith(getFleetPrefix(type).toLowerCase(Locale.ENGLISH));
	}
	private String getFleetPrefix(FleetType type){
		return IDPREFIX_FLEET+type.name()+"#";
	}
	public String getName(int creationId, FleetType type ){
		return getFleetPrefix(type)+system.structure.faction.getIdFaction()+"#"+system.system.toStringPure()+"#"+creationId;
	}
	public static FleetType getTypeFromFleetName(String name){
		
		String[] split = name.split("#");
		
		if(split.length > 2){
			try{
				return FleetType.valueOf(split[1].trim());
			}catch(Exception e){
				System.err.println("Exception STR: '"+name+"'");
				e.printStackTrace();
			}
		}
		
		return null;
	}
	public static int getFactionIdFromFleetName(String name){
		
		String[] split = name.split("#");
		
		if(split.length > 3){

			try{
				return Integer.parseInt(split[2]);
			}catch(NumberFormatException e){
				System.err.println("Exception STR: '"+name+"'");
				e.printStackTrace();
			}
		}
		
		return 0;
	}
	public static Vector3i getSystemFromFleetName(String name){
		
		String[] split = name.split("#");
		
		if(split.length > 4){
			
			try{
				return Vector3i.parseVector3i(split[3]);
			}catch(NumberFormatException e){
				System.err.println("Exception STR: '"+name+"'");
				e.printStackTrace();
			}
		}
		return null;
	}
	public void spawnFleets(FleetType type){
		
		spawnFleets(type, getWantedFleetCount(type));
		
		FleetContainer fleetContainer = getFleetContainer(type);
		
		if(!fleetContainer.all.isEmpty()){
			if(fleetContainer.empty.size() > 0){
				log("Spawning members for fleet of type ["+type.name()+"] ", LogLevel.NORMAL);
				fillFleets(type);
			}else{
				log("Fleets with type ["+type.name()+"] already filled. no spawning", LogLevel.NORMAL);
			}
		}else{
			log("ERROR spawning patrol fleets: NO Fleets with type ["+type.name()+"] available. no member spawning", LogLevel.ERROR);
		}
	}
	public FleetManager getFleetManager(){
		return getState().getFleetManager();
	}
	private void spawnFleets(FleetType type, int amount){
		
		load();
		
		ObjectArrayList<Fleet> beforeCheck = getState().getFleetManager().getAvailableFleets(getOwnerName());
		
		int currentCount = 0;
		for(Fleet f : beforeCheck){
			if(isType(f, type)){
				currentCount++;
			}
		}
		
		int toCreate = amount - currentCount;
		
		log("Spawning "+toCreate+" (wanted: "+getWantedFleetCount(type)+" have: "+currentCount+") Fleets for type ["+type.name()+"]", LogLevel.NORMAL);
		
		for(int i = 0; i < toCreate; i++){
			String fleetName = getName(stats.getFleetCreatorNumber(), type);
			log("Spawning ["+type.name()+"] fleet: "+fleetName, LogLevel.NORMAL);
			getState().getFleetManager().requestCreateFleet(fleetName, getOwnerName());
			stats.incFleetNumber();
		}
		recalcAll();
	}
	
	private void load() {
		if(!loaded){
			getFleetManager().loadByOwner(getOwnerName());
			loaded = true;
		}		
	}
	
	public void onUncachedFleet(Fleet f){
		getFleetContainer(f.getNpcType()).removeFleet(f);
		this.loaded = false;
		
		log("UNCACHED FLEET "+f.getName()+"; Total System Fleets "+getState().getFleetManager().getAvailableFleets(getOwnerName()).size(), LogLevel.DEBUG);
	}
	
	private void unload() {
//		if(loaded){
//			log("UNLAODING FLEETS", LogLevel.DEBUG);
//			getFleetManager().unloadByOwner(getOwnerName());
//			loaded = false;
//		}
	}
	private void recalcAll() {
		ObjectArrayList<Fleet> availableFleets = getState().getFleetManager().getAvailableFleets(getOwnerName());		
		log("Recalculating Containers. Available Fleets: "+availableFleets.size(), LogLevel.NORMAL);
		for(FleetType type : FleetType.values()){
			FleetContainer fleetContainer = getFleetContainer(type);
			fleetContainer.reset();
			for(Fleet f : availableFleets){
				if(isType(f, type)){
					fleetContainer.addFleetToContainer(f);
				}
			}
			
			log("Finished Container "+type.name()+". Available Fleets: "+fleetContainer, LogLevel.NORMAL);
		}
	}
	private void fillFleets(FleetType type) {
		FleetContainer fleetContainer = getFleetContainer(type);
		
		ShortArrayList td = new ShortArrayList(system.stationMap.keySet());
		
		Random r = new Random(system.seed * stats.getShipCreatorNumber());
		
		int size = fleetContainer.empty.size();
		
		//spawn in for empty fleets 
		for(int i = 0; i < size; i++){
			
			Vector3i posToSpawn = null;
			if(type == FleetType.DEFENDING && system.system.equals(0, 0, 0) && fleetContainer.empty.size() == fleetContainer.all.size()){
				posToSpawn = new Vector3i(5, 2, 2);
			}else{
				Collections.shuffle(td, r);
				for(short localCol : td){
					if(!system.isLocalCoordinateSectorLoadedServer(localCol)){
						posToSpawn = system.getPosSectorFromLocalCoordinate(localCol, new Vector3i());
						log("Found sector to spawn: "+posToSpawn+": spawning (ifPossible) "+size+" for ["+FleetType.DEFENDING.name()+"] fleets", LogLevel.NORMAL);
						break;
					}
				}
			}
			if(posToSpawn != null){
				if(fleetContainer.empty.size() > 0){
					long fleetId = fleetContainer.spawnOne(posToSpawn);
					if(fleetId < 0){
						break;
					}
				}
			}else{
				log("No suitable space to spawn member for ["+type.name()+"] fleets", LogLevel.ERROR);
			}
		}
		
		long spawnedInFleet = -1;
		
		do{
			/*
			 * adds a ship of contingent to each fleet of this type.
			 * if there was no ship added (returning -1) on a full loop through the fleets,
			 * we know that we filled all fleets
			 */
			spawnedInFleet = -1;
			for(Fleet f : fleetContainer.filled){
				
				Vector3i posToSpawn = new Vector3i(f.getMembers().get(0).getSector());
				long s = fleetContainer.spawnOne(posToSpawn);
				
				if(s > 0){
					spawnedInFleet = s;
				}
			}
		}while(spawnedInFleet > 0);
	}


	public NPCEntityContingent getContingent(){
		return system.getContingent();
	}
	public int getWantedPatrolFleetCount(){
		return Math.min(getConfig().maxDefendFleetCount, Math.max(getConfig().minDefendFleetCount, (int)(system.stationMap.size()*getConfig().defendFleetsPerStation)));
	}
	private int getWantedMiningFleetCount() {
		int miningShips = getContingent().getTotalAmountClass(BlueprintClassification.MINING);
		
		return Math.min(getConfig().maxMiningFleetCount, (int) (miningShips / getConfig().miningShipsPerFleet));
	}
	private int getWantedFleetCount(FleetType type) {
		
		switch(type){
		case ATTACKING:
			break;
		case DEFENDING:
			return getWantedPatrolFleetCount();
		case MINING:
			return getWantedMiningFleetCount();
		case SCAVENGE:
			break;
		case TRADING:
			break;
		default:
			break;
		
		}
		
		return 0;
	}
	
	public NPCFactionConfig getConfig() {
		return system.getConfig();
	}
	public GameServerState getState(){
		return system.structure.getState();
	}

	@Override
	public void log(String s, LogLevel lvl) {
		system.log("[FLEETS]"+s, lvl);
	}
	public void lostEntity(long dbId, SegmentController entity) {
		for(FleetContainer c : fleets){
			if(c.checkLost(dbId, entity)){
				return;
			}
		}
	}
	public void cleanUpAllFleets() {
		for(FleetContainer c : fleets){
			c.onSystemRemoveCleanUp();
		}
	}
	
	private Vector3i tmp = new Vector3i();
	private Object2LongOpenHashMap<Vector3i> attackedPos = new Object2LongOpenHashMap<Vector3i>();
	private long lastTimesCheck;
	private long lastAttackCheck;
	public void onAttacked(Fleet fleet, EditableSendableSegmentController seg,
			Damager from) {
		
		long t = System.currentTimeMillis();
		
		
		if(t-lastTimesCheck > 60000){
			
			LongIterator iterator = attackedPos.values().iterator();
			
			while(iterator.hasNext()){
				long att = iterator.nextLong();

				if(t - att > 60000*5){
					iterator.remove();
				}
			}
			
			lastTimesCheck = t;
		}
		
		if(t-lastAttackCheck > 2000){
			Vector3i sector = seg.getSector(tmp);
			
			
			
			if(sector != null && !attackedPos.containsKey(sector)){
				FleetContainer fleetContainer = getFleetContainer(FleetType.DEFENDING);
				int amount = (int) Math.ceil(fleetContainer.all.size() * getConfig().defendFleetsToSendToAttacker); 	
				
				amount = Math.max(amount, getConfig().minDefendFleetsToSendToAttacker);
				
				int sent = 0;
				for(int i = 0; i < fleetContainer.filled.size() ; i++){
					Fleet f = fleetContainer.filled.get(i);
					if(!f.isEmpty()){
						float d = Vector3i.getDisatance(f.getFlagShip().getSector(), sector);
						if(d > 3){
							
							FleetCommand com = new FleetCommand(FleetCommandTypes.PATROL_FLEET, f, new Vector3i(sector));
							getFleetManager().executeCommand(com);
							
							sent++;
						}
					}
					if(sent >= amount){
						break;
					}
				}
				attackedPos.put(new Vector3i(sector), t);
			}
			
			lastAttackCheck = t;
		}
	}
	
	

}
