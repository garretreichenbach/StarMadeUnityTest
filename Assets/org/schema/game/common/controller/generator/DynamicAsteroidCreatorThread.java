package org.schema.game.common.controller.generator;

import org.schema.game.common.controller.FloatingRock;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.server.controller.world.factory.asteroid.WorldCreatorFloatingRockFactory;
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructure;
import org.schema.game.server.controller.world.factory.planet.structures.TerrainStructureList;
import org.schema.game.server.controller.world.factory.terrain.GeneratorResourcePlugin;
import org.schema.game.server.controller.world.factory.terrain.TerrainDeco;

import java.util.Random;

import static org.schema.game.common.data.element.ElementKeyMap.*;

/**
 * Adaptable asteroid creator thread for generic system asteroids. Accommodates asteroid material providers and ores based on the asteroid's internal information.
 */
public class DynamicAsteroidCreatorThread extends AsteroidCreatorThread {
    //TODO: is it bad that there are no visual cues? Should certain ore types override this or add their own terrain structures?
    public static short[] rocks = new short[]{
            ElementKeyMap.TERRAIN_ROCK_NORMAL,
            ElementKeyMap.TERRAIN_ROCK_MARS,
            ElementKeyMap.TERRAIN_ICEPLANET_ROCK,
            ElementKeyMap.TERRAIN_ROCK_WHITE,
            ElementKeyMap.TERRAIN_ROCK_BLACK,
            ElementKeyMap.TERRAIN_ROCK_RED,
            ElementKeyMap.TERRAIN_ROCK_ORANGE,
            ElementKeyMap.TERRAIN_ROCK_YELLOW,
            ElementKeyMap.TERRAIN_ROCK_GREEN,
            ElementKeyMap.TERRAIN_ROCK_BLUE,
            ElementKeyMap.TERRAIN_ROCK_PURPLE
    };

    public DynamicAsteroidCreatorThread(final FloatingRock rock) {
        super(rock);
        this.creator = new WorldCreatorFloatingRockFactory(rock.getSeed()) {
            private final short MAIN_MATERIAL_TYPE;
            private final short CRUST_MATERIAL_TYPE;
            private final short SPECKLE_MATERIAL_TYPE;

            private final int CRUST_MATERIAL_INDEX;
            private final int MAIN_MATERIAL_INDEX;
            private int SPECKLE_MATERIAL_INDEX;

            private final boolean useSpeckleMatAsPatches;
            //private TerrainStructureList deco = null; //TODO: need to be able to list types and materials without coords; needs a new list type ("PossibleTerrainStructureList"?)

            FloatingRock.AsteroidOuterMaterialProvider p = rock.getSurfaceMaterials();
            FloatingRock.RockTemperature temp = rock.getTemperatureLevel();

            final Random rndMaterials = new Random(seed);

            {
                MAIN_MATERIAL_TYPE = rocks[rndMaterials.nextInt(rocks.length)];

                short[] crusts = p.getMainMaterials(temp);
                if(crusts.length > 0){
                    CRUST_MATERIAL_TYPE = crusts[rndMaterials.nextInt(crusts.length)];
                }
                else CRUST_MATERIAL_TYPE = TERRAIN_ROCK_NORMAL; //-1 can also be included as a "no block" value in the array, as an option


                short[] specks = p.getSpeckleMaterials(temp);
                if(specks.length > 0){
                    SPECKLE_MATERIAL_TYPE = specks[rndMaterials.nextInt(specks.length)];
                }
                else SPECKLE_MATERIAL_TYPE = -1; //-1 can also be included as a "no block" value in the array, as an option

                this.CRUST_MATERIAL_INDEX = registerBlock(CRUST_MATERIAL_TYPE);
                this.MAIN_MATERIAL_INDEX = registerBlock(MAIN_MATERIAL_TYPE);
                if(SPECKLE_MATERIAL_TYPE != -1){
                    SPECKLE_MATERIAL_INDEX = registerBlock(SPECKLE_MATERIAL_TYPE);
                    useSpeckleMatAsPatches = rndMaterials.nextBoolean();
                } else useSpeckleMatAsPatches = false;
                //TerrainStructureList[] decos = p.getStructures(temp);
                //if(decos.length > 0) deco = decos[random.nextInt(decos.length)];
            }

            @Override
            protected int getRandomSolidType(float density, Random rand) {
                if (density < 0.07F){
                    if(SPECKLE_MATERIAL_TYPE > 0 && rand.nextFloat() < 0.01f) return SPECKLE_MATERIAL_INDEX;
                    return CRUST_MATERIAL_INDEX;
                }
                return MAIN_MATERIAL_INDEX;
            }

            @Override
            public void setMinable(Random rand) {
                byte[] ores = rock.getOres();
                this.minable = new TerrainDeco[ores.length * 2];
                short resourceIndex;
                for (int i = 0; i < ores.length; i++) {
                    resourceIndex = ores[i];
                    this.minable[i] = new GeneratorResourcePlugin(9, resources[resourceIndex], CRUST_MATERIAL_TYPE); //TODO 9...?
                }

                for (int i = 0; i < ores.length; i++) {
                    resourceIndex = ores[i];
                    this.minable[i + ores.length] = new GeneratorResourcePlugin(9, resources[resourceIndex], MAIN_MATERIAL_TYPE);
                }
            }

            @Override
            protected void terrainStructurePlacement(byte x, byte y, byte z, float depth, TerrainStructureList sl, Random rand) {
                if(useSpeckleMatAsPatches && rand.nextFloat() <= 0.004)
                    sl.add(x, y, z, TerrainStructure.Type.Rock, SPECKLE_MATERIAL_TYPE, CRUST_MATERIAL_TYPE, (short)(2+rand.nextInt(6)));

                short itemID;
                byte[] ores = rock.getOres();
                for (int i = 0; i < ores.length; i++) {
                    short oreID = ores[i];
                    itemID = resources[oreID];
                    float resFrequency = rock.getOreFrequencies()[i]/128f;
                    if (rand.nextFloat() <= defaultResourceChance * resFrequency) {
                        sl.add(x, y, z, TerrainStructure.Type.ResourceBlob, itemID, MAIN_MATERIAL_TYPE, defaultResourceSize);
                        sl.add(x, y, z, TerrainStructure.Type.ResourceBlob, itemID, CRUST_MATERIAL_TYPE, defaultResourceSize);
                    }
                }
                //TODO add decos - if(deco != null && rand.nextFloat() <= deco[???].frequency) sl.add(x,y,z,deco[???].type,???,deco[???].material,deco[???].scale)); needs a deco skeleton object w/ frequency variable
            }
        };
    }
}
