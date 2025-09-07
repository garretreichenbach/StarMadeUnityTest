package org.schema.game.server.controller.world.factory.asteroid.old;

import java.util.Random;

import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.server.controller.world.factory.asteroid.WorldCreatorFloatingRockFactory;
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructure;
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructureList;
import org.schema.game.server.controller.world.factory.terrain.GeneratorResourcePlugin;
import org.schema.game.server.controller.world.factory.terrain.TerrainDeco;

public class WorldCreatorFloatingRockRockyTempoFactory extends WorldCreatorFloatingRockFactory {

	public WorldCreatorFloatingRockRockyTempoFactory(long seed) {
		super(seed);
	}

	private final static int ROCK_RED = registerBlock(ElementKeyMap.TERRAIN_ROCK_RED);
	private final static int MARS_DIRT = registerBlock(ElementKeyMap.TERRAIN_MARS_DIRT);
	private final static int LAVA = registerBlock(ElementKeyMap.TERRAIN_LAVA_ID);

	@Override
	protected int getRandomSolidType(float density, Random rand) {

		if (density < 0.08f && rand.nextFloat() > 0.30f)
			return MARS_DIRT;
		if (density > 0.3f && rand.nextFloat() > 0.68f)
			return LAVA;

		return ROCK_RED;
	}

	@Override
	public void setMinable(Random rand) {
		this.minable = new TerrainDeco[2];
		this.minable[0] = new GeneratorResourcePlugin(9, ElementKeyMap.RESS_CRYS_VARAT, ElementKeyMap.TERRAIN_ROCK_RED);
		this.minable[1] = new GeneratorResourcePlugin(9, ElementKeyMap.RESS_GAS_ZERCANER, ElementKeyMap.TERRAIN_ROCK_RED);
		//		this.minable[0] = new GeneratorResourcePlugin(ElementKeyMap.TERRAIN_POLONIUM_ID, 6, ElementKeyMap.TERRAIN_MARS_ROCK);
		//		this.minable[1] = new GeneratorResourcePlugin(ElementKeyMap.TERRAIN_PLATINUM_ID, 6, ElementKeyMap.TERRAIN_MARS_DIRT);
		//		this.minable[2] = new GeneratorResourcePlugin(ElementKeyMap.TERRAIN_MERCURY_ID, 8, ElementKeyMap.TERRAIN_MARS_ROCK);
	}

	@Override
	protected void terrainStructurePlacement(byte x, byte y, byte z, float depth, TerrainStructureList sl, Random
		rand) {

		if (rand.nextFloat() <= defaultResourceChance)
			sl.add(x, y, z, TerrainStructure.Type.ResourceBlob, ElementKeyMap.RESS_CRYS_VARAT, ElementKeyMap.TERRAIN_ROCK_RED, defaultResourceSize);
		if (rand.nextFloat() <= defaultResourceChance)
			sl.add(x, y, z, TerrainStructure.Type.ResourceBlob, ElementKeyMap.RESS_GAS_ZERCANER, ElementKeyMap.TERRAIN_ROCK_RED, defaultResourceSize);

	}

}

