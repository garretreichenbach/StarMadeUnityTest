package org.schema.game.server.controller.world.factory;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.Planet;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.world.Segment;
import org.schema.game.common.data.world.SegmentDataWriteException;
import org.schema.game.server.controller.RequestData;
import org.schema.game.server.controller.RequestDataPlanet;
import org.schema.game.server.controller.world.factory.regions.PyramidRegion;
import org.schema.game.server.controller.world.factory.regions.Region;
import org.schema.game.server.controller.world.factory.regions.TresureRegion;
import org.schema.game.server.controller.world.factory.terrain.*;

import javax.vecmath.Vector3f;
import java.util.Random;

public class WorldCreatorPlanetDesertFactory extends WorldCreatorPlanetFactory {

	private TerrainDeco[] minable;

	public WorldCreatorPlanetDesertFactory(long seed, Vector3f[] poly, float radius) {
		super(seed, poly, radius);
		Random r = new Random(seed);
		minable = new TerrainDeco[7];

		minable[0] = new GeneratorResourcePluginChunk16(5, getRandomRockResource(r), ElementKeyMap.TERRAIN_ROCK_NORMAL);
		minable[1] = new GeneratorResourcePluginChunk16(5, getRandomRockResource(r), ElementKeyMap.TERRAIN_ROCK_NORMAL);
		//		minable[0] = new GeneratorResourcePlugin(ElementKeyMap.TERRAIN_GOLD_ID, 3, ElementKeyMap.TERRAIN_ROCK_ID);
		//		minable[1] = new GeneratorResourcePlugin(ElementKeyMap.TERRAIN_DIRT_ID, 14, ElementKeyMap.TERRAIN_ROCK_ID);
		//		minable[2] = new GeneratorResourcePlugin(ElementKeyMap.TERRAIN_LITHIUM_ID, 6, ElementKeyMap.TERRAIN_ROCK_ID);
		//		minable[3] = new GeneratorResourcePlugin(ElementKeyMap.TERRAIN_EXTRANIUM_ID, 1, ElementKeyMap.TERRAIN_ROCK_ID);
		minable[2] = new TerrainDecoCactus(this, ElementKeyMap.TERRAIN_TREE_TRUNK_ID, ElementKeyMap.TERRAIN_CACTUS_ID);
		minable[3] = new GeneratorFloraPlugin(ElementKeyMap.TERRAIN_CACTUS_SMALL_SPRITE, getTop(), getFiller(), getSolid());
		minable[4] = new GeneratorFloraPlugin(ElementKeyMap.TERRAIN_CACTUS_ARCHED_SPRITE, getTop(), getFiller(), getSolid());
		minable[5] = new GeneratorFloraPlugin(ElementKeyMap.TERRAIN_FLOWERS_DESERT_SPRITE, getTop(), getFiller(), getSolid());
		minable[6] = new GeneratorFloraPlugin(ElementKeyMap.TERRAIN_ROCK_SPRITE, getTop(), getFiller(), getSolid());

	}

	@Override
	public void createAdditionalRegions(Random r) {

		int amount = r.nextInt(10);
		if (amount == 0) {
			amount = 3;
		} else if (amount < 3) {
			amount = 2;
		} else {
			amount = 1;
		}
		Region[] regions = new Region[amount * 2];
		int regCount = 0;
		int startingPriority = 5;
		for (int i = 0; i < amount; i++) {
			int posMod = r.nextInt(100) - 50;
			int heightMod = r.nextInt(30);
			int depthMod = r.nextInt(20) - 10;

			PyramidRegion pyramidRegion = new PyramidRegion(r.nextBoolean(), regions, new Vector3i(-50 + posMod, 20 + depthMod, -50 + posMod), new Vector3i(50 + posMod, 60 + heightMod, 50 + posMod), startingPriority--, 0);
			regions[regCount++] = pyramidRegion;
			Vector3i chestPos = new Vector3i(posMod, 20 + depthMod + 2, posMod);
			TresureRegion tresureRegion = new TresureRegion(chestPos, regions, new Vector3i(chestPos.x - 1, chestPos.y, chestPos.z - 1), new Vector3i(chestPos.x + 1, chestPos.y + 1, chestPos.z + 1), 6, 0, (byte) 0);
			regions[regCount++] = tresureRegion;
		}

		for (int i = 0; i < regions.length; i++) {
			regions[i].calculateOverlapping();
		}


		generator.setRegions(regions);
	}

	@Override
	public void createWorld(SegmentController world, Segment w, RequestData requestData) {
		synchronized (this) {
			if (!initialized) {
				generator = new DesertTerrainGenerator(((Planet) world).getSeed());
				generator.setWorldCreator(this);
				generator.setFlatness(1.15000000000000001D);

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
		return ElementKeyMap.TERRAIN_SAND_ID;
	}

	@Override
	public TerrainDeco[] getGen() {
		return minable;
	}

	@Override
	public short getSolid() {
		return ElementKeyMap.TERRAIN_ROCK_NORMAL;
	}

	@Override
	public short getTop() {
		return ElementKeyMap.TERRAIN_SAND_ID;
	}

}
