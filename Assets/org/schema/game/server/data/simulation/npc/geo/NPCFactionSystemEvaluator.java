package org.schema.game.server.data.simulation.npc.geo;

import java.util.Random;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.server.data.Galaxy;
import org.schema.game.server.data.GalaxyTmpVars;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.simulation.npc.NPCFaction;

public class NPCFactionSystemEvaluator {
	public static final double OTHER_FACTION_CODE = -999988887777D;

	private final NPCFaction faction;

	private final GameServerState state;

	public NPCFactionSystemEvaluator(NPCFaction faction) {
		super();
		this.faction = faction;
		this.state = (GameServerState)faction.getState();
		
		Random r = new Random(faction.getIdFaction());
		
		for(int i = 0; i < expansionSpecificResourceValue.length; i++){
			expansionSpecificResourceValue[i] = r.nextDouble() * (r.nextBoolean() ? 1d : -1d) * 3d;
		}
	}
	
	public int expansionMaxRadius = 42;
	
	public int expansionSearchRadius = 3;
	
	public double expansionStarValue = 1500;
	public double expansionGiantValue = 1600;
	public double expansionDoubleStarValue = 1700;
	public double expansionVoidValue = -1000;

	public double expansionBlackHoleValue = Double.NEGATIVE_INFINITY;
	
	public double expansionSpecificResourceValue[] = new double[VoidSystem.RESOURCES];

	public double expansionDangerValue = -500;

	public double expansionDistanceValue = -200;
	
	
	
	private GalaxyTmpVars tmpVars = new GalaxyTmpVars();
	
	private StarSystemResourceRequestContainer tmp = new StarSystemResourceRequestContainer();
	
	public double getSystemValue(Vector3i system, short lvl){
		Galaxy galaxy = state.getUniverse().getGalaxyFromSystemPos(system);
		
		StarSystemResourceRequestContainer systemResources =
				state.getUniverse().updateSystemResourcesWithDatabaseValues(system, galaxy, tmp, tmpVars);
		
		if(systemResources.factionId != 0 && faction.getIdFaction() != systemResources.factionId){
			
			
			
			return OTHER_FACTION_CODE;
		}
		
		double value = 0;
		for(int i = 0; i < expansionSpecificResourceValue.length; i++){
			value += expansionSpecificResourceValue[i] * systemResources.res[i];
		}
		value += Vector3i.getDisatance(system, faction.npcFactionHomeSystem) * expansionDistanceValue;
		
		if(!galaxy.isVoidAbs(system)){
			int systemType = galaxy.getSystemType(system);
			switch(systemType) {
				case (Galaxy.TYPE_BLACK_HOLE) -> value += expansionBlackHoleValue;
				case (Galaxy.TYPE_DOUBLE_STAR) -> value += expansionDoubleStarValue;
				case (Galaxy.TYPE_GIANT) -> value += expansionGiantValue;
				case (Galaxy.TYPE_SUN) -> value += expansionStarValue;
			}
		}else{
			value += expansionVoidValue;
		}
		
//		System.err.println("RES VALUE :::::::: "+value);

		return value;
	}

	public GameServerState getState() {
		return state;
	}
	
}
