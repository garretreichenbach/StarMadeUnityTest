package org.schema.game.server.controller.world.factory.asteroid.old;

import java.util.Random;

import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.server.controller.world.factory.asteroid.WorldCreatorFloatingRockFactory;
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructure;
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructureList;
import org.schema.game.server.controller.world.factory.terrain.GeneratorResourcePlugin;
import org.schema.game.server.controller.world.factory.terrain.TerrainDeco;

public class WorldCreatorFloatingRockIcyFactory extends WorldCreatorFloatingRockFactory {

	public WorldCreatorFloatingRockIcyFactory(long seed) {
		super(seed);
	}

	private final static int ROCK_WHITE = registerBlock(ElementKeyMap.TERRAIN_ROCK_WHITE);
	private final static int ICE = registerBlock(ElementKeyMap.TERRAIN_ICE_ID);

	@Override
	protected int getRandomSolidType(float density, Random rand) {

		if (density < 0.02f && rand.nextFloat() > 0.05f)
			return ICE;
		else
			return ROCK_WHITE;
	}

	@Override
	public void setMinable(Random rand) {
		this.minable = new TerrainDeco[2];
		this.minable[0] = new GeneratorResourcePlugin(9, ElementKeyMap.RESS_CRYS_HATTEL, ElementKeyMap.TERRAIN_ROCK_WHITE);
		this.minable[1] = new GeneratorResourcePlugin(9, ElementKeyMap.RESS_ORE_HYLAT, ElementKeyMap.TERRAIN_ROCK_WHITE);
	}

	@Override
	protected void terrainStructurePlacement(byte x, byte y, byte z, float depth, TerrainStructureList sl, Random rand) {		
			
		if (rand.nextFloat() <= defaultResourceChance)			
			sl.add(x, y, z, TerrainStructure.Type.ResourceBlob, ElementKeyMap.RESS_CRYS_HATTEL, ElementKeyMap.TERRAIN_ROCK_WHITE, defaultResourceSize);
		if (rand.nextFloat() <= defaultResourceChance)			
			sl.add(x, y, z, TerrainStructure.Type.ResourceBlob, ElementKeyMap.RESS_ORE_HYLAT, ElementKeyMap.TERRAIN_ROCK_WHITE, defaultResourceSize);
		
	}

}
