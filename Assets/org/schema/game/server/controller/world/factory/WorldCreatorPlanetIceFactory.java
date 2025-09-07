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
import org.schema.game.server.controller.world.factory.terrain.GeneratorFloraPlugin;
import org.schema.game.server.controller.world.factory.terrain.GeneratorResourcePluginChunk16;
import org.schema.game.server.controller.world.factory.terrain.IceTerrainGenerator;
import org.schema.game.server.controller.world.factory.terrain.TerrainDeco;

public class WorldCreatorPlanetIceFactory extends WorldCreatorPlanetFactory {

	private TerrainDeco[] minable;

	public WorldCreatorPlanetIceFactory(long seed, Vector3f[] poly, float radius) {
		super(seed, poly, radius);
		minable = new TerrainDeco[6];
		Random r = new Random(seed);
		minable[0] = new GeneratorResourcePluginChunk16(5, getRandomRockResource(r), ElementKeyMap.TERRAIN_ICEPLANET_ROCK);
		minable[1] = new GeneratorResourcePluginChunk16(5, getRandomRockResource(r), ElementKeyMap.TERRAIN_ICEPLANET_ROCK);
		//		minable[0] = new GeneratorResourcePlugin(ElementKeyMap.TERRAIN_POLONIUM_ID, 4, ElementKeyMap.TERRAIN_ICEPLANET_ROCK);
		//		minable[1] = new GeneratorResourcePlugin(ElementKeyMap.TERRAIN_PLATINUM_ID, 4, ElementKeyMap.TERRAIN_ICEPLANET_ROCK);
		//		minable[2] = new GeneratorResourcePlugin(ElementKeyMap.TERRAIN_MERCURY_ID, 4, ElementKeyMap.TERRAIN_ICEPLANET_ROCK);
		//		minable[3] = new GeneratorResourcePlugin(ElementKeyMap.TERRAIN_IRIDIUM_ID, 4, ElementKeyMap.TERRAIN_ICEPLANET_ROCK);

		minable[2] = new GeneratorFloraPlugin(1, ElementKeyMap.TERRAIN_CORAL_ICE_SPRITE, getTop(), getFiller());
		minable[3] = new GeneratorFloraPlugin(1, ElementKeyMap.TERRAIN_ICE_CRAG_SPRITE, getTop(), getFiller());
		minable[4] = new GeneratorFloraPlugin(1, ElementKeyMap.TERRAIN_SNOW_BUD_SPRITE, getTop(), getFiller());
		minable[5] = new GeneratorFloraPlugin(1, ElementKeyMap.TERRAIN_FAN_FLOWER_ICE_SPRITE, getTop(), getFiller());
		//		minable[8] = new WorldGeneratorPineTrees(false);

	}

	@Override
	public void createAdditionalRegions(Random r) {
		
	}

	@Override
	public void createWorld(SegmentController world, Segment w, RequestData requestData) {
		synchronized (this) {
			if (!initialized) {
				generator = new IceTerrainGenerator(((Planet) world).getSeed());
				generator.setWorldCreator(this);
				init(world);
				initialized = true;
			}
		}

		try {
			gen(w, (RequestDataPlanet) requestData);
		} catch (SegmentDataWriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public short getCaveBottom() {
		return ElementKeyMap.TERRAIN_ICEPLANET_CRYSTAL;
	}

	@Override
	public short getFiller() {
		return ElementKeyMap.TERRAIN_ICE_ID;
	}

	@Override
	public TerrainDeco[] getGen() {
		return minable;
	}

	@Override
	public short getSolid() {
		return ElementKeyMap.TERRAIN_ICEPLANET_ROCK;
	}

	@Override
	public short getTop() {
		return ElementKeyMap.TERRAIN_ICEPLANET_SURFACE;
	}

}
