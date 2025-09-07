package org.schema.game.server.controller.world.factory.asteroid.old;

import java.util.Random;

import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.server.controller.world.factory.asteroid.WorldCreatorFloatingRockFactory;
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructure;
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructureList;
import org.schema.game.server.controller.world.factory.terrain.GeneratorResourcePlugin;
import org.schema.game.server.controller.world.factory.terrain.TerrainDeco;

public class WorldCreatorFloatingRockRockyPurpleFactory extends WorldCreatorFloatingRockFactory {

	public WorldCreatorFloatingRockRockyPurpleFactory(long seed) {
		super(seed);
	}

	private final static int ROCK_PURPLE = registerBlock(ElementKeyMap.TERRAIN_ROCK_PURPLE);
	private final static int LAVA = registerBlock(ElementKeyMap.TERRAIN_LAVA_ID);

	@Override
	protected int getRandomSolidType(float density, Random rand) {

		if (density > 0.3f && rand.nextFloat() > 0.68f)
			return LAVA;

		return ROCK_PURPLE;
	}

	@Override
	public void setMinable(Random rand) {
		this.minable = new TerrainDeco[2];
		this.minable[0] = new GeneratorResourcePlugin(9, ElementKeyMap.RESS_CRYS_SINTYR, ElementKeyMap.TERRAIN_ROCK_PURPLE);
		this.minable[1] = new GeneratorResourcePlugin(9, ElementKeyMap.RESS_ORE_THRENS, ElementKeyMap.TERRAIN_ROCK_PURPLE);
		//		this.minable[0] = new GeneratorResourcePlugin(ElementKeyMap.TERRAIN_NEGAGATE_ID , 6, ElementKeyMap.TERRAIN_PURPLE_ALIEN_VINE);
		//		this.minable[1] = new GeneratorResourcePlugin(ElementKeyMap.TERRAIN_QUANTACIDE_ID , 6, ElementKeyMap.TERRAIN_PURPLE_ALIEN_VINE);
		//		this.minable[2] = new GeneratorResourcePlugin(ElementKeyMap.TERRAIN_INSANIUNM_ID , 9, ElementKeyMap.TERRAIN_PURPLE_ALIEN_VINE);
	}

	@Override
	protected void terrainStructurePlacement(byte x, byte y, byte z, float depth, TerrainStructureList sl, Random rand) {		
			
		if (rand.nextFloat() <= defaultResourceChance)			
			sl.add(x, y, z, TerrainStructure.Type.ResourceBlob, ElementKeyMap.RESS_CRYS_SINTYR, ElementKeyMap.TERRAIN_ROCK_PURPLE, defaultResourceSize);
		if (rand.nextFloat() <= defaultResourceChance)			
			sl.add(x, y, z, TerrainStructure.Type.ResourceBlob, ElementKeyMap.RESS_ORE_THRENS, ElementKeyMap.TERRAIN_ROCK_PURPLE, defaultResourceSize);
		
	}

}

