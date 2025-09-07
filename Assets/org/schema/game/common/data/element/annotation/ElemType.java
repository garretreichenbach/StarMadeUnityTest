package org.schema.game.common.data.element.annotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.vecmath.Vector4f;

import org.schema.game.common.controller.damage.effects.InterEffectHandler.InterEffectType;
import org.schema.game.common.data.element.BlockFactory;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementInformation.ResourceInjectionType;
import org.schema.game.common.data.element.ElementParser;
import org.schema.game.common.data.element.ElementParserException;
import org.schema.game.common.data.element.FactoryResource;
import org.schema.game.common.facedit.ElementInformationOption;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public enum ElemType implements Comparable<ElemType>{
	CONSISTENCE("Consistence", (node, info) -> {

		FactoryResource[] parseResource = parseResource(node);


		for (int i = 0; i < parseResource.length; i++) {
			info.getConsistence().add(parseResource[i]);
		}

	}),
	CUBATON_CONSISTENCE("CubatomConsistence", (node, info) -> {
		FactoryResource[] parseResource = parseResource(node);

		for (int i = 0; i < parseResource.length; i++) {
			info.cubatomConsistence.add(parseResource[i]);
		}
	}),

	CONTROLLED_BY("ControlledBy", (node, info) -> {
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {

			Node item = childNodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {

				if (!item.getNodeName().equals("Element")) {
					throw new ElementParserException("[controlledBy] All child nodes of " + node.getNodeName() + " have to be \"Element\" but is " + item.getNodeName() + " (" + node.getParentNode().getNodeName() + ")");
				}

				short typeId = 0;
				String typeProperty = ElementParser.properties.getProperty(item.getTextContent());
				if (typeProperty == null) {
					throw new ElementParserException("[controlledBy] The value of " + item.getTextContent() + " has not been found");
				}
				try {

					typeId = (short) Integer.parseInt(typeProperty);
				} catch (NumberFormatException e) {
					throw new ElementParserException("[controlledBy] The property " + typeProperty + " has to be an Integer value");
				}
				info.getControlledBy().add(typeId);

			}

		}
	}),

	CONTROLLING("Controlling", (node, info) -> {
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {

			Node item = childNodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {

				if (!item.getNodeName().equals("Element")) {
					throw new ElementParserException("All child nodes of " + node.getNodeName() + " have to be \"Element\" but is " + item.getNodeName() + " (" + node.getParentNode().getNodeName() + ")");
				}

				short typeId = 0;
				String typeProperty = ElementParser.properties.getProperty(item.getTextContent());
				if (typeProperty == null) {
					throw new ElementParserException("[controlling] The value of " + item.getTextContent() + " has not been found");
				}
				try {
					typeId = (short) Integer.parseInt(typeProperty);
				} catch (NumberFormatException e) {
					throw new ElementParserException("[controlling] The property " + typeProperty + " has to be an Integer value");
				}
				//				System.err.println("ADDING CONTROLLING "+s);
				info.getControlling().add(typeId);

			}

		}
	}),

	RECIPE_BUY_RESOURCE("RecipeBuyResource", (node, info) -> {
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {

			Node item = childNodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {

				if (!item.getNodeName().equals("Element")) {
					throw new ElementParserException("All child nodes of " + node.getNodeName() + " have to be \"Element\" but is " + item.getNodeName() + " (" + node.getParentNode().getNodeName() + ")");
				}

				short typeId = 0;
				String typeProperty = ElementParser.properties.getProperty(item.getTextContent());
				if (typeProperty == null) {
					throw new ElementParserException("The value of " + item.getTextContent() + " has not been found");
				}
				try {
					typeId = (short) Integer.parseInt(typeProperty);
				} catch (NumberFormatException e) {
					throw new ElementParserException("The property " + typeProperty + " has to be an Integer value");
				}
				info.getRecipeBuyResources().add(typeId);

			}

		}
	}),

	ARMOR_VALUE("ArmorValue", (node, info) -> info.setArmorValue(parseFloat(node))),

	NAME("Name", (node, info) -> {

	}),

	BUILD_ICON("BuildIcon", (node, info) -> {

	}),

	FULL_NAME("FullName", (node, info) -> {
		String desc = node.getTextContent();
		info.setFullName(desc);
	}),

	PRICE("Price", (node, info) -> {
		info.setPrice(parseInt(node));
		if (info.getPrice(false) < 0) {
			throw new ElementParserException("Price for " + node.getParentNode().getNodeName() + " has to be greater or equal zero");
		}
	}),

	DESCRIPTION("Description", (node, info) -> {
		String desc = node.getTextContent();

		desc = desc.replaceAll("\\r\\n|\\r|\\n", "");
		desc = desc.replaceAll("\\\\n", "\n");
		desc = desc.replaceAll("\\\\r", "\r");
		desc = desc.replaceAll("\\\\t", "\t");
		desc = desc.replaceAll("\\r", "\r");
		desc = desc.replaceAll("\r","");
		//getting rid of -Structural Stats-*
		desc = desc.split("-Struct")[0];

		info.setDescription(desc);
	}),

	BLOCK_RESOURCE_TYPE("BlockResourceType", (node, info) -> info.blockResourceType = ((parseInt(node)))),

	PRODUCED_IN_FACTORY("ProducedInFactory", (node, info) -> info.setProducedInFactory(((parseInt(node))))),

	BASIC_RESOURCE_FACTORY("BasicResourceFactory", (node, info) -> {
		short typeId = 0;

		if (node.getTextContent().trim().length() == 0) {

		} else {
			String typeProperty = ElementParser.properties.getProperty(node.getTextContent());
			if (typeProperty == null) {
				info.setBasicResourceFactory(parseShort(node));
				return;
			}
			try {
				typeId = (short) Integer.parseInt(typeProperty);
			} catch (NumberFormatException e) {
				throw new ElementParserException("The property " + typeProperty + " has to be an Integer value");
			}
			info.setBasicResourceFactory(typeId);
			//			System.err.println("PROJECTION PARSED: "+info.getProjectionTo());
		}
	}),

	FACTORY_BAKE_TIME("FactoryBakeTime", (node, info) -> info.setFactoryBakeTime(parseFloat(node))),

	INVENTORY_GROUP("InventoryGroup", (node, info) -> info.setInventoryGroup(node.getTextContent().toLowerCase(Locale.ENGLISH))),

	FACTORY("Factory", (node, info) -> {
		List<FactoryResource[]> inputs = new ObjectArrayList<FactoryResource[]>();
		List<FactoryResource[]> outputs = new ObjectArrayList<FactoryResource[]>();
		BlockFactory f = new BlockFactory();
		info.setFactory(f);
		if (node.getTextContent().toLowerCase(Locale.ENGLISH).equals("input")) {
			//nothing to do: input factory
			return;
		}
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node product = childNodes.item(i);
			if (product.getNodeType() == Node.ELEMENT_NODE) {
				if (!product.getNodeName().toLowerCase(Locale.ENGLISH).equals("product")) {
					throw new ElementParserException("All child nodes of " + product.getNodeName() + " have to be \"product\" but is " + product.getNodeName() + " (" + node.getParentNode().getNodeName() + ")");
				}
				NodeList productChilds = product.getChildNodes();
				FactoryResource[] input = null;
				FactoryResource[] output = null;
				for (int g = 0; g < productChilds.getLength(); g++) {
					Node item = productChilds.item(g);
					if (item.getNodeType() == Node.ELEMENT_NODE) {
						if (!item.getNodeName().toLowerCase(Locale.ENGLISH).equals("output")
								&& !item.getNodeName().toLowerCase(Locale.ENGLISH).equals("input")) {
							throw new ElementParserException("All child nodes of " +
									node.getNodeName() + " have to be \"output\" or \"input\" but is " + item.getNodeName() + " (" + node.getParentNode().getNodeName() + ")");
						}
						if (item.getNodeName().toLowerCase(Locale.ENGLISH).equals("input")) {
							input = parseResource(item);
						}
						if (item.getNodeName().toLowerCase(Locale.ENGLISH).equals("output")) {
							output = parseResource(item);
						}

					}
				}
				if (input == null) {
					throw new ElementParserException("No input defined for " + node.getNodeName() + " in (" + node.getParentNode().getNodeName() + ")");
				}
				if (output == null) {
					throw new ElementParserException("No output defined for " + node.getNodeName() + " in (" + node.getParentNode().getNodeName() + ")");
				}

				inputs.add(input);
				outputs.add(output);
			}
		}
		if (inputs.size() != outputs.size()) {
			throw new ElementParserException("Factory Parsing failed for " + node.getNodeName() + " in (" + node.getParentNode().getNodeName() + ")");
		}

		f.input = new FactoryResource[inputs.size()][];
		f.output = new FactoryResource[outputs.size()][];

		for (int i = 0; i < f.input.length; i++) {
			f.input[i] = inputs.get(i);
			f.output[i] = outputs.get(i);
		}

		if (inputs.size() == 0 && outputs.size() == 0) {
			info.setFactory(null);
		}
	}),

	ANIMATED("Animated", (node, info) -> info.setAnimated(parseBoolean(node))),

	STRUCTURE_HP("StructureHPContribution", (node, info) -> {
		info.structureHP = (parseInt(node));

		if (info.structureHP < 0) {
			throw new ElementParserException("StructureHP for " + node.getParentNode().getNodeName() + " has to be positive");
		}
	}),

	TRANSPARENCY("Transparency", (node, info) -> info.setBlended(parseBoolean(node))),

	IN_SHOP("InShop", (node, info) -> {
		info.setShoppable(parseBoolean(node));
		info.setInRecipe(info.isShoppable());
	}),

	ORIENTATION("Orientation", (node, info) -> info.setOrientatable(parseBoolean(node))),

	BLOCK_COMPUTER_REFERENCE("BlockComputerReference", (node, info) -> info.computerType = ((parseInt(node)))),

	SLAB("Slab", (node, info) -> info.slab = (parseInt(node))),
	SLAB_IDS("SlabIds", (node, info) -> {
		if(node.getTextContent() != null && node.getTextContent().trim().length() > 0){
			try {
				String[] split = node.getTextContent().split(",");

				short[] tex = new short[]{0, 0, 0};

				for (int i = 0; i < split.length; i++) {
					tex[i] = Short.parseShort(split[i].trim());
				}
				assert (tex[0] >= 0);

				info.slabIds = tex;
			} catch (NumberFormatException e) {
				e.printStackTrace();
				throw new ElementParserException(ElementParser.currentName + ": The value of " + node.getNodeName() + " has to be an Integer value for " + node.getParentNode().getNodeName());
			}
		}else{
		}
	}),

	STYLE_IDS("StyleIds", (node, info) -> {
		if(node.getTextContent() != null && node.getTextContent().trim().length() > 0){
			try {
				String[] split = node.getTextContent().split(",");

				short[] tex = new short[split.length];

				for (int i = 0; i < split.length; i++) {
					tex[i] = Short.parseShort(split[i].trim());
				}
				assert (tex[0] >= 0);

				info.styleIds = tex;
			} catch (NumberFormatException e) {
				e.printStackTrace();
				throw new ElementParserException(ElementParser.currentName + ": The value of " + node.getNodeName() + " has to be an Integer value for " + node.getParentNode().getNodeName());
			}
		}else{
		}
	}),

	WILDCARD_IDS("WildcardIds", (node, info) -> {
		short[] tex = parseShortArray(ElementParser.currentName, node, info);
		if(tex != null){
			assert (tex[0] >= 0);
			info.wildcardIds = tex;
		}
	}),
	SOURCE_REFERENCE("SourceReference", (node, info) -> info.setSourceReference(parseInt(node))),

	GENERAL_CHAMBER("GeneralChamber", (node, info) -> info.chamberGeneral = ((parseBoolean(node)))),

	EDIT_REACTOR("Edit Reactor", (node, info) -> {

	}),

	CHAMBER_CAPACITY("ChamberCapacity", (node, info) -> info.chamberCapacity = parseFloat(node)),

	CHAMBER_ROOT("ChamberRoot", (node, info) -> info.chamberRoot = ((parseInt(node)))),

	CHAMBER_PARENT("ChamberParent", (node, info) -> info.chamberParent = ((parseInt(node)))),

	CHAMBER_UPGRADES_TO("ChamberUpgradesTo", (node, info) -> info.chamberUpgradesTo = ((parseInt(node)))),

	CHAMBER_PREREQUISITES("ChamberPrerequisites", (node, info) -> {
		short[] tex = parseShortArray(ElementParser.currentName, node, info);
		if(tex != null){
			assert (tex[0] >= 0);
			for(short s : tex){
				info.chamberPrerequisites.add(s);
			}
		}
	}),

	CHAMBER_MUTUALLY_EXCLUSIVE("ChamberMutuallyExclusive", (node, info) -> {
		short[] tex = parseShortArray(ElementParser.currentName, node, info);
		if(tex != null){
			assert (tex[0] >= 0);
			for(short s : tex){
				info.chamberMutuallyExclusive.add(s);
			}
		}
	}),

	CHAMBER_CHILDREN("ChamberChildren", (node, info) -> {
		short[] tex = parseShortArray(ElementParser.currentName, node, info);
		if(tex != null){
			assert (tex[0] >= 0);
			for(short s : tex){
				info.chamberChildren.add(s);
			}
		}
	}),

	CHAMBER_CONFIG_GROUPS("ChamberConfigGroups", (node, info) -> {
		info.chamberConfigGroupsLowerCase.clear();
		List<String> parseList = parseList(node, "Element");
		for(String s : parseList){
			info.chamberConfigGroupsLowerCase.add(s.toLowerCase(Locale.ENGLISH));
		}
	}),

	CHAMBER_APPLIES_TO("ChamberAppliesTo", (node, info) -> info.chamberAppliesTo = parseInt(node)),

	REACTOR_HP("ReactorHp", (node, info) -> info.reactorHp = parseInt(node)),

	REACTOR_GENERAL_ICON_INDEX("ReactorGeneralIconIndex", (node, info) -> info.reactorGeneralIconIndex = parseInt(node)),

	ENTERABLE("Enterable", (node, info) -> info.setEnterable(parseBoolean(node))),

	MASS("Mass", (node, info) -> info.mass = (parseFloat(node))),

	HITPOINTS("Hitpoints", (node, info) -> {
		int hp =  parseInt(node);

		if (hp < 1) {
			try {
				throw new ElementParserException("Hitpoints for " + info.getName() + ": " + node.getParentNode().getNodeName() + " has to be more than 0");
			} catch (ElementParserException e) {
				e.printStackTrace();
				info.setMaxHitPointsE(100);
			}
		} else {
			info.setMaxHitPointsE(hp);
		}
	}),

	PLACABLE("Placable", (node, info) -> info.setPlacable(parseBoolean(node))),

	IN_RECIPE("InRecipe", (node, info) -> info.setInRecipe((parseBoolean(node)))),

	CAN_ACTIVATE("CanActivate", (node, info) -> info.setCanActivate(parseBoolean(node))),

	INDIVIDUAL_SIDES("IndividualSides", (node, info) -> {
		info.setIndividualSides(parseInt(node));
		if (info.getIndividualSides() == 1 || info.getIndividualSides() == 3 || info.getIndividualSides() == 6) {
		} else {
			throw new ElementParserException("Individual Sides for " + node.getParentNode().getNodeName() + " has to be either 1 (default), 3, or 6, but was: " + info.getIndividualSides());
		}
	}),

	SIDE_TEXTURE_POINT_TO_ORIENTATION("SideTexturesPointToOrientation", (node, info) -> info.sideTexturesPointToOrientation = (parseBoolean(node))),

	HAS_ACTIVE_TEXTURE("HasActivationTexture", (node, info) -> info.setHasActivationTexure(parseBoolean(node))),

	MAIN_COMBINATION_CONTROLLER("MainCombinationController", (node, info) -> info.setMainCombinationController(parseBoolean(node))),

	SUPPORT_COMBINATION_CONTROLLER("SupportCombinationController", (node, info) -> info.setSupportCombinationController(parseBoolean(node))),

	EFFECT_COMBINATION_CONTROLLER("EffectCombinationController", (node, info) -> info.setEffectCombinationController(parseBoolean(node))),
	
	BEACON("Beacon", (node, info) -> info.beacon = (parseBoolean(node))),

	PHYSICAL("Physical", (node, info) -> info.setPhysical((parseBoolean(node)))),

	BLOCK_STYLE("BlockStyle", (node, info) -> info.setBlockStyle((parseInt(node)))),

	LIGHT_SOURCE("LightSource", (node, info) -> info.setLightSource(parseBoolean(node))),

	DOOR("Door", (node, info) -> info.setDoor(parseBoolean(node))),

	SENSOR_INPUT("SensorInput", (node, info) -> info.sensorInput = parseBoolean(node)),

	DEPRECATED("Deprecated", (node, info) -> info.setDeprecated((parseBoolean(node)))),

	RESOURCE_INJECTION("ResourceInjection", (node, info) -> info.resourceInjection = (ResourceInjectionType.values()[parseInt(node)])),

	LIGHT_SOURCE_COLOR("LightSourceColor", (node, info) -> info.getLightSourceColor().set(parseVector4f(node))),

	EXTENDED_TEXTURE_4x4("ExtendedTexture4x4", (node, info) -> info.extendedTexture = (parseBoolean(node))),

	ONLY_DRAW_IN_BUILD_MODE("OnlyDrawnInBuildMode", (node, info) -> info.setDrawOnlyInBuildMode(parseBoolean(node))),
	LOD_COLLISION_PHYSICAL("LodCollisionPhysical", (node, info) -> info.lodCollisionPhysical = parseBoolean(node)),
	LOD_USE_DETAIL_COLLISION("UseDetailedCollisionForAstronautMode", (node, info) -> info.lodUseDetailCollision = (parseBoolean(node))),
	CUBE_CUBE_COLLISION("CubeCubeCollision", (node, info) -> info.cubeCubeCollision = (parseBoolean(node))),
	LOD_DETAIL_COLLISION("DetailedCollisionForAstronautMode", (node, info) -> info.lodDetailCollision.parse(node)),
	LOD_COLLISION("CollisionDefault", (node, info) -> info.lodCollision.parse(node)),

	LOD_SHAPE("LodShape", (node, info) -> info.lodShapeString = node.getTextContent().trim()),
	LOD_SHAPE_ACTIVE("LodShapeSwitchStyleActive", (node, info) -> info.lodShapeStringActive = node.getTextContent().trim()),

	LOD_SHAPE_FROM_FAR("LodShapeFromFar", (node, info) -> info.lodShapeStyle = parseInt(node)),
	LOD_ACTIVATION_ANIMATION_STYLE("LodActivationAnimationStyle", new NodeSettingWithDependency() {
		public void parse(Node node, ElementInformation info) throws ElementParserException {
			info.lodActivationAnimationStyle = parseInt(node);
		}

		@Override
		public void onSwitch(ElementInformationOption opt, ElementInformation info, Element elem) {
			opt.editPanel.onSwitchActivationAnimationStyle(info.lodActivationAnimationStyle);
		}
	}),

	LOW_HP_SETTING("LowHpSetting", (node, info) -> info.lowHpSetting = (parseBoolean(node))),

	OLD_HITPOINTS("OldHitpoints", (node, info) -> info.setHpOldByte(parseShort(node))),
	VOLUME("Volume", (node, info) -> info.volume = (parseFloat(node))),
	EXPLOSION_ABSOBTION("ExplosionAbsorbtion", (node, info) -> info.setExplosionAbsorbtion(parseFloat(node))),
	CHAMBER_PERMISSION("ChamberPermission", (node, info) -> info.chamberPermission = parseInt(node)),
	ID("ID", (node, info) -> {
	}),
	TEXTURE("Texture", (node, info) -> {
	}),
	EFFECT_ARMOR("EffectArmor", (node, info) -> {
		NodeList childs = node.getChildNodes();
		for(int i = 0; i < childs.getLength(); i++) {
			Node d = childs.item(i);
			if(d.getNodeType() == Node.ELEMENT_NODE) {
				String nm = d.getNodeName().toLowerCase(Locale.ENGLISH);
				for(InterEffectType e : InterEffectType.values()) {
					if(e.id.toLowerCase(Locale.ENGLISH).equals(nm)) {
						try {
							info.effectArmor.setStrength(e, Float.parseFloat(d.getTextContent()));
						}catch(NumberFormatException ex) {
							ex.printStackTrace();
							throw new ElementParserException("value has to be floating point. "+d.getNodeName()+"; "+node.getNodeName()+"; "+node.getParentNode().getNodeName()+"; "+ElementParser.currentName);
						}
					}
				}
			}
		}
	}),
	SYSTEM_BLOCK("SystemBlock", (node, info) -> info.systemBlock = parseBoolean(node)),
	DRAW_LOGIC_CONNECTION("DrawLogicConnection", (node, info) -> info.drawLogicConnection = parseBoolean(node)),
	LOGIC_BLOCK("LogicBlock", (node, info) -> info.signal = parseBoolean(node)),
	LOGIC_SIGNALED_BY_RAIL("LogicSignaledByRail", (node, info) -> info.signaledByRail = parseBoolean(node)),
	LOGIC_BUTTON("LogicBlockButton", (node, info) -> info.button = parseBoolean(node)),
	;
	private static short[] parseShortArray(String currentName, Node node, ElementInformation info){
		if(node.getTextContent() != null && node.getTextContent().trim().length() > 0){
			try {
				String s = node.getTextContent().replaceAll("\\{", "").replaceAll("\\}", "");
				if(s.length() > 0){
					String[] split = s.split(",");
				
					short[] tex = new short[split.length];
					
					for (int i = 0; i < split.length; i++) {
						tex[i] = Short.parseShort(split[i].trim());
					}
					return tex;
				}else{
					return null;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
				throw new ElementParserException(currentName + ": The value of " + node.getNodeName() + " has to be an Integer value for " + node.getParentNode().getNodeName());
			}
		}
		return null;
	}
	private static List<String> parseList(Node node, String elemName) throws ElementParserException {
		List<String> l = new ObjectArrayList<String>();
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			
			Node item = childNodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				
				if (!item.getNodeName().equals(elemName)) {
					throw new ElementParserException("All child nodes of " + node.getNodeName() + " have to be \""+elemName+"\" but is " + item.getNodeName() + " (" + node.getParentNode().getNodeName() + ")");
				}
				
				l.add(item.getTextContent());
				
			}
			
		}
		return l;
	}
	
	private static boolean parseBoolean(Node node) throws ElementParserException {
		try {
			return Boolean.parseBoolean(node.getTextContent().trim());
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new ElementParserException("The value of " + node.getNodeName() + " has to be an Boolean value for " + node.getParentNode().getNodeName()+" but was "+node.getTextContent());
		}
	}
	private static short parseShort(Node node) throws ElementParserException {
		try {
			return Short.parseShort(node.getTextContent().trim());
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new ElementParserException("The value of " + node.getNodeName() + " has to be a Short value for " + node.getParentNode().getNodeName());
		}
	}
	public static int parseInt(Node node) throws ElementParserException {
		try {
			return Integer.parseInt(node.getTextContent().trim());
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new ElementParserException("The value of " + node.getNodeName() + " has to be an Integer value for " + node.getParentNode().getNodeName()+" but was: "+node.getTextContent());
		}
	}
	private static Vector4f parseVector4f(Node node) throws ElementParserException {
		Vector4f v = new Vector4f();

		String[] split = node.getTextContent().trim().split(",");

		if (split.length != 4) {
			throw new ElementParserException("The value of " + node.getNodeName() + " has to be 4 Float values seperated by commas");
		}
		try {
			v.set(Float.parseFloat(split[0].trim()), Float.parseFloat(split[1].trim()), Float.parseFloat(split[2].trim()), Float.parseFloat(split[3].trim()));
		} catch (NumberFormatException e) {
			throw new ElementParserException("The value of " + node.getNodeName() + " has to be a Float value");
		}
		return v;
	}
	public static FactoryResource[] parseResource(Node n) {

		ArrayList<FactoryResource> r = new ArrayList<FactoryResource>();
		NodeList cNodes = n.getChildNodes();
		for (int j = 0; j < cNodes.getLength(); j++) {
			Node cItem = cNodes.item(j);
			if (cItem.getNodeType() == Node.ELEMENT_NODE) {
				if (!cItem.getNodeName().toLowerCase(Locale.ENGLISH).equals("item")) {
					throw new ElementParserException("All child nodes of " + n.getNodeName() + " have to be \"item\" but is " + n.getParentNode().getNodeName() + " (" + n.getParentNode().getParentNode().getNodeName() + ")");
				}

				NamedNodeMap attributes = cItem.getAttributes();
				if (attributes != null && attributes.getLength() != 1) {
					throw new ElementParserException("Element has wrong attribute count (" + attributes.getLength() + ", but should be 4)");
				}

				Node typeNode = parseType(cItem, attributes, "count");
				int count = 0;
				try {
					count = Integer.parseInt(typeNode.getNodeValue());
				} catch (NumberFormatException e) {
					throw new ElementParserException("Cant parse count in " + cItem.getNodeName() + ", in " + n.getParentNode().getNodeName() + " (" + n.getParentNode().getParentNode().getNodeName() + ")");
				}

				short typeId = 0;
				String typeProperty = ElementParser.properties.getProperty(cItem.getTextContent());
				if (typeProperty == null) {
					throw new ElementParserException(n.getParentNode().getParentNode().getParentNode().getNodeName() + " -> " + n.getParentNode().getNodeName() + " -> " + n.getNodeName() + " The value of \"" + cItem.getTextContent() + "\" has not been found");
				}
				try {
					typeId = (short) Integer.parseInt(typeProperty);
				} catch (NumberFormatException e) {
					throw new ElementParserException("The property " + typeProperty + " has to be an Integer value");
				}
				r.add(new FactoryResource(count, typeId));

			}
		}
		FactoryResource[] a = new FactoryResource[r.size()];
		r.toArray(a);
		return a;
	}
	private static float parseFloat(Node node) throws ElementParserException {
		try {
			return Float.parseFloat(node.getTextContent().trim());
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new ElementParserException("The value of " + node.getNodeName() + " has to be a Float value for " + node.getParentNode().getNodeName());
		}
	}
	public static Node parseType(Node node, NamedNodeMap attributes, String name) throws ElementParserException {
		Node typeNode = attributes.getNamedItem(name);
		if (typeNode == null) {
			throw new ElementParserException("Obligatory attribute \"" + name + "\" not found in " + node.getNodeName());
		}
		return typeNode;
	}
	public final NodeSetting fac;
	public final String tag;

	private ElemType(String tag, NodeSetting s) {
		this.tag = tag;
		this.fac = s;
	}

}
