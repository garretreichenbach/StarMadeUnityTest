package org.schema.game.server.data.simulation.npc.geo;

import org.schema.common.util.ByteUtil;
import org.schema.game.common.controller.SpaceStation.SpaceStationType;
import org.schema.game.common.data.world.SectorGenerationInterface;
import org.schema.game.common.data.world.SectorInformation;
import org.schema.game.common.data.world.SectorInformation.SectorType;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.server.data.Galaxy;
import org.schema.game.server.data.GameServerState;

import java.util.Random;

public class NPCCreator implements SectorGenerationInterface{
	
	
	private final NPCSystem npcSystem;
	
	
	public NPCCreator(NPCSystem system) {
		this.npcSystem = system;
	}
	@Override
	public void generate(GameServerState state, int secX, int secY, int secZ,
			StellarSystem system, int index, Galaxy galaxy, Random rand) {
		if(SectorInformation.generateOrbits(state, galaxy, secX, secY, secZ, system, index, rand, this)){
			return;
		}
		if (rand.nextInt(40) == 0) {
			system.setSectorType(index, SectorType.ASTEROID);
			npcSystem.totalAsteroidSectors++;
			return;
		} else {
			system.setSectorType(index, SectorType.VOID);
			return;
		}		
	}
	@Override
	public boolean orbitTakenByGeneration(GameServerState state, int secX,
			int secY, int secZ, StellarSystem system, int index, Galaxy galaxy,
			Random rand) {
		if (rand.nextInt(90) == 0) {

			system.setSectorType(index, SectorType.PLANET);
			SectorInformation.generateTerrestrialPlanet(secX, secY, secZ, system, index, rand);
			return true;
		} else {
			system.setSectorType(index, SectorType.VOID);
			return true;
		}
	}

	@Override
	public void definitePlanet(GameServerState state, int secX,
			int secY, int secZ, StellarSystem system, int index, Galaxy galaxy,
			Random rand) {
		system.setSectorType(index, SectorType.PLANET);
		npcSystem.totalPlanetSectors++;
		SectorInformation.generateTerrestrialPlanet(secX, secY, secZ, system, index, rand);
	}

	@Override
	public boolean onOrbitButNoPlanet(GameServerState state, int secX,
			int secY, int secZ, StellarSystem system, int index, Galaxy galaxy,
			Random rand) {
		
		return false;
	}

	@Override
	public void onAsteroidBelt(GameServerState state, int secX, int secY,
			int secZ, StellarSystem system, int index, Galaxy galaxy,
			Random rand) {
		system.setSectorType(index, SectorType.ASTEROID);	
		npcSystem.totalAsteroidSectors++;
	}

	@Override
	public boolean staticSectorGeneration(GameServerState state, int secX,
			int secY, int secZ, StellarSystem system, int index, Galaxy galaxy,
			Random rand) {
		
		int x = ByteUtil.modU16(secX);
		int y = ByteUtil.modU16(secY);
		int z = ByteUtil.modU16(secZ);
		
		if(npcSystem.stationMap.containsKey(NPCSystem.getLocalIndex(x, y, z))){
			//system owner base
			system.setSectorType(index, SectorType.SPACE_STATION);
			system.setStationType(index, SpaceStationType.FACTION);
			return true;
		}
		return false;
	}
}
