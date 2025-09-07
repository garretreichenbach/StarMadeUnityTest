package org.schema.game.server.controller.world.factory.asteroid.old;

import java.util.Random;

import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.server.controller.world.factory.asteroid.WorldCreatorFloatingRockFactory;
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructure;
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructureList;
import org.schema.game.server.controller.world.factory.terrain.GeneratorResourcePlugin;
import org.schema.game.server.controller.world.factory.terrain.TerrainDeco;

public class WorldCreatorFloatingRockGoldyFactory extends WorldCreatorFloatingRockFactory {

	public WorldCreatorFloatingRockGoldyFactory(long seed) {
		super(seed);
	}

	private final static int MARS_DIRT = registerBlock(ElementKeyMap.TERRAIN_MARS_DIRT);
	private final static int ROCK_YELLOW = registerBlock(ElementKeyMap.TERRAIN_ROCK_YELLOW);

	@Override
	protected int getRandomSolidType(float density, Random rand) {
		if (density < 0.07f) {
			return MARS_DIRT;
		} else {
			return ROCK_YELLOW;
		}

	}

	@Override
	public void setMinable(Random rand) {
		this.minable = new TerrainDeco[2];
		this.minable[0] = new GeneratorResourcePlugin(9, ElementKeyMap.RESS_GAS_BASTYN, ElementKeyMap.TERRAIN_ROCK_YELLOW);
		this.minable[1] = new GeneratorResourcePlugin(9, ElementKeyMap.RESS_ORE_FERTIKEEN, ElementKeyMap.TERRAIN_ROCK_YELLOW);
	}

	@Override
	protected void terrainStructurePlacement(byte x, byte y, byte z, float depth, TerrainStructureList sl, Random rand) {		
			
		if (rand.nextFloat() <= defaultResourceChance)			
			sl.add(x, y, z, TerrainStructure.Type.ResourceBlob, ElementKeyMap.RESS_GAS_BASTYN, ElementKeyMap.TERRAIN_ROCK_YELLOW, defaultResourceSize);
		if (rand.nextFloat() <= defaultResourceChance)			
			sl.add(x, y, z, TerrainStructure.Type.ResourceBlob, ElementKeyMap.RESS_ORE_FERTIKEEN, ElementKeyMap.TERRAIN_ROCK_YELLOW, defaultResourceSize);
		
	}

}
