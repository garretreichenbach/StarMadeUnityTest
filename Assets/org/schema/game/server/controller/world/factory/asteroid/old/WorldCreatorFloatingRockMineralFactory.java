package org.schema.game.server.controller.world.factory.asteroid.old;

import java.util.Random;

import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.server.controller.world.factory.asteroid.WorldCreatorFloatingRockFactory;
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructure;
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructureList;
import org.schema.game.server.controller.world.factory.terrain.GeneratorResourcePlugin;
import org.schema.game.server.controller.world.factory.terrain.TerrainDeco;

public class WorldCreatorFloatingRockMineralFactory extends WorldCreatorFloatingRockFactory {

	public WorldCreatorFloatingRockMineralFactory(long seed) {
		super(seed);
	}

	private final static int ROCK_GREEN = registerBlock(ElementKeyMap.TERRAIN_ROCK_GREEN);
	private final static int SAND = registerBlock(ElementKeyMap.TERRAIN_SAND_ID);
	private final static int LAVA = registerBlock(ElementKeyMap.TERRAIN_LAVA_ID);

	@Override
	protected int getRandomSolidType(float density, Random rand) {

		if (density < 0.05f && rand.nextFloat() > 0.70f)
			return SAND;

		if (density > 0.3f && rand.nextFloat() > 0.68f)
			return LAVA;

		return ROCK_GREEN;
	}

	@Override
	public void setMinable(Random rand) {
		this.minable = new TerrainDeco[2];
		this.minable[0] = new GeneratorResourcePlugin(9, ElementKeyMap.RESS_CRYS_NOCX, ElementKeyMap.TERRAIN_ROCK_GREEN);
		this.minable[1] = new GeneratorResourcePlugin(9, ElementKeyMap.RESS_ORE_METAL_COMMON, ElementKeyMap.TERRAIN_ROCK_GREEN);
		//		this.minable[0] = new GeneratorResourcePlugin(ElementKeyMap.TERRAIN_EXTRANIUM_ID, 6, ElementKeyMap.TERRAIN_ROCK_ID);
		//		this.minable[1] = new GeneratorResourcePlugin(ElementKeyMap.TERRAIN_MERCURY_ID, 6, ElementKeyMap.TERRAIN_ROCK_ID);
		//		this.minable[2] = new GeneratorResourcePlugin(ElementKeyMap.TERRAIN_LITHIUM_ID, 6, ElementKeyMap.TERRAIN_SAND_ID);
		//		this.minable[3] = new GeneratorResourcePlugin(ElementKeyMap.TERRAIN_POLONIUM_ID, 9, ElementKeyMap.TERRAIN_ROCK_ID);
		//		this.minable[4] = new GeneratorResourcePlugin(ElementKeyMap.TERRAIN_GOLD_ID, 6, ElementKeyMap.TERRAIN_ROCK_ID);
	}

	@Override
	protected void terrainStructurePlacement(byte x, byte y, byte z, float depth, TerrainStructureList sl, Random rand) {		
			
		if (rand.nextFloat() <= defaultResourceChance)			
			sl.add(x, y, z, TerrainStructure.Type.ResourceBlob, ElementKeyMap.RESS_CRYS_NOCX, ElementKeyMap.TERRAIN_ROCK_GREEN, defaultResourceSize);
		if (rand.nextFloat() <= defaultResourceChance)			
			sl.add(x, y, z, TerrainStructure.Type.ResourceBlob, ElementKeyMap.RESS_ORE_METAL_COMMON, ElementKeyMap.TERRAIN_ROCK_GREEN, defaultResourceSize);
		
	}

}
