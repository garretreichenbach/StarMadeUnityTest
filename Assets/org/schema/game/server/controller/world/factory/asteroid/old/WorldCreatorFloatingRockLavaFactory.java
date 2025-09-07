package org.schema.game.server.controller.world.factory.asteroid.old;

import java.util.Random;

import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.server.controller.world.factory.asteroid.WorldCreatorFloatingRockFactory;
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructure;
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructureList;
import org.schema.game.server.controller.world.factory.terrain.GeneratorResourcePlugin;
import org.schema.game.server.controller.world.factory.terrain.TerrainDeco;

public class WorldCreatorFloatingRockLavaFactory extends WorldCreatorFloatingRockFactory {

	public WorldCreatorFloatingRockLavaFactory(long seed) {
		super(seed);
	}

	private final static int ROCK_BLACK = registerBlock(ElementKeyMap.TERRAIN_ROCK_BLACK);
	private final static int LAVA = registerBlock(ElementKeyMap.TERRAIN_LAVA_ID);

	@Override
	protected int getRandomSolidType(float density, Random rand) {
		return ROCK_BLACK;
	}

	@Override
	public void setMinable(Random rand) {
		this.minable = new TerrainDeco[2];
		this.minable[0] = new GeneratorResourcePlugin(9, ElementKeyMap.RESS_CRYS_MATTISE, ElementKeyMap.TERRAIN_ROCK_BLACK);
		this.minable[1] = new GeneratorResourcePlugin(9, ElementKeyMap.RESS_ORE_JISPER, ElementKeyMap.TERRAIN_ROCK_BLACK);
	}

	@Override
	protected void terrainStructurePlacement(byte x, byte y, byte z, float depth, TerrainStructureList sl, Random rand) {		
			
		if (rand.nextFloat() <= defaultResourceChance)			
			sl.add(x, y, z, TerrainStructure.Type.ResourceBlob, ElementKeyMap.RESS_CRYS_MATTISE, ElementKeyMap.TERRAIN_ROCK_BLACK, defaultResourceSize);
		if (rand.nextFloat() <= defaultResourceChance)			
			sl.add(x, y, z, TerrainStructure.Type.ResourceBlob, ElementKeyMap.RESS_ORE_JISPER, ElementKeyMap.TERRAIN_ROCK_BLACK, defaultResourceSize);
		
	}
	
	@Override
	protected int getVeinBlock(float density, Random rand){
		return LAVA;
	}

}
