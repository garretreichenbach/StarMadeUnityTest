package org.schema.game.server.data.simulation;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map.Entry;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.ShopSpaceStation;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.player.catalog.CatalogWavePermission;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.Universe;
import org.schema.game.server.ai.program.simpirates.PirateSimulationProgram;
import org.schema.game.server.ai.program.simpirates.TradingRouteSimulationProgram;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.ShipSpawnWave;
import org.schema.game.server.data.blueprintnw.BlueprintType;
import org.schema.game.server.data.simulation.groups.AttackSingleEntitySimulationGroup;
import org.schema.game.server.data.simulation.groups.RavegingSimulationGroup;
import org.schema.game.server.data.simulation.groups.SimulationGroup;
import org.schema.game.server.data.simulation.groups.SimulationGroup.GroupType;
import org.schema.game.server.data.simulation.groups.TargetSectorSimulationGroup;
import org.schema.game.server.data.simulation.jobs.SimulationJob;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.network.RegisteredClientInterface;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.resource.DiskWritable;
import org.schema.schine.resource.tag.FinishTag;
import org.schema.schine.resource.tag.Tag;
import org.schema.schine.resource.tag.Tag.Type;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

public class SimulationManager implements DiskWritable {

	private final ObjectArrayList<SimulationGroup> simulationGroups = new ObjectArrayList<SimulationGroup>();
	private final GameServerState state;
	private final SimulationPlanner planner;
	private final ObjectArrayFIFOQueue<SimulationJob> jobs = new ObjectArrayFIFOQueue<SimulationJob>();
	private long startTime = 0;
	private long runningTime = 0;
	private long lastupdateTime = 0;
	private long uniquegroups;

	public SimulationManager(GameServerState state) {
		this.state = state;
		startTime = System.currentTimeMillis();
		lastupdateTime = startTime;
		planner = new SimulationPlanner(this);
		//		assert(GroupType.check());

	}

	public void addGroup(SimulationGroup g) {
		synchronized (simulationGroups) {
			if (simulationGroups.size() < ServerConfig.CONCURRENT_SIMULATION.getInt()) {
				simulationGroups.add(g);
			} else {
				System.err.println("[SIMUALTION] WARNING: Simulation group " + g + " ignored: MAX GROUPS REACHED " + simulationGroups.size() + "/" + ServerConfig.CONCURRENT_SIMULATION.getInt());
			}
		}
	}

	public void addJob(SimulationJob job) {
		synchronized (jobs) {
			jobs.enqueue(job);
		}
	}

	public void aggressive(Ship entity, SimpleTransformableSendableObject from,
			float actualDamage) {
		for (int i = 0; i < simulationGroups.size(); i++) {
			SimulationGroup simulationGroup = simulationGroups.get(i);
			if (simulationGroup.getMembers().contains(entity.getUniqueIdentifier())) {
				simulationGroup.aggro(from, actualDamage);
			}
		}
	}

	public void createRandomPirateGroup(Vector3i from, int count) {
		if (existsGroupInSector(from)) {
			System.err.println("[SIM] cannot spawn group: collision at " + from);
			return;
		}
		synchronized (simulationGroups) {
			if (simulationGroups.size() >= ServerConfig.CONCURRENT_SIMULATION.getInt()) {
				System.err.println("[SIM] cannot spawn group: LIMIT REACHED " + simulationGroups.size());
				return;
			}
		}
		RavegingSimulationGroup g = new RavegingSimulationGroup(state);

		g.createFromBlueprints(from, getUniqueGroupUId(), FactionManager.PIRATES_ID, getBlueprintList(count, 1, FactionManager.PIRATES_ID));

		g.setCurrentProgram(new PirateSimulationProgram(g, false));

		addGroup(g);
	}

	public boolean createRandomPiratePatrolGroup(Vector3i from, Vector3i to, int count) {
		if (existsGroupInSector(from)) {
			System.err.println("[SIM] cannot spawn group: collision at " + from);
			return false;
		}
		synchronized (simulationGroups) {
			if (simulationGroups.size() >= ServerConfig.CONCURRENT_SIMULATION.getInt()) {
				System.err.println("[SIM] cannot spawn group: LIMIT REACHED " + simulationGroups.size());
				return false;
			}
		}
		TargetSectorSimulationGroup g = new TargetSectorSimulationGroup(state, new Vector3i(to));

		g.createFromBlueprints(from, getUniqueGroupUId(), FactionManager.PIRATES_ID, getBlueprintList(count, 1, FactionManager.PIRATES_ID));

		g.setCurrentProgram(new TradingRouteSimulationProgram(g, false));

		addGroup(g);
		return true;
	}

	public boolean createRandomTradigRouteGroup(Vector3i from, Vector3i to, int count) {
		if (existsGroupInSector(from)) {
			System.err.println("[SIM] cannot spawn group: collision at " + from);
			return false;
		}
		synchronized (simulationGroups) {
			if (simulationGroups.size() >= ServerConfig.CONCURRENT_SIMULATION.getInt()) {
				System.err.println("[SIM] cannot spawn group: LIMIT REACHED " + simulationGroups.size());
				return false;
			}
		}
		TargetSectorSimulationGroup g = new TargetSectorSimulationGroup(state, new Vector3i(to));
		if (ServerConfig.SIMULATION_TRADING_FILLS_SHOPS.isOn()) {
			g.hasStockToDeliver = true;
		}

		boolean found = false;

		try {
			Sector s = g.getState().getUniverse().getSector(new Vector3i(to));
			for (Sendable g2 : g.getState().getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) {
				if (g2 instanceof ShopSpaceStation && ((ShopSpaceStation) g2).getSectorId() == s.getId()) {
					if (((ShopSpaceStation) g2).isAiShop()) {

						g.createFromBlueprints(from, getUniqueGroupUId(), FactionManager.TRAIDING_GUILD_ID, getBlueprintList(count, 1, FactionManager.TRAIDING_GUILD_ID));
						TradingRouteSimulationProgram tradingRouteSimulationProgram = new TradingRouteSimulationProgram(g, false);
						g.setCurrentProgram(tradingRouteSimulationProgram);
						addGroup(g);
						found = true;
						break;

					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return found;
	}

	public void disband(SimulationGroup simGroup) {
		System.err.println("[Simulation] disbanding sim group: " + simGroup);
		simGroup.deleteMembers();
		removeGroup(simGroup);
	}

	public boolean existsGroupInSector(Vector3i from) {
		Vector3i tmp = new Vector3i();
		for (int i = 0; i < simulationGroups.size(); i++) {
			SimulationGroup simulationGroup = simulationGroups.get(i);
			for (String s : simulationGroup.getMembers()) {
				try {
					if (simulationGroup.getSector(s, tmp).equals(from)) {
						return true;
					}
				} catch (EntityNotFountException e) {
					e.printStackTrace();
					System.err.println("Exception REMOVING GROUP NOW " + simulationGroup.getMembers());
					simulationGroups.remove(i);
					i--;
					break;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	@Override
	public void fromTagStructure(Tag tag) {
		Tag[] tags = (Tag[]) tag.getValue();

		byte version = (Byte) tags[0].getValue();

		Tag[] simGroups = (Tag[]) tags[1].getValue();

		for (int i = 0; i < simGroups.length && simGroups[i].getType() != Type.FINISH; i++) {
			synchronized (simulationGroups) {
				//type was placed in the simGroup tag
				int type = (Integer) ((Tag[]) simGroups[i].getValue())[1].getValue();

				try {

					SimulationGroup newInstance = GroupType.values()[type].clazz.instantiate(state);

					System.err.println("[SIM] loading simulation group type: " + type + ": " + newInstance);
					newInstance.fromTagStructure(simGroups[i]);

					addGroup(newInstance);

				} catch (SecurityException e) {
					e.printStackTrace();
					assert (false);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}

			}

		}
		if (tags.length > 2 && tags[2].getType() == Type.LONG) {
			uniquegroups = (Long) tags[2].getValue();
		}
	}

	@Override
	public Tag toTagStructure() {
		Tag version = new Tag(Type.BYTE, null, (byte) 0);
		Tag groups;
		synchronized (simulationGroups) {
			Tag[] groupsArray = new Tag[simulationGroups.size() + 1];
			for (int i = 0; i < simulationGroups.size(); i++) {
				groupsArray[i] = simulationGroups.get(i).toTagStructure();
			}
			groupsArray[simulationGroups.size()] = FinishTag.INST;
			groups = new Tag(Type.STRUCT, null, groupsArray);
		}
		Tag grps = new Tag(Type.LONG, null, uniquegroups);
		return new Tag(Type.STRUCT, "SimulationState", new Tag[]{
				version,
				groups,
				grps,
				FinishTag.INST,
		});
	}

	public CatalogPermission[] getBlueprintList(int shipCountToSpawn, int level, int factionid, BlueprintType ... typesToUse) {
		Collection<CatalogPermission> catalog = state.getCatalogManager().getCatalog();

		int closestDiffOverall = -1;
		int closestOverall = Integer.MAX_VALUE;
		Object2ObjectOpenHashMap<CatalogPermission, CatalogWavePermission> toPossiblySpawn = new Object2ObjectOpenHashMap<CatalogPermission, CatalogWavePermission>();
		
		for (CatalogPermission c : catalog) {
			boolean ok = false;
			for(BlueprintType canUse : typesToUse){
				if(canUse == c.type){
					ok = true;
					break;
				}
			}
			if(!ok){
				continue;
			}
			CatalogWavePermission clWp = null;
			for (CatalogWavePermission wp : c.wavePermissions) {
				int closest = Integer.MAX_VALUE;
				if(wp.factionId == factionid){
					
					int d = Math.abs(wp.difficulty - level);
					if(d < closest){
						closestDiffOverall = wp.difficulty;
						if(d < closestOverall){
							closestDiffOverall = wp.difficulty;
							closestOverall = d;
						}
						clWp = wp;
						closest = d;
					}
					
				}
			}
			if(clWp != null){
				toPossiblySpawn.put(c, clWp);
			}
		}
		ObjectIterator<Entry<CatalogPermission, CatalogWavePermission>> iterator = toPossiblySpawn.entrySet().iterator();
		ArrayList<CatalogPermission> c = new ArrayList<CatalogPermission>();
		while(iterator.hasNext()){
			Entry<CatalogPermission, CatalogWavePermission> next = iterator.next();
			
			if(next.getValue().difficulty != closestDiffOverall){
				iterator.remove();
			}else{
				for(int i = 0; i < next.getValue().amount; i++){
					c.add(next.getKey());
				}
			}
		}
		
		
		if(toPossiblySpawn.size() > 0){
			
			
			
			CatalogPermission[] cc = new CatalogPermission[c.size()];
			
			for(int i = 0; i < c.size(); i++){
				cc[i] = c.get(i);
			}
			return cc;
		}else{
			return getBlueprintListOld(shipCountToSpawn, level, factionid, typesToUse);
		}
	}
	public CatalogPermission[] getBlueprintList(int shipCountToSpawn, int level, int factionid) {
		return getBlueprintList(shipCountToSpawn, level, factionid, BlueprintType.SHIP);
	}
	public CatalogPermission[] getBlueprintListOld(int shipCountToSpawn, int level, int faction, BlueprintType ... typesToUse) {

		Collection<CatalogPermission> catalog = state.getCatalogManager().getCatalog();

		ArrayList<CatalogPermission> toPossiblySpawn = new ArrayList<CatalogPermission>();
		for (CatalogPermission c : catalog) {
			boolean ok = false;
			for(BlueprintType canUse : typesToUse){
				if(canUse == c.type){
					ok = true;
					break;
				}
			}
			if(!ok){
				continue;
			}
			if (c.enemyUsable()) {
				toPossiblySpawn.add(c);
			}

		}
		if (toPossiblySpawn.isEmpty()) {
			System.err.println("[WAVE] Server will not spawn any waves, the catalog is empty");
			return new CatalogPermission[0];
		}
		Collections.sort(toPossiblySpawn, (o1, o2) -> o1.price > o2.price ? 1 : (o1.price < o2.price ? -1 : 0));
		float d = (float) toPossiblySpawn.size() / (float) ShipSpawnWave.MAX_LEVEL;
		int toIndex = (int) Math.min(toPossiblySpawn.size() - 1, Math.ceil(d * level));

		CatalogPermission[] toSpawn = new CatalogPermission[shipCountToSpawn];

		for (int i = 0; i < shipCountToSpawn; i++) {
			int index = Math.min(toPossiblySpawn.size() - 1, Math.max(0, toIndex - 2 + i));

			toSpawn[i] = (toPossiblySpawn.get(index));
		}
		return toSpawn;
	}

	public String getDebugStringFor(SimpleTransformableSendableObject entity) {
		for (int i = 0; i < simulationGroups.size(); i++) {
			SimulationGroup simulationGroup = simulationGroups.get(i);
			if (simulationGroup.getMembers().contains(entity.getUniqueIdentifier())) {
				return simulationGroup.getDebugString();
			}
		}
		return "NOSIM";
	}

	/**
	 * @return the planner
	 */
	public SimulationPlanner getPlanner() {
		return planner;
	}

	/**
	 * @return the state
	 */
	public GameServerState getState() {
		return state;
	}

	@Override
	public String getUniqueIdentifier() {
		return "SIMULATION_STATE";
	}

	@Override
	public boolean isVolatile() {
		return false;
	}

	public Vector3i getUnloadedSectorAround(Vector3i from, Vector3i out) {
		int distance = 2;

		out.set(from);

		while (state.getUniverse().isSectorLoaded(out)) {

			int r = Universe.getRandom().nextInt(3);

			if (r == 0) {
				out.set(from.x + distance, from.y, from.z);
				if (!state.getUniverse().isSectorLoaded(out)) {
					break;
				}
				out.set(from.x - distance, from.y, from.z);
				if (!state.getUniverse().isSectorLoaded(out)) {
					break;
				}

				out.set(from.x, from.y + distance, from.z);
				if (!state.getUniverse().isSectorLoaded(out)) {
					break;
				}
				out.set(from.x, from.y - distance, from.z);
				if (!state.getUniverse().isSectorLoaded(out)) {
					break;
				}

				out.set(from.x, from.y, from.z + distance);
				if (!state.getUniverse().isSectorLoaded(out)) {
					break;
				}
				out.set(from.x, from.y, from.z - distance);
			} else if (r == 1) {

				out.set(from.x, from.y + distance, from.z);
				if (!state.getUniverse().isSectorLoaded(out)) {
					break;
				}
				out.set(from.x, from.y - distance, from.z);
				if (!state.getUniverse().isSectorLoaded(out)) {
					break;
				}

				out.set(from.x + distance, from.y, from.z);
				if (!state.getUniverse().isSectorLoaded(out)) {
					break;
				}
				out.set(from.x - distance, from.y, from.z);
				if (!state.getUniverse().isSectorLoaded(out)) {
					break;
				}

				out.set(from.x, from.y, from.z + distance);
				if (!state.getUniverse().isSectorLoaded(out)) {
					break;
				}
				out.set(from.x, from.y, from.z - distance);
			} else {

				out.set(from.x, from.y, from.z + distance);
				if (!state.getUniverse().isSectorLoaded(out)) {
					break;
				}
				out.set(from.x, from.y, from.z - distance);
				if (!state.getUniverse().isSectorLoaded(out)) {
					break;
				}

				out.set(from.x + distance, from.y, from.z);
				if (!state.getUniverse().isSectorLoaded(out)) {
					break;
				}
				out.set(from.x - distance, from.y, from.z);
				if (!state.getUniverse().isSectorLoaded(out)) {
					break;
				}

				out.set(from.x, from.y + distance, from.z);
				if (!state.getUniverse().isSectorLoaded(out)) {
					break;
				}
				out.set(from.x, from.y - distance, from.z);
				if (!state.getUniverse().isSectorLoaded(out)) {
					break;
				}

			}

			distance++;

		}

		return out;

	}

	public void initialize() {
		Tag readEntity;
		try {
			readEntity = state.getController().readEntity("SIMULATION_STATE", "sim");
			this.fromTagStructure(readEntity);
		} catch (IOException e) {
			System.err.println("ERROR LOADING SIMULATION_STATE.sim");
			e.printStackTrace();
		} catch (EntityNotFountException e) {
			System.err.println("[SIMULATION] no simulation state found on disk. creating new...");
		} catch (Exception e) {
			System.err.println("ERROR LOADING SIMULATION_STATE.sim");
			e.printStackTrace();
		} 
		planner.start();
	}

	public void onAIDeactivated(SimpleTransformableSendableObject entity) {
		if (entity.getUniqueIdentifier().startsWith("ENTITY_SHIP_MOB_SIM")) {
			//remove this entity since it wont be
			//doing simulation anymore
			removeMemberFromGroups(entity.getUniqueIdentifier());
		}
	}

	public void removeGroup(SimulationGroup g) {
		synchronized (simulationGroups) {
			simulationGroups.remove(g);
		}
	}

	public void removeMemberFromGroups(String uniqueIdentifier) {
		for (int i = 0; i < simulationGroups.size(); i++) {
			SimulationGroup simulationGroup = simulationGroups.get(i);
			simulationGroup.getMembers().remove(uniqueIdentifier);
		}
	}

	public SimulationGroup sendToAttackSpecific(
			SimpleTransformableSendableObject target, int fromFaction, int count) {

		synchronized (simulationGroups) {
			if (simulationGroups.size() >= ServerConfig.CONCURRENT_SIMULATION
					.getInt()) {
				System.err.println("[SIM] cannot spawn group: LIMIT REACHED "
						+ simulationGroups.size());
				return null;
			}
		}

		Sector s = state.getUniverse().getSector(target.getSectorId());

		if (s != null) {
			AttackSingleEntitySimulationGroup g = new AttackSingleEntitySimulationGroup(state, new Vector3i(s.pos),
					target.getUniqueIdentifier());
			Vector3i unloadedSectorAround = getUnloadedSectorAround(s.pos,
					new Vector3i());
			if (existsGroupInSector(unloadedSectorAround)) {
				System.err.println("[SIM] cannot spawn group: collision at "
						+ unloadedSectorAround);
				return null;
			}
			System.err.println("[SIM] Sending group from: " + unloadedSectorAround + " (position of attack target: " + s.pos + ")");
			CatalogPermission[] blueprintList = getBlueprintList(count, 1, fromFaction);
			for(CatalogPermission p : blueprintList){
				assert(p.type == BlueprintType.SHIP);
			}
			g.createFromBlueprints(unloadedSectorAround, getUniqueGroupUId(),
					fromFaction, blueprintList);

			TradingRouteSimulationProgram tradingRouteSimulationProgram = new TradingRouteSimulationProgram(
					g, false);

			tradingRouteSimulationProgram.setSpecificTargetId(target.getId());
			g.setCurrentProgram(tradingRouteSimulationProgram);

			addGroup(g);
			return g;
		}
		return null;
	}

	public synchronized long getUniqueGroupUId() {
		return uniquegroups++;
	}

	public void update(Timer timer) {

		if (!jobs.isEmpty()) {
			synchronized (jobs) {
				while (!jobs.isEmpty()) {
					jobs.dequeue().executeJob(this);
				}
			}
		}
		//		if(!spawnedTest){
		//			createRandomPirateGroup(new Vector3i(5,5,5));
		//			spawnedTest = true;
		//		}
		//		if(!spawnedTradingTest){
		//			spawnedTradingTest = createRandomTradigRouteGroup(new Vector3i(-1,-1,-1));
		//		}

		long time = timer.currentTime;

		runningTime += (time - lastupdateTime);

		long ticks = runningTime / SimulationGroup.SIMULATION_TICK;

		runningTime -= ticks * SimulationGroup.SIMULATION_TICK;

		lastupdateTime = time;
		if (ticks > 0) {
			for (int i = 0; i < simulationGroups.size(); i++) {
				if (simulationGroups.get(i).getMembers().isEmpty()) {
					synchronized (simulationGroups) {
						System.err.println("[SIMULATION] Removing Sim Group for lack of members");
						simulationGroups.remove(i);
						i--;
					}
				} else {
					simulationGroups.get(i).updateTicks(ticks);
				}
			}
		}
	}

	public void writeToDatabase() {
		synchronized (simulationGroups) {
			for (int i = 0; i < simulationGroups.size(); i++) {
				simulationGroups.get(i).writeToDatabase();
			}
		}
	}

	public void print(RegisteredClientInterface client) throws IOException{
		client.serverMessage("-------SIMULATION INFO START----------");
		client.serverMessage("Current Total Groups: "+simulationGroups.size());
		synchronized (simulationGroups) {
			for (int i = 0; i < simulationGroups.size(); i++) {
				simulationGroups.get(i).print(client);
			}
		}
	}

	public void clearAll() {
		synchronized (simulationGroups) {
			for (int i = 0; i < simulationGroups.size(); i++) {
				simulationGroups.get(i).despawn();
			}
		}
		
	}

	public void shutdown() {
		if(planner != null){
			planner.shutdown();
		}
	}
}
