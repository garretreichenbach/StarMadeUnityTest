package org.schema.game.common.data.world;

import java.util.Random;

import org.schema.game.common.controller.SpaceStation.SpaceStationType;
import org.schema.game.common.data.world.SectorInformation.PlanetType;
import org.schema.game.common.data.world.SectorInformation.SectorType;
import org.schema.game.server.data.Galaxy;
import org.schema.game.server.data.GameServerState;

public class SectorGenerationNoGalaxies implements SectorGenerationInterface{

	@Override
	public boolean staticSectorGeneration(GameServerState state, int secX,
			int secY, int secZ, StellarSystem system, int index, Galaxy galaxy, Random rand) {
		//FIXME DEBUG
		if (Sector.isNeighborNotSelf(secX, secY, secZ, 2, 2, 2)) {
			if(rand.nextInt(3) == 0){
				system.setSectorType(index, SectorType.ASTEROID);
			}else{
				system.setSectorType(index, SectorType.VOID);
			}
			return true;
		}else if (Sector.isNeighborNotSelf(secX, secY, secZ, 8, 8, 5)) {
			system.setSectorType(index, SectorType.ASTEROID);
			return true;
		} else if ((secX == 8 && secY == 8 & secZ == 5)) {
			system.setSectorType(index, SectorType.PLANET);
			system.addPlanet(index, PlanetType.EARTH);
			return true;
		}
		if ((secX == 100 && secY == 100 & secZ == 100)) {
			system.setSectorType(index, SectorType.PLANET);
			system.addPlanet(index, PlanetType.EARTH);
			return true;
		}
		if ((secX == 222 && secY == 222 & secZ == 222)) {
			system.setSectorType(index, SectorType.PLANET);
			system.addPlanet(index, PlanetType.DESERT);
			return true;
		}
		if ((secX == 333 && secY == 333 & secZ == 333)) {
			system.setSectorType(index, SectorType.PLANET);
			system.addPlanet(index, PlanetType.FROZEN);
			return true;
		}
		if ((secX == 444 && secY == 444 & secZ == 444)) {
			system.setSectorType(index, SectorType.PLANET);
			system.addPlanet(index, PlanetType.MESA);
			return true;
		}
		if ((secX == 555 && secY == 555 & secZ == 555)) {
			system.setSectorType(index, SectorType.PLANET);
			system.addPlanet(index, PlanetType.CORRUPTED);
			return true;
		}
		if ((secX == 8 && secY == 5 & secZ == 8)) {
			system.setSectorType(index, SectorType.SPACE_STATION);
			system.setStationType(index, SpaceStationType.RANDOM);
			return true;
		}
		if ((secX == 8 && secY == 5 & secZ == 5)) {
			system.setSectorType(index, SectorType.SPACE_STATION);
			system.setStationType(index, SpaceStationType.PIRATE);
			return true;
		}
		if (Sector.DEFAULT_SECTOR.equals(secX, secY, secZ)) {
			system.setSectorType(index, SectorType.MAIN);
			return true;
		}
		return false;
	}

	@Override
	public void generate(GameServerState state, int secX, int secY, int secZ,
			StellarSystem system, int index, Galaxy galaxy, Random rand) {
		assert(false):"Old universes are deprecated";
		int randomInt = rand.nextInt(250);
		if (randomInt < 5 && !SectorInformation.isPlanetSpotTaken(secX, secY, secZ, system)) {

			system.setSectorType(index, SectorType.PLANET);
			SectorInformation.generateTerrestrialPlanet(secX, secY, secZ, system, index, rand);

		} else {
			if (randomInt < 242) {
				system.setSectorType(index, SectorType.ASTEROID);
			} else {
				system.setSectorType(index, SectorType.SPACE_STATION);
				int nextInt = rand.nextInt(5);
				if (nextInt == 0) {
					system.setStationType(index, SpaceStationType.PIRATE);
				} else {
					system.setStationType(index, SpaceStationType.RANDOM);
				}
			}
		}	
	}

	@Override
	public boolean orbitTakenByGeneration(GameServerState state, int secX,
			int secY, int secZ, StellarSystem system, int index, Galaxy galaxy,
			Random rand) {
		assert(false);
		return true;
	}

	@Override
	public void definitePlanet(GameServerState state, int secX,
			int secY, int secZ, StellarSystem system, int index, Galaxy galaxy,
			Random rand) {
		assert(false);
	}

	@Override
	public boolean onOrbitButNoPlanet(GameServerState state, int secX,
			int secY, int secZ, StellarSystem system, int index, Galaxy galaxy,
			Random rand) {
		assert(false);
		return true;
	}

	@Override
	public void onAsteroidBelt(GameServerState state, int secX, int secY,
			int secZ, StellarSystem system, int index, Galaxy galaxy,
			Random rand) {
		assert(false);
	}

}
