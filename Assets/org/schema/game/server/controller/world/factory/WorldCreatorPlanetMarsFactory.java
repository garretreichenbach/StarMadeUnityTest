package org.schema.game.server.controller.world.factory;

import java.util.Random;

import javax.vecmath.Vector3f;

import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.server.controller.world.factory.terrain.GeneratorFloraPlugin;
import org.schema.game.server.controller.world.factory.terrain.GeneratorResourcePluginChunk16;
import org.schema.game.server.controller.world.factory.terrain.TerrainDeco;

public class WorldCreatorPlanetMarsFactory extends WorldCreatorPlanetFactory {

	private TerrainDeco[] minable;

	public WorldCreatorPlanetMarsFactory(long seed, Vector3f[] poly, float radius) {
		super(seed, poly, radius);
		Random r = new Random(seed);
		minable = new TerrainDeco[6];
		minable[0] = new GeneratorResourcePluginChunk16(5, getRandomRockResource(r), ElementKeyMap.TERRAIN_ROCK_MARS);
		minable[1] = new GeneratorResourcePluginChunk16(5, getRandomRockResource(r), ElementKeyMap.TERRAIN_ROCK_MARS);
		//		minable[0] = new GeneratorResourcePlugin(ElementKeyMap.TERRAIN_IRIDIUM_ID, 8, ElementKeyMap.TERRAIN_MARS_ROCK);
		//		minable[1] = new GeneratorResourcePlugin(ElementKeyMap.TERRAIN_PLATINUM_ID, 8, ElementKeyMap.TERRAIN_MARS_ROCK);
		//		minable[2] = new GeneratorResourcePlugin(ElementKeyMap.TERRAIN_MAGNESIUM_ID, 6, ElementKeyMap.TERRAIN_MARS_ROCK);
		//		minable[3] = new GeneratorResourcePlugin(ElementKeyMap.TERRAIN_PALLADIUM_ID, 10, ElementKeyMap.TERRAIN_MARS_ROCK);
		minable[2] = new GeneratorFloraPlugin(ElementKeyMap.TERRAIN_CORAL_RED_SPRITE, getTop(), getFiller());
		minable[3] = new GeneratorFloraPlugin(ElementKeyMap.TERRAIN_SHROOM_RED_SPRITE, getTop(), getFiller());
		minable[4] = new GeneratorFloraPlugin(ElementKeyMap.TERRAIN_FUNGAL_GROWTH_SPRITE, getTop(), getFiller());
		minable[5] = new GeneratorFloraPlugin(ElementKeyMap.TERRAIN_FUNGAL_TRAP_SPRITE, getTop(), getFiller());

	}

	@Override
	public void createAdditionalRegions(Random r) {
	}

	@Override
	public short getFiller() {
		return ElementKeyMap.TERRAIN_MARS_DIRT;
	}

	@Override
	public TerrainDeco[] getGen() {
		return minable;
	}

	@Override
	public short getSolid() {
		return ElementKeyMap.TERRAIN_ROCK_MARS;
	}

	@Override
	public short getTop() {
		return ElementKeyMap.TERRAIN_MARS_TOP;
	}

}
