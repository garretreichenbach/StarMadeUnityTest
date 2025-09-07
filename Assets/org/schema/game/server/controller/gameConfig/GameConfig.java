package org.schema.game.server.controller.gameConfig;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.schema.common.XMLTools;
import org.schema.common.config.ConfigParserException;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.meta.BlueprintMetaItem;
import org.schema.game.common.data.element.meta.Logbook;
import org.schema.game.common.data.element.meta.MetaObject;
import org.schema.game.common.data.element.meta.MetaObjectManager;
import org.schema.game.common.data.element.meta.MetaObjectManager.MetaObjectType;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.world.SimpleTransformableSendableObject.EntityType;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.controller.gameConfig.InventoryStarterGearFactory.SlotType;
import org.schema.game.server.data.BlueprintInterface;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.ServerConfig;
import org.schema.game.server.data.blueprintnw.BlueprintEntry;
import org.schema.game.server.data.blueprintnw.BlueprintType;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.graphicsengine.forms.BoundingBox;
import org.schema.schine.resource.FileExt;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.Short2IntOpenHashMap;

public class GameConfig {

	private static final String defaultConfigPath = "." + File.separator + "data" + File.separator + "config" + File.separator + "GameConfigDefault.xml";
	private static final String configPath = "." + File.separator + "GameConfig.xml";
	private final ObjectArrayList<InventoryStarterGearFactory> starterGearList = new ObjectArrayList<InventoryStarterGearFactory>();

	public final Short2IntOpenHashMap groupLimits = new Short2IntOpenHashMap();
	public final Short2IntOpenHashMap controllerLimits = new Short2IntOpenHashMap();
	
	public double massLimitShip = -1;
	public double massLimitPlanet = -1;
	public double massLimitStation = -1;
	public int blockLimitShip = -1;
	public int blockLimitPlanet = -1;
	public int blockLimitStation = -1;

	public float sunDamagePerBlock = 1;

	public float sunMinIntensityDamageRange = 1.42f;
	public float sunMaxDelayBetweenHits = 0.2f;
	public float sunDamageRadius = 8;
	public float sunDamageDelay = 5;
	public float sunDamageMin = 100;
	public float sunDamageMax = 1000000;
	public Vector3i stationSize = new Vector3i(-1, -1, -1);
	public Vector3i shipSize = new Vector3i(-1, -1, -1);
	private boolean hasMaxDim;
	private static int defaultVersion;
	private static int version;

	public GameConfig(GameServerState state) {
		super();
		float f = (ServerConfig.RESTRICT_BUILDING_SIZE.getFloat());
		if(f > 0){
			int secsize = (ServerConfig.SECTOR_SIZE.getInt());
			int rSize = (int)(secsize*f);
			stationSize.set(rSize, rSize, rSize);
			shipSize.set(rSize, rSize, rSize);
		}
	}
	public static void removeConfig() throws IOException{
		File configFile = new FileExt(configPath);
		File configFileBackUp = new FileExt(configPath+"."+System.currentTimeMillis()+".backup.xml");
		FileUtil.copyFile(configFile, configFileBackUp);
		configFile.delete();
	}
	public static boolean isLatestVersion() throws IOException, SAXException, ParserConfigurationException{
		File configFile = new FileExt(configPath);
		File defaultConfigFile = new FileExt(defaultConfigPath);
		Document doc;
		if (!configFile.exists()) {
			FileUtil.copyFile(defaultConfigFile, configFile);
			doc = XMLTools.loadXML(configFile);
		}else{
			Document defdoc = XMLTools.loadXML(defaultConfigFile);
			String v = defdoc.getDocumentElement().getAttribute("version");
			if(v != null){
				try{
					defaultVersion = Integer.parseInt(v);
				}catch(Exception e){
				}
			}
			doc = XMLTools.loadXML(configFile);
			v = doc.getDocumentElement().getAttribute("version");
			if(v != null){
				try{
					version = Integer.parseInt(v);
				}catch(Exception e){
				}
			}
		}
		return version >= defaultVersion;
	}
	public void parse() throws IOException, SAXException, ParserConfigurationException, ConfigParserException {
		if(!isLatestVersion()){
			System.err.println("[GAMECONFIG] old game config version detected: current: "+version+"; required version: "+defaultVersion+"; backing up and using new default config!");
			removeConfig();
		}
		File configFile = new FileExt(configPath);
		File defaultConfigFile = new FileExt(defaultConfigPath);
		Document doc;
		if (!configFile.exists()) {
			FileUtil.copyFile(defaultConfigFile, configFile);
		}
		doc = XMLTools.loadXML(configFile);
		parse(doc);
	}

	private void parse(Document config) throws ConfigParserException {
		org.w3c.dom.Element root = config.getDocumentElement();
		NodeList childNodesTop = root.getChildNodes();
		starterGearList.clear();
		groupLimits.clear();
		controllerLimits.clear();
		boolean foundTop = false;
		for (int j = 0; j < childNodesTop.getLength(); j++) {
			Node itemTop = childNodesTop.item(j);
			if (itemTop.getNodeType() == Node.ELEMENT_NODE) {

				String name = itemTop.getNodeName().toLowerCase(Locale.ENGLISH);

				if (name.equals("startinggear")) {
					parseStartingGear(itemTop);
				}
				
				if (name.equals("grouplimits")) {
					parseGroupLimits(itemTop);
				}

				if (name.equals("shiplimits") || name.equals("planetlimits") || name.equals("stationlimits")) {
					oarseControllerLimits(name, itemTop);
				}
				
				if (name.equals("sunheatdamage")) {
					parseHeatDamage(itemTop);
				}
				if (name.equals("maxdimensionship")) {
					parseMaxDimension(itemTop, shipSize);
				}
				if (name.equals("maxdimensionstation")) {
					parseMaxDimension(itemTop, stationSize);
				}
			}
		}
	}

	

	private void parseHeatDamage(Node itemTop) {
		NodeList childsStartingGearTop = itemTop.getChildNodes();
		for (int j = 0; j < childsStartingGearTop.getLength(); j++) {
			Node gearItem = childsStartingGearTop.item(j);

			if (gearItem.getNodeType() == Node.ELEMENT_NODE) {
				String n = gearItem.getNodeName();				
				if(n.toLowerCase(Locale.ENGLISH).equals("damageperblock")){
					sunDamagePerBlock = Float.parseFloat(gearItem.getTextContent());
				}
				if(n.toLowerCase(Locale.ENGLISH).equals("sunminintensitydamagerange")){
					sunMinIntensityDamageRange = Float.parseFloat(gearItem.getTextContent());
				}
				if(n.toLowerCase(Locale.ENGLISH).equals("maxdelaybetweenhits")){
					sunMaxDelayBetweenHits = Float.parseFloat(gearItem.getTextContent());
				}
				if(n.toLowerCase(Locale.ENGLISH).equals("sundamageradius")){
					sunDamageRadius = Float.parseFloat(gearItem.getTextContent());
				}
				if(n.toLowerCase(Locale.ENGLISH).equals("sundamagedelayinsecs")){
					sunDamageDelay = Float.parseFloat(gearItem.getTextContent());
				}
				if(n.toLowerCase(Locale.ENGLISH).equals("sundamagemax")){
					sunDamageMax = Float.parseFloat(gearItem.getTextContent());
				}
				if(n.toLowerCase(Locale.ENGLISH).equals("sundamagemin")){
					sunDamageMax = Float.parseFloat(gearItem.getTextContent());
				}
			}
		}
	}
	private void parseMaxDimension(Node itemTop, Vector3i v) {
		hasMaxDim = true;
		NodeList childsStartingGearTop = itemTop.getChildNodes();
		for (int j = 0; j < childsStartingGearTop.getLength(); j++) {
			Node gearItem = childsStartingGearTop.item(j);

			if (gearItem.getNodeType() == Node.ELEMENT_NODE) {
				String n = gearItem.getNodeName().toLowerCase(Locale.ENGLISH);
				try{
					int value = Integer.parseInt(gearItem.getTextContent());
					if(n.equals("x")){
						v.x = value;
					}
					if(n.equals("y")){
						v.y = value;
					}
					if(n.equals("z")){
						v.z = value;
					}
				}catch(NumberFormatException e){
					e.printStackTrace();
				}
			}
		}
	}
	private void oarseControllerLimits(String name, Node itemTop) throws ConfigParserException {
		NodeList childsStartingGearTop = itemTop.getChildNodes();
		for (int j = 0; j < childsStartingGearTop.getLength(); j++) {
			Node gearItem = childsStartingGearTop.item(j);

			if (gearItem.getNodeType() == Node.ELEMENT_NODE) {
				String n = gearItem.getNodeName().toLowerCase(Locale.ENGLISH);
				if(n.equals("mass")){
					try{
						float value = Float.parseFloat(gearItem.getTextContent());
						if (name.contains("ship")) {
							massLimitShip = value;
						} else if (name.contains("planet")) {
							massLimitPlanet = value;
						} else if (name.contains("station")) {
							massLimitStation = value;
						}
					}catch(NumberFormatException e){
						e.printStackTrace();
					}
				}
				if(n.equals("blocks")){
					try{
						int value = Integer.parseInt(gearItem.getTextContent());
						if (name.contains("ship")) {
							blockLimitShip = value;
						} else if (name.contains("planet")) {
							blockLimitPlanet = value;
						} else if (name.contains("station")) {
							blockLimitStation = value;
						}
					}catch(NumberFormatException e){
						e.printStackTrace();
					}
				}
			}
		}
	}
	private void parseGroupLimits(Node itemTop) throws ConfigParserException {
		
		NodeList childsStartingGearTop = itemTop.getChildNodes();
		for (int j = 0; j < childsStartingGearTop.getLength(); j++) {
			Node gearItem = childsStartingGearTop.item(j);

			if (gearItem.getNodeType() == Node.ELEMENT_NODE) {
				String n = gearItem.getNodeName();
				if(n.toLowerCase(Locale.ENGLISH).equals("controller")){
					Integer id = null;
					Integer groupMax = null;
					Integer computerMax = null;
					NodeList cList = gearItem.getChildNodes();
					for (int i = 0; i < cList.getLength(); i++) {
						Node m = cList.item(i);
						if (m.getNodeType() == Node.ELEMENT_NODE) {
//							System.err.println("PP "+m.getNodeName()+"; "+m.getTextContent());
							String nm = m.getNodeName().toLowerCase(Locale.ENGLISH);
							if(nm.equals("id")){
								try{
									id = Integer.parseInt(m.getTextContent());
								}catch(NumberFormatException e){
									e.printStackTrace();
								}
							}
							if(nm.equals("groupmax")){
								try{
									groupMax = Integer.parseInt(m.getTextContent());
								}catch(NumberFormatException e){
									e.printStackTrace();
								}
							}
							if(nm.equals("computermax")){
								try{
									computerMax = Integer.parseInt(m.getTextContent());
								}catch(NumberFormatException e){
									e.printStackTrace();
								}
							}
						}
					}
					
					if(id == null){
						throw new ConfigParserException("Error parsing GroupLimits; 'id' missing");
					}
					if(groupMax == null && computerMax == null){
						throw new ConfigParserException("Error parsing GroupLimits; 'GroupMax' or 'ComputerMax' missing");
					}else{
						if(groupMax != null){
							groupLimits.put(id.shortValue(), groupMax.intValue());
						}
						if(computerMax != null){
							controllerLimits.put(id.shortValue(), computerMax.intValue());
						}
					}
				}
			}
		}
	}

	private void parseStartingGear(Node startingGearTop) throws ConfigParserException {
		NodeList childsStartingGearTop = startingGearTop.getChildNodes();
		for (int j = 0; j < childsStartingGearTop.getLength(); j++) {
			Node gearItem = childsStartingGearTop.item(j);

			if (gearItem.getNodeType() == Node.ELEMENT_NODE) {
				String n = gearItem.getNodeName();

				if (n.toLowerCase(Locale.ENGLISH).equals("credits")) {
					parseCredits(gearItem);
				} else if (n.toLowerCase(Locale.ENGLISH).equals("block")) {
					parseBlock(gearItem);
				} else if (n.toLowerCase(Locale.ENGLISH).equals("tool")) {
					parseTool(gearItem);
				} else if (n.toLowerCase(Locale.ENGLISH).equals("helmet")) {
					parseHelmet(gearItem);
				} else if (n.toLowerCase(Locale.ENGLISH).equals("buildinhibiter")) {
					parseInhibiter(gearItem);
				} else if (n.toLowerCase(Locale.ENGLISH).equals("flashlight")) {
					parseFlashlight(gearItem);
				} else if (n.toLowerCase(Locale.ENGLISH).equals("logbook")) {
					parseLogbook(gearItem);
				} else if (n.toLowerCase(Locale.ENGLISH).equals("blueprint")) {
					parseBlueprint(gearItem);
				}
			}
		}
	}

	private void parseCredits(Node gearItem) throws ConfigParserException {

		final SlotType slotType = null;
		final Integer count;
		try {
			count = Integer.parseInt(gearItem.getTextContent().trim());
		} catch (Exception e) {
			throw new ConfigParserException("Error parsing " + gearItem.getNodeName() + "; one or more fields missing or wrong", e);
		}


		starterGearList.add(new InventoryStarterGearFactory(slotType, (slotType1, hotbarSlot, inevntorySlot, inventory, player) -> player.setCredits(count)));
	}

	private void parseBlueprint(Node gearItem) throws ConfigParserException {
		NodeList c = gearItem.getChildNodes();

		SlotType slotType = null;
		String name = null;
		Boolean filled = null;

		for (int j = 0; j < c.getLength(); j++) {
			Node item = c.item(j);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				try {
					if (item.getNodeName().toLowerCase(Locale.ENGLISH).equals("slot")) {
						slotType = SlotType.valueOf(item.getTextContent().trim().toUpperCase(Locale.ENGLISH));
					} else if (item.getNodeName().toLowerCase(Locale.ENGLISH).equals("name")) {
						name = item.getTextContent();
					} else if (item.getNodeName().toLowerCase(Locale.ENGLISH).equals("filled")) {
						filled = Boolean.parseBoolean(item.getTextContent().trim());
					}
				} catch (Exception e) {
					throw new ConfigParserException("Error parsing " + gearItem.getNodeName() + "; one or more fields missing or wrong", e);
				}
			}
		}
		if (slotType == null || name == null || filled == null) {
			throw new ConfigParserException("Error parsing " + gearItem.getNodeName() + "; one or more fields missing or wrong. needs Slot, Name, and Filled");
		}
		final String bbName = name;
		final boolean filledUp = filled;
		starterGearList.add(new InventoryStarterGearFactory(slotType, (slotType1, hotbarSlot, inevntorySlot, inventory, player) -> {
			try {
				BlueprintEntry bb = BluePrintController.active.getBlueprint( bbName);
				MetaObject mo = MetaObjectManager.instantiate(MetaObjectType.BLUEPRINT, (short) -1, true);

				BlueprintMetaItem m = ((BlueprintMetaItem) mo);
				m.blueprintName = bbName;

				m.goal = new ElementCountMap(bb.getElementCountMapWithChilds());
				if (!filledUp) {
					m.progress = new ElementCountMap();
				} else {
					m.progress = new ElementCountMap(m.goal);
				}

				if (slotType1 == SlotType.HOTBAR) {
					inventory.put(hotbarSlot, mo);
				} else {
					inventory.put(inevntorySlot, mo);
				}
			} catch (EntityNotFountException e) {
				e.printStackTrace();
			}
		}));
	}

	private void parseLogbook(Node gearItem) throws ConfigParserException {
		NodeList c = gearItem.getChildNodes();

		SlotType slotType = null;
		String message = null;

		for (int j = 0; j < c.getLength(); j++) {
			Node item = c.item(j);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				try {
					if (item.getNodeName().toLowerCase(Locale.ENGLISH).equals("slot")) {
						slotType = SlotType.valueOf(item.getTextContent().trim().toUpperCase(Locale.ENGLISH));
					} else if (item.getNodeName().toLowerCase(Locale.ENGLISH).equals("message")) {
						message = item.getTextContent();
					}
				} catch (Exception e) {
					throw new ConfigParserException("Error parsing " + gearItem.getNodeName() + "; one or more fields missing or wrong", e);
				}
			}
		}
		if (slotType == null || message == null) {
			throw new ConfigParserException("Error parsing " + gearItem.getNodeName() + "; one or more fields missing or wrong");
		}
		final String txt = message;
		starterGearList.add(new InventoryStarterGearFactory(slotType, (slotType1, hotbarSlot, inevntorySlot, inventory, player) -> {
			MetaObject logbook = MetaObjectManager.instantiate(MetaObjectType.LOG_BOOK, (short) -1, true);

			((Logbook) logbook).setTxt(txt);

			if (slotType1 == SlotType.HOTBAR) {
				inventory.put(hotbarSlot, logbook);
			} else {
				inventory.put(inevntorySlot, logbook);
			}
		}));
	}

	private void parseHelmet(Node gearItem) throws ConfigParserException {
		NodeList c = gearItem.getChildNodes();

		SlotType slotType = null;

		for (int j = 0; j < c.getLength(); j++) {
			Node item = c.item(j);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				try {
					if (item.getNodeName().toLowerCase(Locale.ENGLISH).equals("slot")) {
						slotType = SlotType.valueOf(item.getTextContent().trim().toUpperCase(Locale.ENGLISH));
					}
				} catch (Exception e) {
					throw new ConfigParserException("Error parsing " + gearItem.getNodeName() + "; one or more fields missing or wrong", e);
				}
			}
		}
		if (slotType == null) {
			throw new ConfigParserException("Error parsing " + gearItem.getNodeName() + "; one or more fields missing or wrong");
		}

		starterGearList.add(new InventoryStarterGearFactory(slotType, (slotType1, hotbarSlot, inevntorySlot, inventory, player) -> {
			MetaObject logbook = MetaObjectManager
					.instantiate(MetaObjectType.HELMET, (short) -1, true);
			if (slotType1 == SlotType.HOTBAR) {
				inventory.put(hotbarSlot, logbook);
			} else {
				inventory.put(inevntorySlot, logbook);
			}
		}));

	}

	private void parseFlashlight(Node gearItem) throws ConfigParserException {
		NodeList c = gearItem.getChildNodes();

		SlotType slotType = null;

		for (int j = 0; j < c.getLength(); j++) {
			Node item = c.item(j);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				try {
					if (item.getNodeName().toLowerCase(Locale.ENGLISH).equals("slot")) {
						slotType = SlotType.valueOf(item.getTextContent().trim().toUpperCase(Locale.ENGLISH));
					}
				} catch (Exception e) {
					throw new ConfigParserException("Error parsing " + gearItem.getNodeName() + "; one or more fields missing or wrong", e);
				}
			}
		}
		if (slotType == null) {
			throw new ConfigParserException("Error parsing " + gearItem.getNodeName() + "; one or more fields missing or wrong");
		}

		starterGearList.add(new InventoryStarterGearFactory(slotType, (slotType1, hotbarSlot, inevntorySlot, inventory, player) -> {
			MetaObject logbook = MetaObjectManager
					.instantiate(MetaObjectType.FLASH_LIGHT, (short) -1, true);
			if (slotType1 == SlotType.HOTBAR) {
				inventory.put(hotbarSlot, logbook);
			} else {
				inventory.put(inevntorySlot, logbook);
			}
		}));

	}

	private void parseInhibiter(Node gearItem) throws ConfigParserException {
		NodeList c = gearItem.getChildNodes();

		SlotType slotType = null;

		for (int j = 0; j < c.getLength(); j++) {
			Node item = c.item(j);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				try {
					if (item.getNodeName().toLowerCase(Locale.ENGLISH).equals("slot")) {
						slotType = SlotType.valueOf(item.getTextContent().trim().toUpperCase(Locale.ENGLISH));
					}
				} catch (Exception e) {
					throw new ConfigParserException("Error parsing " + gearItem.getNodeName() + "; one or more fields missing or wrong", e);
				}
			}
		}
		if (slotType == null) {
			throw new ConfigParserException("Error parsing " + gearItem.getNodeName() + "; one or more fields missing or wrong");
		}

		starterGearList.add(new InventoryStarterGearFactory(slotType, (slotType1, hotbarSlot, inevntorySlot, inventory, player) -> {
			MetaObject logbook = MetaObjectManager
					.instantiate(MetaObjectType.BUILD_PROHIBITER, (short) -1, true);
			if (slotType1 == SlotType.HOTBAR) {
				inventory.put(hotbarSlot, logbook);
			} else {
				inventory.put(inevntorySlot, logbook);
			}
		}));

	}

	private void parseTool(Node gearItem) throws ConfigParserException {
		NodeList c = gearItem.getChildNodes();

		SlotType slotType = null;
		Integer subId = null;

		for (int j = 0; j < c.getLength(); j++) {
			Node item = c.item(j);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				try {
					if (item.getNodeName().toLowerCase(Locale.ENGLISH).equals("slot")) {
						slotType = SlotType.valueOf(item.getTextContent().trim().toUpperCase(Locale.ENGLISH));
					} else if (item.getNodeName().toLowerCase(Locale.ENGLISH).equals("subid")) {
						subId = Integer.parseInt(item.getTextContent().trim());
					}
				} catch (Exception e) {
					throw new ConfigParserException("Error parsing " + gearItem.getNodeName() + "; one or more fields missing or wrong", e);
				}
			}
		}
		if (slotType == null || subId == null) {
			throw new ConfigParserException("Error parsing " + gearItem.getNodeName() + "; one or more fields missing or wrong");
		}
		final int sid = subId;
		starterGearList.add(new InventoryStarterGearFactory(slotType, (slotType1, hotbarSlot, inevntorySlot, inventory, player) -> {
			MetaObject logbook = MetaObjectManager
					.instantiate(MetaObjectType.WEAPON, (short) sid, true);
			if (slotType1 == SlotType.HOTBAR) {
				inventory.put(hotbarSlot, logbook);
			} else {
				inventory.put(inevntorySlot, logbook);
			}
		}));

	}

	private void parseBlock(Node gearItem) throws ConfigParserException {
		NodeList c = gearItem.getChildNodes();

		SlotType slotType = null;
		Integer id = null;
		Integer count = null;

		for (int j = 0; j < c.getLength(); j++) {
			Node item = c.item(j);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				try {
					if (item.getNodeName().toLowerCase(Locale.ENGLISH).equals("slot")) {
						slotType = SlotType.valueOf(item.getTextContent().trim().toUpperCase(Locale.ENGLISH));
					} else if (item.getNodeName().toLowerCase(Locale.ENGLISH).equals("id")) {
						id = Integer.parseInt(item.getTextContent().trim());
					} else if (item.getNodeName().toLowerCase(Locale.ENGLISH).equals("count")) {
						count = Integer.parseInt(item.getTextContent().trim());
					}
				} catch (Exception e) {
					throw new ConfigParserException("Error parsing " + gearItem.getNodeName() + "; one or more fields missing or wrong", e);
				}
			}
		}
		if (slotType == null || id == null || count == null) {
			throw new ConfigParserException("Error parsing " + gearItem.getNodeName() + "; one or more fields missing or wrong");
		}
		final short blockId = id.shortValue();
		final int blockCount = count;
		starterGearList.add(new InventoryStarterGearFactory(slotType, (slotType1, hotbarSlot, inevntorySlot, inventory, player) -> {
			if (slotType1 == SlotType.HOTBAR) {
				inventory.put(hotbarSlot, blockId, blockCount, -1);
			} else {
				inventory.put(inevntorySlot, blockId, blockCount, -1);
			}
		}));

	}

	public void fillInventory(PlayerState player) {
		int slotHotbar = 0;
		int slotInventory = 10;
		for (InventoryStarterGearFactory a : starterGearList) {

			a.executor.execute(a.slotType, slotHotbar, slotInventory, player.getInventory(), player);

			if (a.slotType == SlotType.HOTBAR) {
				slotHotbar++;
			} else if (a.slotType == SlotType.INVENTORY) {
				slotInventory++;
			}
		}
	}

	public int getGroupLimit(short controllerId) {
		return groupLimits.get(controllerId); 
	}
	public boolean hasGroupLimit(short controllerId, int size) {
		return groupLimits.containsKey(controllerId) && size > groupLimits.get(controllerId) ;
	}
	public int getControllerLimit(short controllerId) {
		return controllerLimits.get(controllerId); 
	}
	public boolean hasControllerLimit(short controllerId, int size) {
		return controllerLimits.containsKey(controllerId) && size > controllerLimits.get(controllerId) ;
	}

	public String toStringAllowedSize(SegmentController s) {
		return s.getType() == EntityType.SHIP ? toStringSize(shipSize) : toStringSize(stationSize);
	}

	private String toStringSize(Vector3i s) {
		return "X: "+(s.x > 0 ? s.x : "~")+", Y: "+(s.y > 0 ? s.y : "~")+", Z: "+(s.z > 0 ? s.z : "~");
	}

	private boolean isOk(BoundingBox bb, Vector3i s) {
		return 
				(s.x <= 0 || (bb.max.x - bb.min.x) <= s.x+2) && 
				(s.y <= 0 || (bb.max.y - bb.min.y) <= s.y+2) &&
				(s.z <= 0 || (bb.max.z - bb.min.z) <= s.z+2);
	}

	public boolean isOk(BoundingBox bb, SegmentController s) {
//		System.err.println("BB: "+bb+"; "+toStringAllowedSize(s)+"; "+shipSize+"; "+stationSize);
		return !hasMaxDim || 
				(!(s.getType() == EntityType.SHIP || s.getType() == EntityType.SPACE_STATION) || 
				(s.getType() == EntityType.SHIP && isOk(bb, shipSize)) || 
				(s.getType() == EntityType.SPACE_STATION && isOk(bb, stationSize)));
	}

	public boolean isBBOk(BlueprintInterface en) {
		return !hasMaxDim || 
				!(en.getType() == BlueprintType.SHIP && en.getType() == BlueprintType.SPACE_STATION) || 
				(en.getType() == BlueprintType.SHIP && isOk(en.getBb(), shipSize)) || 
				(en.getType() == BlueprintType.SPACE_STATION && isOk(en.getBb(), stationSize));
	}

	public String toStringAllowedSize(BlueprintInterface en) {
		return en.getType() == BlueprintType.SHIP ? toStringSize(shipSize) : toStringSize(stationSize);
	}

	public boolean isHasMaxDim() {
		return hasMaxDim;
	}

	public void setHasMaxDim(boolean hasMaxDim) {
		this.hasMaxDim = hasMaxDim;
	}
}
