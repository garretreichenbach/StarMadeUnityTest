package org.schema.game.server.controller.world.factory;

import java.util.Random;

import javax.vecmath.Vector3f;

import org.schema.game.common.controller.Planet;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.RequestData;
import org.schema.game.server.controller.RequestDataPlanet;
import org.schema.game.server.controller.world.factory.terrain.ColumnTerrainGenerator;
import org.schema.game.server.controller.world.factory.terrain.GeneratorFloraPlugin;
import org.schema.game.server.controller.world.factory.terrain.GeneratorResourcePluginChunk16;
import org.schema.game.server.controller.world.factory.terrain.TerrainDeco;

public class WorldCreatorPlanetColumnyFactory extends WorldCreatorPlanetFactory {

	private TerrainDeco[] minable;

	public WorldCreatorPlanetColumnyFactory(long seed, Vector3f[] poly, float radius) {
		super(seed, poly, radius);
		minable = new TerrainDeco[6];
		Random r = new Random(seed);

		minable[0] = new GeneratorResourcePluginChunk16(5, getRandomRockResource(r), ElementKeyMap.TERRAIN_PURPLE_ALIEN_ROCK);
		minable[1] = new GeneratorResourcePluginChunk16(5, getRandomRockResource(r), ElementKeyMap.TERRAIN_PURPLE_ALIEN_ROCK);

		//		minable[2] = new GeneratorResourcePlugin(ElementKeyMap.TERRAIN_LITHIUM_ID, 6, ElementKeyMap.TERRAIN_PURPLE_ALIEN_ROCK);
		//		minable[3] = new GeneratorResourcePlugin(ElementKeyMap.TERRAIN_EXTRANIUM_ID, 4, ElementKeyMap.TERRAIN_PURPLE_ALIEN_ROCK);
		//		minable[4] = new GeneratorResourcePlugin(ElementKeyMap.TERRAIN_IRIDIUM_ID, 4, ElementKeyMap.TERRAIN_PURPLE_ALIEN_ROCK);

		minable[2] = new GeneratorFloraPlugin(ElementKeyMap.TERRAIN_FLOWER_FAN_PURPLE_SPRITE, getTop(), getFiller());
		minable[3] = new GeneratorFloraPlugin(ElementKeyMap.TERRAIN_GLOW_TRAP_SPRITE, getTop(), getFiller());
		minable[4] = new GeneratorFloraPlugin(ElementKeyMap.TERRAIN_WEEDS_PURPLE_SPRITE, getTop(), getFiller());
		minable[5] = new GeneratorFloraPlugin(ElementKeyMap.TERRAIN_YHOLE_PURPLE_SPRITE, getTop(), getFiller());

	}

	@Override
	public void createAdditionalRegions(Random r) {
		
	}

	@Override
	public void createWorld(SegmentController world, Segment w, RequestData requestData) {
		synchronized (this) {
			if (!initialized) {
				generator = new ColumnTerrainGenerator(((Planet) world).getSeed());
				generator.setWorldCreator(this);
				init(world);
				initialized = true;
			}
		}

		try {
			gen(w, (RequestDataPlanet) requestData);
		} catch (SegmentDataWriteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public short getFiller() {
		return ElementKeyMap.TERRAIN_PURPLE_ALIEN_VINE;
	}

	@Override
	public TerrainDeco[] getGen() {
		return minable;
	}

	@Override
	public short getSolid() {
		return ElementKeyMap.TERRAIN_PURPLE_ALIEN_ROCK;
	}

	@Override
	public short getTop() {
		return ElementKeyMap.TERRAIN_PURPLE_ALIEN_TOP;
	}

}
