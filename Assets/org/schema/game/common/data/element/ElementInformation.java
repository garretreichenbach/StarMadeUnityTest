package org.schema.game.common.data.element;

import api.config.BlockConfig;
import api.element.block.Blocks;
import api.mod.StarMod;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.linearmath.Transform;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.layout.mxParallelEdgeLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.*;
import org.schema.common.FastMath;
import org.schema.common.ParseException;
import org.schema.common.util.StringTools;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.cubes.shapes.BlockShapeAlgorithm;
import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.client.view.cubes.shapes.orientcube.Oriencube;
import org.schema.game.client.view.gui.GUIBlockSprite;
import org.schema.game.client.view.gui.shop.shopnew.ShopItemElement;
import org.schema.game.client.view.tools.SingleBlockDrawer;
import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.damage.effects.InterEffectHandler;
import org.schema.game.common.controller.damage.effects.InterEffectSet;
import org.schema.game.common.controller.elements.ShipManagerContainer;
import org.schema.game.common.controller.elements.effectblock.EffectElementManager;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.blockeffects.config.ConfigGroup;
import org.schema.game.common.data.blockeffects.config.ConfigPool;
import org.schema.game.common.data.element.annotation.ElemType;
import org.schema.game.common.data.element.annotation.Element;
import org.schema.game.common.data.element.meta.RecipeInterface;
import org.schema.game.common.data.physics.ConvexHullShapeExt;
import org.schema.game.common.data.physics.octree.ArrayOctree;
import org.schema.game.common.data.world.SegmentData;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.facedit.AddElementEntryDialog;
import org.schema.game.server.data.ServerConfig;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.AbstractSceneNode;
import org.schema.schine.graphicsengine.forms.Mesh;
import org.schema.schine.graphicsengine.forms.MeshGroup;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.graphicsengine.forms.gui.GUIColoredRectangle;
import org.schema.schine.graphicsengine.forms.gui.GUITextOverlay;
import org.schema.schine.graphicsengine.forms.gui.graph.GUIGraph;
import org.schema.schine.graphicsengine.forms.gui.graph.GUIGraphElement;
import org.schema.schine.input.InputState;
import org.schema.schine.input.KeyboardMappings;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.*;

public class ElementInformation implements Comparable<ElementInformation> {

	public static final String[] rDesc = {"ore", "plant", "basic", "Cubatom-Splittable", "manufactory", "advanced", "capsule"};
	public static final int FAC_NONE = 0;
	public static final int FAC_CAPSULE = 1;
	public static final int FAC_MICRO = 2;
	public static final int FAC_COMPONENT = 3;
	public static final int FAC_BLOCK = 4;
	public static final int FAC_CHEM = 5;
	public static final int RT_ORE = 0;
	public static final int RT_PLANT = 1;
	public static final int RT_BASIC = 2;
	public static final int RT_CUBATOM_SPLITTABLE = 3;
	public static final int RT_MANUFACTORY = 4;
	public static final int RT_ADVANCED = 5;
	public static final int RT_CAPSULE = 6;
	public static final int CHAMBER_APPLIES_TO_SELF = 0;
	public static final int CHAMBER_APPLIES_TO_SECTOR = 1;
	public static final String[] slabStrings = {"full block", "3/4 block", "1/2 block", "1/4 block"};
	public static final int CHAMBER_PERMISSION_ANY = 0;
	public static final int CHAMBER_PERMISSION_SHIP = 1;
	public static final int CHAMBER_PERMISSION_STATION = 2;
	public static final int CHAMBER_PERMISSION_PLANET = 4;
	private static final float AB = 1.0f / 127.0f;
	//not write as tag (its an attribute)
	@Element(writeAsTag = false, canBulkChange = false, parser = ElemType.ID, cat = EIC.BASICS, order = -1)
	public final short id;
	@Element(consistence = true, parser = ElemType.CONSISTENCE, cat = EIC.CRAFTING_ECONOMY, order = 0)
	public final List<FactoryResource> consistence = new ObjectArrayList<FactoryResource>();
	@Element(cubatomConsistence = true, parser = ElemType.CUBATON_CONSISTENCE, cat = EIC.DEPRECATED, order = 0)
	public final List<FactoryResource> cubatomConsistence = new ObjectArrayList<FactoryResource>();
	@Element(collectionElementTag = "Element", elementSet = true, collectionType = "blockTypes", parser = ElemType.CONTROLLED_BY, cat = EIC.FEATURES, order = 10)
	public final ShortSet controlledBy = new ShortOpenHashSet();
	@Element(collectionElementTag = "Element", elementSet = true, collectionType = "blockTypes", parser = ElemType.CONTROLLING, cat = EIC.FEATURES, order = 11)
	public final ShortSet controlling = new ShortOpenHashSet();
	@Element(collectionElementTag = "Element", elementSet = true, collectionType = "blockTypes", parser = ElemType.RECIPE_BUY_RESOURCE, cat = EIC.CRAFTING_ECONOMY, order = 4)
	public final ShortList recipeBuyResources = new ShortArrayList();
	public final ObjectOpenHashSet<String> parsed = new ObjectOpenHashSet<String>(2048);
	/**
	 * ChamberPrerequisites: chambers needed to specify this one
	 */
	@Element(editable = false, canBulkChange = false, shortSet = true, parser = ElemType.CHAMBER_PREREQUISITES, cat = EIC.POWER_REACTOR, order = 7)
	public final ShortSet chamberPrerequisites = new ShortOpenHashSet();
	/**
	 * ChamberMutuallyExclusive: chamber trees that are mutually exclusive and cannot be built on the same entity
	 */
	@Element(editable = true, canBulkChange = true, shortSet = true, parser = ElemType.CHAMBER_MUTUALLY_EXCLUSIVE, cat = EIC.POWER_REACTOR, order = 8)
	public final ShortSet chamberMutuallyExclusive = new ShortOpenHashSet();
	/**
	 * ChamberChildren: what branches off this chamber
	 */
	@Element(canBulkChange = false, editable = false, shortSet = true, parser = ElemType.CHAMBER_CHILDREN, cat = EIC.POWER_REACTOR, order = 9)
	public final ShortSet chamberChildren = new ShortOpenHashSet();
	@Element(vector4f = true, parser = ElemType.LIGHT_SOURCE_COLOR, cat = EIC.FEATURES, order = 9)
	public final Vector4f lightSourceColor = new Vector4f(1, 1, 1, 1);
	private final int[] textureLayerMapping = new int[6];
	private final int[] textureIndexLocalMapping = new int[6];
	private final int[] textureLayerMappingActive = new int[6];
	private final int[] textureIndexLocalMappingActive = new int[6];
	private final List<FactoryResource> rawConsistence = new ObjectArrayList<FactoryResource>();
	private final List<FactoryResource> totalConsistence = new ObjectArrayList<FactoryResource>();
	private final ElementCountMap rawBlocks = new ElementCountMap();
	@Element(from = 0, to = 10000000, parser = ElemType.ARMOR_VALUE, cat = EIC.HP_ARMOR, order = 6)
	public float armorValue;
	@Element(writeAsTag = false, canBulkChange = false, parser = ElemType.NAME, cat = EIC.BASICS, order = 0)
	public String name = "n/a";
	public ElementCategory type;
	@Element(parser = ElemType.EFFECT_ARMOR, canBulkChange = true, cat = EIC.HP_ARMOR, order = 7)
	public InterEffectSet effectArmor = new InterEffectSet();
	@Element(writeAsTag = false, canBulkChange = false, parser = ElemType.BUILD_ICON, cat = EIC.BASICS, order = 4)
	public int buildIconNum = 62;
	@Element(canBulkChange = false, parser = ElemType.FULL_NAME, cat = EIC.BASICS, order = 1)
	public String fullName = "";
	@Element(from = 0, to = Integer.MAX_VALUE, parser = ElemType.PRICE, cat = EIC.CRAFTING_ECONOMY, order = 1)
	public long price = 100;
	@Element(textArea = true, parser = ElemType.DESCRIPTION, cat = EIC.BASICS, order = 2)
	public String description = "undefined description";
	@Element(states = {"0", "1", "2", "3", "4", "5", "6"}, stateDescs = {"ore", "plant", "basic", "Cubatom-Splittable", "manufactory", "advanced", "capsule"}, parser = ElemType.BLOCK_RESOURCE_TYPE, cat = EIC.CRAFTING_ECONOMY, order = 5)
	public int blockResourceType = 2;
	@Element(states = {"0", "1", "2", "3", "4", "5"}, stateDescs = {"none", "capsule refinery", "micro reprocessor", "component fabricator", "block assembler", "chemical factory"}, parser = ElemType.PRODUCED_IN_FACTORY, cat = EIC.CRAFTING_ECONOMY, order = 6)
	public int producedInFactory;
	@Element(type = true, parser = ElemType.BASIC_RESOURCE_FACTORY, cat = EIC.CRAFTING_ECONOMY, order = 7)
	public short basicResourceFactory;
	@Element(from = 0, to = 1000000, parser = ElemType.FACTORY_BAKE_TIME, cat = EIC.CRAFTING_ECONOMY, order = 8)
	public float factoryBakeTime = 5;
	@Element(inventoryGroup = true, parser = ElemType.INVENTORY_GROUP, cat = EIC.BASICS, order = 3)
	public String inventoryGroup = "";
	@Element(factory = true, parser = ElemType.FACTORY, cat = EIC.CRAFTING_ECONOMY, order = 9)
	public BlockFactory factory;
	@Element(parser = ElemType.ANIMATED, cat = EIC.CRAFTING_ECONOMY, order = 7)
	public boolean animated;
	@Element(from = 0, to = Integer.MAX_VALUE, parser = ElemType.STRUCTURE_HP, cat = EIC.FEATURES, order = 0)
	public int structureHP;
	@Element(parser = ElemType.TRANSPARENCY, cat = EIC.FEATURES, order = 1)
	public boolean blended;
	@Element(parser = ElemType.IN_SHOP, cat = EIC.CRAFTING_ECONOMY, order = 2)
	public boolean shoppable = true;
	@Element(parser = ElemType.ORIENTATION, cat = EIC.BASICS, order = 6)
	public boolean orientatable;
	@Element(selectBlock = true, parser = ElemType.BLOCK_COMPUTER_REFERENCE, cat = EIC.BASICS, order = 7)
	public int computerType;
	@Element(states = {"0", "1", "2", "3"}, stateDescs = {"full block", "3/4 block", "1/2 block", "1/4 block"}, parser = ElemType.SLAB, cat = EIC.BASICS, order = 8)
	public int slab;
	@Element(canBulkChange = false, parser = ElemType.SLAB_IDS, cat = EIC.BASICS, order = 9)
	public short[] slabIds;
	@Element(canBulkChange = false, parser = ElemType.STYLE_IDS, cat = EIC.BASICS, order = 10)
	public short[] styleIds;
	@Element(canBulkChange = false, parser = ElemType.WILDCARD_IDS, cat = EIC.BASICS, order = 11)
	public short[] wildcardIds;
	public short[] blocktypeIds;
	@Element(canBulkChange = false, editable = false, parser = ElemType.SOURCE_REFERENCE, cat = EIC.BASICS, order = 12)
	public int sourceReference;
	/**
	 * GeneralChamber: true if the block is a general chamber block that can be later specified (e.g. Jump Chamber (general) -> Jump Distance (specified))
	 */
	@Element(parser = ElemType.GENERAL_CHAMBER, cat = EIC.POWER_REACTOR, order = 0)
	public boolean chamberGeneral;
	@Element(writeAsTag = false, parser = ElemType.EDIT_REACTOR, cat = EIC.POWER_REACTOR, order = 1)
	public ElementReactorChange change;
	@Element(parser = ElemType.CHAMBER_CAPACITY, cat = EIC.POWER_REACTOR, order = 2)
	public float chamberCapacity;
	/**
	 * ChamberRoot: the top level chamber (jump distance 0)
	 */
	@Element(editable = false, selectBlock = true, parser = ElemType.CHAMBER_ROOT, cat = EIC.POWER_REACTOR, order = 3)
	public int chamberRoot;
	/**
	 * ChamberParent: the parent of a chamber (jump distance 1 has jump distance 0 as parent)
	 */
	@Element(editable = false, selectBlock = true, parser = ElemType.CHAMBER_PARENT, cat = EIC.POWER_REACTOR, order = 4)
	public int chamberParent;
	@Element(editable = false, selectBlock = true, parser = ElemType.CHAMBER_UPGRADES_TO, cat = EIC.POWER_REACTOR, order = 5)
	public int chamberUpgradesTo;
	@Element(states = {"0", "1", "6", "2", "4"}, stateDescs = {"Any", "Ship Only", "Station/Planet Only", "Station Only", "Planet Only"}, parser = ElemType.CHAMBER_PERMISSION, cat = EIC.POWER_REACTOR, order = 6)
	public int chamberPermission;
	@Element(collectionElementTag = "Element", configGroupSet = true, collectionType = "String", stringSet = true, parser = ElemType.CHAMBER_CONFIG_GROUPS, cat = EIC.POWER_REACTOR, order = 10)
	public List<String> chamberConfigGroupsLowerCase = new ObjectArrayList<String>();
	@Element(states = {"0", "1"}, stateDescs = {"self", "sector"}, parser = ElemType.CHAMBER_APPLIES_TO, cat = EIC.POWER_REACTOR, order = 11)
	public int chamberAppliesTo;
	@Element(editable = true, canBulkChange = true, parser = ElemType.REACTOR_HP, cat = EIC.POWER_REACTOR, order = 12)
	public int reactorHp;
	@Element(editable = true, canBulkChange = true, parser = ElemType.REACTOR_GENERAL_ICON_INDEX, cat = EIC.POWER_REACTOR, order = 13)
	public int reactorGeneralIconIndex;
	@Element(parser = ElemType.ENTERABLE, cat = EIC.FEATURES, order = 2)
	public boolean enterable;
	@Element(parser = ElemType.MASS, cat = EIC.HP_ARMOR, order = 0)
	public float mass = 0.1f;
	@Element(parser = ElemType.VOLUME, cat = EIC.HP_ARMOR, order = 1)
	public float volume = -1.0f;
	@Element(from = 1, to = Integer.MAX_VALUE, parser = ElemType.HITPOINTS, cat = EIC.HP_ARMOR, order = 2)
	public int maxHitPointsFull = 100;
	@Element(parser = ElemType.PLACABLE, cat = EIC.FEATURES, order = 3)
	public boolean placable = true;
	@Element(parser = ElemType.IN_RECIPE, cat = EIC.CRAFTING_ECONOMY, order = 3)
	public boolean inRecipe = shoppable;
	@Element(parser = ElemType.CAN_ACTIVATE, cat = EIC.FEATURES, order = 4)
	public boolean canActivate;
	@Element(states = {"1", "3", "6"}, updateTextures = true, parser = ElemType.INDIVIDUAL_SIDES, cat = EIC.TEXTURES, order = 7)
	public int individualSides = 1;
	@Element(parser = ElemType.SIDE_TEXTURE_POINT_TO_ORIENTATION, cat = EIC.TEXTURES, order = 8)
	public boolean sideTexturesPointToOrientation;
	@Element(parser = ElemType.HAS_ACTIVE_TEXTURE, cat = EIC.TEXTURES, order = 9)
	public boolean hasActivationTexure;
	@Element(parser = ElemType.MAIN_COMBINATION_CONTROLLER, cat = EIC.FEATURES, order = 12)
	public boolean mainCombinationController;
	@Element(parser = ElemType.SUPPORT_COMBINATION_CONTROLLER, cat = EIC.FEATURES, order = 13)
	public boolean supportCombinationController;
	@Element(parser = ElemType.EFFECT_COMBINATION_CONTROLLER, cat = EIC.FEATURES, order = 14)
	public boolean effectCombinationController;
	@Element(editable = true, canBulkChange = true, parser = ElemType.BEACON, cat = EIC.FEATURES, order = 15)
	public boolean beacon;
	@Element(parser = ElemType.PHYSICAL, cat = EIC.FEATURES, order = 5)
	public boolean physical = true;
	@Element(parser = ElemType.BLOCK_STYLE, cat = EIC.BASICS, order = 5)
	public BlockStyle blockStyle = BlockStyle.NORMAL;
	@Element(parser = ElemType.LIGHT_SOURCE, cat = EIC.FEATURES, order = 8)
	public boolean lightSource;
	@Element(parser = ElemType.DOOR, cat = EIC.FEATURES, order = 6)
	public boolean door;
	@Element(parser = ElemType.SENSOR_INPUT, cat = EIC.FEATURES, order = 7)
	public boolean sensorInput;
	
	@Element( editable = true, canBulkChange = true, parser = ElemType.DRAW_LOGIC_CONNECTION, cat = EIC.FEATURES, order = 18)
	public boolean drawLogicConnection = false;


	@Element( parser = ElemType.DEPRECATED, cat = EIC.BASICS, order = 13)
	public boolean deprecated;
	public long dynamicPrice = -1;
	@Element(parser = ElemType.RESOURCE_INJECTION, cat = EIC.CRAFTING_ECONOMY, order = 10)
	public ResourceInjectionType resourceInjection = ResourceInjectionType.OFF;
	@Element(from = 0, to = 100000, parser = ElemType.EXPLOSION_ABSOBTION, cat = EIC.HP_ARMOR, order = 8)
	public float explosionAbsorbtion;
	@Element(editable = true, canBulkChange = true, parser = ElemType.EXTENDED_TEXTURE_4x4, cat = EIC.TEXTURES, order = 10)
	public boolean extendedTexture;
	@Element(editable = true, canBulkChange = true, parser = ElemType.ONLY_DRAW_IN_BUILD_MODE, cat = EIC.BASICS, order = 14)
	public boolean drawOnlyInBuildMode;
	@Element(editable = true, canBulkChange = true, parser = ElemType.LOD_SHAPE, modelSelect = true, modelSelectFilter = "lod", cat = EIC.MODELS, order = 2)
	public String lodShapeString = "";
	@Element(states = {"0", "1", "2"}, stateDescs = {"None", "Switch Model", "Keyframe Animation (not implemented yet)"}, parser = ElemType.LOD_ACTIVATION_ANIMATION_STYLE, cat = EIC.MODELS, order = 3)
	public int lodActivationAnimationStyle;
	@Element(editable = true, canBulkChange = true, parser = ElemType.LOD_SHAPE_ACTIVE, modelSelect = true, modelSelectFilter = "lod", cat = EIC.MODELS, order = 4)
	public String lodShapeStringActive = "";
	@Element(editable = true, canBulkChange = true, parser = ElemType.LOD_COLLISION_PHYSICAL, modelSelect = true, modelSelectFilter = "lod", cat = EIC.MODELS, order = 5)
	public boolean lodCollisionPhysical = true;
	@Element(states = {"0", "1", "2"}, stateDescs = {"solid block", "sprite", "invisible"}, parser = ElemType.LOD_SHAPE_FROM_FAR, cat = EIC.MODELS, order = 6)
	public int lodShapeStyle;
	@Element(editable = true, canBulkChange = true, parser = ElemType.LOD_COLLISION, cat = EIC.MODELS, order = 7)
	public LodCollision lodCollision = new LodCollision();
	@Element(editable = true, canBulkChange = true, parser = ElemType.CUBE_CUBE_COLLISION, cat = EIC.MODELS, order = 8)
	public boolean cubeCubeCollision = true;
	@Element(editable = true, canBulkChange = true, parser = ElemType.LOD_USE_DETAIL_COLLISION, cat = EIC.MODELS, order = 9)
	public boolean lodUseDetailCollision;
	@Element(editable = true, canBulkChange = true, parser = ElemType.LOD_DETAIL_COLLISION, cat = EIC.MODELS, order = 10)
	public LodCollision lodDetailCollision = new LodCollision();
	@Element(editable = true, canBulkChange = true, parser = ElemType.LOW_HP_SETTING, cat = EIC.DEPRECATED, order = 0)
	public boolean lowHpSetting;
	@Element(from = 1, to = 127, editable = false, parser = ElemType.OLD_HITPOINTS, cat = EIC.DEPRECATED, order = 0)
	public short oldHitpoints;
	@Element(editable = true, canBulkChange = true, parser = ElemType.SYSTEM_BLOCK, cat = EIC.BASICS, order = 15)
	public boolean systemBlock;
	@Element(editable = true, canBulkChange = true, parser = ElemType.LOGIC_BLOCK, cat = EIC.BASICS, order = 16)
	public boolean signal;
	@Element(editable = true, canBulkChange = true, parser = ElemType.LOGIC_SIGNALED_BY_RAIL, cat = EIC.BASICS, order = 17)
	public boolean signaledByRail;
	@Element(editable = true, canBulkChange = true, parser = ElemType.LOGIC_BUTTON, cat = EIC.BASICS, order = 18)
	public boolean button;
	//wilcard index assigned after initialization
	public int wildcardIndex;
	public String idName;
	public boolean isCornerLodBlock;
	public boolean isSourceBlockTmp;
	//INSERTED CODE
	// Extra mod info for blocks, allows blocks to use hard coded vanilla functionality more easily
	//-- Rail Rotators
	public boolean rotatesCW;
	public StarMod mod;
	@Nullable
	public BlockConfig.RailType railType;
	@Element(writeAsTag = false, canBulkChange = false, parser = ElemType.TEXTURE, cat = EIC.TEXTURES, order = 0)
	private short[] textureId;
	private FixedRecipe productionRecipe;
	private double maxHitpointsInverse;
	private double maxHitpointsByteToFull;
	private double maxHitpointsFullToByte;
	private ShopItemElement shopItemElement;
	private boolean createdTX;
	private boolean specialBlock = true;


	public ElementInformation(ElementInformation v, short id, String name) {
		this.name = name;
		type = v.type;
		setTextureId(v.textureId);
		this.id = id;

		Field[] fields = ElementInformation.class.getFields();
		for(Field f : fields) {
			try {
				if(f.get(v) == null || Modifier.isFinal(f.getModifiers()) || "name".equals(f.getName())) {
					continue;
				}
				//				if(f.getType().isPrimitive()){
				//					System.err.println("SKDJHKSJAHSDKJH "+f.getName()+": "+f.get(v));
				//				}
				f.set(this, f.get(v));
			} catch(IllegalArgumentException e) {
				e.printStackTrace();
			} catch(IllegalAccessException e) {
				e.printStackTrace();
			}

		}
//		signal = calcIsSignal(id);
		assert (v.blockStyle == this.blockStyle);
	}

	public ElementInformation(short id, String name, ElementCategory class1, short[] textureId) {
		this.name = name;
		type = class1;
		this.id = id;
		setTextureId(textureId);
//		signal = calcIsSignal(id);
	}

	public static BlockOrientation convertOrientation(ElementInformation info, byte orientation) {
		BlockOrientation o = new BlockOrientation();

		o.blockOrientation = orientation;
		o.activateBlock = info.activateOnPlacement();

		assert (o.blockOrientation < SegmentData.FULL_ORIENT);

		if(o.blockOrientation > 15) {
//			if (info.getBlockStyle() == 1) {
////				o.blockOrientation -= SegmentData.MAX_ORIENT;
////				o.activateBlock = false;
//				System.err.println("[CLIENT][PLACEBLOCK] BLOCK ORIENTATION (1) EXTENSION: " + (o.blockOrientation + 4) + " -> " + o.blockOrientation);
//			} else if (info.getBlockStyle() == 2) {
////				o.blockOrientation -= SegmentData.MAX_ORIENT;
////				o.activateBlock = false;
//				System.err.println("[CLIENT][PLACEBLOCK] BLOCK ORIENTATION (2) EXTENSION: " + (o.blockOrientation + 8) + " -> " + o.blockOrientation);
//			} else if (info.getBlockStyle() == BlockStyle.SPRITE) {
////				o.blockOrientation -= SegmentData.MAX_ORIENT;
////				o.activateBlock = false;
//				System.err.println("[CLIENT][PLACEBLOCK] BLOCK ORIENTATION (3) EXTENSION: " + (o.blockOrientation + 8) + " -> " + o.blockOrientation);
//			} else if (info.getBlockStyle() == 4) {
////				o.blockOrientation -= SegmentData.MAX_ORIENT;
////				o.activateBlock = false;
//				System.err.println("[CLIENT][PLACEBLOCK] BLOCK ORIENTATION (4) EXTENSION: " + (o.blockOrientation + 8) + " -> " + o.blockOrientation);
//			} else if (info.getBlockStyle() == 5) {
////				o.blockOrientation -= SegmentData.MAX_ORIENT;
////				o.activateBlock = false;
//				System.err.println("[CLIENT][PLACEBLOCK] BLOCK ORIENTATION (5) EXTENSION: " + (o.blockOrientation + 8) + " -> " + o.blockOrientation);
//			} else if (info.getBlockStyle() == 6) {
////				o.blockOrientation -= SegmentData.MAX_ORIENT;
////				o.activateBlock = false;
//				System.err.println("[CLIENT][PLACEBLOCK] BLOCK ORIENTATION (5) EXTENSION: " + (o.blockOrientation + 8) + " -> " + o.blockOrientation);
//			} 
			if(info.blockStyle == BlockStyle.NORMAL) {
				System.err.println("[CLIENT][PLACEBLOCK] Exception: Block orientation was set over 8 on block style " + info + ": " + o.blockOrientation);
				o.blockOrientation = 0;
			}
		}
		return o;
	}

	public static byte defaultActive(short type) {
		return (byte) (type == ElementKeyMap.WEAPON_ID || type == ElementKeyMap.MISSILE_DUMB_ID || type == ElementKeyMap.SHIELD_SUPPLY_MODULE_ID || type == ElementKeyMap.SHIELD_DRAIN_MODULE_ID || type == ElementKeyMap.REPAIR_ID || type == ElementKeyMap.SALVAGE_ID ? 0 : 1);
	}

	public static String getKeyId(short id) {
		Set<Map.Entry<Object, Object>> entrySet = ElementKeyMap.properties.entrySet();
		String key = null;
		for(Map.Entry<Object, Object> e : entrySet) {
			if(e.getValue().equals(String.valueOf(id))) {
				key = e.getKey().toString();
				break;
			}
		}
		return key;
	}

	private static boolean calcIsSignal(short id) {
		return id == ElementKeyMap.ACTIVAION_BLOCK_ID || id == ElementKeyMap.SIGNAL_AND_BLOCK_ID || id == ElementKeyMap.SIGNAL_DELAY_BLOCK_ID || id == ElementKeyMap.SIGNAL_NOT_BLOCK_ID || id == ElementKeyMap.SIGNAL_TRIGGER_STEPON || id == ElementKeyMap.SIGNAL_DELAY_NON_REPEATING_ID || id == ElementKeyMap.SIGNAL_RANDOM || ElementKeyMap.isButton(id) || id == ElementKeyMap.LOGIC_FLIP_FLOP || id == ElementKeyMap.LOGIC_WIRELESS || id == ElementKeyMap.LOGIC_REMOTE_INNER || id == ElementKeyMap.SIGNAL_OR_BLOCK_ID;
	}

	public static boolean isPhysical(SegmentPiece pointUnsave) {
		return isAlwaysPhysical(pointUnsave.getType()) || ElementKeyMap.getInfo(pointUnsave.getType()).isPhysical(pointUnsave.isActive());
	}

	public static boolean isAlwaysPhysical(short type) {
		return !ElementKeyMap.isDoor(type);
	}

	public static boolean isPhysical(short type, SegmentData segmentData, int dataIndex) {
		ElementInformation c = ElementKeyMap.getInfoFast(type);

		return !(ElementKeyMap.isDoor(type) && !segmentData.isActive(dataIndex)) && !(c.hasLod() && c.lodShapeStyle == 2);
	}

	public static boolean isPhysicalRayTests(short type, SegmentData segmentData, int dataIndex) {
		ElementInformation c = ElementKeyMap.getInfoFast(type);

		return !(ElementKeyMap.isDoor(type) && !segmentData.isActive(dataIndex));
	}

	public static boolean isPhysicalFast(short type, SegmentData segmentData,
	                                     int dataIndex) {
		return !(ElementKeyMap.infoArray[type].door && !segmentData.isActive(dataIndex));
	}

	public static void updatePhysical(short newType, short oldType, boolean wasActive, byte x, byte y, byte z, ArrayOctree octree, SegmentData segmentData, int index) {
		if(oldType != newType && ElementKeyMap.isDoor(newType)) {
			boolean nowActive = segmentData.isActive(index);
			if(nowActive && !wasActive) {
				assert (newType != ElementKeyMap.PICKUP_RAIL);
				//door became active
				octree.insert(x, y, z, index);
			} else if(!nowActive && wasActive) {
				//doore became inactive
				octree.delete(x, y, z, index, newType);
			}
		}
	}

	public static boolean isBlendedSpecial(short type, boolean act) {
		return (ElementKeyMap.isDoor(type) && !act) || type == ElementKeyMap.CARGO_SPACE;
	}

	public static boolean isVisException(ElementInformation info,
	                                     short containIndexType, boolean isActive) {
		return info.door && containIndexType < 0;
	}

	public static byte activateOnPlacement(short type) {
		return (type > 0 && ElementKeyMap.getInfo(type).activateOnPlacement()) ? (byte) 1 : (byte) 0;
	}

	public static boolean canBeControlledByAny(short toType) {
		if(!ElementKeyMap.isValidType(toType)) {
			return false;
		}
		return ElementKeyMap.getInfoFast(toType).getControlledBy().size() > 0 ||
				//INSERTED CODE
				ElementKeyMap.getInfoFast(toType).controlling.contains(toType) ||
				///
				(toType == ElementKeyMap.SHIPYARD_CORE_POSITION) || (toType == ElementKeyMap.CARGO_SPACE) || (toType == ElementKeyMap.REACTOR_STABILIZER_STREAM_NODE) || (ElementKeyMap.getInfoFast(toType).lightSource) || (ElementKeyMap.getInfoFast(toType).isInventory()) || (ElementKeyMap.getInfoFast(toType).isInventory()) || (ElementKeyMap.getInfoFast(toType).isInventory()) || (ElementKeyMap.getInfoFast(toType).isSensorInput()) || (toType == ElementKeyMap.ACTIVAION_BLOCK_ID) || (toType == ElementKeyMap.SIGNAL_AND_BLOCK_ID) || (toType == ElementKeyMap.SIGNAL_OR_BLOCK_ID) || (toType == ElementKeyMap.ACTIVAION_BLOCK_ID) || (ElementKeyMap.getInfoFast(toType).signal) || (toType == ElementKeyMap.ACTIVATION_GATE_CONTROLLER) || (ElementKeyMap.isStash(toType)) || (ElementKeyMap.isStash(toType) || (ElementKeyMap.getInfo(toType).isRailTrack()) || (toType == ElementKeyMap.ACTIVAION_BLOCK_ID) ||

				(ElementKeyMap.getInfo(toType).signal) || (ElementKeyMap.getInfo(toType).signal) ||

				(ElementKeyMap.getInfoFast(toType).signal) ||

				(toType == ElementKeyMap.RAIL_BLOCK_DOCKER) || (ElementKeyMap.getInfoFast(toType).canActivate()) || (ElementKeyMap.getInfoFast(toType).isRailTrack()) || isLightConnectAny(toType) || (ElementKeyMap.getInfoFast(toType).mainCombinationController) || (ElementKeyMap.getInfoFast(toType).mainCombinationController));
	}

	public static boolean canBeControlled(short fromType, short toType) {
		if(!ElementKeyMap.isValidType(toType) || !ElementKeyMap.isValidType(fromType)) {
			return false;
		}
		return
				//INSERTED CODE (take a look at uu code)
				ElementKeyMap.getInfoFast(fromType).controlling.contains(toType) ||
						(fromType == ElementKeyMap.GRAVITY_ID && toType == ElementKeyMap.ACTIVAION_BLOCK_ID) ||
						(fromType == ElementKeyMap.GRAVITY_EXIT_ID && toType == ElementKeyMap.ACTIVAION_BLOCK_ID) ||
						(fromType == ElementKeyMap.GRAVITY_ID && toType == ElementKeyMap.GRAVITY_ID) ||
						(fromType == ElementKeyMap.GRAVITY_EXIT_ID && toType == ElementKeyMap.GRAVITY_ID) ||
						(fromType == ElementKeyMap.GRAVITY_ID && toType == ElementKeyMap.GRAVITY_EXIT_ID) ||
						(fromType == ElementKeyMap.GRAVITY_EXIT_ID && toType == ElementKeyMap.GRAVITY_EXIT_ID) ||
						///
						(fromType == ElementKeyMap.SHIPYARD_COMPUTER && toType == ElementKeyMap.SHIPYARD_CORE_POSITION) ||
						(fromType == ElementKeyMap.SHOP_BLOCK_ID && toType == ElementKeyMap.CARGO_SPACE) ||
						(fromType == ElementKeyMap.REACTOR_STABILIZER_STREAM_NODE && toType == ElementKeyMap.REACTOR_STABILIZER_STREAM_NODE) ||
						(fromType == ElementKeyMap.CORE_ID && ElementKeyMap.getInfoFast(toType).lightSource) ||
						(ElementKeyMap.isToStashConnectable(fromType) && ElementKeyMap.getInfoFast(toType).isInventory()) ||
						(ElementKeyMap.getInfoFast(fromType).isInventory() && ElementKeyMap.getInfoFast(toType).isInventory()) ||
						(ElementKeyMap.getInfoFast(fromType).isRailTrack() && ElementKeyMap.getInfoFast(toType).isInventory()) ||
						(fromType == ElementKeyMap.SIGNAL_SENSOR && ElementKeyMap.getInfoFast(toType).isSensorInput()) ||
						(ElementKeyMap.isStash(fromType) && toType == ElementKeyMap.ACTIVAION_BLOCK_ID) ||
						(ElementKeyMap.isStash(fromType) && toType == ElementKeyMap.SIGNAL_AND_BLOCK_ID) ||
						(ElementKeyMap.isStash(fromType) && toType == ElementKeyMap.SIGNAL_OR_BLOCK_ID) ||
						(ElementKeyMap.getInfo(fromType).isRailRotator() && toType == ElementKeyMap.ACTIVAION_BLOCK_ID) ||
						(fromType == ElementKeyMap.ACTIVATION_GATE_CONTROLLER && ElementKeyMap.getInfoFast(toType).signal) ||
						(ElementKeyMap.getInfoFast(fromType).signal && toType == ElementKeyMap.ACTIVATION_GATE_CONTROLLER) ||
//				(fromType == ElementKeyMap.SALVAGE_CONTROLLER_ID && toType == ElementKeyMap.STASH_ELEMENT) || //done in isToStashConnectable
						(ElementKeyMap.getFactorykeyset().contains(fromType) && ElementKeyMap.isStash(fromType)) ||
						(fromType == ElementKeyMap.RAIL_RAIL_SPEED_CONTROLLER && ElementKeyMap.getInfo(toType).isRailTrack()) ||
						(fromType == ElementKeyMap.RAIL_RAIL_SPEED_CONTROLLER && toType == ElementKeyMap.ACTIVAION_BLOCK_ID) ||

						(fromType == ElementKeyMap.SIGNAL_TRIGGER_AREA_CONTROLLER && ElementKeyMap.getInfo(toType).signal) ||
						(fromType == ElementKeyMap.ACTIVATION_GATE_CONTROLLER && ElementKeyMap.getInfo(toType).signal) ||

						(ElementKeyMap.getInfoFast(fromType).isRailDockable() && ElementKeyMap.getInfoFast(toType).signal) ||
						(fromType == ElementKeyMap.TRACTOR_BEAM_COMPUTER && ElementKeyMap.getInfoFast(toType).signal) ||

						(ElementKeyMap.getInfoFast(fromType).signal && toType == ElementKeyMap.RAIL_BLOCK_DOCKER) ||
						(ElementKeyMap.getInfoFast(fromType).signal && ElementKeyMap.getInfoFast(toType).canActivate()) ||
						(ElementKeyMap.getInfoFast(fromType).signal && ElementKeyMap.getInfoFast(toType).isRailTrack()) ||
						ElementKeyMap.getInfoFast(fromType).isLightConnect(toType) ||
						(ElementKeyMap.getInfoFast(fromType).mainCombinationController && ElementKeyMap.getInfoFast(toType).mainCombinationController) ||
						(ElementKeyMap.getInfoFast(fromType).supportCombinationController && ElementKeyMap.getInfoFast(toType).mainCombinationController);
	}

	private static CharSequence getFactoryResourceString(ElementInformation info) {

		if (info.factory == null) {
			return "CANNOT DISPLAY RESOURCES: NOT A FACTORY";
		}
		StringBuffer sb = new StringBuffer();
		if (info.factory.input != null) {
			sb.append("----------Factory Production--------------\n\n");
			assert (info.factory.input != null) : info;
			for (int i = 0; i < info.factory.input.length; i++) {
				sb.append("----------Product-<" + (i + 1) + ">--------------\n");
				sb.append("--- Required Resources:\n");

				for (FactoryResource r : info.factory.input[i]) {
					sb.append(r.count + "x " + ElementKeyMap.getInfo(r.type).getName() + "\n");
				}
				sb.append("\n\n--- Produces Resources:\n");
				for (FactoryResource r : info.factory.output[i]) {
					sb.append(r.count + "x " + ElementKeyMap.getInfo(r.type).getName() + "\n");
				}
				sb.append("\n");
			}
			sb.append("\n---------------------------------------------\n\n");
		}
		return sb.toString();
	}

	public static boolean allowsMultiConnect(short controlledType) {
//		assert(controlledType != ElementKeyMap.CARGO_SPACE || !ElementKeyMap.getInfo(controlledType).isMultiControlled());
		return ElementKeyMap.isValidType(controlledType) && ElementKeyMap.getInfoFast(controlledType).isMultiControlled() && !ElementKeyMap.getInfoFast(controlledType).isRestrictedMultiControlled();
	}

	public static boolean isMedical(short bId) {
		return bId == ElementKeyMap.MEDICAL_CABINET || bId == ElementKeyMap.MEDICAL_SUPPLIES;
	}

	public static boolean isLightConnectAny(short to) {
		if(ElementKeyMap.isValidType(to)) {
			ElementInformation info = ElementKeyMap.getInfo(to);
			return info.lightSource;
		}
		return false;
	}

	public static boolean canBeControlledOld(short fromType, short controlledType) {
		if(fromType != 0 && controlledType != 0) {
			ElementInformation from = ElementKeyMap.getInfo(fromType);
			ElementInformation to = ElementKeyMap.getInfo(controlledType);
			return from.id == ElementKeyMap.CORE_ID && to.effectCombinationController;
		}
		return false;

	}

	private boolean isSensorInput() {
		return isInventory() || door || id == Blocks.SHIPYARD_COMPUTER.getId() || sensorInput;
	}

	public boolean isProducedIn(short factoryId) {
		//INSERTED CODE
		int customFactoryId = BlockConfig.customFactories.get(factoryId);
		if(customFactoryId != 0) {
			if(producedInFactory == customFactoryId) {
				return true;
			}
		}
		///
		return !deprecated && ((factoryId == ElementKeyMap.FACTORY_CAPSULE_REFINERY_ID && producedInFactory == FAC_CAPSULE) || (factoryId == ElementKeyMap.FACTORY_MICRO_ASSEMBLER_ID && producedInFactory == FAC_MICRO) || (factoryId == ElementKeyMap.FACTORY_COMPONENT_FAB_ID && producedInFactory == FAC_COMPONENT) || (factoryId == ElementKeyMap.FACTORY_BLOCK_ASSEMBLER_ID && producedInFactory == FAC_BLOCK) || (factoryId == ElementKeyMap.FACTORY_CHEMICAL_ID && producedInFactory == FAC_CHEM));
	}

	public short getProducedInFactoryType() {
		switch(producedInFactory) {
			case (FAC_CAPSULE):
				return ElementKeyMap.FACTORY_CAPSULE_REFINERY_ID;
			case (FAC_MICRO):
				return ElementKeyMap.FACTORY_MICRO_ASSEMBLER_ID;
			case (FAC_COMPONENT):
				return ElementKeyMap.FACTORY_COMPONENT_FAB_ID;
			case (FAC_BLOCK):
				return ElementKeyMap.FACTORY_BLOCK_ASSEMBLER_ID;
			case (FAC_CHEM):
				return ElementKeyMap.FACTORY_CHEMICAL_ID;

			//INSERTED CODE
			default:
				if(BlockConfig.customFactoryIds.containsKey(producedInFactory)) return BlockConfig.customFactoryIds.get(producedInFactory);
				else return 0;
				///
		}
	}

	private String getDivString(float c) {

		for(float i = 0; i < 8; i++) {
			if(i * 0.125f == c) {
				return (int) i + "/8";
			}
		}
		if (c - FastMath.round(c) == 0) {
			return String.valueOf(c);
		}
		return StringTools.formatPointZeroZero(c);
	}

	public GUIGraphElement getGraphElement(InputState state, String text, int x, int y, int w, int h) {
		return getGraphElement(state, text, x, y, w, h, new Vector4f(0.17f, 0.27f, 0.37f, 1));
	}

	public GUIGraphElement getGraphElement(InputState state, String text, int x, int y, int w, int h, Vector4f color) {

		GUIColoredRectangle content = new GUIColoredRectangle(state, w, h, color);
		content.rounded = 4;

		GUITextOverlay t = new GUITextOverlay(FontSize.TINY_12, state);
		t.setColor(1, 1, 1, 1);
		t.setTextSimple(text);
		t.setPos(5, 5, 0);

		GUIBlockSprite b = new GUIBlockSprite(state, id);
		b.getScale().set(0.5f, 0.5f, 0.5f);
		b.getPos().x = w - 32;
		b.getPos().y = h - 32;
		content.attach(b);
		content.attach(t);
		GUIGraphElement g = new GUIGraphElement(state, content);
		g.setPos(x, y, 0);
		return g;
	}

	public ShopItemElement getShopItemElement(InputState state) {
		if(shopItemElement == null || shopItemElement.getState() != state) {
			shopItemElement = new ShopItemElement(state, this);
			shopItemElement.onInit();
		}
		return shopItemElement;
	}

	public GUIGraph getRecipeGraph(InputState state) {
		GUIGraph graph = new GUIGraph(state);
		int startX = 20;
		int startY = 20;
		int w = 130;
		int h = 80;
		int col = 0;
		String label = getName();
		Vector4f color = new Vector4f(0.17f, 0.27f, 0.37f, 1);
		if (basicResourceFactory != 0) {
			if (basicResourceFactory == ElementKeyMap.FACTORY_MICRO_ASSEMBLER_ID) {
				color.set(0.37f, 0.27f, 0.17f, 1);
			} else {
				color.set(0.37f, 0.17f, 0.27f, 1);
			}
			label += Lng.str("\nManufactured in \n%s", ElementKeyMap.getInfo(basicResourceFactory).getName());
		}

		GUIGraphElement v1 = graph.addVertex(getGraphElement(state, label, startX, startY, w, h, color));
		int i = 0;

		Int2IntOpenHashMap map = new Int2IntOpenHashMap();
		for(FactoryResource c : consistence) {

			ElementKeyMap.getInfo(c.type).addRecipeGraph(map, col + 1, i, w, h, graph, state, v1, c.count, c.count);
			map.put(0, 1);
			i++;
		}

		return graph;
	}

	private void addRecipeGraph(Int2IntOpenHashMap map, int col, int localrow, int w, int h, GUIGraph graph, InputState state, GUIGraphElement v1, float count, float currentMult) {
		int r = map.get(col);
		String cS;
		String totCS;

		cS = getDivString(count);
		totCS = getDivString(currentMult);

		String label = Lng.str("%s\nOne: x %s\nTotal: x %s", getName(), cS, totCS);
		Vector4f color = new Vector4f(0.17f, 0.27f, 0.37f, 1);
		if (basicResourceFactory != 0) {
			if (basicResourceFactory == ElementKeyMap.FACTORY_MICRO_ASSEMBLER_ID) {
				color.set(0.37f, 0.27f, 0.17f, 1);
			} else {
				color.set(0.37f, 0.17f, 0.27f, 1);
			}
			label += Lng.str("\nManufactured in \n%s", ElementKeyMap.getInfo(basicResourceFactory).getName());
		}
		//		GUIGraphElement v2 = graph.addVertex(getGraphElement(state, label, col * (w*2+10), r*(h+10), w, h));
		GUIGraphElement v2 = graph.addVertex(getGraphElement(state, label, r * (w + 10), col * (h * 2 + 10), w, h, color));

		graph.addConnection(v2, v1);

		//		Object e1 = graph
		//				.insertEdge(
		//						parent,
		//						null,
		//						"",
		//						v2,
		//						v1);

		map.put(col, r + 1);

		int j = 0;
		for(FactoryResource c : consistence) {
			System.err.println("ADD CONSISTENCE NORMAL " + c);
			ElementKeyMap.getInfo(c.type).addRecipeGraph(map, col + 1, j, w, h, graph, state, v2, c.count, c.count * currentMult);
			j++;
		}
	}

	private void addGraph(Int2IntOpenHashMap map, int col, int localrow, int w, int h, mxGraph graph, Object v1, Object parent, float count, float currentMult) {

		int r = map.get(col);
		String cS;
		String totCS;

		cS = getDivString(count);
		totCS = getDivString(currentMult);

		String label = getName() + "\n(x" + cS + ")\ntot(x" + totCS + ")";
		Object v2 = graph.insertVertex(parent, null, label, col * (w * 2 + 10), r * (h + 10), w, h);

		Object e1 = graph.insertEdge(parent, null, "", v2, v1);

		map.put(col, r + 1);

		int j = 0;
		if(isCapsule()) {
			System.err.println("CAPSULE " + this + "; Consistence " + consistence);
			if(consistence.size() > 0) {
				FactoryResource factoryResource = consistence.get(0);
				ElementInformation from = ElementKeyMap.getInfo(factoryResource.type);
				System.err.println("CAPSULE " + this + "; Consistence " + consistence + " split-> " + from.cubatomConsistence);
				for (FactoryResource c : from.cubatomConsistence) {
					if (c.type == id) {
						System.err.println("ADDING CUBATOM CONSISTENS FOR " + this + " -> " + ElementKeyMap.getInfo(c.type));
						from.addGraph(map, col + 1, j, w, h, graph, v2, parent, 1.0f / c.count, (1.0f / c.count) * currentMult);
					}
				}
			}
		} else {
			for(FactoryResource c : consistence) {
				System.err.println("ADD CONSISTENCE NORMAL " + c);
				ElementKeyMap.getInfo(c.type).addGraph(map, col + 1, j, w, h, graph, v2, parent, c.count, c.count * currentMult);
				j++;
			}
		}
	}

	public JPanel getGraph() {
		JPanel p = new JPanel(new GridBagLayout());
		mxGraph graph = new mxGraph();
		Object parent = graph.getDefaultParent();
		graph.setCellsEditable(false);
		graph.setConnectableEdges(false);
		graph.getModel().beginUpdate();
		int startX = 20;
		int w = 100;
		int h = 180;
		int col = 0;
		try {
			Object v1 = graph.insertVertex(parent, String.valueOf(id), getName(), startX, 20, w, h);
			int i = 0;

			Int2IntOpenHashMap map = new Int2IntOpenHashMap();
			if(isCapsule()) {
				System.err.println("CAPSULE " + this + "; Consistence " + consistence);
				if(consistence.size() > 0) {
					FactoryResource factoryResource = consistence.get(0);
					ElementInformation from = ElementKeyMap.getInfo(factoryResource.type);
					System.err.println("CAPSULE " + this + "; Consistence " + consistence + " split-> " + from.cubatomConsistence);
					for (FactoryResource c : from.cubatomConsistence) {
						if (c.type == id) {
							System.err.println("ADDING CUBATOM CONSISTENS FOR " + this + " -> " + ElementKeyMap.getInfo(c.type));
							from.addGraph(map, col + 1, i, w, h, graph, v1, parent, 1.0f / c.count, 1.0f / (c.count));
						}
					}
				}
			} else {
				for(FactoryResource c : consistence) {

					ElementKeyMap.getInfo(c.type).addGraph(map, col + 1, i, w, h, graph, v1, parent, c.count, c.count);
					map.put(0, 1);
					i++;
				}
			}

		} finally {
			graph.getModel().endUpdate();
		}

		mxGraphComponent graphComponent = new mxGraphComponent(graph);
		GridBagConstraints gbc_btnGraph = new GridBagConstraints();
		gbc_btnGraph.anchor = GridBagConstraints.NORTHEAST;
		gbc_btnGraph.gridx = 0;
		gbc_btnGraph.gridy = 0;
		gbc_btnGraph.weightx = 1;
		gbc_btnGraph.weighty = 1;
		gbc_btnGraph.fill = GridBagConstraints.BOTH;
		p.add(graphComponent, gbc_btnGraph);

		return p;
	}

	private void addChamberGraph(Int2IntOpenHashMap map, int col, int localrow, int w, int h, mxGraph graph, Object v1, Object parent) {

		int r = map.get(col);
		String cS;
		String totCS;

		String label = "[" + getName() + "]";
		for(String s : chamberConfigGroupsLowerCase) {
			label += "\n" + s;
		}

		Object v2 = graph.insertVertex(parent, null, label, col * (w + 35), r * (h + 10), w, h);

		Object e1 = graph.insertEdge(parent, null, "", v1, v2);

		map.put(col, r + 1);

		int j = 0;
		for(short s : chamberChildren) {
			ElementInformation cInfo = ElementKeyMap.getInfo(s);
			cInfo.addChamberGraph(map, col + 1, j, w, h, graph, v2, parent);
			map.put(0, 1);
			j++;
		}
	}

	public JPanel getChamberGraph() {
		assert(this.chamberGeneral);
		JPanel p = new JPanel(new GridBagLayout());
		mxGraph graph = new mxGraph();
		Object parent = graph.getDefaultParent();
		graph.setCellsEditable(false);
		graph.setConnectableEdges(true);
		graph.getModel().beginUpdate();
		graph.setAllowLoops(false);
		graph.getStylesheet().getDefaultEdgeStyle().put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ELBOW);
		int startX = 20;
		int w = 230;
		int h = 30;
		int col = 0;
		try {
			Object v1 = graph.insertVertex(parent, String.valueOf(id), getName(), startX, 20, w, h);
			int i = 0;

			Int2IntOpenHashMap map = new Int2IntOpenHashMap();

			for(short s : chamberChildren) {
				ElementInformation cInfo = ElementKeyMap.getInfo(s);
				cInfo.addChamberGraph(map, col + 1, i, w, h, graph, v1, parent);
				map.put(0, 1);
				i++;
			}

		} finally {
			graph.getModel().endUpdate();
		}


		mxIGraphLayout layout = new mxParallelEdgeLayout(graph);
		layout.execute(parent);
		mxGraphComponent graphComponent = new mxGraphComponent(graph);
		GridBagConstraints gbc_btnGraph = new GridBagConstraints();
		gbc_btnGraph.anchor = GridBagConstraints.NORTHEAST;
		gbc_btnGraph.gridx = 0;
		gbc_btnGraph.gridy = 0;
		gbc_btnGraph.weightx = 1;
		gbc_btnGraph.weighty = 1;
		gbc_btnGraph.fill = GridBagConstraints.BOTH;
		p.add(graphComponent, gbc_btnGraph);


		return p;
	}

	public long calculateDynamicPrice() {
		if(dynamicPrice < 0) {
			//			System.err.println("CALC DYN PRICE FOR "+name);
			long price = 0;
			if(!consistence.isEmpty()) {
				for(FactoryResource c : consistence) {
					//System.err.println("CALC DYN PRICE FOR "+name+" adding: "+ElementKeyMap.getInfo(c.type).name);
					ElementInformation typeC = ElementKeyMap.getInfo(c.type);
					if(typeC.equals(this)) {
						System.err.println("WARNING: Consistence of " + this.name + " contains self!" + "(" + typeC.name + ")");
					}
					else {
						price += c.count * ElementKeyMap.getInfo(c.type).calculateDynamicPrice();
						if(ElementKeyMap.getInfo(c.type).consistence.isEmpty()){
							price *= ServerConfig.DYNAMIC_RECIPE_PRICE_MODIFIER.getFloat();
						}
					}
				}
			} else {
				price = this.price;
			}
			dynamicPrice = price;
		}
		return dynamicPrice;
	}

	public boolean isCapsule() {
		return blockResourceType == RT_CAPSULE;
	}

	public boolean isOre() {
		return blockResourceType == RT_ORE;
	}

	public void appendXML(Document doc, org.w3c.dom.Element parent) throws CannotAppendXMLException {
		String tagName = "Block";//getName().replaceAll("[^a-zA-Z]+", "");

		org.w3c.dom.Element child = doc.createElement(tagName);

		//		<Gravity type='GRAVITY_ID' icon='0' textureId='192' name='Gravity Unit'>

		String key = getKeyId(id);

		if (key == null) {
			throw new CannotAppendXMLException("Cannot find property key for Block ID " + id + "; Check your Block properties file");
		}
		child.setAttribute("type", key);
		child.setAttribute("icon", String.valueOf(buildIconNum));

		child.setAttribute("textureId", StringTools.getCommaSeperated(getTextureIds()));
		child.setAttribute("name", name);

		Field[] fields = ElementInformation.class.getFields();
		for(Field f : fields) {
			try {
				f.setAccessible(true);
				if(f.get(this) == null) {
					continue;
				}
			} catch(IllegalArgumentException e) {
				e.printStackTrace();
				throw new CannotAppendXMLException(e.getMessage());
			} catch(IllegalAccessException e) {
				e.printStackTrace();
				throw new CannotAppendXMLException(e.getMessage());
			}

			Element annotation = f.getAnnotation(Element.class);

			if(annotation != null && annotation.writeAsTag()) {

				org.w3c.dom.Element node = doc.createElement(annotation.parser().tag);
				try {
					if (annotation.factory()) {
						if (factory.input == null) {
							node.setTextContent("INPUT");
						} else {

							for (int pid = 0; pid < factory.input.length; pid++) {
								org.w3c.dom.Element prodNode = doc.createElement("Product");

								org.w3c.dom.Element inputNode = doc.createElement("Input");
								org.w3c.dom.Element outputNode = doc.createElement("Output");

								for (int i = 0; i < factory.input[pid].length; i++) {
									FactoryResource factoryResource = factory.input[pid][i];

									inputNode.appendChild(factoryResource.getNode(doc));
								}

								for (int i = 0; i < factory.output[pid].length; i++) {
									FactoryResource factoryResource = factory.output[pid][i];

									outputNode.appendChild(factoryResource.getNode(doc));
								}

								prodNode.appendChild(inputNode);
								prodNode.appendChild(outputNode);
								node.appendChild(prodNode);
							}

						}

					} else if(annotation.type()) {
						short string = f.getShort(this);
						node.setTextContent(String.valueOf(string));
					} else if(annotation.cubatom()) {

//						for (int j = 0; j < getCubatomCompound().length; j++) {
//
//							org.w3c.dom.Element n = doc.createElement("Cubatom");
//
//							for (int i = 0; i < getCubatomCompound()[j].getFlavors().length; i++) {
//								org.w3c.dom.Element c = doc.createElement(getCubatomCompound()[j].getFlavors()[i].getState().name().toLowerCase(Locale.ENGLISH));
//								c.setTextContent(getCubatomCompound()[j].getFlavors()[i].name().toLowerCase(Locale.ENGLISH));
//								n.appendChild(c);
//							}
//							node.appendChild(n);
//						}
					} else if (annotation.consistence()) {
						for (int j = 0; j < consistence.size(); j++) {

							FactoryResource factoryResource = consistence.get(j);
							node.appendChild(factoryResource.getNode(doc));

						}
					} else if(annotation.cubatomConsistence()) {
						for(int j = 0; j < cubatomConsistence.size(); j++) {
							FactoryResource factoryResource = cubatomConsistence.get(j);
							node.appendChild(factoryResource.getNode(doc));

						}
					} else if(annotation.vector3f()) {
						Vector3f v = (Vector3f) f.get(this);

						node.setTextContent(v.x + "," + v.y + "," + v.z);

					} else if(annotation.vector4f()) {
						Vector4f v = (Vector4f) f.get(this);

						node.setTextContent(v.x + "," + v.y + "," + v.z + "," + v.w);

					} else if(f.getType().equals(short[].class)) {
						short[] v = (short[]) f.get(this);
						StringBuffer d = new StringBuffer();
						for(int i = 0; i < v.length; i++) {
							d.append(v[i]);
							if(i < v.length - 1) {
								d.append(", ");
							}
						}
						node.setTextContent(d.toString());

					} else if(f.getType().equals(int[].class)) {
						int[] v = (int[]) f.get(this);
						StringBuffer d = new StringBuffer();
						for(int i = 0; i < v.length; i++) {
							d.append(v[i]);
							if(i < v.length - 1) {
								d.append(", ");
							}
						}
						node.setTextContent(d.toString());

					} else if("blocktypes".equals(annotation.collectionType().toLowerCase(Locale.ENGLISH))) {
						@SuppressWarnings("unchecked") Collection<Short> set = (Collection<Short>) f.get(this);
						if(set.isEmpty()) {
							continue;
						}
						for(Short s : set) {
							//							if(ElementKeyMap.getFactorykeyset().contains(getId()) && ElementKeyMap.getFactorykeyset().contains(s)){
							//								continue;
							//								//DO NOT WRITE FACTORIES. they are added automatically
							//							}
							org.w3c.dom.Element item = doc.createElement(annotation.collectionElementTag());
							String keyId = getKeyId(s);
							if(keyId == null) {
								throw new CannotAppendXMLException("[BlockSet] " + f.getName() + " Cannot find property key for Block ID " + s + "; Check your Block properties file");
							}
							item.setTextContent(keyId);
							node.appendChild(item);
						}

					} else if(f.getType().equals(InterEffectSet.class)) {
						InterEffectSet set = (InterEffectSet) f.get(this);

						for(InterEffectHandler.InterEffectType t : InterEffectHandler.InterEffectType.values()) {
							org.w3c.dom.Element item = doc.createElement(t.id);
							String a = String.valueOf(set.getStrength(t));
							item.setTextContent(a);
							node.appendChild(item);

//							System.err.println("SAVING InterEffect "+t.id+": "+a);
						}

					} else if("string".equals(annotation.collectionType().toLowerCase(Locale.ENGLISH))) {
						@SuppressWarnings("unchecked") Collection<String> set = (Collection<String>) f.get(this);
						if(set.isEmpty()) {
							continue;
						}
						for(String s : set) {
							org.w3c.dom.Element item = doc.createElement(annotation.collectionElementTag());
							item.setTextContent(s);
							node.appendChild(item);
						}

					} else if(f.getType() == BlockStyle.class) {
						BlockStyle string = (BlockStyle) f.get(this);

						node.setTextContent(String.valueOf(string.id));
						node.appendChild(doc.createComment(string.realName + ": " + string.desc));

					} else if(f.getType() == LodCollision.class) {
						LodCollision string = (LodCollision) f.get(this);
						string.write(node, doc);

					} else if(f.getType() == ResourceInjectionType.class) {
						ResourceInjectionType string = (ResourceInjectionType) f.get(this);

						node.setTextContent(String.valueOf(string.ordinal()));
						node.appendChild(doc.createComment(string.name().toLowerCase(Locale.ENGLISH)));

					} else {
						String string = f.get(this).toString();

						if(annotation.textArea()) {
							string = string.replace("\n", "\\n\\r").replaceAll("\\r", "\r");
						}
						if(string.length() == 0) {
							continue;
						}
						node.setTextContent(string);

					}
				} catch(Exception e1) {
					e1.printStackTrace();
					throw new CannotAppendXMLException(e1.getMessage());
				}

				child.appendChild(node);
			}
		}

		parent.appendChild(child);
	}

	public short[] getTextureIds() {
		return textureId;
	}

	public boolean canActivate() {
		return canActivate;
	}

	@Override
	public int compareTo(ElementInformation o) {
		return name.compareTo(o.name);
	}

	/**
	 * @return the blockStyle
	 */
	public BlockStyle getBlockStyle() {
		return blockStyle;
	}

	/**
	 * @param blockStyle the blockStyle to set
	 * @throws ParseException
	 */
	public void setBlockStyle(int blockStyle) throws ElementParserException {
		this.blockStyle = BlockStyle.getById(blockStyle);
//		solidBlockStyle = blockStyle > 0 && blockStyle != 3 && blockStyle != 6;
//		blendedBlockStyle = blockStyle == 3 ;
	}

	/**
	 * @return the buildIconNum
	 */
	public int getBuildIconNum() {
		return buildIconNum;
	}

	/**
	 * @param buildIconNum the buildIconNum to set
	 */
	public void setBuildIconNum(int buildIconNum) {
		this.buildIconNum = buildIconNum;
	}

	public Set<Short> getControlledBy() {
		return controlledBy;
	}

	public Set<Short> getControlling() {
		return controlling;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		String n = ElementKeyMap.descriptionTranslations.get(id);
		if(n != null) {
			return n;
		}
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	public byte getExtraOrientation() {
		return (byte) 0;
	}

	public BlockFactory getFactory() {
		return factory;
	}

	public void setFactory(BlockFactory f) {
		factory = f;
	}

	/**
	 * @return the fullName
	 */
	public String getFullName() {
		if(fullName == null) {
			return getName();
		}
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	/**
	 * @return the id
	 */
	public short getId() {
		return id;
	}

	public int getIndividualSides() {
		return individualSides;
	}

	public void setIndividualSides(int individualSides) {
		this.individualSides = individualSides;
	}

	/**
	 * @return the lightSourceColor
	 */
	public Vector4f getLightSourceColor() {
		return lightSourceColor;
	}

	/**
	 * @return the maxHitPoints
	 */
	public int getMaxHitPointsFull() {
		return maxHitPointsFull;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		String n = ElementKeyMap.nameTranslations.get(id);
		if(n != null) {
			return n;
		}
		return name;
	}

	public String getNameUntranslated() {
		return name;
	}

	/**
	 * @return the price
	 */
	public long getPrice(boolean dynamic) {
		if(!dynamic) {
			return price;
		} else {
			return dynamicPrice;
		}
	}

	public List<Short> getRecipeBuyResources() {
		return recipeBuyResources;
	}

	/**
	 * @return the textureId
	 */
	public short getTextureId(int side) {
		return textureId[side];
	}

	public ElementCategory getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return ((ElementInformation) obj).id == id;
	}

	@Override
	public String toString() {
		return getName() + "(" + id + ")";
	}

	/**
	 * @return the animated
	 */
	public boolean isAnimated() {
		return animated;
	}

	/**
	 * @param animated the animated to set
	 */
	public void setAnimated(boolean animated) {
		this.animated = animated;
	}

	/**
	 * @return the blended
	 */
	public boolean isBlended() {
		return blended;
	}

	/**
	 * @param blended the blended to set
	 */
	public void setBlended(boolean blended) {
		this.blended = blended;
	}

	public boolean isController() {
		return !getControlling().isEmpty() || controlsAll();
	}

	public boolean isOldDockable() {
		return id == ElementKeyMap.TURRET_DOCK_ID || id == ElementKeyMap.FIXED_DOCK_ID;
	}

	/**
	 * @return the enterable
	 */
	public boolean isEnterable() {
		return enterable;
	}

	/**
	 * @param enterable the enterable to set
	 */
	public void setEnterable(boolean enterable) {
		this.enterable = enterable;
	}

	/**
	 * @return the inRecipe
	 */
	public boolean isInRecipe() {
		return inRecipe;
	}

	/**
	 * @param inRecipe the inRecipe to set
	 */
	public void setInRecipe(boolean inRecipe) {
		this.inRecipe = inRecipe;
	}

	public boolean isLightSource() {
		return lightSource;
	}

	public void setLightSource(boolean lightSource) {
		this.lightSource = lightSource;
	}

	public boolean isOrientatable() {
		return orientatable;
	}

	public void setOrientatable(boolean orientatable) {
		this.orientatable = orientatable;
	}

	/**
	 * @return the physical
	 */
	public boolean isPhysical() {

		return physical;
	}

	/**
	 * @param physical the physical to set
	 */
	public void setPhysical(boolean physical) {
		this.physical = physical;
	}

	/**
	 * @return the physical
	 */
	public boolean isPhysical(boolean active) {
		return door ? active : physical;
	}

	public boolean isPlacable() {
		return placable;
	}

	/**
	 * @param placable the placable to set
	 */
	public void setPlacable(boolean placable) {
		this.placable = placable;
	}

	/**
	 * @return the shoppable
	 */
	public boolean isShoppable() {
		return shoppable;
	}

	/**
	 * @param shoppable the shoppable to set
	 */
	public void setShoppable(boolean shoppable) {
		this.shoppable = shoppable;
	}

	/**
	 * @param canActivate the canActivate to set
	 */
	public void setCanActivate(boolean canActivate) {
		this.canActivate = canActivate;
	}

	/**
	 * @param price the price to set
	 */
	public void setPrice(long price) {
		this.price = price;
	}

	public int getDefaultOrientation() {

		if(id == ElementKeyMap.CARGO_SPACE) {
			return 4;
		}
		if(id == ElementKeyMap.TRANSPORTER_MODULE) {
			return org.schema.game.common.data.element.Element.TOP;
		}
		if(blockStyle == BlockStyle.SPRITE || individualSides == 3) {
			return org.schema.game.common.data.element.Element.TOP;
		}

		if(blockStyle == BlockStyle.NORMAL24) {
			return 14; //top front
		}
		return id == ElementKeyMap.GRAVITY_ID ? org.schema.game.common.data.element.Element.BOTTOM : org.schema.game.common.data.element.Element.FRONT;
	}

	public boolean isBlendBlockStyle() {
		return blockStyle.blendedBlockStyle || (hasLod() && lodShapeStyle == 1);
	}

	public boolean controlsAll() {
		return signal;
	}

	public boolean isSignal() {
		return signal;
	}

	/**
	 * @return the hasActivationTexure
	 */
	public boolean isHasActivationTexure() {
		return hasActivationTexure;
	}

	/**
	 * @param hasActivationTexure the hasActivationTexure to set
	 */
	public void setHasActivationTexure(boolean hasActivationTexure) {
		this.hasActivationTexure = hasActivationTexure;
	}

	public boolean drawConnection() {
		//default: (ElementKeyMap.factoryInfoArray[e.getKey()] || e.getValue().isSignal());
		return EngineSettings.G_DRAW_ALL_CONNECTIONS.isOn() || (EngineSettings.G_DRAW_ANY_CONNECTIONS.isOn() && drawLogicConnection);
	}

	/**
	 * @return the supportCombinationController
	 */
	public boolean isSupportCombinationControllerB() {
		return supportCombinationController;
	}

	/**
	 * @param supportCombinationController the supportCombinationController to set
	 */
	public void setSupportCombinationController(boolean supportCombinationController) {
		this.supportCombinationController = supportCombinationController;
	}

	/**
	 * @return the mainCombinationController
	 */
	public boolean isMainCombinationControllerB() {
		return mainCombinationController;
	}

	/**
	 * @param mainCombinationController the mainCombinationController to set
	 */
	public void setMainCombinationController(boolean mainCombinationController) {
		this.mainCombinationController = mainCombinationController;
	}

	/**
	 * ok, when: main->main or support->main
	 *
	 * @param to
	 * @return
	 */
	public boolean isCombiConnectSupport(short to) {
		if(ElementKeyMap.isValidType(to)) {
			ElementInformation info = ElementKeyMap.getInfo(to);
			if (mainCombinationController && info.mainCombinationController) {
				return true;
			}
			if (supportCombinationController && info.mainCombinationController) {
				return true;
			}
			return supportCombinationController && info.mainCombinationController;
		}

		return false;
	}

	public boolean isCombiConnectEffect(short to) {
		ElementInformation info = ElementKeyMap.getInfo(to);
		return mainCombinationController && info.effectCombinationController;
	}

	/**
	 * @return the effectCombinationController
	 */
	public boolean isEffectCombinationController() {
		return effectCombinationController;
	}

	/**
	 * @param effectCombinationController the effectCombinationController to set
	 */
	public void setEffectCombinationController(boolean effectCombinationController) {
		this.effectCombinationController = effectCombinationController;
	}

	public boolean isCombiConnectAny(short to) {
		return isCombiConnectSupport(to) || isCombiConnectEffect(to) || isLightConnect(to);
	}

	public boolean isLightConnect(short to) {
		if(ElementKeyMap.isValidType(to)) {
			ElementInformation info = ElementKeyMap.getInfo(to);
			if (mainCombinationController && info.lightSource) {
				return true;
			}
		}
		return false;
	}

	public boolean activateOnPlacement() {
		return !signal && (ElementKeyMap.getFactorykeyset().contains(id) || lightSource || door || ElementKeyMap.isStash(id) || id == ElementKeyMap.PICKUP_AREA);
	}

	public String[] parseDescription(GameClientState state) {
		String d = getDescription();

		d = Lng.str("Block-Armor: ") + "<?>" + " \n\n" + d;
		d = Lng.str("Block-HP: ") + maxHitPointsFull + " \n" + d;
		String ahp = Lng.str("Armor-HP  for Structure: ");
		d = Lng.str("System-HP for Structure: ") + structureHP + " \n" + d;
		d = Lng.str("Mass: ") + StringTools.formatPointZeroZero(mass) + " \n" + d;

		String[] split = d.split("\\n");
		for(int i = 0; i < split.length; i++) {
			split[i] = split[i].replace("$ACTIVATE", KeyboardMappings.ACTIVATE.getKeyChar());

			if(split[i].contains("$RESOURCES")) {
				split[i] = split[i].replace("$RESOURCES", getFactoryResourceString(this));
			}
			if(split[i].contains("$ARMOUR")) {
				split[i] = split[i].replace("$ARMOUR", ""/*String.valueOf(getArmor())*/);
			}
			if (split[i].contains("$HP")) {
				split[i] = split[i].replace("$HP", String.valueOf(maxHitPointsFull));
			}

			if(split[i].contains("$EFFECT")) {
				ShipManagerContainer c = new ShipManagerContainer(state, new Ship(state));
				EffectElementManager<?, ?, ?> effect = c.getEffect(id);
				if (effect != null) {
					split[i] = split[i].replace("$EFFECT",
							Lng.str("You can use this system to upgrade your weapons or to use defensively on your ship.\n\nTo link your Controller to its Modules, press %s on the Controller, then %s on the individual modules, \nor alternatively Shift + $CONNECT_MODULE to mass select grouped modules.\n\nAfterwards you can link it to a weapon by connecting the weapon controller you want to upgrade\nwith the effect controller you just made.\nYou can do this manually with $SELECT_MODULE and $CONNECT_MODULE or in the Weapons Menu.\n\n\nEffect:\n%s\n\nNote that for the full effect, \nyou need to connect 1:1 in size. \nThe amount of linked modules of your effect has to be the same amount of your weapon.\n\nIf not used linked to a weapon, \nthe system has a defensive effect you can enable\nPlace the computer on your hotbar in the Weapons Menu\nand activate it:\n\n",  
									KeyboardMappings.SELECT_MODULE.getKeyChar(),  KeyboardMappings.CONNECT_MODULE.getKeyChar(),  
									"[PLACEHOLDER]"));
				} else {
					split[i] = split[i].replace("$EFFECT", "NO_EFFECT(invalid $ var)");
				}
			}
			if(split[i].contains("$MAINCOMBI")) {

				split[i] = split[i].replace("$MAINCOMBI", Lng.str("This controller can be connected to other weapons\nand systems (cannon, beam, damage pulse, missile)\nto customize your weapon.\n\nTo link your Controller to its Modules, press %s on the Controller, then %s on the individual modules, \nor alternatively Shift + $CONNECT_MODULE to mass select grouped modules.\n\nAfterwards you can link it to another weapon by connecting the weapon controller you want to upgrade\nwith the weapon controller you just made.\nYou can do this manually with $SELECT_MODULE and $CONNECT_MODULE or in the Weapons Menu.\n\nNote that for the full effect, \nyou need to connect 1:1 in size.\n\n", KeyboardMappings.SELECT_MODULE.getKeyChar(), KeyboardMappings.CONNECT_MODULE.getKeyChar()));

			}
		}
		return split;
	}

	/**
	 * @return the deprecated
	 */
	public boolean isDeprecated() {
		return deprecated;
	}

	public void setDeprecated(boolean b) {
		deprecated = b;
	}

	/**
	 * @return the consistence
	 */
	public List<FactoryResource> getConsistence() {
		return consistence;
	}

	public void setProductionRecipe(RecipeInterface productionRecipe) {
		this.productionRecipe = (FixedRecipe) productionRecipe;
	}

	public RecipeInterface getProductionRecipe() {

		if(productionRecipe == null) {
			productionRecipe = new FixedRecipe();
			productionRecipe.costAmount = -1;
			productionRecipe.costType = -1;
			if(consistence.isEmpty()) {
				//empty recipe
				productionRecipe.recipeProducts = new FixedRecipeProduct[0];
			} else if(isCapsule()) {
				ElementInformation from = ElementKeyMap.getInfo(consistence.get(0).type);
				productionRecipe.recipeProducts = new FixedRecipeProduct[1];
				productionRecipe.recipeProducts[0] = new FixedRecipeProduct();

				productionRecipe.recipeProducts[0].input = new FactoryResource[1];
				productionRecipe.recipeProducts[0].input[0] = new FactoryResource(1, from.id);

				productionRecipe.recipeProducts[0].output = new FactoryResource[from.cubatomConsistence.size()];
				int i = 0;
				for(FactoryResource c : from.cubatomConsistence) {
					productionRecipe.recipeProducts[0].output[i] = c;
					i++;
				}

			} else {
				productionRecipe.recipeProducts = new FixedRecipeProduct[1];
				productionRecipe.recipeProducts[0] = new FixedRecipeProduct();

				productionRecipe.recipeProducts[0].input = new FactoryResource[consistence.size()];
				int i = 0;
				for(FactoryResource c : consistence) {
					assert (c != null);
					productionRecipe.recipeProducts[0].input[i] = c;
					i++;
				}

				productionRecipe.recipeProducts[0].output = new FactoryResource[1];
				productionRecipe.recipeProducts[0].output[0] = new FactoryResource(1, id);
			}
		}

		return productionRecipe;
	}

	/**
	 * @return the maxHitpointsInverse
	 */
	public double getMaxHitpointsFullInverse() {
		return maxHitpointsInverse;
	}

	public double getMaxHitpointsByteToFull() {
		return maxHitpointsByteToFull;
	}

	public double getMaxHitpointsFullToByte() {
		return maxHitpointsFullToByte;
	}

	/**
	 * @return the basicResourceFactory
	 */
	public short getBasicResourceFactory() {
		return basicResourceFactory;
	}

	/**
	 * @param basicResourceFactory the basicResourceFactory to set
	 */
	public void setBasicResourceFactory(short basicResourceFactory) {
		this.basicResourceFactory = basicResourceFactory;
	}

	/**
	 * @return the explosionAbsorbtion
	 */
	public float getExplosionAbsorbtion() {
		return explosionAbsorbtion;
	}

	/**
	 * @param explosionAbsorbtion the explosionAbsorbtion to set
	 */
	public void setExplosionAbsorbtion(float explosionAbsorbtion) {
		this.explosionAbsorbtion = explosionAbsorbtion;
	}

	/**
	 * @return the factoryBakeTime
	 */
	public float getFactoryBakeTime() {
		return factoryBakeTime;
	}

	/**
	 * @param factoryBakeTime the factoryBakeTime to set
	 */
	public void setFactoryBakeTime(float factoryBakeTime) {
		this.factoryBakeTime = factoryBakeTime;
	}

	public boolean isMultiControlled() {
		return
				signal ||
						isInventory() ||
						isRailTrack() ||
						isSensorInput() ||
						ElementKeyMap.isTextBox(id);
	}

	public boolean isRestrictedMultiControlled() {
		return id == ElementKeyMap.CARGO_SPACE;
	}

	/**
	 * @return all types that can't have more than one connection to this
	 * e.g. a cago block should only ever be assigned to one inventory
	 */
	public ShortArrayList getRestrictedMultiControlled() {
		if(id == ElementKeyMap.CARGO_SPACE){
			return ElementKeyMap.inventoryTypes;
		} else {
			assert (false) : this;
			return null;
		}
	}

	public String createWikiStub() {
		StringBuffer s = new StringBuffer();

		s.append("{{infobox block" + "\n");
		s.append("  |type=" + getName() + "\n");
		s.append("	|hp=" + maxHitPointsFull + "\n");
		s.append("	|armor=" + "-" + "\n");
		s.append("	|light=" + (lightSource ? "yes" : "no") + "\n");
		if (lightSource) {
			s.append("	|lightColor=" + lightSourceColor + "\n");
		}
		s.append("	|dv=" + id + "\n");
		s.append("}}" + "\n\n");

//		s.append("==Description=="+"\n");
//		s.append(getFilledDescription()+"\n");
//		s.append("-----\n");

		return s.toString();
	}

	public boolean isDoor() {
		return door;
	}

	public void setDoor(boolean c) {
		door = c;
	}

	public int getProducedInFactory() {
		return producedInFactory;
	}

	public void setProducedInFactory(int producedInFactory) {
		this.producedInFactory = producedInFactory;
	}

	/**
	 * @return the inventoryGroup
	 */
	public String getInventoryGroup() {
		return inventoryGroup;
	}

	/**
	 * @param inventoryGroup the inventoryGroup to set
	 */
	public void setInventoryGroup(String inventoryGroup) {
		this.inventoryGroup = inventoryGroup.trim();
	}

	public boolean hasInventoryGroup() {
		return inventoryGroup.length() > 0;
	}

	public boolean isNormalBlockStyle() {
		return blockStyle.cube;
	}

	public boolean isVanilla() {
		return mod == null;
	}

	public boolean isRotatesClockwise() {
		return id == ElementKeyMap.RAIL_BLOCK_CW || rotatesCW;
	}

	public boolean isRailTrack() {
		//INSERTED CODE
		if(railType == BlockConfig.RailType.TRACK) {
			return true;
		}
		///
		return blockStyle == BlockStyle.NORMAL24 && !hasLod() &&
				id != ElementKeyMap.RAIL_BLOCK_DOCKER && 
				id != ElementKeyMap.RAIL_BLOCK_TURRET_Y_AXIS && 
				id != ElementKeyMap.SHIPYARD_CORE_POSITION && 
				id != ElementKeyMap.PICKUP_AREA && 
				id != ElementKeyMap.SHIPYARD_MODULE;
	}
	//-- ....

	///

	public boolean isRailShipyardCore() {
		return (id == ElementKeyMap.SHIPYARD_CORE_POSITION);
	}

	public boolean isRailRotator() {
		//INSERTED CODE
		if(railType == BlockConfig.RailType.ROTATOR) {
			return true;
		}
		///
		return (id == ElementKeyMap.RAIL_BLOCK_CW || id == ElementKeyMap.RAIL_BLOCK_CCW);
	}

	public boolean isRailTurret() {
		//INSERTED CODE
		if(railType == BlockConfig.RailType.TURRET) {
			return true;
		}
		///
		return id == ElementKeyMap.RAIL_BLOCK_TURRET_Y_AXIS;
	}

	public boolean isRailDockable() {
		return isRailTrack() || isRailRotator() || isRailTurret();
	}

	void recreateTextureMapping() {
		for(byte i = 0; i < 6; i++) {
			textureLayerMapping[i] = calcTextureLayerCode(false, i);
			textureIndexLocalMapping[i] = calcTextureIndexLocalCode(false, i);

			textureLayerMappingActive[i] = calcTextureLayerCode(true, i);
			textureIndexLocalMappingActive[i] = calcTextureIndexLocalCode(true, i);
		}

		createdTX = true;

	}

	public byte getTextureLayer(boolean active, byte side) {
		assert (createdTX);
		return (byte) (active ? textureLayerMappingActive[side] : textureLayerMapping[side]);
	}

	public short getTextureIndexLocal(boolean active, byte side) {
		assert (createdTX);
		return (short) (active ? textureIndexLocalMappingActive[side] : textureIndexLocalMapping[side]);
	}

	/**
	 * used to cache
	 *
	 * @param active
	 * @param orientationCode
	 * @return the texture layer (number of texture file) the texture is on
	 */
	private byte calcTextureLayerCode(boolean active, byte orientationCode) {
		return (byte) (Math.abs(getTextureId(active, orientationCode)) / 256);
	}

	/**
	 * used to cache
	 *
	 * @param active
	 * @param orientationCode
	 * @return the texture index on its layer
	 */
	private short calcTextureIndexLocalCode(boolean active, byte orientationCode) {
		return (short) ((getTextureId(active, orientationCode)) % 256);
	}

	private short getTextureId(boolean active, int side) {
		assert (id != ElementKeyMap.ACTIVAION_BLOCK_ID || hasActivationTexure);

		if(hasActivationTexure && !active) {
			return (short) (textureId[side] + 1);
		}
		return textureId[side];
	}

	/**
	 * @param textureId the textureId to set
	 */
	public void setTextureId(short[] textureId) {
		this.textureId = Arrays.copyOf(textureId, textureId.length);
		createdTX = false;
	}

	public void setTextureId(int side, short tex) {
		textureId[side] = tex;
		createdTX = false;
	}

	public void normalizeTextureIds() {
		short texIdBasic = textureId[0];

		if(individualSides >= 6) {
			//each side has its specific texture
			//in the default case all 6 follow in line in the sheet
			for(int side = 0; side < 6; side++) {
				textureId[side] = (short) (texIdBasic + side);
			}
		} else if(individualSides == 3) {
			//assign first and second to top and bottom

			textureId[org.schema.game.common.data.element.Element.TOP] = (texIdBasic);
			textureId[org.schema.game.common.data.element.Element.BOTTOM] = (short) (texIdBasic + 1);

			//give the rest the third in line
			textureId[org.schema.game.common.data.element.Element.LEFT] = (short) (texIdBasic + 2);
			textureId[org.schema.game.common.data.element.Element.RIGHT] = (short) (texIdBasic + 2);
			textureId[org.schema.game.common.data.element.Element.FRONT] = (short) (texIdBasic + 2);
			textureId[org.schema.game.common.data.element.Element.BACK] = (short) (texIdBasic + 2);
		} else {
			//simplest case where all sides share the same texture

			for(int side = 1; side < 6; side++) {
				textureId[side] = texIdBasic;
			}
		}

		recreateTextureMapping();
	}

	public boolean isRailDocker() {
		return id == ElementKeyMap.RAIL_BLOCK_DOCKER;
	}

	public boolean isRailSpeedActivationConnect(short controlledType) {
		return id == ElementKeyMap.RAIL_RAIL_SPEED_CONTROLLER && controlledType == ElementKeyMap.ACTIVAION_BLOCK_ID;
	}

	public boolean isRailSpeedTrackConnect(short controlledType) {
		return id == ElementKeyMap.RAIL_RAIL_SPEED_CONTROLLER && ElementKeyMap.isValidType(controlledType) && ElementKeyMap.getInfo(controlledType).isRailTrack();
	}

	public boolean needsCoreConnectionToWorkOnHotbar() {

		return id != ElementKeyMap.RAIL_BLOCK_DOCKER && id != ElementKeyMap.LOGIC_REMOTE_INNER && id != ElementKeyMap.POWER_BATTERY;
	}

	/**
	 * @return mass of block
	 */
	public float getMass() {
		return mass;
	}

	public int getSlab() {
		return slab;
	}

	public int getSlab(int orientation) {
		if(id == ElementKeyMap.CARGO_SPACE) {
			return orientation;
		}
		return slab;
	}

	public float getVolume() {
		return volume;
	}

	public boolean isInventory() {

		return
				id == ElementKeyMap.STASH_ELEMENT ||
				id == ElementKeyMap.FACTORY_COMPONENT_FAB_ID ||
				id == ElementKeyMap.SHIPYARD_COMPUTER ||
				id == ElementKeyMap.FACTORY_CAPSULE_REFINERY_ID ||
				id == ElementKeyMap.FACTORY_CAPSULE_REFINERY_ADV_ID ||
				id == ElementKeyMap.FACTORY_MICRO_ASSEMBLER_ID ||
				id == ElementKeyMap.FACTORY_BLOCK_ASSEMBLER_ID ||
				id == ElementKeyMap.FACTORY_BLOCK_RECYCLER_ID ||
				id == ElementKeyMap.FACTORY_CHEMICAL_ID ||
				id == ElementKeyMap.FACTORY_GAS_EXTRACTOR ||
				id == ElementKeyMap.FACTORY_CORE_EXTRACTOR ||
				id == ElementKeyMap.SHOP_BLOCK_ID ||
				id == Blocks.LOCK_BOX.getId() ||
				factory != null;
	}

	public boolean isSpecialBlock() {
		return specialBlock;
	}

	public void setSpecialBlock(boolean specialBlock) {
		this.specialBlock = specialBlock;
	}

	public boolean isDrawnOnlyInBuildMode() {
		assert (!drawOnlyInBuildMode || blended);
		return drawOnlyInBuildMode;
	}

	public void setDrawOnlyInBuildMode(boolean drawOnlyInBuildMode) {
		this.drawOnlyInBuildMode = drawOnlyInBuildMode;

	}

	public boolean isConsole() {
		String rawName = name.toLowerCase(Locale.ENGLISH).replaceAll("_", " ");
		return (hasLod() && rawName.contains("console")) || rawName.contains("decorative screen"); //TODO Make this a standardized property; this is incredibly hacky.
	}

	public boolean isInOctree() {
		return id != ElementKeyMap.PICKUP_AREA && id != ElementKeyMap.EXIT_SHOOT_RAIL && id != ElementKeyMap.PICKUP_RAIL;
	}

	public boolean isLightPassOnBlockItself() {

		return EngineSettings.CUBE_LIGHT_NORMALIZER_NEW_M.isOn() && (blockStyle.solidBlockStyle || slab > 0);
	}

	public void onInit() {
		calculateDynamicPrice();
	}

	public boolean hasLod() {
		return lodShapeString.length() > 0;
	}

	public int getModelCount(boolean active) {
		Mesh mesh;
		if(active && lodShapeStringActive != null && lodShapeStringActive.length() > 0) {
			mesh = Controller.getResLoader().getMesh(lodShapeStringActive);
		} else {
			mesh = Controller.getResLoader().getMesh(lodShapeString);
		}
		return mesh.getChilds().size();
	}

	public Mesh getModel(int lod, boolean active) {
		Mesh mesh;
		if(active && lodShapeStringActive != null && lodShapeStringActive.length() > 0) {
			mesh = Controller.getResLoader().getMesh(lodShapeStringActive);
		} else {
			mesh = Controller.getResLoader().getMesh(lodShapeString);
		}
		if(mesh == null) {
			throw new RuntimeException("Model " + lodShapeString + " not found");
		}
		Mesh m = (Mesh) mesh.getChilds().get(lod);
		assert (m != null) : lodShapeString;
		return m;
	}

	private void recalcRawConsistenceRec(FactoryResource cs, int count) {
		ElementInformation info = ElementKeyMap.getInfoFast(cs.type);	
		if(info.consistence.isEmpty()){
			rawConsistence.add(cs);
			rawBlocks.inc(cs.type, count);
			//System.out.println("RAW CONSISTENCE: Base " + ElementKeyMap.getInfo(cs.type) + " " + count);
		} else {
			for(FactoryResource c : info.consistence) {
				recalcRawConsistenceRec(c, c.count * count);
			}
		}
	}

	private void recalcTotalConsistenceRec(FactoryResource cs) {
		ElementInformation info = ElementKeyMap.getInfoFast(cs.type);
		totalConsistence.add(cs);
		for(FactoryResource c : info.consistence){
			recalcTotalConsistenceRec(c);
		}

	}

	public void recalcTotalConsistence() {
		rawBlocks.checkArraySize();
		rawConsistence.clear();
		totalConsistence.clear();
		//System.out.println("RAW CONSISTENCE: " + this.name);
		//Take source block for crafting consistence, probably temporary as consistence itself should already account for this
		List<FactoryResource> sourceConsistence = consistence;
		if(getSourceReference() != 0 && ElementKeyMap.isValidType(getSourceReference())){
			sourceConsistence = ElementKeyMap.getInfo(getSourceReference()).consistence;
		}

		for(FactoryResource c : sourceConsistence) {
			recalcRawConsistenceRec(c, c.count);
		}
		//System.out.println("RAW CONSISTENCE: ----------");
		for(FactoryResource c : sourceConsistence) {
			recalcTotalConsistenceRec(c);
		}
	}

	public ElementCountMap getRawBlocks() {
		return rawBlocks;
	}

	public List<FactoryResource> getRawConsistence() {
		return rawConsistence;
	}

	public List<FactoryResource> getTotalConsistence() {
		return totalConsistence;
	}

	public boolean isExtendedTexture() {
		return extendedTexture;
	}

	public void setBuildIconToFree() {
		short buildIconNum = 0;
		ShortOpenHashSet taken = new ShortOpenHashSet();
		for(Short s : ElementKeyMap.typeList()) {
			if(!ElementKeyMap.isValidType(s)) continue;
			buildIconNum = (short) Math.max(buildIconNum, ElementKeyMap.getInfo(s).buildIconNum);
			taken.add((short) ElementKeyMap.getInfo(s).buildIconNum);
		}
		for(Short s : AddElementEntryDialog.addedBuildIcons) {
			buildIconNum = (short) Math.max(buildIconNum, s);
			taken.add(s);
		}
		for(short s = 0; s < buildIconNum + 2; s++) {
			if(!taken.contains(s) && s > 1000) {
				//remove old if it was added
				
				this.buildIconNum = s;
				
				
				break;
			}
		}
	}

	public boolean isReactorChamberAny() {
		return chamberGeneral || isReactorChamberSpecific();
	}

	public boolean isReactorChamberGeneral() {
		return chamberGeneral;
	}

	public boolean isReactorChamberSpecific() {
		return chamberRoot != 0;
	}

	public short getComputer() {
		return (short) computerType;
	}

	public boolean needsComputer() {
		return ElementKeyMap.isValidType(computerType);
	}

	public ShortSet getChamberChildrenOnLevel(ShortSet out) {
		out.addAll(chamberChildren);
		if(chamberParent != 0) {
			ElementInformation info = ElementKeyMap.getInfo(chamberParent);
			if(info.chamberUpgradesTo == id) {
				info.getChamberChildrenOnLevel(out);
				out.remove((short) info.chamberUpgradesTo);
			}
		}
		return out;
	}

	public short getChamberUpgradedRoot() {
		if(chamberParent != 0) {
			ElementInformation parent = ElementKeyMap.getInfo(chamberParent);
			if(parent.chamberUpgradesTo == id) {
				return parent.getChamberUpgradedRoot();
			} else {
				return id;
			}
		} else {
			return id;
		}
	}

	public boolean isChamberChildrenUpgradableContains(short type) {
		if(chamberChildren.contains(type)) {
			return true;
		}
		if(chamberParent != 0) {
			ElementInformation parent = ElementKeyMap.getInfo(chamberParent);
			if(parent.chamberUpgradesTo == id) {
				return parent.isChamberChildrenUpgradableContains(type);
			}
		}
		return false;
	}

	public boolean isChamberUpgraded() {
		return ElementKeyMap.isValidType(chamberParent) && ElementKeyMap.getInfo(chamberParent).chamberUpgradesTo == id;
	}

	public void sanatizeReactorValues() {
		if(chamberParent != 0 && (!ElementKeyMap.isValidType(chamberParent) || !ElementKeyMap.isChamber((short) chamberParent))) {
			System.err.println("SANATIZED REACTOR chamberParent " + getName() + " -> " + ElementKeyMap.toString(chamberParent));
			chamberParent = 0;
		}
		if(chamberRoot != 0 && (!ElementKeyMap.isValidType(chamberRoot) || !ElementKeyMap.isChamber((short) chamberRoot))) {
			System.err.println("SANATIZED REACTOR chamberRoot " + getName() + " -> " + ElementKeyMap.toString(chamberRoot));
			chamberRoot = 0;
		}
		if(chamberUpgradesTo != 0 && (!ElementKeyMap.isValidType(chamberUpgradesTo) || !ElementKeyMap.isChamber((short) chamberUpgradesTo))) {
			System.err.println("SANATIZED REACTOR chamberUpgradesTo " + getName() + " -> " + ElementKeyMap.toString(chamberUpgradesTo));
			chamberUpgradesTo = 0;
		}

		ShortIterator iterator = chamberPrerequisites.iterator();
		while(iterator.hasNext()) {
			short s = iterator.nextShort();
			if(s != 0 && (!ElementKeyMap.isValidType(s) || !ElementKeyMap.isChamber(s))) {
				System.err.println("SANATIZED REACTOR chamberPrereq " + getName() + " -> " + ElementKeyMap.toString(s));
				iterator.remove();
			}
		}
		iterator = chamberChildren.iterator();
		while(iterator.hasNext()) {
			short s = iterator.nextShort();
			if(s != 0 && (!ElementKeyMap.isValidType(s) || !ElementKeyMap.isChamber(s))) {
				System.err.println("SANATIZED REACTOR chamberChildren " + getName() + " -> " + ElementKeyMap.toString(s));
				iterator.remove();
			}
		}
		if(isReactorChamberSpecific() || id == ElementKeyMap.REACTOR_MAIN || id == ElementKeyMap.REACTOR_CONDUIT || id == ElementKeyMap.REACTOR_STABILIZER){
			if(this.reactorHp == 0){
				this.reactorHp = 10;
			}
		} else {
			reactorHp = 0;
		}
	}

	public String getChamberEffectInfo(ConfigPool pool) {
		if(chamberConfigGroupsLowerCase.isEmpty()) {
			return Lng.str("No Effect");
		} else {
			StringBuffer sb = new StringBuffer();
			for(String s : chamberConfigGroupsLowerCase) {
				ConfigGroup configGroup = pool.poolMapLowerCase.get(s);
				if(configGroup != null) {
					sb.append(configGroup.getEffectDescription());
				}
			}
			return sb.toString().trim();
		}
	}

	private float getChamberCapacityBranchRec(float rec) {
		if(ElementKeyMap.isValidType(chamberParent)) {
			ElementInformation m = ElementKeyMap.getInfoFast(chamberParent);
			return m.getChamberCapacityBranchRec(rec + m.chamberCapacity);
		}
		return rec;
	}

	public float getChamberCapacityBranch() {
		return getChamberCapacityBranchRec(chamberCapacity);
	}

	public int getSourceReference() {
		if(chamberRoot != 0) {
			return chamberRoot;
		}
		return sourceReference;
	}

	public void setSourceReference(int sourceReference) {
		this.sourceReference = sourceReference;
	}

	public float getChamberCapacityWithUpgrades() {
		float chamUp = chamberCapacity;
		if(isChamberUpgraded()) {
			if(ElementKeyMap.isValidType(chamberParent)) {
				chamUp += ElementKeyMap.getInfo(chamberParent).getChamberCapacityWithUpgrades();
			}
		}
		return chamUp;
	}

	public boolean isChamberPermitted(SimpleTransformableSendableObject.EntityType t) {
		if(chamberPermission == CHAMBER_PERMISSION_ANY) {
			return true;
		}
		return switch(t) {
			case PLANET_CORE, PLANET_ICO, PLANET_SEGMENT -> (chamberPermission & CHAMBER_PERMISSION_PLANET) == CHAMBER_PERMISSION_PLANET;
			case SHIP -> (chamberPermission & CHAMBER_PERMISSION_SHIP) == CHAMBER_PERMISSION_SHIP;
			case SHOP, SPACE_STATION -> (chamberPermission & CHAMBER_PERMISSION_STATION) == CHAMBER_PERMISSION_STATION;
			default -> false;
		};
	}

	public String getDescriptionIncludingChamberUpgraded() {
		if(isChamberUpgraded()) {
			return ElementKeyMap.getInfo(chamberParent).getDescription();
		}
		return getDescription();
	}

	public boolean isThisOrParentChamberMutuallyExclusive(short type) {
		if(chamberMutuallyExclusive.contains(type)) {
			return true;
		}
		if(ElementKeyMap.isValidType(chamberParent)) {
			return ElementKeyMap.getInfoFast(chamberParent).isThisOrParentChamberMutuallyExclusive(type);
		}
		return false;
	}

	public boolean isArmor() {
		return armorValue > 0;
	}

	public float getMaxHitPointsOneDivByByte() {
		return AB;
	}

	public byte getMaxHitPointsByte() {
		return 127;
	}

	/**
	 * @param maxHitPoints the maxHitPoints to set
	 */
	public void setMaxHitPointsE(int maxHitPoints) {
		assert (maxHitPoints > 0);
		maxHitPointsFull = maxHitPoints;
		maxHitpointsInverse = (1.0d / maxHitPoints);
		maxHitpointsFullToByte = 127.0d / maxHitPoints;
		maxHitpointsByteToFull = maxHitPoints / 127.0d;
	}

	public short convertToByteHp(int hpFull) {
//		System.err.println("CONVERT: "+hpFull+"; "+maxHitpointsFullToByte+"; "+getMaxHitPointsFull());
		return (short)Math.max(0, Math.min(ElementKeyMap.MAX_HITPOINTS, FastMath.round(((double)hpFull * maxHitpointsFullToByte)))) ; //hp * (127 / maxHitPoints)
	}

	public int convertToFullHp(short hpByte) {
		return (int) (hpByte * maxHitpointsByteToFull); //hp * (maxHitPoints / 127)
	}

	public short getHpOldByte() {
		return oldHitpoints;
	}

	public void setHpOldByte(short oldHitpoints) {
		this.oldHitpoints = oldHitpoints;
	}

	public float getArmorValue() {
		return armorValue;
	}

	public void setArmorValue(float armorValue) {
		this.armorValue = armorValue;
	}

	public boolean isMineAddOn() {
		return id == ElementKeyMap.MINE_MOD_FRIENDS || id == ElementKeyMap.MINE_MOD_PERSONAL || id == ElementKeyMap.MINE_MOD_STEALTH || id == ElementKeyMap.MINE_MOD_STRENGTH;
	}

	public boolean isMineType() {
		return id == ElementKeyMap.MINE_TYPE_CANNON || id == ElementKeyMap.MINE_TYPE_MISSILE || id == ElementKeyMap.MINE_TYPE_PROXIMITY;
	}

	public boolean isDecorative() {
		return !isArmor() && !systemBlock;
	}

	public boolean isBeacon() {
		return beacon;
	}

	public boolean isPlant() {
		return id == ElementKeyMap.TERRAIN_FLOWERS_BLUE_SPRITE || id == ElementKeyMap.TERRAIN_GRASS_LONG_SPRITE ||
				id == ElementKeyMap.TERRAIN_BERRY_BUSH_SPRITE || id == ElementKeyMap.TERRAIN_FLOWERS_YELLOW_SPRITE ||
				id == ElementKeyMap.TERRAIN_CACTUS_SMALL_SPRITE || id == ElementKeyMap.TERRAIN_CACTUS_ARCHED_SPRITE ||
				id == ElementKeyMap.TERRAIN_FLOWERS_DESERT_SPRITE || id == ElementKeyMap.TERRAIN_ROCK_SPRITE ||
				id == ElementKeyMap.TERRAIN_CORAL_RED_SPRITE || id == ElementKeyMap.TERRAIN_SHROOM_RED_SPRITE ||
				id == ElementKeyMap.TERRAIN_FUNGAL_GROWTH_SPRITE || id == ElementKeyMap.TERRAIN_FUNGAL_TRAP_SPRITE ||
				id == ElementKeyMap.TERRAIN_FLOWER_FAN_PURPLE_SPRITE || id == ElementKeyMap.TERRAIN_GLOW_TRAP_SPRITE ||
				id == ElementKeyMap.TERRAIN_WEEDS_PURPLE_SPRITE || id == ElementKeyMap.TERRAIN_YHOLE_PURPLE_SPRITE ||
				id == ElementKeyMap.TERRAIN_FAN_FLOWER_ICE_SPRITE || id == ElementKeyMap.TERRAIN_ICE_CRAG_SPRITE ||
				id == ElementKeyMap.TERRAIN_CORAL_ICE_SPRITE || id == ElementKeyMap.TERRAIN_SNOW_BUD_SPRITE;
	}

	public enum EIC {
		BASICS("Basics"), MODELS("Models"), TEXTURES("Textures"), FEATURES("Features"), HP_ARMOR("Hp & Armor"), CRAFTING_ECONOMY("Crafting & Economy"), POWER_REACTOR("Power & Reactor"), DEPRECATED("Deprecated");

		private final String name;
		public boolean collapsed = true;

		EIC(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public enum ResourceInjectionType {
		OFF(0), ORE(1), FLORA(17);

		public final int index;

		ResourceInjectionType(int index) {
			this.index = index;
		}
	}

	public static class LodCollision {
		private final Vector3f localPosTmp = new Vector3f();
		private final Transform tmpTrans0 = new Transform();
		private final Transform tmpTrans1 = new Transform();
		private final Matrix3f rotTmp = new Matrix3f();
		public String modelId;
		public MeshGroup meshGroup;
		public ConvexHullShapeExt[] hulls;
		public CollisionShape shape;
		public boolean valid = true;
		public LodCollisionType type = LodCollisionType.BLOCK_TYPE;
		public BlockStyle blockTypeToEmulate = BlockStyle.NORMAL;
		public int colslab;

		public void load() {
			if(type == LodCollisionType.NONE) {
				return;
			} else if(type == LodCollisionType.BLOCK_TYPE) {
				return;
			}
			meshGroup = (MeshGroup) Controller.getResLoader().getMesh(modelId);
			if(meshGroup == null) {
				throw new RuntimeException("Collision Mesh Not found: '" + modelId + "'");
			}
			int hullCount = 0;
			for(AbstractSceneNode n : meshGroup.getChilds()) {
				if(n instanceof Mesh) {
					hullCount++;
				}
			}
			hulls = new ConvexHullShapeExt[hullCount];
			CompoundShape cs = new CompoundShape();
			int i = 0;
			for(AbstractSceneNode n : meshGroup.getChilds()) {
				if(n instanceof Mesh) {
					Mesh mesh = (Mesh) n;
					ConvexHullShapeExt c = new ConvexHullShapeExt(mesh.getVerticesListInstance());
					hulls[i] = c;

					Transform initialTransformWithoutScale = mesh.getInitialTransformWithoutScale(new Transform());
					cs.addChildShape(initialTransformWithoutScale, c);
					i++;
				}
			}
			shape = cs;

		}

		public boolean isValid() {
			return valid;
		}

		public String toString() {
			if(type == LodCollisionType.CONVEX_HULL) {
				return "LOD_COLLSISION(Type: " + type.type + "; Model: '" + modelId + "')";
			} else {
				return "LOD_COLLSISION(Type: " + type.type + "; Model: '" + blockTypeToEmulate + "')";
			}
		}

		public void parse(Node rnode) {
			NodeList cn = rnode.getChildNodes();
			Node tt = rnode.getAttributes().getNamedItem("type");
			if(tt == null) {
				return;
//				throw new RuntimeException("no type for col");
			}
			String typeString = tt.getNodeValue();

			for(int i = 0; i < LodCollisionType.values().length; i++) {
				if(LodCollisionType.values()[i].type.toLowerCase(Locale.ENGLISH).equals(typeString.toLowerCase(Locale.ENGLISH))) {
					type = LodCollisionType.values()[i];
					break;
				}
			}
			if(type == null) {
				throw new RuntimeException("NO TYPE: " + typeString);
			}
			switch(type) {
				case NONE:
					break;
				case BLOCK_TYPE:
					colslab = Integer.parseInt(rnode.getAttributes().getNamedItem("slab").getNodeValue());
					blockTypeToEmulate = BlockStyle.parse((org.w3c.dom.Element) rnode);
					break;
				case CONVEX_HULL:
					for(int i = 0; i < cn.getLength(); i++) {
						Node item = cn.item(i);
						if(item.getNodeType() == Node.ELEMENT_NODE) {
							if("mesh".equals(item.getNodeName().toLowerCase(Locale.ENGLISH))) {
								modelId = item.getTextContent();
							}
						}
					}
					break;
				default:
					throw new RuntimeException("UNKNOWN TYPE: " + type.name());
			}
			valid = true;
		}

		public void write(org.w3c.dom.Element node, Document doc) {
			Attr createAttribute = doc.createAttribute("type");
			createAttribute.setValue(type.type);
			node.getAttributes().setNamedItem(createAttribute);
			switch(type) {
				case NONE:
					break;
				case BLOCK_TYPE:
					blockTypeToEmulate.write(node, doc);
					Attr a = doc.createAttribute("slab");
					a.setNodeValue(String.valueOf(colslab));
					node.getAttributes().setNamedItem(a);
					break;
				case CONVEX_HULL:
					org.w3c.dom.Element bb = doc.createElement("Mesh");
					bb.setTextContent(modelId);
					node.appendChild(bb);
					break;
				default:
					throw new RuntimeException("UNKNOWN TYPE: " + type.name());
			}
		}

		public CollisionShape getShape(short t, byte orientation, Transform convexHullTransOut) {
			switch(type) {
				case BLOCK_TYPE:
					return BlockShapeAlgorithm.getShape(blockTypeToEmulate, orientation);
				case CONVEX_HULL:
					ElementInformation info = ElementKeyMap.getInfoFast(t);
					Oriencube algo = (Oriencube) BlockShapeAlgorithm.algorithms[5][info.blockStyle == BlockStyle.SPRITE ? (orientation % 6) * 4 : orientation];//(Oriencube)BlockShapeAlgorithm.getAlgo(BlockStyle.NORMAL24, orientation);
//				localPosTmp.set(0,0,0);
//				Transform primaryTransform = algo.getMirrorAlgo().getPrimaryTransform(localPosTmp, 0, tmpTrans0);
//				Transform secondaryTransform = algo.getMirrorAlgo().getSecondaryTransform(tmpTrans1);
//				convexHullTransOut.set(primaryTransform);
//				convexHullTransOut.mul(secondaryTransform);
					convexHullTransOut.set(algo.getBasicTransform());

					if(info.getBlockStyle() == BlockStyle.SPRITE) {
						//rotate sprite shape LOD because their inital rot is fucked up
						rotTmp.setIdentity();
						rotTmp.rotX(SingleBlockDrawer.timesR * (FastMath.PI / 2.0f));
						convexHullTransOut.basis.mul(rotTmp);
					}

					convexHullTransOut.origin.set(0, 0, 0);
					return shape;
				case NONE:
					return null;
				default:
					throw new RuntimeException("UNKNOWN TYPE: " + type.name());
			}
		}

		public enum LodCollisionType {
			BLOCK_TYPE("BlockType"), CONVEX_HULL("ConvexHull"), NONE("None"),
			;
			public final String type;

			LodCollisionType(String type) {
				this.type = type;
			}

			public String toString() {
				return type;
			}
		}
	}


}
