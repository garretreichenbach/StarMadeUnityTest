package org.schema.game.server.controller.world.factory;

import java.util.Random;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.server.controller.world.factory.regions.Region;
import org.schema.game.server.controller.world.factory.regions.TresureRegion;
import org.schema.game.server.controller.world.factory.regions.city.BuildingRegion;
import org.schema.game.server.controller.world.factory.regions.city.BuildingWorld;
import org.schema.game.server.controller.world.factory.terrain.GeneratorFloraPlugin;
import org.schema.game.server.controller.world.factory.terrain.GeneratorResourcePlugin;
import org.schema.game.server.controller.world.factory.terrain.TerrainDeco;
import org.schema.game.server.controller.world.factory.terrain.TerrainDecoTrees;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class WorldCreatorCubePlanetEarthFactory extends WorldCreatorCubePlanetFactory {

	private TerrainDeco[] minable;

	public WorldCreatorCubePlanetEarthFactory(long seed) {
		super(seed);

		Random r = new Random(seed);

		minable = new TerrainDeco[7];

		minable[0] = new GeneratorResourcePlugin(5, getRandomRockResource(r), ElementKeyMap.TERRAIN_ROCK_NORMAL);
		minable[1] = new GeneratorResourcePlugin(5, getRandomRockResource(r), ElementKeyMap.TERRAIN_ROCK_NORMAL);
		//		minable[2] = new GeneratorResourcePlugin(6, getRandomRockResource(r), ElementKeyMap.TERRAIN_ROCK_ID);
		//		minable[3] = new GeneratorResourcePlugin(3, getRandomRockResource(r), ElementKeyMap.TERRAIN_ROCK_ID);
		minable[2] = new TerrainDecoTrees();
		minable[3] = new GeneratorFloraPlugin(ElementKeyMap.TERRAIN_FLOWERS_BLUE_SPRITE, getTop(), getFiller());
		minable[4] = new GeneratorFloraPlugin(ElementKeyMap.TERRAIN_GRASS_LONG_SPRITE, getTop(), getFiller());
		minable[5] = new GeneratorFloraPlugin(ElementKeyMap.TERRAIN_BERRY_BUSH_SPRITE, getTop(), getFiller());
		minable[6] = new GeneratorFloraPlugin(ElementKeyMap.TERRAIN_FLOWERS_YELLOW_SPRITE, getTop(), getFiller());

	}

	@Override
	public void createAdditionalRegions(Random rand) {
		ObjectArrayList<Region> regionList = new ObjectArrayList<Region>();
		Vector3i chestPos = new Vector3i(8, BuildingRegion.HEIGTH + 6, 8);
		TresureRegion tresureRegion = new TresureRegion(chestPos, null, new Vector3i(chestPos.x - 1, chestPos.y, chestPos.z - 1), new Vector3i(chestPos.x + 1, chestPos.y + 1, chestPos.z + 1), 40, 0, (byte) 0);
		regionList.add(tresureRegion);

		BuildingWorld w = new BuildingWorld(rand, regionList);
		w.reset();

		Object[] elements = regionList.elements();

		Region[] regions = new Region[regionList.size()];

		System.err.println("[EARTH] created extra regions: " + regionList.size());

		for (int i = 0; i < regionList.size(); i++) {
			assert (elements[i] != null);
			regions[i] = (Region) elements[i];
		}

		for (int i = 0; i < regions.length; i++) {
			regions[i].setRegions(regions);
		}
		generator.optimizedRegions = new Object2ObjectOpenHashMap();
		for (int i = 0; i < regions.length; i++) {
			regions[i].calculateOverlappingOptimized(generator.optimizedRegions, 16);
		}

		generator.setRegions(regions);
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
		return ElementKeyMap.TERRAIN_EARTH_TOP_DIRT;
	}

}
