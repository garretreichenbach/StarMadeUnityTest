package org.schema.game.common.data.world;

import org.schema.game.common.controller.SpaceStation.SpaceStationType;
import org.schema.game.common.data.world.SectorInformation.PlanetType;
import org.schema.game.server.data.Galaxy;
import org.schema.game.server.data.GameServerState;

import java.util.Random;

public class SectorGenerationDefault implements SectorGenerationInterface{

	@Override
	public boolean staticSectorGeneration(GameServerState state, int secX,
			int secY, int secZ, StellarSystem system, int index, Galaxy galaxy, Random rand) {
		//FIXME DEBUG
		if (Sector.DEFAULT_SECTOR.equals(secX, secY, secZ)) {
			system.setSectorType(index, SectorInformation.SectorType.MAIN);
			return true;
		}
		if (Sector.isNeighborNotSelf(secX, secY, secZ, 2, 2, 2)) {
			if(rand.nextInt(3) == 0){
				system.setSectorType(index, SectorInformation.SectorType.ASTEROID);
			}else{
				system.setSectorType(index, SectorInformation.SectorType.VOID);
			}
			return true;
		}else if (Sector.isNeighborNotSelf(secX, secY, secZ, 8, 8, 5)) {
			system.setSectorType(index, SectorInformation.SectorType.ASTEROID);
			return true;
		} else if ((secX == 8 && secY == 8 & secZ == 5)) {
			system.setSectorType(index, SectorInformation.SectorType.PLANET);
			system.addPlanet(index, PlanetType.EARTH);
			return true;
		}
		if ((secX == 100 && secY == 100 & secZ == 100)) {
			system.setSectorType(index, SectorInformation.SectorType.PLANET);
			system.addPlanet(index, PlanetType.EARTH);
			return true;
		}
		if ((secX == 222 && secY == 222 & secZ == 222)) {
			system.setSectorType(index, SectorInformation.SectorType.PLANET);
			system.addPlanet(index, PlanetType.DESERT);
			return true;
		}
		if ((secX == 333 && secY == 333 & secZ == 333)) {
			system.setSectorType(index, SectorInformation.SectorType.PLANET);
			system.addPlanet(index, PlanetType.FROZEN);
			return true;
		}
		if ((secX == 444 && secY == 444 & secZ == 444)) {
			system.setSectorType(index, SectorInformation.SectorType.PLANET);
			system.addPlanet(index, PlanetType.MESA);
			return true;
		}
		if ((secX == 555 && secY == 555 & secZ == 555)) {
			system.setSectorType(index, SectorInformation.SectorType.PLANET);
			system.addPlanet(index, PlanetType.CORRUPTED);
			return true;
		}
		if ((secX == 8 && secY == 5 & secZ == 8)) {
			system.setSectorType(index, SectorInformation.SectorType.SPACE_STATION);
			system.setStationType(index, SpaceStationType.RANDOM);
			return true;
		}
		if ((secX == 8 && secY == 5 & secZ == 5)) {
			system.setSectorType(index, SectorInformation.SectorType.SPACE_STATION);
			system.setStationType(index, SpaceStationType.PIRATE);
			return true;
		}

		return false;
	}

	@Override
	public void generate(GameServerState state, int secX, int secY, int secZ,
			StellarSystem system, int index, Galaxy galaxy, Random rand) {
		if(SectorInformation.generateOrbits(state, galaxy, secX, secY, secZ, system, index, rand, this)){
			return;
		}
		if (rand.nextInt(150) == 0) {
			system.setSectorType(index, SectorInformation.SectorType.SPACE_STATION);
			if (rand.nextInt(4) == 0) {
				system.setStationType(index, SpaceStationType.RANDOM);
			} else {
				system.setStationType(index, SpaceStationType.PIRATE);
			}
			return;
		} else if (rand.nextInt(25) == 0) {
			system.setSectorType(index, SectorInformation.SectorType.LOW_ASTEROID);
			return;
		} else {
			system.setSectorType(index, SectorInformation.SectorType.VOID);
			return;
		}
	}

	@Override
	public boolean orbitTakenByGeneration(GameServerState state, int secX,
			int secY, int secZ, StellarSystem system, int index, Galaxy galaxy,
			Random rand) {
		if (rand.nextInt(90) == 0) {
			system.setSectorType(index, SectorInformation.SectorType.PLANET);
			SectorInformation.generateTerrestrialPlanet(secX, secY, secZ, system, index, rand);
			return true;
		} else {
			if (rand.nextInt(40) == 0) {
				system.setSectorType(index, SectorInformation.SectorType.SPACE_STATION);
				system.setStationType(index, SpaceStationType.RANDOM);
				return true;
			} else {
				system.setSectorType(index, SectorInformation.SectorType.VOID);
				return true;
			}
		}
	}

	@Override
	public void definitePlanet(GameServerState state, int secX,
			int secY, int secZ, StellarSystem system, int index, Galaxy galaxy,
			Random rand) {
		if(rand.nextBoolean()) {
			system.log("[SectorGenerationDefault] Creating terrestrial planet @ (" + secX + ", " + secY + ", " + secZ +
					") (Provided index " + index + "; calculated index from getindex " + system.getIndex(system.getLocalCoordinate(secX),system.getLocalCoordinate(secY),system.getLocalCoordinate(secZ)) + ")");
			system.setSectorType(index, SectorInformation.SectorType.PLANET);
			SectorInformation.generateTerrestrialPlanet(secX, secY, secZ, system, index, rand);
		} else {
			system.log("[SectorGenerationDefault] Creating gas planet @ (" + secX + ", " + secY + ", " + secZ +
					") (Provided index " + index + "; calculated index from getindex " + system.getIndex(system.getLocalCoordinate(secX),system.getLocalCoordinate(secY),system.getLocalCoordinate(secZ)) + ")");
			system.setSectorType(index, SectorInformation.SectorType.GAS_PLANET);
			SectorInformation.generateGasPlanet(secX, secY, secZ, system, index, rand);
		}
	}

	@Override
	public boolean onOrbitButNoPlanet(GameServerState state, int secX,
			int secY, int secZ, StellarSystem system, int index, Galaxy galaxy,
			Random rand) {
		if (rand.nextInt(40) == 0) {
			system.setSectorType(index, SectorInformation.SectorType.SPACE_STATION);

			if (rand.nextInt(22) == 0) {
				system.setStationType(index, SpaceStationType.PIRATE);
				return true;
			} else {
				if (system.getOwnerFaction() < 0 && rand.nextInt(10) > 0) {
					system.setStationType(index, SpaceStationType.FACTION);
					return true;
				} else {
					system.setStationType(index, SpaceStationType.RANDOM);
					return true;
				}

			}
		}
		return false;
	}

	@Override
	public void onAsteroidBelt(GameServerState state, int secX, int secY,
			int secZ, StellarSystem system, int index, Galaxy galaxy,
			Random rand) {
		system.setSectorType(index, SectorInformation.SectorType.ASTEROID);
	}

}
