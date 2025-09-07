package org.schema.game.common.data.world.planet.terrestrial;

import org.apache.poi.util.NotImplemented;
import org.schema.game.common.data.world.SectorInformation;
import org.schema.game.server.controller.world.factory.terrain.GeneratorFloraPlugin;
import org.schema.game.server.controller.world.factory.terrain.GeneratorResourcePlugin;
import org.schema.game.server.controller.world.factory.terrain.TerrainDeco;
import org.schema.game.server.controller.world.factory.terrain.TerrainDecoTrees;

import static org.schema.game.common.data.element.ElementKeyMap.*;

/**
 * Imported and adapted from "Resources ReSourced" mod by Ithirahad.
 * For future adaptation as planet types to test parametric planets,
 * before transitioning to fully procedural types based on colour families/etc.
 */
@NotImplemented
public enum TerrestrialPlanetGroundTypePredefs implements TerrestrialBodyInformation.TerrestrialPlanetMaterialsProvider {
    COLD_BLUE("chilly planet", TERRAIN_ICEPLANET_SURFACE, TERRAIN_ICEPLANET_ROCK, TERRAIN_ROCK_BLUE, TERRAIN_ICE_ID, TERRAIN_ICE_ID, SectorInformation.PlanetType.FROZEN, TERRAIN_WEEDS_PURPLE_SPRITE),
    COLD_CRYSTAL("frozen planet", TERRAIN_ICEPLANET_SURFACE, TERRAIN_ICEPLANET_ROCK, TERRAIN_ICE_ID, TERRAIN_ICE_ID,  TERRAIN_ICEPLANET_CRYSTAL, SectorInformation.PlanetType.FROZEN, TERRAIN_ICE_CRAG_SPRITE, TERRAIN_FAN_FLOWER_ICE_SPRITE),
    COLD_PERMAFROST("permafrost-covered planet", TERRAIN_ICEPLANET_ROCK, TERRAIN_DIRT_ID, TERRAIN_ROCK_WHITE, TERRAIN_ICE_ID, TERRAIN_ICE_ID, SectorInformation.PlanetType.FROZEN, TERRAIN_ROCK_SPRITE, TERRAIN_FLOWERS_DESERT_SPRITE),
    COLD_ICE("icy planet", TERRAIN_ICE_ID, TERRAIN_ICEPLANET_ROCK, TERRAIN_ROCK_WHITE, TERRAIN_ICE_ID, TERRAIN_ICEPLANET_ROCK, SectorInformation.PlanetType.FROZEN),
    COLD_GLOW("strange frozen planet", TERRAIN_ICE_ID, CRYS_WHITE, TERRAIN_ROCK_WHITE, TERRAIN_ICE_ID, CRYS_WHITE, SectorInformation.PlanetType.FROZEN), //Snowblindness, but it's a planet.

    TEMPERATE_STONY("temperate, rocky world", TERRAIN_EARTH_TOP_ROCK, TERRAIN_ROCK_NORMAL, TERRAIN_ROCK_WHITE, WATER, SectorInformation.PlanetType.EARTH),
    TEMPERATE_DIRT("temperate, dirt-covered planet", TERRAIN_DIRT_ID, TERRAIN_ROCK_YELLOW, TERRAIN_ROCK_NORMAL, WATER, SectorInformation.PlanetType.EARTH),
    TEMPERATE_GRASS("verdant planet", TERRAIN_EARTH_TOP_DIRT, TERRAIN_DIRT_ID, TERRAIN_ROCK_NORMAL, WATER, SectorInformation.PlanetType.EARTH,
            new TerrainDecoTrees(),
            (short) 93,
            (short) 98,
            (short) 102,
            (short) 106),
    TEMPERATE_WEIRD("odd growth-encrusted planet", TERRAIN_PURPLE_ALIEN_VINE, TERRAIN_ROCK_BLACK, TERRAIN_ROCK_NORMAL, WATER, SectorInformation.PlanetType.CORRUPTED),

    DESERT_SANDY("desert planet", TERRAIN_SAND_ID, TERRAIN_SAND_ID, TERRAIN_ROCK_YELLOW, WATER, SectorInformation.PlanetType.DESERT, TERRAIN_CACTUS_SMALL_SPRITE, TERRAIN_CACTUS_ARCHED_SPRITE),
    DESERT_RED("ruddy desert planet", TERRAIN_MARS_TOP, TERRAIN_MARS_DIRT, TERRAIN_ROCK_MARS, WATER, SectorInformation.PlanetType.DESERT, TERRAIN_ROCK_SPRITE, TERRAIN_FUNGAL_GROWTH_SPRITE),
    DESERT_ORANGE("rugged desert planet", TERRAIN_ROCK_ORANGE, TERRAIN_ROCK_WHITE, TERRAIN_ROCK_NORMAL, WATER, SectorInformation.PlanetType.DESERT, TERRAIN_FLOWERS_DESERT_SPRITE),
    DESERT_BLACK("dark desert planet", TERRAIN_ROCK_BLACK, TERRAIN_ROCK_MARS, TERRAIN_ROCK_NORMAL, WATER, SectorInformation.PlanetType.DESERT, TERRAIN_FLOWERS_DESERT_SPRITE, TERRAIN_FLOWERS_YELLOW_SPRITE),
    DESERT_TAN("dusty desert world", TERRAIN_ROCK_YELLOW, TERRAIN_ROCK_YELLOW, TERRAIN_ROCK_NORMAL, WATER, SectorInformation.PlanetType.DESERT, TERRAIN_ROCK_SPRITE, TERRAIN_FLOWERS_DESERT_SPRITE, TERRAIN_CACTUS_SMALL_SPRITE, TERRAIN_CACTUS_ID),

    BARREN_DARK("dark, barren world", TERRAIN_ROCK_MARS, TERRAIN_ROCK_BLACK, TERRAIN_ROCK_BLACK, TERRAIN_ROCK_BLACK, SectorInformation.PlanetType.MESA),
    BARREN_WHITE("bleached, barren world", TERRAIN_ROCK_WHITE, TERRAIN_ROCK_NORMAL, CRYS_WHITE, CRYS_WHITE, SectorInformation.PlanetType.MESA, TERRAIN_ROCK_SPRITE),
    BARREN_GREEN("strange, barren planet", TERRAIN_ROCK_GREEN, TERRAIN_ROCK_GREEN, TERRAIN_ROCK_NORMAL, TERRAIN_ROCK_GREEN, SectorInformation.PlanetType.MESA, TERRAIN_ROCK_SPRITE),
    BARREN_LUNAR("barren planet which may have once been a stray moon", TERRAIN_ROCK_NORMAL, TERRAIN_ROCK_NORMAL, TERRAIN_ROCK_BLACK, TERRAIN_ROCK_WHITE, SectorInformation.PlanetType.MESA, TERRAIN_ROCK_SPRITE),

    METAL_RUST("dense, highly-metallic planet", TERRAIN_MARS_TOP, TERRAIN_ROCK_ORANGE, TERRAIN_ROCK_BLACK,TERRAIN_MARS_DIRT, SectorInformation.PlanetType.MESA, TERRAIN_ROCK_SPRITE, TERRAIN_CORAL_RED_SPRITE),
    METAL_BLACK_STONE("dense, metallic world", TERRAIN_ROCK_MARS, TERRAIN_ROCK_BLACK, TERRAIN_ROCK_NORMAL, TERRAIN_MARS_DIRT, SectorInformation.PlanetType.MESA, TERRAIN_FAN_FLOWER_ICE_SPRITE, TERRAIN_GLOW_TRAP_SPRITE),
    METAL_BLACK_TEKT("metallic planet", TERRAIN_ROCK_BLACK, TERRAIN_ROCK_BLACK, TERRAIN_ROCK_MARS, TERRAIN_ROCK_MARS, SectorInformation.PlanetType.MESA, TERRAIN_WEEDS_PURPLE_SPRITE),

    CRYSTAL_WHITE("gleaming crystal-rich planet", TERRAIN_ROCK_WHITE, CRYS_YELLOW, TERRAIN_ROCK_MARS, CRYS_YELLOW, CRYS_YELLOW, SectorInformation.PlanetType.CORRUPTED),
    CRYSTAL_BLACK("glowing crystal-rich planet", TERRAIN_ROCK_BLACK, CRYS_RED, TERRAIN_ROCK_NORMAL, CRYS_RED, CRYS_YELLOW, SectorInformation.PlanetType.CORRUPTED),
    CRYSTAL_PURPLE("glowing crystal-rich planet", TERRAIN_ROCK_PURPLE, CRYS_WHITE, TERRAIN_ROCK_BLACK, CRYS_WHITE, CRYS_YELLOW, SectorInformation.PlanetType.CORRUPTED),

    FERRON_ORANGE("Ferron-rich, rusty planet", TERRAIN_ROCK_ORANGE, TERRAIN_MARS_DIRT, TERRAIN_ROCK_NORMAL,TERRAIN_MARS_DIRT, SectorInformation.PlanetType.MESA),
    FERRON_BLACK("Ferron-rich planet", TERRAIN_ROCK_BLACK, TERRAIN_ROCK_BLACK, TERRAIN_ROCK_NORMAL,TERRAIN_MARS_DIRT, SectorInformation.PlanetType.MESA),

    MIST("mistbound world", TERRAIN_ROCK_WHITE, CRYS_WHITE, TERRAIN_ROCK_NORMAL, CRYS_WHITE, CRYS_WHITE, SectorInformation.PlanetType.FROZEN),

    HOT_VOLCANIC("scalding hot planet", TERRAIN_ROCK_BLACK, TERRAIN_ROCK_RED, TERRAIN_LAVA_ID, TERRAIN_LAVA_ID, SectorInformation.PlanetType.MESA),
    HOT_LAVA_SEA("world of churning lava", TERRAIN_LAVA_ID, TERRAIN_ROCK_MARS, TERRAIN_ROCK_BLACK, TERRAIN_ROCK_BLACK, SectorInformation.PlanetType.MESA),
    HOT_RED("burning hot world", TERRAIN_ROCK_RED, TERRAIN_MARS_DIRT, TERRAIN_ROCK_NORMAL,TERRAIN_LAVA_ID, SectorInformation.PlanetType.MESA),

    EXTRADIMENSIONAL("extradimensional planetoid", TERRAIN_ROCK_BLACK, TERRAIN_PURPLE_ALIEN_ROCK, CRYS_YELLOW, CRYS_BLACK, SectorInformation.PlanetType.CORRUPTED),
    ;
    private final String description;
    private final short surface;
    private final short soil;
    private final short substrate; //TODO: Maybe ignore this and use random rock tbh lol
    private final short ocean;
    private final short caveBottomLiquid;
    private final SectorInformation.PlanetType oldEnumType;
    private final TerrainDeco[] decos;

    public static final TerrestrialPlanetGroundTypePredefs[] COLD_TYPES = {COLD_BLUE, COLD_CRYSTAL, COLD_GLOW, COLD_ICE, COLD_PERMAFROST};
    public static final TerrestrialPlanetGroundTypePredefs[] TEMPERATE_TYPES = {TEMPERATE_DIRT, TEMPERATE_GRASS, TEMPERATE_STONY, TEMPERATE_WEIRD};
    public static final TerrestrialPlanetGroundTypePredefs[] HOT_TYPES = {HOT_VOLCANIC, HOT_LAVA_SEA, HOT_RED};
    public static final TerrestrialPlanetGroundTypePredefs[] DESERT_TYPES = {DESERT_BLACK, DESERT_ORANGE, DESERT_RED, DESERT_SANDY, DESERT_TAN};
    public static final TerrestrialPlanetGroundTypePredefs[] BARREN_TYPES = {BARREN_DARK, BARREN_LUNAR, BARREN_WHITE, BARREN_GREEN};
    public static final TerrestrialPlanetGroundTypePredefs[] CRYSTAL_TYPES = {CRYSTAL_BLACK, CRYSTAL_WHITE, CRYSTAL_PURPLE};
    public static final TerrestrialPlanetGroundTypePredefs[] FERRON_TYPES = {FERRON_ORANGE, FERRON_BLACK};
    public static final TerrestrialPlanetGroundTypePredefs[] METAL_TYPES = {METAL_RUST, METAL_BLACK_STONE, METAL_BLACK_TEKT};

    TerrestrialPlanetGroundTypePredefs(String description, int surface, int soil, int substrate, int ocean, SectorInformation.PlanetType correspondingType) {
        this.description = description;
        this.surface = (short) surface;
        this.soil = (short) soil;
        this.substrate = (short) substrate;
        this.oldEnumType = correspondingType;
        this.decos = new GeneratorResourcePlugin[]{};
        this.caveBottomLiquid = TERRAIN_LAVA_ID;
        this.ocean = (short) ocean;
    }

    TerrestrialPlanetGroundTypePredefs(String description, int surface, int soil, int substrate, int ocean, int caveBottomLiquid, SectorInformation.PlanetType correspondingType) {
        this.description = description;
        this.surface = (short) surface;
        this.soil = (short) soil;
        this.substrate = (short) substrate;
        this.oldEnumType = correspondingType;
        this.decos = new GeneratorResourcePlugin[]{};
        this.caveBottomLiquid = (short) caveBottomLiquid;
        this.ocean = (short) ocean;
    }

    TerrestrialPlanetGroundTypePredefs(String description, int surface, int soil, int substrate, int ocean, SectorInformation.PlanetType correspondingType, TerrainDeco... decos) {
        this.description = description;
        this.surface = (short) surface;
        this.soil = (short) soil;
        this.substrate = (short) substrate;
        this.oldEnumType = correspondingType;
        this.decos = decos;
        this.caveBottomLiquid = TERRAIN_LAVA_ID;
        this.ocean = (short) ocean;
    }

    TerrestrialPlanetGroundTypePredefs(String description, int surface, int soil, int substrate, int ocean, SectorInformation.PlanetType correspondingType, TerrainDeco deco, short... flora) {
        this.description = description;
        this.surface = (short) surface;
        this.soil = (short) soil;
        this.substrate = (short) substrate;
        this.oldEnumType = correspondingType;
        TerrainDeco[] decoGen = new TerrainDeco[flora.length + 1];
        for (short i = 0; i < flora.length; i++) {
            decoGen[i] = new GeneratorFloraPlugin(flora[i], (short) surface, (short) soil);
        }
        decoGen[flora.length] = deco;
        this.decos = decoGen;
        this.caveBottomLiquid = TERRAIN_LAVA_ID;
        this.ocean = (short) ocean;
    }

    TerrestrialPlanetGroundTypePredefs(String description, int surface, int soil, int substrate, int ocean, SectorInformation.PlanetType correspondingType, short... flora) {
        this.description = description;
        this.surface = (short) surface;
        this.soil = (short) soil;
        this.substrate = (short) substrate;
        this.oldEnumType = correspondingType;
        TerrainDeco[] floraGen = new TerrainDeco[flora.length];
        for (short i = 0; i < flora.length; i++) {
            floraGen[i] = new GeneratorFloraPlugin(flora[i], (short) surface, (short) soil);
        }
        this.decos = floraGen;
        this.caveBottomLiquid = TERRAIN_LAVA_ID;
        this.ocean = (short) ocean;
    }

    TerrestrialPlanetGroundTypePredefs(String description, int surface, int soil, int substrate, int ocean, int caveBottomLiquid, SectorInformation.PlanetType correspondingType, short... flora) {
        this.description = description;
        this.surface = (short) surface;
        this.soil = (short) soil;
        this.substrate = (short) substrate;
        this.oldEnumType = correspondingType;
        TerrainDeco[] floraGen = new TerrainDeco[flora.length];
        for (short i = 0; i < flora.length; i++) {
            floraGen[i] = new GeneratorFloraPlugin(flora[i], (short) surface, (short) soil);
        }
        this.decos = floraGen;
        this.caveBottomLiquid = (short) caveBottomLiquid;
        this.ocean = (short) ocean;
    }

    public String getDescription() {
        return description;
    }

    public short getSurface() {
        return surface;
    }

    public short getSoil() {
        return soil;
    }

    public short getSubstrate() {
        return substrate;
    }

    public short getCaveBottomLiquid() {
        return caveBottomLiquid;
    }

    public SectorInformation.PlanetType getOldEnumType() {
        return oldEnumType;
    }

    public TerrainDeco[] getDecos() {
        return decos;
    }

    public short getOcean() {
        return ocean;
    }
}
