package org.schema.game.server.controller.world.factory.asteroid.old;

import java.util.Random;

import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.server.controller.world.factory.asteroid.WorldCreatorFloatingRockFactory;
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructure;
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructureList;
import org.schema.game.server.controller.world.factory.terrain.GeneratorResourcePlugin;
import org.schema.game.server.controller.world.factory.terrain.TerrainDeco;

public class WorldCreatorFloatingRockRockyIceCoreFactory extends WorldCreatorFloatingRockFactory {

	public WorldCreatorFloatingRockRockyIceCoreFactory(long seed) {
		super(seed);
	}

	private final static int ROCK_ORANGE = registerBlock(ElementKeyMap.TERRAIN_ROCK_ORANGE);
	private final static int ICE = registerBlock(ElementKeyMap.TERRAIN_ICE_ID);

	@Override
	protected int getRandomSolidType(float density, Random rand) {

		if (density < 0.01f && rand.nextFloat() > 0.8f)
			return ICE;

		return ROCK_ORANGE;
	}

	@Override
	public void setMinable(Random rand) {
		this.minable = new TerrainDeco[2];
		this.minable[0] = new GeneratorResourcePlugin(9, ElementKeyMap.RESS_CRYS_CRYSTAL_COMMON, ElementKeyMap.TERRAIN_ROCK_ORANGE);
		this.minable[1] = new GeneratorResourcePlugin(9, ElementKeyMap.RESS_ORE_SAPSUN, ElementKeyMap.TERRAIN_ROCK_ORANGE);
		//		this.minable[0] = new GeneratorResourcePlugin(ElementKeyMap.TERRAIN_EXTRANIUM_ID , 6, ElementKeyMap.TERRAIN_ROCK_ID);
		//		this.minable[1] = new GeneratorResourcePlugin(ElementKeyMap.TERRAIN_INSANIUNM_ID , 6, ElementKeyMap.TERRAIN_ROCK_ID);
		//		this.minable[2] = new GeneratorResourcePlugin(ElementKeyMap.TERRAIN_METATE_ID , 8, ElementKeyMap.TERRAIN_ICE_ID);
	}

	@Override
	protected void terrainStructurePlacement(byte x, byte y, byte z, float depth, TerrainStructureList sl, Random rand) {		
			
		if (rand.nextFloat() <= defaultResourceChance)			
			sl.add(x, y, z, TerrainStructure.Type.ResourceBlob, ElementKeyMap.RESS_CRYS_CRYSTAL_COMMON, ElementKeyMap.TERRAIN_ROCK_ORANGE, defaultResourceSize);
		if (rand.nextFloat() <= defaultResourceChance)			
			sl.add(x, y, z, TerrainStructure.Type.ResourceBlob, ElementKeyMap.RESS_ORE_SAPSUN, ElementKeyMap.TERRAIN_ROCK_ORANGE, defaultResourceSize);
		
	}

}

