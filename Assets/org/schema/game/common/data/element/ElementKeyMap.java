package org.schema.game.common.data.element;

import api.StarLoaderHooks;
import api.common.GameCommon;
import api.config.BlockConfig;
import api.element.block.Blocks;
import api.element.recipe.FixedRecipeBuilder;
import api.listener.events.register.BlockRecyclerRecipeCreateEvent;
import api.mod.StarLoader;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import org.schema.common.ParseException;
import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.common.Starter;
import org.schema.game.common.data.player.inventory.InventorySlot;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.util.GuiErrorHandler;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.resource.FileExt;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;

import static org.schema.game.common.data.element.ElementInformation.FAC_BLOCK;

/**
 * Contains a bunch of commonly used element keys and their values.
 * <br/><br/>
 * NOTE: Please use the Blocks.java class for accessing elements in the game rather than this class in future, as this is not a
 * complete list and some element ids may have been reassigned or removed since this was written.
 *
 * @author Schema
 */
public class ElementKeyMap {
	public static final Short2ObjectOpenHashMap<String> nameTranslations = new Short2ObjectOpenHashMap<String>();
	public static final Short2ObjectOpenHashMap<String> descriptionTranslations = new Short2ObjectOpenHashMap<String>();

	public static final short WEAPON_CONTROLLER_ID = 6;
	public static final short WEAPON_ID = 16;
	public static final short CORE_ID = 1;
	public static final short DEATHSTAR_CORE_ID = 65;
	public static final short HULL_ID = 5;
	public static final short GLASS_ID = 63;
	public static final short THRUSTER_ID = 8;
	public static final short TURRET_DOCK_ID = 7;
	public static final short TURRET_DOCK_ENHANCE_ID = 88;
	public static final short POWER_ID_OLD = 2;
	public static final short POWER_CAP_ID = 331;
	public static final short SHIELD_CAP_ID = 3;
	public static final short SHIELD_REGEN_ID = 478;
	public static final short EXPLOSIVE_ID = 14;
	public static final short STEALTH_COMPUTER = 22;
	public static final short STEALTH_MODULE = 15;
	public static final short SALVAGE_ID = 24;
	public static final short MISSILE_DUMB_CONTROLLER_ID = 38;
	public static final short MISSILE_DUMB_ID = 32;
	public static final short SHIELD_DRAIN_CONTROLLER_ID = 46;
	public static final short SHIELD_DRAIN_MODULE_ID = 40;
	public static final short SHIELD_SUPPLY_CONTROLLER_ID = 54;
	public static final short SHIELD_SUPPLY_MODULE_ID = 48;
	public static final short SALVAGE_CONTROLLER_ID = 4;
	public static final short GRAVITY_ID = 56;
	public static final short GRAVITY_EXIT_ID = 1738;
	public static final short REPAIR_ID = 30;
	public static final short REPAIR_CONTROLLER_ID = 39;
	public static final short COCKPIT_ID = 47;
	public static final short LIGHT_ID = 55;
	public static final short TERRAIN_ICE_ID = 64;
	public static final short HULL_COLOR_PURPLE_ID = 69;
	public static final short HULL_COLOR_BROWN_ID = 70;
	public static final short HULL_COLOR_BLACK_ID = 75;
	public static final short HULL_COLOR_RED_ID = 76;
	public static final short HULL_COLOR_BLUE_ID = 77;
	public static final short HULL_COLOR_GREEN_ID = 78;
	public static final short HULL_COLOR_YELLOW_ID = 79;
	public static final short HULL_COLOR_WHITE_ID = 81;
	public static final short LANDING_ELEMENT = 112;
	public static final short LIFT_ELEMENT = 113;
	public static final short RECYCLER_ELEMENT_OLD = 114; //deprecated
	public static final short STASH_ELEMENT = 120;
	public static final short AI_ELEMENT = 121;
	public static final short DOOR_ELEMENT = 122;
	public static final short BUILD_BLOCK_ID = 123;
	public static final short TERRAIN_LAVA_ID = 80;
	public static final short TERRAIN_GOLD_ID = 128;
	public static final short TERRAIN_IRIDIUM_ID = 128 + 1;
	public static final short TERRAIN_MERCURY_ID = 128 + 2;
	public static final short TERRAIN_PALLADIUM_ID = 128 + 3;
	public static final short TERRAIN_PLATINUM_ID = 128 + 4;
	public static final short TERRAIN_LITHIUM_ID = 128 + 5;
	public static final short TERRAIN_MAGNESIUM_ID = 128 + 6;
	public static final short TERRAIN_TITANIUM_ID = 128 + 7;
	public static final short TERRAIN_URANIUM_ID = 128 + 8;
	public static final short TERRAIN_POLONIUM_ID = 128 + 9;
	public static final short TERRAIN_EXTRANIUM_ID = 72;
	public static final short TERRAIN_INSANIUNM_ID = 210;
	public static final short TERRAIN_METATE_ID = 209;
	public static final short TERRAIN_NEGAGATE_ID = 208;
	public static final short TERRAIN_QUANTACIDE_ID = 207;
	public static final short TERRAIN_NEGACIDE_ID = 206;
	public static final short TERRAIN_MARS_TOP = 128 + 10;
	public static final short TERRAIN_MARS_DIRT = 128 + 12;
	public static final short TERRAIN_ROCK_NORMAL = 73;
	public static final short TERRAIN_ROCK_MARS = 139;
	public static final short TERRAIN_ROCK_BLUE = 143;
	public static final short TERRAIN_ROCK_ORANGE = 151;
	public static final short TERRAIN_ROCK_YELLOW = 155;
	public static final short TERRAIN_ROCK_WHITE = 159;
	public static final short TERRAIN_ROCK_PURPLE = 163;
	public static final short TERRAIN_ROCK_RED = 171;
	public static final short TERRAIN_ROCK_GREEN = 179;
	public static final short TERRAIN_ROCK_BLACK = 203;
	public static final short TERRAIN_SAND_ID = 74;
	public static final short TERRAIN_EARTH_TOP_DIRT = 82;
	public static final short TERRAIN_EARTH_TOP_ROCK = 83;
	public static final short TERRAIN_TREE_TRUNK_ID = 84;
	public static final short TERRAIN_TREE_LEAF_ID = 85;

	//	143 - 151 - 155 - 159 - 163 - 171 - 179 - 203
	public static final short TERRAIN_WATER = 86;
	public static final short TERRAIN_DIRT_ID = 87;
	public static final short TERRAIN_VINES_ID = TERRAIN_TREE_LEAF_ID;
	public static final short TERRAIN_CACTUS_ID = 89;
	public static final short TERRAIN_PURPLE_ALIEN_TOP = 90;
	public static final short TERRAIN_PURPLE_ALIEN_ROCK = 91;
	public static final short TERRAIN_PURPLE_ALIEN_VINE = 92;
	public static final short WATER = 86;
	public static final short PLAYER_SPAWN_MODULE = 94;
	public static final short LIGHT_BULB_YELLOW = 340;
	//earth like sprite stuff
	public static final short TERRAIN_FLOWERS_BLUE_SPRITE = 93;
	public static final short TERRAIN_GRASS_LONG_SPRITE = 98;
	public static final short TERRAIN_BERRY_BUSH_SPRITE = 102;
	public static final short TERRAIN_FLOWERS_YELLOW_SPRITE = 106;
	//desert sprite stuff
	public static final short TERRAIN_CACTUS_SMALL_SPRITE = 95;
	public static final short TERRAIN_CACTUS_ARCHED_SPRITE = 103;
	public static final short TERRAIN_FLOWERS_DESERT_SPRITE = 99;
	public static final short TERRAIN_ROCK_SPRITE = 107;
	//mars sprite stuff
	public static final short TERRAIN_CORAL_RED_SPRITE = 96;
	public static final short TERRAIN_SHROOM_RED_SPRITE = 104;
	public static final short TERRAIN_FUNGAL_GROWTH_SPRITE = 100;
	public static final short TERRAIN_FUNGAL_TRAP_SPRITE = 108;
	//columny sprite stuff
	public static final short TERRAIN_FLOWER_FAN_PURPLE_SPRITE = 97;
	public static final short TERRAIN_GLOW_TRAP_SPRITE = 101;
	public static final short TERRAIN_WEEDS_PURPLE_SPRITE = 105;
	public static final short TERRAIN_YHOLE_PURPLE_SPRITE = 109;
	//ice sprite stuff
	public static final short TERRAIN_FAN_FLOWER_ICE_SPRITE = 278;
	public static final short TERRAIN_ICE_CRAG_SPRITE = 279;
	public static final short TERRAIN_CORAL_ICE_SPRITE = 280;
	public static final short TERRAIN_SNOW_BUD_SPRITE = 281;
	//FACTORY
	public static final short FACTORY_COMPONENT_FAB_ID = 211;
	public static final short FACTORY_CHEMICAL_ID = 259;
	public static final short FACTORY_INPUT_ENH_ID = 212;
	public static final short FACTORY_CAPSULE_REFINERY_ID = 213;
	public static final short FACTORY_CAPSULE_REFINERY_ADV_ID = 214;
	public static final short FACTORY_MICRO_ASSEMBLER_ID = 215;
	public static final short FACTORY_BLOCK_RECYCLER_ID = 216;
	public static final short FACTORY_BLOCK_ASSEMBLER_ID = 217;
	public static final short FACTORY_GAS_EXTRACTOR = 218;
	public static final short FACTORY_CORE_EXTRACTOR = 262;

	//ICE TERRAIN
	public static final short TERRAIN_ICEPLANET_SURFACE = 274;
	public static final short TERRAIN_ICEPLANET_ROCK = 275;
	public static final short TERRAIN_ICEPLANET_WOOD = 276;
	public static final short TERRAIN_ICEPLANET_LEAVES = 277;

	public static final short LIGHT_RED = 282;
	public static final short LIGHT_BLUE = 283;
	public static final short LIGHT_GREEN = 284;
	public static final short LIGHT_YELLOW = 285;
	public static final short TERRAIN_ICEPLANET_CRYSTAL = 286;
	public static final short TERRAIN_REDWOOD = 287;
	public static final short TERRAIN_REDWOOD_LEAVES = 288;
	public static final short FIXED_DOCK_ID = 289;
	public static final short FIXED_DOCK_ID_ENHANCER = 290;
	public static final short FACTION_BLOCK = 291;
	public static final short FACTION_HUB_BLOCK = 292;
	public static final short DECORATIVE_PANEL_1 = 336;
	public static final short DECORATIVE_PANEL_2 = 337;
	public static final short DECORATIVE_PANEL_3 = 338;
	public static final short DECORATIVE_PANEL_4 = 339;

	//MORE SYSTEMS
	public static final short POWER_CELL = 219;
	public static final short POWER_COIL = 220;
	public static final short POWER_DRAIN_BEAM_COMPUTER = 332;
	public static final short POWER_DRAIN_BEAM_MODULE = 333;
	public static final short POWER_SUPPLY_BEAM_COMPUTER = 334;
	public static final short POWER_SUPPLY_BEAM_MODULE = 335;
	public static final short PUSH_PULSE_CONTROLLER_ID = 344;
	public static final short PUSH_PULSE_ID = 345;
	public static final short FACTION_PUBLIC_EXCEPTION_ID = 346;
	public static final short FACTION_FACTION_EXCEPTION_ID = 936;
	public static final short SHOP_BLOCK_ID = 347;
	public static final short ACTIVAION_BLOCK_ID = 405;
	public static final short SIGNAL_DELAY_NON_REPEATING_ID = 406;
	public static final short SIGNAL_DELAY_BLOCK_ID = 407;
	public static final short SIGNAL_AND_BLOCK_ID = 408;
	public static final short SIGNAL_OR_BLOCK_ID = 409;
	public static final short SIGNAL_NOT_BLOCK_ID = 410;
	public static final short SIGNAL_TRIGGER_AREA = 411;
	public static final short SIGNAL_TRIGGER_STEPON = 412;
	public static final short SIGNAL_TRIGGER_AREA_CONTROLLER = 413;
	public static final short DAMAGE_BEAM_COMPUTER = 414;
	public static final short DAMAGE_BEAM_MODULE = 415;
	public static final short DAMAGE_PULSE_COMPUTER = 416;
	public static final short DAMAGE_PULSE_MODULE = 417;
	public static final short EFFECT_PIERCING_COMPUTER = 418;
	public static final short EFFECT_PIERCING_MODULE = 419;
	public static final short EFFECT_EXPLOSIVE_COMPUTER = 420;
	public static final short EFFECT_EXPLOSIVE_MODULE = 421;
	public static final short EFFECT_PUNCHTHROUGH_COMPUTER = 422;
	public static final short EFFECT_PUNCHTHROUGH_MODULE = 423;
	public static final short EFFECT_EMP_COMPUTER = 424;
	public static final short EFFECT_EMP_MODULE = 425;
	public static final short EFFECT_STOP_COMPUTER = 460;
	public static final short EFFECT_STOP_MODULE = 461;
	public static final short EFFECT_PUSH_COMPUTER = 462;
	public static final short EFFECT_PUSH_MODULE = 463;
	public static final short EFFECT_PULL_COMPUTER = 464;
	public static final short EFFECT_PULL_MODULE = 465;
	public static final short EFFECT_ION_COMPUTER = 466;
	public static final short EFFECT_ION_MODULE = 467;
	public static final short EFFECT_OVERDRIVE_COMPUTER = 476;
	public static final short EFFECT_OVERDRIVE_MODULE = 477;
	public static final short TEXT_BOX = 479;
	public static final short WARP_GATE_CONTROLLER = 542;
	public static final short WARP_GATE_MODULE = 543;
	public static final short JUMP_DRIVE_CONTROLLER = 544;
	public static final short JUMP_DRIVE_MODULE = 545;
	public static final short MEDICAL_SUPPLIES = 445;
	public static final short MEDICAL_CABINET = 446;
	public static final short SCRAP_ALLOYS = 546;
	public static final short SCRAP_COMPOSITE = 547;
	public static final short SCANNER_COMPUTER = 654;
	public static final short SCANNER_MODULE = 655;
	public static final short METAL_MESH = 440;
	public static final short CRYSTAL_CRIRCUITS = 220;
	public static final short LOGIC_BUTTON_NORM = 666;
	public static final short LOGIC_FLIP_FLOP = 667;
	public static final short LOGIC_WIRELESS = 668;
	public static final short SHIPYARD_COMPUTER = 677;
	public static final short SHIPYARD_MODULE = 678;
	public static final short SHIPYARD_CORE_POSITION = 679;
	public static final short REPULSE_MODULE = 1126;

	public static final short INTELL_COMPUTER = 1820;
	public static final short INTELL_ANTENNA = 1821;

	public static final short JUMP_INHIBITOR_COMPUTER = 681;
	public static final short JUMP_INHIBITOR_MODULE = 682;

	//ORIENTATION MAPPING
	@Deprecated
	public static final short Hattel = 1;
	@Deprecated
	public static final short Sintyr = 2;
	@Deprecated
	public static final short Mattise = 3;
	@Deprecated
	public static final short Fertikeen = 14;
	@Deprecated
	public static final short Sertise = 12;

	public static final short Rammet = 4;
	public static final short Varat = 5;
	public static final short Bastyn = 6; //now gas, shouldn't really be used
	public static final short CommonCrystal = 7;
	public static final short Nocx = 8;

	public static final short Threns = 9;
	public static final short Jisper = 10;
	public static final short Zercaner = 11; //now gas, shouldn't really be used
	public static final short Hylat = 13;
	public static final short Sapsun = 15;
	public static final short CommonMetal = 16;

	public static final short Quantanium = 17;
	public static final short Metate = 18;
	public static final short Exogen = 19;

	public static final short CRYS_PURPLE = 452;
	public static final short CRYS_BLACK = 453;
	public static final short CRYS_WHITE = 454;
	public static final short CRYS_YELLOW = 455;
	public static final short CRYS_RED = 456;
	public static final short CRYS_ORANGE = 457;
	public static final short CRYS_GREEN = 458;
	public static final short CRYS_BLUE = 459;

	public static final short RESS_CRYS_CRYSTAL_COMMON = 486;
	public static final short RESS_ORE_METAL_COMMON = 495;

	@Deprecated
	public static final short RESS_CRYS_HATTEL = 480;
	@Deprecated
	public static final short RESS_CRYS_SINTYR = 481;
	@Deprecated
	public static final short RESS_CRYS_MATTISE = 482;
	public static final short RESS_CRYS_RAMMET = 483;
	public static final short RESS_CRYS_VARAT = 484;
	public static final short RESS_GAS_BASTYN = 485;

	public static final short RESS_CRYS_NOCX = 487;
	public static final short RESS_ORE_THRENS = 488;
	public static final short RESS_ORE_JISPER = 489;
	public static final short RESS_GAS_ZERCANER = 490;
	@Deprecated
	public static final short RESS_ORE_SERTISE = 491;
	@Deprecated
	public static final short RESS_ORE_HYLAT = 492;
	@Deprecated
	public static final short RESS_ORE_FERTIKEEN = 493;
	public static final short RESS_ORE_SAPSUN = 494;

	public static final short RESS_RARE_EXOGEN = 257;
	public static final short RESS_RARE_METATE = 256;
	public static final short RESS_RARE_QUANTANIUM = 255;

	public static final short RAIL_BLOCK_BASIC = 662;
	public static final short RAIL_BLOCK_DOCKER = 663;
	public static final short RAIL_BLOCK_CW = 664;
	public static final short RAIL_BLOCK_CCW = 669;
	public static final short RAIL_BLOCK_TURRET_Y_AXIS = 665;
	public static final short RAIL_RAIL_SPEED_CONTROLLER = 672;
	public static final short RAIL_MASS_ENHANCER = 671;
	public static final short RAIL_LOAD = 1104;
	public static final short RAIL_UNLOAD = 1105;
	public static final short LOGIC_REMOTE_INNER = 670;

	public static final short RACE_GATE_CONTROLLER = 683;
	public static final short RACE_GATE_MODULE = 684;
	public static final short ACTIVATION_GATE_CONTROLLER = 685;
	public static final short ACTIVATION_GATE_MODULE = 686;
	public static final short TRANSPORTER_CONTROLLER = 687;
	public static final short TRANSPORTER_MODULE = 688;

	public static final short CARGO_SPACE = 689;

	public static final short POWER_BATTERY = 978;

	public static final short PICKUP_AREA = 937;
	public static final short PICKUP_RAIL = 938;
	public static final short EXIT_SHOOT_RAIL = 939;

	public static final short SIGNAL_RANDOM = 979;
	public static final short SIGNAL_SENSOR = 980;

	public static final short BLUEPRINT_EMPTY = 999;

	public static final short REACTOR_STABILIZER_STREAM_NODE = 66;

	public static final short REACTOR_MAIN = 1008;
	public static final short REACTOR_STABILIZER = 1009;
	public static final short REACTOR_CONDUIT = 1010;
	public static final short REACTOR_CHAMBER_MOBILITY = 1011;
	public static final short REACTOR_CHAMBER_SCANNER = 1012;
	public static final short REACTOR_CHAMBER_JUMP = 1013;
	public static final short REACTOR_CHAMBER_STEALTH = 1014;
	public static final short REACTOR_CHAMBER_LOGISTICS = 1015;
	public static final short REACTOR_CHAMBER_JUMP_DISTANCE_0 = 1100;
	public static final short REACTOR_CHAMBER_JUMP_DISTANCE_1 = 1101;
	public static final short REACTOR_CHAMBER_JUMP_DISTANCE_2 = 1102;
	public static final short REACTOR_CHAMBER_JUMP_DISTANCE_3 = 1103;

	//Crew Nodes
	public static final short CREW_PATH_NODE = 1701;
	public static final short CREW_AVOID_NODE = 1702;

	//Crew Modules
	public static final short GAS_SCOOP_CONTROLLER = 1818;
	public static final short GAS_SCOOP_MODULE = 1819;

	public static boolean isInventory(short id) {
		return id == STASH_ELEMENT || id == SHIPYARD_COMPUTER || id == Blocks.LOCK_BOX.getId() || id == SHOP_BLOCK_ID;
	}

	//INSERTED CODE
	//TODO Find a better way than just increasing the array size
	public static final short[] resources = new short[32];
	///
	public static final short[] orientationToResIDMapping = new short[32];
	public static final byte[] resIDToOrientationMapping = new byte[4096];

	public static final short MINE_CORE = 37;
	public static final short MINE_LAYER = 41;

	public static final short EFFECT_EM_COMPUTER = 349;
	public static final short EFFECT_EM = 350;
	public static final short EFFECT_HEAT_COMPUTER = 351;
	public static final short EFFECT_HEAT = 352;
	public static final short EFFECT_KINETIC_COMPUTER = 353;
	public static final short EFFECT_KINETIC = 354;

	public static final short MINE_TYPE_CANNON = 355;
	public static final short MINE_TYPE_MISSILE = 356;
	public static final short MINE_TYPE_PROXIMITY = 358;
	public static final short MINE_TYPE_D = 359;
	public static final short TRACTOR_BEAM_COMPUTER = 360;
	public static final short TRACTOR_BEAM = 361;

	public static final short MINE_MOD_STRENGTH = 363;
	public static final short MINE_MOD_PERSONAL = 364;
	public static final short MINE_MOD_FRIENDS = 365;
	public static final short MINE_MOD_STEALTH = 366;

	public static final short CANNON_CAPACITY_MODULE = 1816;
	public static final short BEAM_CAPACITY_MODULE = 1817;
	public static final short MISSILE_CAPACITY_MODULE = 362;
	public static final short REPAIR_PASTE_MODULE = 1139;

	public static final short LONG_RANGE_SCANNER_COMPUTER = 1818;
	public static final short LONG_RANGE_SCANNER_MODULE = 1819;

	/**
	 * corresponding texture position (starting with 1) from block orientation
	 */
	public static final int[] orientationToResOverlayMapping = new int[32];
	public static final ShortOpenHashSet keySet = new ShortOpenHashSet(256);
	public static final ShortArrayList doorTypes = new ShortArrayList();
	public static final ShortArrayList inventoryTypes = new ShortArrayList();
	public static final ShortArrayList chamberAnyTypes = new ShortArrayList();
	public static final ShortArrayList chamberGeneralTypes = new ShortArrayList();
	public static final ShortArrayList lightTypes = new ShortArrayList();
	public static final ShortArrayList sourcedTypes = new ShortArrayList();
	public static final Short2ObjectOpenHashMap<ElementInformation> informationKeyMap = new Short2ObjectOpenHashMap<ElementInformation>();
	private static final ShortOpenHashSet factoryKeySet = new ShortOpenHashSet(256);
	private static final ShortOpenHashSet leveldKeySet = new ShortOpenHashSet(256);
	private static final Short2ObjectOpenHashMap<ElementInformation> projected = new Short2ObjectOpenHashMap<ElementInformation>();
	public static int highestType;
	public static final short MAX_TYPE_ID = 8191; // 8192 is the maximum type id, as defined in ElementInformation.java
	public static final short MOD_BLOCKS_START = 4096; //~4k blocks for vanilla and ~4k for mods should be plenty
	public static final short MOD_BLOCKS_END = 8191;
	public static ElementInformation[] infoArray;
	public static boolean[] factoryInfoArray;
	public static boolean[] validArray;
	public static boolean[] lodShapeArray;
	public static short[] signalArray;
	public static short[] signaledByRailArray;
	public static boolean initialized;
	public static Properties properties;
	public static FixedRecipes fixedRecipes;
	public static FixedRecipe capsuleRecipe;
	public static FixedRecipe advCapsuleRecipe;
	public static FixedRecipe microAssemblerRecipe;
	public static FixedRecipe recyclerRecipe;
	public static FixedRecipe macroBlockRecipe;
	public static ObjectArrayList<ElementInformation> sortedByName;
	public static FixedRecipe personalMeshAndCompositeRecipe;
	public static FixedRecipe personalComponentRecipe;
	private static short[] keyArray;
	private static final ShortOpenHashSet signalSet = new ShortOpenHashSet();
	private static final ShortOpenHashSet signaledByRailSet = new ShortOpenHashSet();
	private static ElementCategory categoryHirarchy;
	private static List<String> categoryNames;
	public static String propertiesPath;
	public static File configFile;
	private static boolean loadedForGame;
	public static String propertiesHash = "none";
	public static String configHash = "none";

	public static final short[] HULL_HELPER = {HULL_COLOR_PURPLE_ID, HULL_COLOR_BROWN_ID, HULL_COLOR_BLACK_ID, HULL_COLOR_RED_ID, HULL_COLOR_BLUE_ID, HULL_COLOR_GREEN_ID, HULL_COLOR_YELLOW_ID, HULL_COLOR_WHITE_ID};
	public static final byte MAX_HITPOINTS = 127;
	public static final float MAX_HITPOINTS_INV = 1.0f / 127.0f;

	public static final int PLEX_DOOR = 0;
	public static final int BLAST_DOOR = 1;
	public static final int GLASS_DOOR = 2;
	public static final int FORCE_FIELD = 3;

	static {
		resources[0] = RESS_CRYS_HATTEL;
		resources[1] = RESS_CRYS_SINTYR;
		resources[2] = RESS_CRYS_MATTISE;
		resources[3] = RESS_CRYS_RAMMET;
		resources[4] = RESS_CRYS_VARAT;
		resources[5] = RESS_GAS_BASTYN;
		resources[6] = RESS_CRYS_CRYSTAL_COMMON;
		resources[7] = RESS_CRYS_NOCX;

		resources[8] = RESS_ORE_THRENS;
		resources[9] = RESS_ORE_JISPER;
		resources[10] = RESS_GAS_ZERCANER;
		resources[11] = RESS_ORE_SERTISE;
		resources[12] = RESS_ORE_HYLAT;
		resources[13] = RESS_ORE_FERTIKEEN;
		resources[14] = RESS_ORE_SAPSUN;
		resources[15] = RESS_ORE_METAL_COMMON;

		resources[16] = RESS_RARE_QUANTANIUM;
		resources[17] = RESS_RARE_METATE;
		resources[18] = RESS_RARE_EXOGEN;
	}

	public static short getResourceIndexFromItemID(short id) {
		return (short) (resIDToOrientationMapping[id] - 1);
	}

	static {
		orientationToResIDMapping[Hattel] = RESS_CRYS_HATTEL;
		orientationToResIDMapping[Sintyr] = RESS_CRYS_SINTYR;
		orientationToResIDMapping[Mattise] = RESS_CRYS_MATTISE;
		orientationToResIDMapping[Rammet] = RESS_CRYS_RAMMET;
		orientationToResIDMapping[Varat] = RESS_CRYS_VARAT;
		orientationToResIDMapping[Bastyn] = RESS_GAS_BASTYN;
		orientationToResIDMapping[CommonCrystal] = RESS_CRYS_CRYSTAL_COMMON;
		orientationToResIDMapping[Nocx] = RESS_CRYS_NOCX;

		orientationToResIDMapping[Threns] = RESS_ORE_THRENS;
		orientationToResIDMapping[Jisper] = RESS_ORE_JISPER;
		orientationToResIDMapping[Zercaner] = RESS_GAS_ZERCANER;
		orientationToResIDMapping[Sertise] = RESS_ORE_SERTISE;
		orientationToResIDMapping[Hylat] = RESS_ORE_HYLAT;
		orientationToResIDMapping[Fertikeen] = RESS_ORE_FERTIKEEN;
		orientationToResIDMapping[Sapsun] = RESS_ORE_SAPSUN;
		orientationToResIDMapping[CommonMetal] = RESS_ORE_METAL_COMMON;

		orientationToResIDMapping[Exogen] = RESS_RARE_EXOGEN;
		orientationToResIDMapping[Metate] = RESS_RARE_METATE;
		orientationToResIDMapping[Quantanium] = RESS_RARE_QUANTANIUM;
	}

	static {
		resIDToOrientationMapping[RESS_CRYS_HATTEL] = Hattel;
		resIDToOrientationMapping[RESS_CRYS_SINTYR] = Sintyr;
		resIDToOrientationMapping[RESS_CRYS_MATTISE] = Mattise;
		resIDToOrientationMapping[RESS_CRYS_RAMMET] = Rammet;
		resIDToOrientationMapping[RESS_CRYS_VARAT] = Varat;
		resIDToOrientationMapping[RESS_GAS_BASTYN] = Bastyn;
		resIDToOrientationMapping[RESS_CRYS_CRYSTAL_COMMON] = CommonCrystal;
		resIDToOrientationMapping[RESS_CRYS_NOCX] = Nocx;

		resIDToOrientationMapping[RESS_ORE_THRENS] = Threns;
		resIDToOrientationMapping[RESS_ORE_JISPER] = Jisper;
		resIDToOrientationMapping[RESS_GAS_ZERCANER] = Zercaner;
		resIDToOrientationMapping[RESS_ORE_SERTISE] = Sertise;
		resIDToOrientationMapping[RESS_ORE_HYLAT] = Hylat;
		resIDToOrientationMapping[RESS_ORE_FERTIKEEN] = Fertikeen;
		resIDToOrientationMapping[RESS_ORE_SAPSUN] = Sapsun;
		resIDToOrientationMapping[RESS_ORE_METAL_COMMON] = CommonMetal;

		resIDToOrientationMapping[RESS_RARE_EXOGEN] = Exogen;
		resIDToOrientationMapping[RESS_RARE_METATE] = Metate;
		resIDToOrientationMapping[RESS_RARE_QUANTANIUM] = Quantanium;
	}

	//never use orientation 0 for anything, else old planets suddenly get minerals
	static {
		orientationToResOverlayMapping[Hattel] = 1;
		orientationToResOverlayMapping[Sintyr] = 2;
		orientationToResOverlayMapping[Mattise] = 3;
		orientationToResOverlayMapping[Rammet] = 4;
		orientationToResOverlayMapping[Varat] = 5;
		orientationToResOverlayMapping[Bastyn] = 6;
		orientationToResOverlayMapping[CommonCrystal] = 7;
		orientationToResOverlayMapping[Nocx] = 8;

		orientationToResOverlayMapping[Threns] = 9;
		orientationToResOverlayMapping[Jisper] = 10;
		orientationToResOverlayMapping[Zercaner] = 11;
		orientationToResOverlayMapping[Sertise] = 12;
		orientationToResOverlayMapping[Hylat] = 13;
		orientationToResOverlayMapping[Fertikeen] = 14;
		orientationToResOverlayMapping[Sapsun] = 15;
		orientationToResOverlayMapping[CommonMetal] = 16;

		orientationToResOverlayMapping[Quantanium] = 17;
		orientationToResOverlayMapping[Metate] = 18;
		orientationToResOverlayMapping[Exogen] = 19;
	}

	public static final int[] UBIQUITOUS_RESOURCES = new int[]{
			CommonCrystal,
			CommonMetal
	};
	public static final int[] RARE_RESOURCES = new int[]{
			Exogen,
			Metate,
			Quantanium
	};

	public static boolean isShard(short type) {
		return type == RESS_CRYS_HATTEL || type == RESS_CRYS_SINTYR || type == RESS_CRYS_MATTISE || type == RESS_CRYS_RAMMET || type == RESS_CRYS_VARAT || type == RESS_CRYS_CRYSTAL_COMMON || type == RESS_CRYS_NOCX;
	}

	public static boolean isOre(short type) {
		return type == RESS_ORE_THRENS || type == RESS_ORE_JISPER || type == RESS_ORE_SERTISE || type == RESS_ORE_HYLAT || type == RESS_ORE_FERTIKEEN || type == RESS_ORE_SAPSUN || type == RESS_ORE_METAL_COMMON;
	}

	public static boolean isGas(short type) {
		return type == RESS_GAS_BASTYN || type == RESS_GAS_ZERCANER;
	}
	//TODO these should be moddable

	public static boolean hasResourceInjected(short type, byte orientation) {
		//INSERTED CODE
		//Removed the <17 check since we can add more ores, im not sure what the 17 refers to so this may break something
		///
		return isValidType(type) && getInfo(type).resourceInjection != ElementInformation.ResourceInjectionType.OFF && orientation > 0;// ;
	}

	private static void add(short key, ElementInformation information) throws ParserConfigurationException {
		if(keySet.contains(key)) {
			throw new ParserConfigurationException("Duplicate Block ID " + key + " (" + information.getName() + " and " + informationKeyMap.get(key).getName() + ")");
		}
		keySet.add(key);
		informationKeyMap.put(key, information);
		highestType = Math.max(highestType, key);

		if(information.getFactory() != null) {
			factoryKeySet.add(key);
		}

	}

	private static void initializeInternalArrays(int size, boolean includeFactoryInfo) {
		infoArray = new ElementInformation[size];
		if(includeFactoryInfo) factoryInfoArray = new boolean[size];
		validArray = new boolean[size];
		lodShapeArray = new boolean[size];
	}

	public static void addInformationToExisting(ElementInformation info) throws ParserConfigurationException {

		boolean ins = categoryHirarchy.insertRecusrive(info);
		assert (ins) : info.getType();
		add(info.getId(), info);

		initializeInternalArrays(highestType + 1, true);

		for(Map.Entry<Short, ElementInformation> e : informationKeyMap.entrySet()) {

			infoArray[e.getKey()] = e.getValue();
			validArray[e.getKey()] = true;

		}
		if(factoryKeySet.contains(info.getId())) {
			factoryInfoArray[info.getId()] = true;
			info.getFactory().enhancer = FACTORY_INPUT_ENH_ID;
			//			info.getControlling().addAll(factoryKeySet);
			//			info.getControlling().remove(info.getId());
			//			info.getControlledBy().addAll(factoryKeySet);
		}
		if(info.hasLod()) {
			lodShapeArray[info.getId()] = true;
		}
		keyArray = new short[keySet.size()];
		int i = 0;
		for(short s : keySet) {
			keyArray[i] = s;
			i++;
		}
	}

	public static void clear() {
		informationKeyMap.clear();
		infoArray = null;
		highestType = 0;
		factoryKeySet.clear();
		keySet.clear();
		projected.clear();
		leveldKeySet.clear();
		categoryHirarchy.clear();
		categoryNames = null;
		factoryInfoArray = null;
		validArray = null;
		signalArray = null;
		signaledByRailArray = null;
		lodShapeArray = null;
	}

	public static boolean exists(int q) {
		return q > 0 && (q < infoArray.length && infoArray[q] != null);
	}

	public static String[] getCategoryNames(ElementCategory cat) {
		if(categoryNames == null) {
			categoryNames = new ArrayList<String>();
		}
		categoryNames.clear();
		getCategoryNames(cat, categoryNames);
		return categoryNames.toArray(new String[categoryNames.size()]);
	}

	public static void getCategoryNames(ElementCategory cat, List<String> out) {
		for(ElementCategory child : cat.getChildren()) {
			out.add(child.getCategory());
			getCategoryNames(child, out);
		}
	}

	/**
	 * @return the categoryHirarchy
	 */
	public static ElementCategory getCategoryHirarchy() {
		return categoryHirarchy;
	}

	/**
	 * @return the factorykeyset
	 */
	public static ShortOpenHashSet getFactorykeyset() {
		return factoryKeySet;
	}

	public static void cleanUpUnusedBlockIds() throws IOException {
		Iterator<Map.Entry<Object, Object>> iterator = properties.entrySet().iterator();
		while(iterator.hasNext()) {
			Map.Entry<Object, Object> e = iterator.next();
			short val = Short.parseShort(e.getValue().toString());
			if(!keySet.contains(val)) {
				iterator.remove();
				System.err.println("REMOVED: " + e.getKey());
			}
		}
		BufferedReader r = new BufferedReader(new FileReader(configFile));
		String line;
		StringBuffer b = new StringBuffer();
		while((line = r.readLine()) != null) {
			b.append(line + "\n");
		}
		r.close();
		Properties rep = new Properties();
		for(short type : keySet) {
			ElementInformation info = getInfo(type);
			String idName = info.getNameUntranslated().toUpperCase(Locale.ENGLISH).replaceAll("\\s", "_");
			int i = 0;
			String n = idName;
			while((rep.get(n) != null && rep.get(n) != info) || (properties.get(n) != null && properties.get(n) != info)) {
				n = idName + "_" + i;
				i++;
			}
			idName = n;
			Iterator<Map.Entry<Object, Object>> et = properties.entrySet().iterator();
			while(et.hasNext()) {
				Map.Entry<Object, Object> e = et.next();
				short val = Short.parseShort(e.getValue().toString());
				if(val == type && !idName.equals(e.getKey().toString())) {
					int index;
					while((index = b.indexOf("\"" + e.getKey().toString() + "\"")) >= 0) {
						b.replace(index + 1, index + e.getKey().toString().length() + 1, idName);
						System.err.println("REPLACE: " + index + "; " + (index + e.getKey().toString().length()) + ": " + e.getKey().toString() + " -> " + idName);
					}
					while((index = b.indexOf(">" + e.getKey().toString() + "<")) >= 0) {
						b.replace(index + 1, index + e.getKey().toString().length() + 1, idName);
						System.err.println("REPLACE: " + index + "; " + (index + e.getKey().toString().length()) + ": " + e.getKey().toString() + " -> " + idName);
					}

					et.remove();
					System.err.println("RENAMED: " + e.getKey() + " -> " + idName);
					rep.put(idName, e.getValue());
					break;
				}
			}
		}
		BufferedWriter w = new BufferedWriter(new FileWriter(configFile));
		w.write(b.toString());
		w.close();

		properties.putAll(rep);
		writePropertiesOrdered();
	}

	public static ElementInformation getInfoFast(short type) {
		return infoArray[type];
	}

	public static ElementInformation getInfoFast(int type) {
		return infoArray[type];
	}

	public static ElementInformation getInfo(short type) {
		//Commented out this line due to it invalidating the if statements that gave more accurate debug info below
		//		assert (type > 0 && ((type < infoArray.length && infoArray[type] != null))) : "type " + type + " unknown, please check the properties and the xml ";

		ElementInformation elementInformation;
		if(type < 0) {
			throw new NullPointerException("Exception: REQUESTED TYPE " + type + " IS NULL");
		}
		//INSERTED CODE
		if(type >= infoArray.length) {
			throw new IllegalArgumentException("Requested type " + type + " was outside of infoArray (size " + infoArray.length + "). " + "If this is a block id for a modded block, remember to use config.add(elementInfo); to add it!");
		}
		///
		elementInformation = infoArray[type];
		if(elementInformation == null) {
			throw new NullPointerException("Exception: REQUESTED TYPE " + type + " IS NULL");
		}

		return elementInformation;
	}

	//INSERTED CODE
	public static Short2ObjectOpenHashMap<ElementInformation> getInformationKeyMap() {
		return informationKeyMap;
	}

	public static ElementInformation getInfo(int type) {
		assert (type > 0 && ((type < infoArray.length && infoArray[type] != null))) : "type " + type + " unknown, please check the properties and the xml ";
		ElementInformation elementInformation;
		if(type < 0) {
			throw new NullPointerException("Exception: REQUESTED TYPE " + type + " IS NULL");
		}
		elementInformation = infoArray[type];
		if(elementInformation == null) {
			throw new NullPointerException("Exception: REQUESTED TYPE " + type + " IS NULL");
		}

		return elementInformation;
	}

	/**
	 * @return the leveldkeyset
	 */
	public static ShortOpenHashSet getLeveldkeyset() {
		return leveldKeySet;
	}

	public static String getNameSave(short type) {
		return exists(type) ? getInfo(type).getName() : "unknown(" + type + ")";
	}

	public static void initElements(List<ElementInformation> infos, ElementCategory load) throws ParserConfigurationException {
		for(ElementInformation e : infos) {
			add(e.getId(), e);
		}
		categoryHirarchy = load;

		initializeInternalArrays(highestType + 1, true);

		for(Map.Entry<Short, ElementInformation> e : informationKeyMap.entrySet()) {
			infoArray[e.getKey()] = e.getValue();
			validArray[e.getKey()] = true;

		}
		ShortArrayList signal = new ShortArrayList();
		ShortArrayList signaledByRail = new ShortArrayList();
		for(it.unimi.dsi.fastutil.shorts.Short2ObjectMap.Entry<ElementInformation> e : informationKeyMap.short2ObjectEntrySet()) {
			e.getValue().onInit();
			if(e.getValue().isSignal()) {
				signal.add(e.getShortKey());
			}
			if(e.getValue().signaledByRail) {
				signaledByRail.add(e.getShortKey());
			}
			lodShapeArray[e.getShortKey()] = e.getValue().hasLod();
		}
		signal.toArray((signalArray = new short[signal.size()]));
		signaledByRail.toArray((signaledByRailArray = new short[signaledByRail.size()]));
		if(projected.size() > 0) {
			projected.trim();
		}
		for(short s : factoryKeySet) {
			factoryInfoArray[s] = true;
			getInfo(s).getFactory().enhancer = FACTORY_INPUT_ENH_ID;
		}
		sortedByName = new ObjectArrayList(informationKeyMap.values());

		Collections.sort(sortedByName, (o1, o2) -> o1.getName().toLowerCase(Locale.ENGLISH).compareTo(o2.getName().toLowerCase(Locale.ENGLISH)));
		initialized = true;

		for(Map.Entry<Short, ElementInformation> e : informationKeyMap.entrySet()) {
			if(!(e.getValue().resourceInjection == ElementInformation.ResourceInjectionType.OFF || e.getValue().getIndividualSides() == 1)) {
				try {
					throw new ParseException("BlockConfig.xml Error: " + e.getValue() + " cannot have resource injection (resOverlay) and multiple sides");
				} catch(ParseException e1) {
					throw new RuntimeException(e1);
				}
			}
			if(!(e.getValue().resourceInjection == ElementInformation.ResourceInjectionType.OFF || !e.getValue().orientatable)) {
				try {
					throw new ParseException("BlockConfig.xml Error: " + e.getValue() + " cannot have resource injection (resOverlay) and be orientatable");
				} catch(ParseException e1) {
					throw new RuntimeException(e1);
				}
			}
			if(e.getValue().getSourceReference() != 0 && e.getValue().getSourceReference() != e.getKey().shortValue()) {
				e.getValue().consistence.clear();
				e.getValue().producedInFactory = 0;
				e.getValue().inRecipe = false;
				e.getValue().shoppable = false;
			}
			if(e.getValue().getHpOldByte() == 0) {
				e.getValue().setHpOldByte((short) e.getValue().getMaxHitPointsFull());
			}

			if(e.getValue().slabIds != null || e.getValue().styleIds != null || e.getValue().wildcardIds != null) {
				ShortArrayList sl = new ShortArrayList();

				int wildCardIndexGen = 1;
				if(e.getValue().wildcardIds != null) {
					for(short s : e.getValue().wildcardIds) {
						if(isValidType(s)) {
							if(!sl.contains(s) && s != e.getValue().id) {
								sl.add(s);
							}
							informationKeyMap.get(s).wildcardIndex = wildCardIndexGen;
							wildCardIndexGen++;
						} else {
							try {
								throw new Exception("WARNING: block type reference invalid: (wildcardIds of " + e.getValue().getName() + "): " + s);
							} catch(Exception e1) {
								e1.printStackTrace();
							}
						}
					}
				}
				if(e.getValue().styleIds != null) {
					for(short s : e.getValue().styleIds) {
						if(isValidType(s)) {
							if(!sl.contains(s) && s != e.getValue().id) {
								sl.add(s);
							}
						} else {
							try {
								throw new Exception("WARNING: block type reference invalid: (styleIds of " + e.getValue().getName() + "): " + s);
							} catch(Exception e1) {
								e1.printStackTrace();
							}
						}
					}
				}
				if(e.getValue().slabIds != null) {
					for(short s : e.getValue().slabIds) {
						if(isValidType(s)) {
							if(!sl.contains(s) && s != e.getValue().id) {
								sl.add(s);
							}
						} else {
							try {
								throw new Exception("WARNING: block type reference invalid (slabIds of " + e.getValue().getName() + "): " + s);
							} catch(Exception e1) {
								e1.printStackTrace();
							}
						}
					}
				}
				e.getValue().blocktypeIds = new short[sl.size()];
				for(int i = 0; i < e.getValue().blocktypeIds.length; i++) {

					e.getValue().blocktypeIds[i] = sl.getShort(i);

				}
			}
		}

		for(int i : informationKeyMap.keySet()) {
			getInfo(i).isSourceBlockTmp = true;
		}
		for(int i : informationKeyMap.keySet()) {
			ElementInformation info = getInfo(i);
			if(info.blocktypeIds != null) {

				for(short s : info.blocktypeIds) {
					ElementInformation other = getInfo(s);

					other.isSourceBlockTmp = false;
					//those items are not shoppable by default
					other.shoppable = false;
				}
			}
			if(getInfo(i).getType().hasParent("Terrain")) {
				getInfo(i).setSpecialBlock(false);
			}

			getInfo(i).recalcTotalConsistence();

			getInfo(i).sanatizeReactorValues();

		}
		for(int i : informationKeyMap.keySet()) {
			ElementInformation info = getInfo(i);
			if(info.isSourceBlockTmp && info.blocktypeIds != null) {
				for(short s : info.blocktypeIds) {
					ElementInformation other = getInfo(s);
//					assert(other.sourceReference == 0 || other.sourceReference == info.getId()):info+"; "+Arrays.toString(info.blocktypeIds)+"  "+other+"; other src: "+toString(other.sourceReference);
					//set source reference to block that don't appear in any reference and are therefore the root
					other.sourceReference = info.getId();
				}
			}
		}
		loadedForGame = false;
	}

	// INSERTED CODE
	// A boolean to track if the game has loaded all LOD models yet.
	// Once initDataForGame is called, it means the resourceloader has loaded every model, so we can safely
	//  call initDataForGame at any time.
	public static boolean loadedAllModels;

	///
	public static synchronized void initDataForGame() {
		if(!loadedForGame) {
			for(Map.Entry<Short, ElementInformation> e : informationKeyMap.entrySet()) {
				e.getValue().lodCollision.load();
				e.getValue().lodDetailCollision.load();
			}
			loadedForGame = true;
		}
	}

	private static void initFixedRecipePrices(FixedRecipe fixedRecipe, boolean dynamicPrice) {

		for(FixedRecipeProduct r : fixedRecipe.getRecipeProduct()) {
			//Take the sum of the output price
			int outputPrice = 0;
			for(FactoryResource f : r.getOutputResource()) {
				outputPrice += f.count * ElementKeyMap.getInfo(f.type).getPrice(dynamicPrice) * ServerConfig.DYNAMIC_RECIPE_PRICE_MODIFIER.getFloat();
			}

			for(FactoryResource f : r.getInputResource()) {
				long craftPrice = (long) (Math.ceil((double) outputPrice / f.count));

				//use most expensive price if the same block is used in multiple fixed recipes
				if(getInfo(f.type).dynamicPrice < craftPrice) {
					getInfo(f.type).setPrice(craftPrice);
					getInfo(f.type).dynamicPrice = craftPrice;
				}
				//System.out.println("FIXED RECIPE DYN PRICE FOR: " + ElementKeyMap.getInfo(f.type).getName() + " outputPrice " + outputPrice + " and this many input blocks " + f.count);
			}

		}
	}

	private static void initFixedRecipes(FixedRecipes fixedRecipes) {
		ElementKeyMap.fixedRecipes = fixedRecipes;

		for(int i = 0; i < fixedRecipes.recipes.size(); i++) {
			String name = fixedRecipes.recipes.get(i).name;
			if("Make Factory Blocks".equals(name)) {
				macroBlockRecipe = fixedRecipes.recipes.get(i);
			}
			if("Micro Reprocessor".equals(name)) {
				microAssemblerRecipe = fixedRecipes.recipes.get(i);
			}
			if("Basic Capsule Refinery".equals(name)) {
				capsuleRecipe = fixedRecipes.recipes.get(i);
				//might also need to do this for other fixed recipes
				initFixedRecipePrices(capsuleRecipe, true);
			}
			if("Advanced Capsule Refinery".equals(name)) {
				advCapsuleRecipe = fixedRecipes.recipes.get(i);
			}
			if("Refine Common Materials".equals(name)) {
				personalMeshAndCompositeRecipe = fixedRecipes.recipes.get(i);
			}
			if("Personal Component Fabricator".equals(name)) {
				personalComponentRecipe = fixedRecipes.recipes.get(i);
			}
		}
		//recyclerRecipe will be populated after all mods are loaded, as they may add their own normal block factory recipes
	}

	private static void initRecyclerRecipe() {
		FixedRecipeBuilder b = new FixedRecipeBuilder();
		for(ElementInformation block : ElementKeyMap.infoArray)
			if(block != null) {
				if(!block.deprecated && !block.consistence.isEmpty() && block.producedInFactory == FAC_BLOCK) {
					b.input(block.id, 1);
					block.consistence.forEach(entry -> b.output(entry.type, entry.count));
					b.next();
				}
			}

		recyclerRecipe = b.build();
		recyclerRecipe.name = "Recycle Blocks Into Components";

		StarLoader.fireEvent(new BlockRecyclerRecipeCreateEvent(recyclerRecipe), GameCommon.isDedicatedServer());
	}

	//INSERTED CODE
	//When loading into online worlds directly (via -uplink, I think), block data is initialized twice. One time before mods are loaded, one time after.
	//This is problematic for mods that depend on onEnable data to load first (as it does in all other contexts)
	//Therefore, we need a parameter that skips the initial block load

	//We also need to re-parse the block config later
	public static boolean skipNextBlockInitializeForMods;
	public static boolean needToReparseBlockDataForMods;

	///

	public static void initializeData(File appendImport) {
		initializeData(null, false, null, appendImport);
	}

	public static void createBlankReactorBlocks(boolean override) throws ParserConfigurationException {

		ElementCategory recCat = categoryHirarchy.getChild("General").getChild("Power").getChild("Chamber");

		Object2ObjectOpenHashMap<String, ElementInformation> nw = new Object2ObjectOpenHashMap<String, ElementInformation>();
		Short2ObjectOpenHashMap<ElementInformation> nwid = new Short2ObjectOpenHashMap<ElementInformation>();
		final String pre = "REACTOR_CHAMBER_";
		for(Object k : properties.keySet()) {
			short id = Short.parseShort(properties.get(k).toString());

			if(k.toString().startsWith(pre) && (override || !keySet.contains(id))) {
				String nm = k.toString().substring(pre.length());
				System.err.println("NEW REACTOR CHAMBER: " + nm);
				ElementInformation info = new ElementInformation(id, nm, recCat, new short[]{0, 0, 0, 0, 0, 0});
				nw.put(nm, info);
				nwid.put(id, info);
			}
		}

		for(ElementInformation info : nw.values()) {
			if(!info.name.contains("_")) {
				info.chamberGeneral = true;
			}
		}
		for(ElementInformation info : nw.values()) {
			if(!info.chamberGeneral) {
				String gStr = info.name.substring(0, info.name.indexOf('_'));

				ElementInformation general = nw.get(gStr);
				if(general == null) {
					general = getInfo(Integer.parseInt(properties.getProperty(pre + gStr).toString()));
				}
				assert (general != null) : gStr;

				info.chamberRoot = general.id;

				String lst = info.name.substring(info.name.lastIndexOf('_') + 1);

				try {
					int lvl = Integer.parseInt(lst);
					String lm = info.name.substring(0, info.name.lastIndexOf('_'));
					if(lvl > 0) {
						String parent = lm + "_" + (lvl - 1);
						ElementInformation p = nw.get(parent);
						if(p == null) {
							p = getInfo(Integer.parseInt(properties.getProperty(pre + parent).toString()));
						}
						assert (p != null) : parent;
						info.chamberParent = p.id;
						ElementInformation pInfo = nwid.get((short) info.chamberParent);
						pInfo.chamberUpgradesTo = info.id;
					} else {
						info.chamberParent = 0; //no parent for lvl 0
					}
				} catch(Exception e) {
					System.err.println("NOT UPGRADABLE: " + info + "; " + lst);
					info.chamberParent = 0; //no parent for standalone (needs manual)
				}
			}
		}
		List<ElementInformation> ord = new ObjectArrayList<ElementInformation>(nw.values());
		Collections.sort(ord, (o1, o2) -> {

			String a = "";
			String b = "";
			for(Map.Entry<Object, Object> k : properties.entrySet()) {
				if(Integer.parseInt(k.getValue().toString()) == o1.id) {
					a = k.getKey().toString();
				}
				if(Integer.parseInt(k.getValue().toString()) == o2.id) {
					b = k.getKey().toString();
				}
			}

			return a.compareTo(b);
		});
		short texInc = 629;
		short specInc = 629 + 9;
		short curId = 0;
		for(ElementInformation info : ord) {
			if(!info.chamberGeneral) {
				info.shoppable = false;
				info.placable = false;
				info.setSourceReference(info.chamberParent);
				info.setTextureId(new short[]{curId, specInc, specInc, curId, curId, curId});
				if(info.chamberParent == 0) {
					ElementInformation rootInfo = nwid.get((short) info.chamberRoot);
					if(rootInfo == null) {
						rootInfo = getInfo(info.chamberRoot);
					}
					assert (rootInfo != null) : "not found " + info + "; " + info.chamberRoot;

					rootInfo.chamberChildren.add(info.id);
				}
			} else {
				info.setTextureId(new short[]{texInc, texInc, texInc, texInc, texInc, texInc});
				curId = texInc;
				texInc++;
			}
			if(!info.chamberGeneral && info.chamberParent != 0) {
				ElementInformation p = nwid.get((short) info.chamberParent);
				if(p == null) {
					p = getInfo(info.chamberParent);
				}
				assert (p != null) : info + " no parent: " + info.chamberParent;
				p.chamberChildren.add(info.id);
				info.chamberPrerequisites.add(p.id);
			}
		}

		for(ElementInformation info : ord) {
			System.err.println("INFO:::: " + info + "; INSERT " + (!keySet.contains(info.id)));
			if(!keySet.contains(info.id)) {
				System.err.println("ADDING: " + info);
				addInformationToExisting(info);
			}
		}
	}

	public static void initializeData(File custom, boolean zipped, String properties, File appendImport) {
		initializeData(custom, zipped, properties, appendImport, false);
	}

	public static void initializeData(File custom, boolean zipped, String properties, File appendImport, boolean initModBlocks) {
		if(initialized) {
			return;
		}

		ElementParser load;
		try {
			load = load(custom, zipped, properties, appendImport);
			initElements(load.getInfoElements(), load.getRootCategory());
			initFixedRecipes(load.getFixedRecipes());
		} catch(Exception e1) {
			e1.printStackTrace();
			GuiErrorHandler.processErrorDialogException(e1);
			initializeData(appendImport);
			return;
		}

		Starter.modManager.onInitializeBlockData();
		//INSERTED CODE @1229
		try {
			throw new RuntimeException("[ElementKeyMap] Not an error; printing call stack of");
		} catch(RuntimeException e) {
			e.printStackTrace();
		}
		if(initModBlocks) {
			StarLoaderHooks.initBlockData();
		}

		initRecyclerRecipe(); //must initialize the recycler recipes after mods/etc. add or modify block consistences, in order to catch everything

		keyArray = new short[keySet.size()];
		int i = 0;
		for(short s : keySet) {
			keyArray[i] = s;
			i++;
		}

		doorTypes.clear();
		inventoryTypes.clear();
		lightTypes.clear();
		chamberAnyTypes.clear();
		chamberGeneralTypes.clear();
		signalSet.clear();
		signaledByRailSet.clear();
		for(short type : keyArray) {
			ElementInformation in = infoArray[type];
			if(in.isInventory()) {
				inventoryTypes.add(in.id);
			}
			if(in.getSourceReference() != 0) {
				sourcedTypes.add(in.id);
			}
			if(in.isLightSource()) {
				lightTypes.add(in.id);
			}
			if(in.isDoor()) {
				doorTypes.add(in.id);
			}
			if(in.isReactorChamberAny()) {
				chamberAnyTypes.add(in.id);
			}
			if(in.isReactorChamberGeneral()) {
				chamberGeneralTypes.add(in.id);
			}
			if(in.isSignal()) {
				signalSet.add(type);
			}
			if(in.signaledByRail) {
				signaledByRailSet.add(type);
			}
		}
		signalArray = new short[signalSet.size()];
		signalArray = signalSet.toArray(signalArray);
		signaledByRailArray = new short[signaledByRailSet.size()];
		signaledByRailArray = signaledByRailSet.toArray(signaledByRailArray);
		inventoryTypes.trim();
		lightTypes.trim();
		doorTypes.trim();
		assert (checkConflicts());

	}

	private static boolean checkConflicts() {
		for(short s : keyArray) {
			for(short s0 : keyArray) {
				if(s != s0 && !getInfo(s).isReactorChamberAny() && !getInfo(s).isDeprecated() && getInfo(s).isShoppable() && !getInfo(s0).isReactorChamberAny() && getInfo(s).getSlab() == 0 && getInfo(s0).getSlab() == 0 && getInfo(s).getBuildIconNum() == getInfo(s0).getBuildIconNum()) {
					try {
						throw new Exception("[INFO] BuildIconConflict: " + toString(s) + " --- " + toString(s0) + "; " + getInfo(s).getBuildIconNum() + "; " + getInfo(s0).getBuildIconNum());
					} catch(Exception e) {
						e.printStackTrace();
					}
					return true;
				}
			}
		}
		return true;
	}

	public static boolean isValidType(short type) {
		return type >= 0 && type < infoArray.length && infoArray[type] != null;
	}

	public static boolean isValidType(int type) {
		return type >= 0 && type < infoArray.length && infoArray[type] != null;
	}

	public static String list() {
		return keySet.toString();
	}

	private static ElementParser load(File custom, boolean zipped, String properties, File appendImport) throws SAXException, IOException, ParserConfigurationException, ElementParserException {
		if(custom == null) {
			ElementParser parser = new ElementParser();
			parser.loadAndParseDefault(appendImport);

			return parser;
		} else {
			ElementParser parser = new ElementParser();
			parser.loadAndParseCustomXML(custom, zipped, properties, appendImport);

			return parser;
		}
	}

	public static boolean initializedModBlockData;

	public static void reinitializeData(File custom, boolean zipped, String properties, File appendInput, boolean initModBlocks) {
		initialized = false;
		categoryHirarchy = null;
		factoryKeySet.clear();
		projected.clear();
		keySet.clear();
		leveldKeySet.clear();
		highestType = 0;
		informationKeyMap.clear();
		fixedRecipes = null;
		infoArray = null;
		factoryInfoArray = null;
		validArray = null;
		signalArray = null;
		lodShapeArray = null;
		keyArray = null;
		signaledByRailArray = null;
		signaledByRailSet.clear();
		signalSet.clear();
		BlockConfig.clearData();
		initializeInternalArrays(highestType + 1, true);
		initializeData(custom, zipped, properties, appendInput, initModBlocks);
	}

	public static void removeFromExisting(ElementInformation info) {
		keySet.remove(info.getId());
		informationKeyMap.remove(info.getId());
		highestType = 0;
		for(short s : keySet) {
			highestType = Math.max(highestType, s);
		}

		factoryKeySet.remove(info.getId());
		factoryInfoArray[info.getId()] = false;
		leveldKeySet.remove(info.getId());

		initializeInternalArrays(highestType + 1, false);

		for(Map.Entry<Short, ElementInformation> e : informationKeyMap.entrySet()) {
			infoArray[e.getKey()] = e.getValue();
			validArray[e.getKey()] = true;
			lodShapeArray[e.getKey()] = e.getValue().hasLod();
		}
		categoryHirarchy.removeRecursive(info);

	}

	public static void reparseProperties() throws IOException {
		// Read properties file.
		properties = new Properties();
		FileInputStream fileInputStream = new FileInputStream("./data/config/BlockTypes.properties");
		properties.load(fileInputStream);
		propertiesHash = FileUtil.getSha1Checksum("./data/config/BlockTypes.properties");
	}

	public static void reparseProperties(String custom) throws IOException {
		// Read properties file.
		properties = new Properties();
		FileInputStream fileInputStream = new FileInputStream(custom);
		properties.load(fileInputStream);

		propertiesHash = FileUtil.getSha1Checksum(custom);
	}

	public static short[] typeList() {
		return keyArray;
	}

	private static void writeCatToXML(ElementCategory h, Element root, Document doc) throws CannotAppendXMLException {

		org.w3c.dom.Element child = doc.createElement(h.getCategory());
		//		System.err.println("[XML] cat "+ElementParser.getStringFromType(h.getCategory()));
		for(ElementCategory e : h.getChildren()) {
			writeCatToXML(e, child, doc);
		}
		for(ElementInformation info : h.getInfoElements()) {
			info.appendXML(doc, child);
		}
		root.appendChild(child);
	}

	public static File writeDocument(File file, ElementCategory h, FixedRecipes fixedRecipes) {
		try {
			// ///////////////////////////
			// Creating an empty XML Document

			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			org.w3c.dom.Element root = doc.createElement("Config");

			// //////////////////////
			// Creating the XML tree

			org.w3c.dom.Element elementRoot = doc.createElement(h.getCategory());

			Comment comment = doc.createComment("autocreated by the starmade block editor");
			elementRoot.appendChild(comment);

			for(ElementCategory g : h.getChildren()) {
				writeCatToXML(g, elementRoot, doc);
			}
			// create the root element and add it to the document

			org.w3c.dom.Element recipeRoot = doc.createElement("Recipes");

			writeRecipes(recipeRoot, doc, fixedRecipes);

			root.appendChild(elementRoot);
			root.appendChild(recipeRoot);

			doc.appendChild(root);
			doc.setXmlVersion("1.0");

			// create a comment and put it in the root element

			// ///////////////
			// Output the XML

			// set up a transformer
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			// create string from xml tree
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(file);
			DOMSource source = new DOMSource(doc);
			trans.transform(source, result);

			return file;
		} catch(Exception e) {
			e.printStackTrace();
			GuiErrorHandler.processErrorDialogException(e);
		}
		return null;
	}

	public static File writeDocument(String path, ElementCategory h, FixedRecipes fixedRecipes) {
		File file = new FileExt(path);
		return writeDocument(file, h, fixedRecipes);
	}

	private static void writeRecipes(Element recipeRoot, Document doc, FixedRecipes fixedRecipes) throws DOMException, CannotAppendXMLException {
		fixedRecipes.appendDoc(recipeRoot, doc);

	}

	public static void removeDuplicateBuildIcons() {
		IntOpenHashSet h = new IntOpenHashSet();
		for(ElementInformation info : informationKeyMap.values()) {
			int i = info.getBuildIconNum();
			while(h.contains(i)) {
				i++;
			}
			info.setBuildIconNum(i);
			h.add(info.getBuildIconNum());
		}
	}

	public static String toString(int type) {
		return initialized ? toString((short) type) : "[" + type + "]";
	}

	public static String toString(short type) {
		if(type == org.schema.game.common.data.element.Element.TYPE_ALL) {
			return "TYPE_ALL";
		} else if(type == org.schema.game.common.data.element.Element.TYPE_NONE) {
			return "TYPE_NONE";
		} else if(type == org.schema.game.common.data.element.Element.TYPE_RAIL_TRACK) {
			return "TYPE_RAIL_TRACK";
		} else if(type == org.schema.game.common.data.element.Element.TYPE_SIGNAL) {
			return "TYPE_SIGNAL";
		} else if(type == InventorySlot.MULTI_SLOT) {
			return "TYPE_MULTI_SLOT";
		}
		return exists(type) ? getInfo(type).toString() : "Unknown(" + type + ")";
	}

	public static String toString(Collection<Short> controlling) {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for(Short s : controlling) {
			sb.append(toString(s) + ";");
		}
		sb.append("}");
		return sb.toString();
	}

	public static short getCollectionType(short type) {
		if(isValidType(type) && getInfo(type).isDoor()) {
			return DOOR_ELEMENT;
		}
		return type;
	}

	public static boolean isDoor(short id) {
		return isValidType(id) && getInfo(id).isDoor();
	}

	public static boolean isMacroFactory(short type) {
		return type == FACTORY_BLOCK_ASSEMBLER_ID;
	}

	public static ElementInformation[] getInfoArray() {
		return infoArray;
	}

	public static void setInfoArray(ElementInformation[] infoArray) {
		ElementKeyMap.infoArray = infoArray;
	}

	public static boolean isGroupCompatible(short a, short b) {
		return isValidType(a) && isValidType(b) && !getInfo(a).getInventoryGroup().isEmpty() && getInfo(a).getInventoryGroup().equals(getInfo(b).getInventoryGroup());
	}

	public static boolean isStash(short type) {
		return type == STASH_ELEMENT || type == Blocks.LOCK_BOX.getId();
	}

	public static void deleteBlockStyles(ElementInformation info) {
		removeByIdName(info.idName + "_" + "WEDGE", false);
		removeByIdName(info.idName + "_" + "CORNER", false);
		removeByIdName(info.idName + "_" + "TETRA", false);
		removeByIdName(info.idName + "_" + "HEPTA", false);
	}

	public static void generateBlockStyles(ElementInformation info) {
		if(info.idName == null) info.idName = info.getNameUntranslated().toUpperCase(Locale.ENGLISH).replaceAll("\\s", "_");
		int wedgeID = insertIntoProperties(info.idName + "_" + "WEDGE");
		int cornerID = insertIntoProperties(info.idName + "_" + "CORNER");
		int tetraID = insertIntoProperties(info.idName + "_" + "TETRA");
		int heptaID = insertIntoProperties(info.idName + "_" + "HEPTA");

		ElementInformation copyWedge = new ElementInformation(info, (short) wedgeID, info.name + " Wedge");
		ElementInformation copyCorner = new ElementInformation(info, (short) cornerID, info.name + " Corner");
		ElementInformation copyTetra = new ElementInformation(info, (short) tetraID, info.name + " Tetra");
		ElementInformation copyHepta = new ElementInformation(info, (short) heptaID, info.name + " Hepta");

		copyWedge.slabIds = new short[0];
		copyWedge.styleIds = new short[0];
		copyCorner.slabIds = new short[0];
		copyCorner.styleIds = new short[0];
		copyTetra.slabIds = new short[0];
		copyTetra.styleIds = new short[0];
		copyHepta.slabIds = new short[0];
		copyHepta.styleIds = new short[0];

		copyWedge.shoppable = false;
		copyCorner.shoppable = false;
		copyTetra.shoppable = false;
		copyHepta.shoppable = false;

		copyWedge.inRecipe = false;
		copyCorner.inRecipe = false;
		copyTetra.inRecipe = false;
		copyHepta.inRecipe = false;

		copyWedge.orientatable = true;
		copyCorner.orientatable = true;
		copyTetra.orientatable = true;
		copyHepta.orientatable = true;

		copyWedge.producedInFactory = 0;
		copyCorner.producedInFactory = 0;
		copyTetra.producedInFactory = 0;
		copyHepta.producedInFactory = 0;

		copyWedge.setBuildIconToFree();
		copyWedge.setBlockStyle(BlockStyle.WEDGE.id);
		copyWedge.setSourceReference(info.getId());
		try {
			addInformationToExisting(copyWedge);
		} catch(ParserConfigurationException exception) {
			exception.printStackTrace();
		}

		copyCorner.setBuildIconToFree();
		copyCorner.setBlockStyle(BlockStyle.CORNER.id);
		copyCorner.setSourceReference(info.getId());
		try {
			addInformationToExisting(copyCorner);
		} catch(ParserConfigurationException exception) {
			exception.printStackTrace();
		}

		copyTetra.setBuildIconToFree();
		copyTetra.setBlockStyle(BlockStyle.TETRA.id);
		copyTetra.setSourceReference(info.getId());
		try {
			addInformationToExisting(copyTetra);
		} catch(ParserConfigurationException exception) {
			exception.printStackTrace();
		}

		copyHepta.setBuildIconToFree();
		copyHepta.setBlockStyle(BlockStyle.HEPTA.id);
		copyHepta.setSourceReference(info.getId());
		try {
			addInformationToExisting(copyHepta);
		} catch(ParserConfigurationException exception) {
			exception.printStackTrace();
		}

		if(info.styleIds != null && info.styleIds.length != 0) {
			short[] newStyleIds = new short[info.styleIds.length + 4];
			for(int i = 0; i < info.styleIds.length; i++) newStyleIds[i] = info.styleIds[i];
			newStyleIds[info.styleIds.length] = copyWedge.getId();
			newStyleIds[info.styleIds.length + 1] = copyCorner.getId();
			newStyleIds[info.styleIds.length + 2] = copyTetra.getId();
			newStyleIds[info.styleIds.length + 3] = copyHepta.getId();
			info.styleIds = newStyleIds;
		} else info.styleIds = new short[]{copyWedge.getId(), copyCorner.getId(), copyTetra.getId(), copyHepta.getId()};
	}

	public static boolean isInvisible(short id) {
		return id == SIGNAL_TRIGGER_AREA;
	}

	public static boolean canOpen(short type) {
		return type == ElementKeyMap.STASH_ELEMENT || type == ElementKeyMap.RECYCLER_ELEMENT_OLD || type == ElementKeyMap.SHIPYARD_COMPUTER || ElementKeyMap.getFactorykeyset().contains(type);
		///relase: //return isStash(type) || type == RECYCLER_ELEMENT || type == SHIPYARD_COMPUTER || getFactorykeyset().contains(type) || getCrewBlocks().contains(type);
	}

	public static void createBlockStyleReferencesFromInvGroup(ElementInformation info) throws ParseException {
		ObjectArrayList<ElementInformation> e = new ObjectArrayList<ElementInformation>();
		for(int i = 0; i < infoArray.length; i++) {
			ElementInformation oth = infoArray[i];
			if(oth != null && oth != info && oth.slab == 0 && oth.inventoryGroup != null && oth.inventoryGroup.length() > 0 && oth.inventoryGroup.equals(info.inventoryGroup)) {
				e.add(oth);
			}
		}
		if(info.wildcardIds != null) {
			for(short s : info.wildcardIds) {
				if(!e.contains(getInfo(s))) {
					e.add(getInfo(s));
				}
			}
		}
		short[] wk = new short[e.size()];

		for(int i = 0; i < wk.length; i++) {
			wk[i] = e.get(i).id;
		}
		info.wildcardIds = wk;
	}

	public static void createBlockStyleReferencesFromName(ElementInformation info) throws ParseException {
		if(info.getBlockStyle() == BlockStyle.NORMAL) {
			ObjectArrayList<ElementInformation> e = new ObjectArrayList<ElementInformation>();
			for(int i = 0; i < infoArray.length; i++) {
				ElementInformation oth = infoArray[i];
				if(oth != null && oth != info && oth.getBlockStyle() != BlockStyle.NORMAL && oth.getBlockStyle() != BlockStyle.NORMAL24) {
					if(oth.name.toLowerCase(Locale.ENGLISH).startsWith(info.name.toLowerCase(Locale.ENGLISH))) {
						oth.setSourceReference(info.id);
						e.add(oth);
					}
				} else if(oth != null && oth != info && oth.slab == 0 && oth.inventoryGroup != null && oth.inventoryGroup.length() > 0 && oth.inventoryGroup.equals(info.inventoryGroup)) {
					e.add(oth);
				}
			}

			short[] styles = new short[e.size()];

			for(int i = 0; i < styles.length; i++) {
				styles[i] = e.get(i).id;
			}
			info.styleIds = styles;
		}
	}

	public static void deleteBlockStyleReferences(ElementInformation info) {
		if(info.styleIds != null) {
			for(short e : info.styleIds) {
				if(isValidType(e)) {
					getInfoFast(e).setSourceReference(0);
				}
			}
			info.styleIds = null;
		}
	}

	public static void deleteWildCardReferences(ElementInformation info) {
		if(info.wildcardIds != null) {
			for(short e : info.wildcardIds) {
				getInfoFast(e).setSourceReference(0);
			}
			info.wildcardIds = null;
		}
	}

	public static void createBlockSlabs(ElementInformation info) throws ParseException {
		if(info.getSlab() != 0) throw new ParseException("Cannot create slab of slab");
		if(info.idName == null) info.idName = info.getNameUntranslated().toUpperCase(Locale.ENGLISH).replaceAll("\\s", "_");
		String quarterName = info.idName + "_" + "QUARTER_SLAB";
		String halfName = info.idName + "_" + "HALF_SLAB";
		String tQuartName = info.idName + "_" + "THREE_QUARTER_SLAB";

		int quarterId = insertIntoProperties(quarterName);
		int halfId = insertIntoProperties(halfName);
		int threeQuarterId = insertIntoProperties(tQuartName);

		ElementInformation copyQuarter = new ElementInformation(info, (short) quarterId, info.name + " 1/4");
		ElementInformation copyHalf = new ElementInformation(info, (short) halfId, info.name + " 1/2");
		ElementInformation copyThreeQuarter = new ElementInformation(info, (short) threeQuarterId, info.name + " 3/4");

		copyQuarter.slabIds = new short[0];
		copyHalf.slabIds = new short[0];
		copyThreeQuarter.slabIds = new short[0];

		copyQuarter.styleIds = new short[0];
		copyHalf.styleIds = new short[0];
		copyThreeQuarter.styleIds = new short[0];

		copyThreeQuarter.shoppable = false;
		copyHalf.shoppable = false;
		copyQuarter.shoppable = false;

		copyThreeQuarter.inRecipe = false;
		copyHalf.inRecipe = false;
		copyQuarter.inRecipe = false;

		copyThreeQuarter.orientatable = true;
		copyHalf.orientatable = true;
		copyQuarter.orientatable = true;

		copyThreeQuarter.producedInFactory = 0;
		copyHalf.producedInFactory = 0;
		copyQuarter.producedInFactory = 0;

		copyQuarter.setBuildIconToFree();
		copyThreeQuarter.slab = (1);
		copyThreeQuarter.setSourceReference(info.getId());
		try {
			addInformationToExisting(copyQuarter);
		} catch(ParserConfigurationException exception) {
			exception.printStackTrace();
		}

		copyHalf.setBuildIconToFree();
		copyHalf.slab = (2);
		copyHalf.setSourceReference(info.getId());
		try {
			addInformationToExisting(copyHalf);
		} catch(ParserConfigurationException exception) {
			exception.printStackTrace();
		}

		copyThreeQuarter.setBuildIconToFree();
		copyQuarter.slab = (3);
		copyQuarter.setSourceReference(info.getId());
		try {
			addInformationToExisting(copyThreeQuarter);
		} catch(ParserConfigurationException exception) {
			exception.printStackTrace();
		}

		info.slabIds = new short[]{copyQuarter.getId(), copyHalf.getId(), copyThreeQuarter.getId()};
	}

	public static int insertIntoProperties(String idName) {
		assert idName != null;
		if(properties.containsKey(idName)) {
			return Integer.parseInt(properties.getProperty(idName).toString());
		} else {
			if(idName.contains("~")) {
				for(int i = MOD_BLOCKS_START; i < MOD_BLOCKS_END; i++) {
					if(!properties.containsValue(String.valueOf(i))) {
						properties.setProperty(idName, String.valueOf(i));
						writePropertiesOrdered();
						return i;
					}
				}
			} else {
				for(int i = 0; i < MOD_BLOCKS_END; i++) {
					if(i < MOD_BLOCKS_START) {
						if(!properties.containsValue(String.valueOf(i))) {
							properties.setProperty(idName, String.valueOf(i));
							writePropertiesOrdered();
							return i;
						}
					} else if(!properties.containsValue(String.valueOf(i)) && !keySet.contains((short) i)) {
						properties.setProperty(idName, String.valueOf(i));
						writePropertiesOrdered();
						return i;
					}
				}
			}
			throw new RuntimeException("No Block ID Free");
		}
	}

	public static void removeByIdName(String idName, boolean removeFromProperties) {
		if(properties.containsKey(idName)) {
			int q;
			if(removeFromProperties) {
				q = Integer.parseInt(properties.remove(idName).toString());
				writePropertiesOrdered();
			} else {
				q = Integer.parseInt(properties.getProperty(idName).toString());
			}

			if(exists(q)) {
				removeFromExisting(getInfo(q));
			}
		}
	}

	public static boolean isLodShape(int id) {
		return id > 0 && id < lodShapeArray.length && lodShapeArray[id];
	}

	public static int getDoorType(short type) {
		return switch(type) {
			case 122, 588, 842, 843, 844 -> PLEX_DOOR;
			case 591, 592, 848, 849, 850 -> BLAST_DOOR;
			case 589, 590, 845, 846, 847 -> GLASS_DOOR;
			case 659, 660, 661, 673, 674, 675, 854, 855, 856, 857, 858, 859, 860, 861, 862, 863, 864 -> FORCE_FIELD;
			default -> -1;
		};
	}

	public static short[] getAllArmorBlocks() {
		ArrayList<Short> armorBlocks = new ArrayList<>();
		for(short s : keyArray) {
			if(getInfo(s).isArmor()) armorBlocks.add(s);
		}
		short[] armorBlocksArray = new short[armorBlocks.size()];
		for(int i = 0; i < armorBlocks.size(); i++) armorBlocksArray[i] = armorBlocks.get(i);
		return armorBlocksArray;
	}

	public static boolean isCrewModule(short type) {
		return false;
	}

	public static class LinkedProperties extends Properties {
		@Serial
		private static final long serialVersionUID = 1L;
		private final Set<Object> keys = new LinkedHashSet<>();

		public LinkedProperties() {
		}

		public Iterable<Object> orderedKeys() {
			return Collections.list(keys());
		}

		@Override
		public Enumeration<Object> keys() {
			return Collections.enumeration(keys);
		}

		@Override
		public Object put(Object key, Object value) {
			keys.add(key);
			return super.put(key, value);
		}
	}

	public static void main(String[] args) throws IOException {
		LinkedProperties properties = new LinkedProperties();
		FileInputStream fileInputStream = new FileInputStream("./data/config/BlockTypes.properties");
		properties.load(fileInputStream);

		IntOpenHashSet taken = new IntOpenHashSet();
		for(Object e : properties.values()) {
			try {
				taken.add(Integer.parseInt(e.toString()));
			} catch(NumberFormatException ex) {
			}
		}
		for(Map.Entry<Object, Object> e : properties.entrySet()) {
			try {
				Integer.parseInt(e.getValue().toString());
			} catch(NumberFormatException ex) {
				for(int i = 100; i < 1000000; i++) {
					if(!taken.contains(i)) {
						e.setValue(String.valueOf(i));
						taken.add(i);
						break;
					}
				}

			}
		}
		System.err.println("CHK: " + properties.size());
		properties.store(new FileWriter("./data/config/BlockTypes.properties"), "");
	}

	public static void writePropertiesOrdered() {
		try {
			Properties p = new Properties() {
				/**
				 *
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public synchronized Enumeration<Object> keys() {
					List<java.util.Map.Entry<Object, Object>> keys = new ArrayList<java.util.Map.Entry<Object, Object>>();

					for(java.util.Map.Entry<Object, Object> k : entrySet()) {
						keys.add(k);
					}

					Collections.sort(keys, (o1, o2) -> Integer.parseInt(o1.getValue().toString()) - Integer.parseInt(o2.getValue().toString()));
					List<Object> ordered = new ArrayList<Object>();

					for(java.util.Map.Entry<Object, Object> e : keys) {
						ordered.add(e.getKey());
					}
					return Collections.enumeration(ordered);
				}
			};
			p.putAll(properties);
			p.store(new FileWriter(propertiesPath), "");
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static void deleteBlockSlabs(ElementInformation info) {
		String quarterName = info.idName + "_" + "QUARTER_SLAB";
		String halfName = info.idName + "_" + "HALF_SLAB";
		String tQuartName = info.idName + "_" + "THREE_QUARTER_SLAB";

		removeByIdName(quarterName, false);
		removeByIdName(halfName, false);
		removeByIdName(tQuartName, false);
	}

	public static boolean isToStashConnectable(short fromType) {
		return fromType == Blocks.LOCK_BOX.getId() || fromType == ElementKeyMap.SHIPYARD_COMPUTER || fromType == ElementKeyMap.SHOP_BLOCK_ID || fromType == ElementKeyMap.MINE_LAYER || fromType == ElementKeyMap.REPAIR_CONTROLLER_ID || fromType == ElementKeyMap.SALVAGE_CONTROLLER_ID;
	}

	public static boolean isReactor(short type) {
		return type == REACTOR_MAIN || type == REACTOR_STABILIZER || type == REACTOR_CONDUIT || isChamber(type);
	}

	public static boolean isChamber(short type) {
		return isValidType(type) && (getInfoFast(type).isReactorChamberAny());
	}

	public static boolean isCommonResource(short type) {
		return type == RESS_CRYS_CRYSTAL_COMMON || type == RESS_ORE_METAL_COMMON;
	}

	public static boolean isRareResource(short type) {
		return type == RESS_RARE_EXOGEN || type == RESS_RARE_METATE || type == RESS_RARE_QUANTANIUM;
	}

	public static boolean isResourceExtractor(short type) {
		return type == FACTORY_CORE_EXTRACTOR || type == FACTORY_GAS_EXTRACTOR || BlockConfig.isCustomModExtractor(type); //TODO custom mod extractors
	}

	public static boolean isInit() {
		return initialized;
	}

	public static boolean isRailLoadOrUnload(short type) {
		return type == RAIL_LOAD || type == RAIL_UNLOAD;
	}
	
	public static boolean isTextBox(short type) {
		return type == Blocks.DISPLAY_MODULE.getId();
	}

	public static ElementInformation getMultiBaseType(short type) {
		if(isValidType(type)) {

			ElementInformation infoPar = getInfo(type);
			ElementInformation info;
			if(infoPar.getSourceReference() != 0) {
				info = getInfo(infoPar.getSourceReference());
			} else {
				info = infoPar;
			}
			if(info.blocktypeIds != null) {
				return info;
			}
		}
		return null;
	}

	public static short convertToByteHP(short type, int hpFull) {
		return getInfo(type).convertToByteHp(hpFull);
	}

	public static int convertToFullHP(short type, short hpByte) {
		return getInfo(type).convertToFullHp(hpByte);
	}

	public static short convertSourceReference(short type) {
		if(isValidType(type)) {
			short sc = (short) getInfoFast(type).getSourceReference();
			return sc != 0 ? sc : type;
		}
		return type;
	}

	public static boolean isButton(short id) {
		return isValidType(id) && getInfoFast(id).button;
	}

	public static short[] getSignalTypesActivatedOnSurround() {
		return signaledByRailArray;
	}

	public static boolean isBeacon(short type) {
		return isValidType(type) && infoArray[type].beacon;
	}

}
