package org.schema.schine.input;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.schema.common.XMLTools;
import org.schema.common.util.security.OperatingSystem;
import org.schema.schine.common.language.Lng;
import org.schema.schine.common.language.Translatable;
import org.schema.schine.common.util.FileUtil;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.MouseButton;
import org.schema.schine.network.StateInterface;
import org.schema.schine.network.client.KBMapInterface;
import org.schema.schine.resource.FileExt;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.*;

public enum KeyboardMappings implements KBMapInterface {

	BUILD_BLOCK_BUILD_MODE(en -> Lng.str("(Build Mode) Build Block"), true, KeyboardContext.BUILD, new InputAction(MouseButton.MOUSE_LEFT)),
	REMOVE_BLOCK_BUILD_MODE(en -> Lng.str("(Build Mode) Remove Block"), true, KeyboardContext.BUILD, new InputAction(MouseButton.MOUSE_RIGHT)),
	SWITCH_HOTBAR_WITH_LOOK_AT_BLOCK(en -> Lng.str("(Build Mode) Select looked at block in hotbar"), false, KeyboardContext.BUILD, new InputAction(MouseButton.MOUSE_MIDDLE, new InputAction(InputType.KEYBOARD_MOD, Keyboard.KeyboardControlKey.CONTROL))),
	SWITCH_TO_ENTITY(en -> Lng.str("(Build Mode) Switch to docked " + "entity"), false, KeyboardContext.BUILD, new InputAction(MouseButton.MOUSE_MIDDLE)),
	PLAYER_LOOK_AROUND(en -> Lng.str("(Character) Free Camera"), false, KeyboardContext.PLAYER, new InputAction(MouseButton.MOUSE_MIDDLE), new InputAction(InputType.KEYBOARD, GLFW_KEY_RIGHT_SHIFT)),
	USE_SLOT_ITEM_CHARACTER(en -> Lng.str("(Character) Use/Build from slot"), false, KeyboardContext.PLAYER, new InputAction(MouseButton.MOUSE_LEFT)),
	REMOVE_BLOCK_CHARACTER(en -> Lng.str("(Character) Remove Block"), false, KeyboardContext.PLAYER, new InputAction(MouseButton.MOUSE_RIGHT)),
	SHIP_PRIMARY_FIRE(en -> Lng.str("(Ship) Primary Fire"), true, KeyboardContext.SHIP, new InputAction(MouseButton.MOUSE_LEFT)), //	SHIP_SECONDARY_FIRE(new Translatable() {
	SHIP_ZOOM(en -> Lng.str("(Ship) Zoom (ADS)"), true, KeyboardContext.SHIP, new InputAction(MouseButton.MOUSE_RIGHT)),
	PLAYER_MESSAGE_LOG_KEY(en -> Lng.str("Radial Menu"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_F4)),
	MAP_SELECT_ITEM(en -> Lng.str("(Map) Click"), false, KeyboardContext.MAP, new InputAction(MouseButton.MOUSE_LEFT)),
	RADIAL_MENU(en -> Lng.str("Radial Menu"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_TAB)),
	STRAFE_LEFT(en -> Lng.str("(Character) Strafe Left "), true, KeyboardContext.PLAYER, new InputAction(InputType.KEYBOARD, GLFW_KEY_A)),
	STRAFE_RIGHT(en -> Lng.str("(Character) Strafe right"), true, KeyboardContext.PLAYER, new InputAction(InputType.KEYBOARD, GLFW_KEY_D)),
	FORWARD(en -> Lng.str("(Character) Forwards"), true, KeyboardContext.PLAYER, new InputAction(InputType.KEYBOARD, GLFW_KEY_W)),
	BACKWARDS(en -> Lng.str("(Character) Backwards"), true, KeyboardContext.PLAYER, new InputAction(InputType.KEYBOARD, GLFW_KEY_S)),
	UP(en -> Lng.str("(Character) Strafe Up"), true, KeyboardContext.PLAYER, new InputAction(InputType.KEYBOARD, GLFW_KEY_E)),
	DOWN(en -> Lng.str("(Character) " + "Strafe Down"), true, KeyboardContext.PLAYER, new InputAction(InputType.KEYBOARD, GLFW_KEY_Q)),

	STRAFE_LEFT_SHIP(en -> Lng.str("(Ship) Strafe Left"), true, KeyboardContext.SHIP, new InputAction(InputType.KEYBOARD, GLFW_KEY_A)),
	STRAFE_RIGHT_SHIP(en -> Lng.str("(Ship) Strafe right"), true, KeyboardContext.SHIP, new InputAction(InputType.KEYBOARD, GLFW_KEY_D)),
	FORWARD_SHIP(en -> Lng.str("(Ship) Forwards"), true, KeyboardContext.SHIP, new InputAction(InputType.KEYBOARD, GLFW_KEY_W)),
	BACKWARDS_SHIP(en -> Lng.str("(Ship) Backwards"), true, KeyboardContext.SHIP, new InputAction(InputType.KEYBOARD, GLFW_KEY_S)),
	UP_SHIP(en -> Lng.str("(Ship) Strafe Up"), true, KeyboardContext.SHIP, new InputAction(InputType.KEYBOARD, GLFW_KEY_SPACE)),
	DOWN_SHIP(en -> Lng.str("(Ship) Strafe Down"), true, KeyboardContext.SHIP, new InputAction(InputType.KEYBOARD, GLFW_KEY_LEFT_CONTROL)),
	ROTATE_LEFT_SHIP(en -> Lng.str("(Ship) Rotate Left"), true, KeyboardContext.SHIP, new InputAction(InputType.KEYBOARD, GLFW_KEY_Q)),
	ROTATE_RIGHT_SHIP(en -> Lng.str("(Ship) Rotate Right"), true, KeyboardContext.SHIP, new InputAction(InputType.KEYBOARD, GLFW_KEY_E)),
	SWITCH_FIRE_MODE(en -> Lng.str("Switch Weapon Firing Mode"), true, KeyboardContext.SHIP, new InputAction(InputType.KEYBOARD, GLFW_KEY_LEFT_ALT)),
	PLAYER_LIST(en -> Lng.str("Server Info / Player " + "List"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_F1)),
	DROP_ITEM(en -> Lng.str("Drop Item"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_BACKSPACE)),
	RECORD_GIF(en -> Lng.str("Record GIF animation"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_PAUSE)),
	NETWORK_STATS_PANEL(en -> Lng.str("Open network stat" + " panel"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_F12)),
	LAG_STATS_PANEL(en -> Lng.str("Open network stat panel"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_F7)),
	OBJECT_VIEW_CAM(en -> Lng.str("Auto-rotation camera"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_BACKSLASH)),
	SCROLL_MOUSE_ZOOM_OUT(en -> Lng.str("Zoom Out"), false, KeyboardContext.GENERAL, true, new InputAction(InputType.MOUSE_WHEEL, Mouse.MouseWheelDir.MOUSE_WHEEL_DOWN, new InputAction(InputType.KEYBOARD_MOD, Keyboard.KeyboardControlKey.SHIFT))),
	SCROLL_MOUSE_ZOOM_IN(en -> Lng.str("Zoom In"), false, KeyboardContext.GENERAL, true, new InputAction(InputType.MOUSE_WHEEL, Mouse.MouseWheelDir.MOUSE_WHEEL_UP, new InputAction(InputType.KEYBOARD_MOD, Keyboard.KeyboardControlKey.SHIFT))),
	SCROLL_BOTTOM_BAR_NEXT(en -> Lng.str("Wheel/Number hotbar switch next"), false, KeyboardContext.GENERAL, true, new InputAction(InputType.MOUSE_WHEEL, Mouse.MouseWheelDir.MOUSE_WHEEL_UP, new InputAction(InputType.KEYBOARD_MOD, Keyboard.KeyboardControlKey.ALT))),
	SCROLL_BOTTOM_BAR_PREVIOUS(en -> Lng.str("Wheel/Number hotbar switch previous"), false, KeyboardContext.GENERAL, true, new InputAction(InputType.MOUSE_WHEEL, Mouse.MouseWheelDir.MOUSE_WHEEL_DOWN, new InputAction(InputType.KEYBOARD_MOD, Keyboard.KeyboardControlKey.ALT))),
	NEXT_SLOT(en -> Lng.str("Next " + "Slot"), false, KeyboardContext.SHIP, true, new InputAction(InputType.MOUSE_WHEEL, Mouse.MouseWheelDir.MOUSE_WHEEL_UP)),
	PREVIOUS_SLOT(en -> Lng.str("Previous Slot"), false, KeyboardContext.SHIP, true, new InputAction(InputType.MOUSE_WHEEL, Mouse.MouseWheelDir.MOUSE_WHEEL_DOWN)),
	BRAKE(en -> Lng.str("Brake"), true, KeyboardContext.SHIP, new InputAction(InputType.KEYBOARD, GLFW_KEY_LEFT_SHIFT)),
	ROLL(en -> Lng.str("Roll Ship"), false, KeyboardContext.SHIP, true, new InputAction(InputType.KEYBOARD, GLFW_KEY_LEFT_ALT)),
	CHANGE_SHIP_MODE(en -> Lng.str("Change Ship Mode"), true, KeyboardContext.SHIP, new InputAction(InputType.KEYBOARD, GLFW_KEY_Z)),

	JUMP(en -> Lng.str("Jump"), true, KeyboardContext.PLAYER, new InputAction(InputType.KEYBOARD, GLFW_KEY_SPACE)),
	GRAPPLING_HOOK(en -> Lng.str("Grapple (Align to ship)"), false, KeyboardContext.PLAYER, new InputAction(InputType.KEYBOARD, GLFW_KEY_SPACE)),
	PLAYER_EMOTE_NEXT(en -> Lng.str("(Player) Next " + "Emote"), false, KeyboardContext.PLAYER, new InputAction(InputType.KEYBOARD, GLFW_KEY_RIGHT)),
	PLAYER_EMOTE_PREVIOUS(en -> Lng.str("(Player) Previous Emote"), false, KeyboardContext.PLAYER, new InputAction(InputType.KEYBOARD, GLFW_KEY_LEFT)),
	WALK(en -> Lng.str("Walk"), true, KeyboardContext.PLAYER, new InputAction(InputType.KEYBOARD, GLFW_KEY_LEFT_SHIFT)),
	JUMP_TO_MODULE(en -> Lng.str("Jump to module"), false, KeyboardContext.BUILD, new InputAction(InputType.KEYBOARD, GLFW_KEY_X)),
	BUILD_MODE_FLASHLIGHT(en -> Lng.str("Build Mode Flashlight"), false, KeyboardContext.BUILD, new InputAction(InputType.KEYBOARD, GLFW_KEY_P)),
	REBOOT_SYSTEMS(en -> Lng.str("Reboot Systems"), false, KeyboardContext.SHIP, new InputAction(InputType.KEYBOARD, GLFW_KEY_Y)),

	FREE_CAM(en -> Lng.str("Free Camera"), true, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_RIGHT_SHIFT)),
	ADJUST_COCKPIT(en -> Lng.str("Adjust Ship Camera"), false, KeyboardContext.SHIP, new InputAction(InputType.KEYBOARD, GLFW_KEY_P)),
	ADJUST_COCKPIT_RESET(en -> Lng.str("Reset Adjust Ship " + "Camera"), false, KeyboardContext.SHIP, new InputAction(InputType.KEYBOARD, GLFW_KEY_O)),
	ENTER_SHIP(en -> Lng.str("Exit Ship"), false, KeyboardContext.SHIP, new InputAction(InputType.KEYBOARD, GLFW_KEY_R)),
	ACTIVATE(en -> Lng.str("Activate " + "Module"), false, KeyboardContext.PLAYER, new InputAction(InputType.KEYBOARD, GLFW_KEY_R)),
	TUTORIAL(en -> Lng.str("Tutorial Screen"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_F8)),

	CREW_CONTROL(en -> Lng.str("Crew Control"), false, KeyboardContext.PLAYER, true, OperatingSystem.getOS() == OperatingSystem.MAC ? new InputAction(InputType.KEYBOARD, GLFW_KEY_MINUS) : new InputAction(InputType.KEYBOARD, GLFW_KEY_LEFT_CONTROL)),
	STUCK_PROTECT(en -> Lng.str("Activate" + " Stuck Protect"), false, KeyboardContext.PLAYER, new InputAction(InputType.KEYBOARD, GLFW_KEY_UP)),
	SIT_ASTRONAUT(en -> Lng.str("Sit"), false, KeyboardContext.PLAYER, new InputAction(InputType.KEYBOARD, GLFW_KEY_O)),
	SPAWN_SHIP(en -> Lng.str("Spawn Ship"), false, KeyboardContext.PLAYER, new InputAction(InputType.KEYBOARD, GLFW_KEY_X)),
	SPAWN_SPACE_STATION(en -> Lng.str("Spawn Space Station"), false, KeyboardContext.PLAYER, new InputAction(InputType.KEYBOARD, GLFW_KEY_P)),
	SELECT_MODULE(en -> Lng.str("Select Module"), false, KeyboardContext.BUILD, new InputAction(InputType.KEYBOARD, GLFW_KEY_C)),
	CONNECT_MODULE(en -> Lng.str("Connect Module"), false, KeyboardContext.BUILD, new InputAction(InputType.KEYBOARD, GLFW_KEY_V)),
	ASTRONAUT_ROTATE_BLOCK(en -> Lng.str("Block Rotate"), false, KeyboardContext.PLAYER, true, new InputAction(InputType.KEYBOARD, GLFW_KEY_LEFT_CONTROL)),
	HELP_SCREEN(en -> Lng.str("Display/Hide Help " + "Screen"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_SLASH)),
	SHAPES_RADIAL_MENU(en -> Lng.str("Open/Close Block Shape menu"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_T)),
	NEXT_BLOCK_ROTATION(en -> Lng.str("Next block rotation"), false, KeyboardContext.BUILD, new InputAction(InputType.MOUSE_WHEEL, Mouse.MouseWheelDir.MOUSE_WHEEL_UP, new InputAction(InputType.KEYBOARD_MOD, Keyboard.KeyboardControlKey.CONTROL))),

	PREVIOUS_BLOCK_ROTATION(en -> Lng.str("Previous block rotation"), false, KeyboardContext.BUILD, new InputAction(InputType.MOUSE_WHEEL, Mouse.MouseWheelDir.MOUSE_WHEEL_DOWN, new InputAction(InputType.KEYBOARD_MOD, Keyboard.KeyboardControlKey.CONTROL))),

	SWITCH_COCKPIT_SHIP_NEXT(en -> Lng.str("Next Docked Ship"), false, KeyboardContext.SHIP, new InputAction(InputType.KEYBOARD, GLFW_KEY_UP)),
	SWITCH_COCKPIT_SHIP_PREVIOUS(en -> Lng.str("Previous " + "Docked Ship"), false, KeyboardContext.SHIP, new InputAction(InputType.KEYBOARD, GLFW_KEY_DOWN)),
	SWITCH_COCKPIT_SHIP_HOLD_FOR_CHAIN(en -> Lng.str("Plus up/down to switch chain"), false, KeyboardContext.SHIP, true, new InputAction(InputType.KEYBOARD, GLFW_KEY_RIGHT_CONTROL)),
	SWITCH_COCKPIT_NEXT(en -> Lng.str("Next " + "Cockpit"), false, KeyboardContext.SHIP, new InputAction(InputType.KEYBOARD, GLFW_KEY_RIGHT)),
	SWITCH_COCKPIT_PREVIOUS(en -> Lng.str("Previous Cockpit"), false, KeyboardContext.SHIP, new InputAction(InputType.KEYBOARD, GLFW_KEY_LEFT)),
	REACTOR_KEY(en -> Lng.str("Reactor Menu"), false, KeyboardContext.SHIP, new InputAction(InputType.KEYBOARD, GLFW_KEY_INSERT)),
	CHAT(en -> Lng.str("Chat"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_ENTER)),
	SHOP_PANEL(en -> Lng.str("Shop Menu"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_B)),
	INVENTORY_SWITCH_ITEM(en -> Lng.str("Fast switch inventory"), false, KeyboardContext.GENERAL, true, new InputAction(InputType.KEYBOARD, GLFW_KEY_LEFT_SHIFT)),
	INVENTORY_PANEL(en -> Lng.str("Inventory Menu"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_I)),
	WEAPON_PANEL(en -> Lng.str("Weapon Menu"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_G)),
	NAVIGATION_PANEL(en -> Lng.str("Navigation Menu"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_N)),
	AI_CONFIG_PANEL(en -> Lng.str("AI Config Menu"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_SEMICOLON)),
	CATALOG_PANEL(en -> Lng.str("Catalog Panel"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_U)), //Z/Y is now free too
	SELECT_ENTITY_NEXT(en -> Lng.str("Select Next Entity"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_RIGHT_BRACKET)),
	SELECT_ENTITY_PREV(en -> Lng.str("Select " + "Previous Entity"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_LEFT_BRACKET)),
	SELECT_NEAREST_ENTITY(en -> Lng.str("Select " + "Nearest Entity"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_APOSTROPHE)),
	SELECT_LOOK_ENTITY(en -> Lng.str("Select Targeted Entity"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_F)),
	SELECT_OUTLINE(en -> Lng.str("Outline selected Target"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_F)),
	ZOOM_MINIMAP(en -> Lng.str("Zoom Minimap"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_EQUAL)),
	RELEASE_MOUSE(en -> Lng.str("Release Mouse"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_F2)),
	NEXT_CONTROLLER(en -> Lng.str("Select next controller"), false, KeyboardContext.BUILD, new InputAction(InputType.KEYBOARD, GLFW_KEY_RIGHT)),
	PREVIOUS_CONTROLLER(en -> Lng.str("Select prev controller"), false, KeyboardContext.BUILD, new InputAction(InputType.KEYBOARD, GLFW_KEY_LEFT)),
	SELECT_CORE(en -> Lng.str("Select core"), false, KeyboardContext.BUILD, new InputAction(InputType.KEYBOARD, GLFW_KEY_UP)),
	BUILD_MODE_FIX_CAM(en -> Lng.str("Advanced Build Mode"), false, KeyboardContext.BUILD, OperatingSystem.getOS() == OperatingSystem.MAC ? new InputAction(InputType.KEYBOARD, GLFW_KEY_MINUS) : new InputAction(InputType.KEYBOARD, GLFW_KEY_LEFT_CONTROL)),
	ALIGN_SHIP(en -> Lng.str("Align Ship " + "Cam"), false, KeyboardContext.SHIP, new InputAction(InputType.KEYBOARD, GLFW_KEY_C)),
	CANCEL_SHIP(en -> Lng.str("Cancel Ship Movement"), false, KeyboardContext.SHIP, new InputAction(InputType.KEYBOARD, GLFW_KEY_V)),
	SCREENSHOT_WITH_GUI(en -> Lng.str("Screenshot (with GUI)"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_F5)),
	SCREENSHOT_WITHOUT_GUI(en -> Lng.str("Screenshot " + "(without GUI)"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_F6)),
	FACTION_MENU(en -> Lng.str("Open Faction Menu"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_H)),
	MAP_PANEL(en -> Lng.str("Map"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_M)),
	STRUCTURE_PANEL(en -> Lng.str("Structure Menu"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_DELETE)),
	LEADERBOARD_PANEL(en -> Lng.str("Show leaderboard"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_PERIOD)),
	FLEET_PANEL(en -> Lng.str("Show Fleet Panel"), false, KeyboardContext.GENERAL, new InputAction(InputType.KEYBOARD, GLFW_KEY_K)),

	BUILD_MODE_FAST_MOVEMENT(en -> Lng.str("Hold for faster Build Mode Move"), false, KeyboardContext.BUILD, true, new InputAction(InputType.KEYBOARD, GLFW_KEY_LEFT_SHIFT)),
	KEY_BULK_CONNECTION_MOD(en -> Lng.str("Hold " + "for" + " bulk connect"), false, KeyboardContext.BUILD, true, new InputAction(InputType.KEYBOARD, GLFW_KEY_LEFT_SHIFT)),
	COPY_AREA_NEXT(en -> Lng.str("Copy Area Next Rotation"), false, KeyboardContext.BUILD, true, new InputAction(InputType.KEYBOARD, GLFW_KEY_PAGE_DOWN)),
	COPY_AREA_PRIOR(en -> Lng.str("Copy Area Prior Rotation"), false, KeyboardContext.BUILD, true, new InputAction(InputType.KEYBOARD, GLFW_KEY_PAGE_UP)),
	COPY_AREA_X_AXIS(en -> Lng.str("Copy Area X " + "Rotation (use next)"), false, KeyboardContext.BUILD, true, new InputAction(InputType.KEYBOARD, GLFW_KEY_LEFT_SHIFT)),
	COPY_AREA_Z_AXIS(en -> Lng.str("Copy Area Z Rotation (use next)"), false, KeyboardContext.BUILD, true, new InputAction(InputType.KEYBOARD, GLFW_KEY_LEFT_CONTROL)),

	PLAYER_TRADE_ACCEPT(en -> Lng.str("Accept incoming trade request"), false, KeyboardContext.GENERAL, true, new InputAction(InputType.KEYBOARD, GLFW_KEY_J)),
	PLAYER_TRADE_CANCEL(en -> Lng.str("Cancel incoming " + "trade" + " request"), false, KeyboardContext.GENERAL, true, new InputAction(InputType.KEYBOARD, GLFW_KEY_L)),
	CREATIVE_MODE(en -> Lng.str("Toggle creative mode (if available)"), false, KeyboardContext.GENERAL, true, new InputAction(InputType.KEYBOARD, GLFW_KEY_F3)),
	MAIN_MENU(en -> Lng.str("Main Menu"), false, KeyboardContext.GENERAL, true, new InputAction(InputType.KEYBOARD, GLFW_KEY_ESCAPE)),
	UNDO(en -> Lng.str("Undo"), false, KeyboardContext.GENERAL, true, new InputAction(InputType.KEYBOARD, GLFW_KEY_Z, new InputAction(InputType.KEYBOARD_MOD, Keyboard.KeyboardControlKey.CONTROL))),
	REDO(en -> Lng.str("Redo"), false, KeyboardContext.GENERAL, true, new InputAction(InputType.KEYBOARD, GLFW_KEY_Y, new InputAction(InputType.KEYBOARD_MOD, Keyboard.KeyboardControlKey.CONTROL))),
	PIN_AI_TARGET(en -> Lng.str("Pin AI Target"), false, KeyboardContext.SHIP, true, new InputAction(InputType.KEYBOARD, GLFW_KEY_X)),

	TUTORIAL_KEY_ZOOM_IN(en -> Lng.str("(Tutorial) Zoom in"), false, KeyboardContext.TUTORIAL, true, new InputAction(InputType.KEYBOARD, GLFW_KEY_PAGE_UP)),
	TUTORIAL_KEY_ZOOM_OUT(en -> Lng.str("(Tutorial) " + "Zoom out"), false, KeyboardContext.TUTORIAL, true, new InputAction(InputType.KEYBOARD, GLFW_KEY_PAGE_DOWN)),
	TUTORIAL_KEY_PAUSE(en -> Lng.str("(Tutorial) " + "Pause/Resume"), false, KeyboardContext.TUTORIAL, true, new InputAction(InputType.KEYBOARD, GLFW_KEY_F3)), //Deprecated by new tutorial menu
	TUTORIAL_KEY_CLOSE(en -> Lng.str("(Tutorial) Close"), false, KeyboardContext.TUTORIAL, true, new InputAction(InputType.KEYBOARD, GLFW_KEY_F4)), //Deprecated by new tutorial menu
//	TUTORIAL_KEY_PIN(en -> Lng.str("(Tutorial) Pin Element to HUD"), false, KeyboardContext.TUTORIAL, true, new InputAction(InputType.KEYBOARD_MOD, GLFW_KEY_LEFT_CONTROL), new InputAction(InputType.MOUSE, GLFW_MOUSE_BUTTON_1)),

	DIALOG_CLOSE(en -> Lng.str("(Dialog) Close"), false, KeyboardContext.DIALOG, true, new InputAction(InputType.KEYBOARD, GLFW_KEY_ESCAPE)),
	DIALOG_CONFIRM(en -> Lng.str("(Dialog) Close"), false, KeyboardContext.DIALOG, true, new InputAction(InputType.KEYBOARD, GLFW_KEY_ENTER), new InputAction(InputType.KEYBOARD, GLFW_KEY_KP_ENTER)),
	DIALOG_NEXT_PAGE(en -> Lng.str("(Tutorial) Next " + "Page"), false, KeyboardContext.DIALOG, true, new InputAction(InputType.KEYBOARD, GLFW_KEY_RIGHT)),
	DIALOG_PREVIOUS_PAGE(en -> Lng.str("(Tutorial) " + "Previous Page"), false, KeyboardContext.DIALOG, true, new InputAction(InputType.KEYBOARD, GLFW_KEY_LEFT)),
	MAP_NAVIGATE_TO(en -> Lng.str("Navigate To"), false, KeyboardContext.MAP, true, new InputAction(InputType.MOUSE, MouseButton.MOUSE_RIGHT.ordinal())),
	FULLSCREEN_TOGGLE(en -> Lng.str("Toggle Fullscreen"), false, KeyboardContext.GENERAL, true, new InputAction(InputType.KEYBOARD, GLFW_KEY_F11));

	public static final short t = 256;
	public static final KeyboardMappings[] remoteMappings;
	public static final Set<KeyboardMappings> duplicates = new ObjectOpenHashSet<KeyboardMappings>();
	public static final Object2ObjectOpenHashMap<InputType, Int2ObjectOpenHashMap<List<KeyboardMappings>>> mappingIndex;
	public static int version;
	public static boolean dirty;
	private static KeyboardMappings[] byNameLength;

	static {
		mappingIndex = new Object2ObjectOpenHashMap<>();
		indexActions();
	}

	static {
		ObjectArrayList<KeyboardMappings> m = new ObjectArrayList<KeyboardMappings>();
		int cc = 1;

		for(KeyboardMappings k : values()) {
			assert (k.getMappings() != null) : k;
			for(InputAction a : k.getMappings()) {
				assert (a != null) : k + ": " + a;
			}
			if(k.NT) {
				k.ntKey = cc;
				cc *= 2;
				m.add(k);
			}
		}
		assert (m.size() < 31);
		remoteMappings = new KeyboardMappings[m.size()];
		int count = 0;
		for(KeyboardMappings k : values()) {
			if(k.NT) {
				remoteMappings[count] = k;
				count++;
			}

		}
		for(int i = 0; i < remoteMappings.length; i++) {
			remoteMappings[i].ntOrdinal = (i + 1);
			remoteMappings[i] = m.get(i);
		}
	}

	public final boolean ignoreDuplicate;
	public final boolean NT;
	private final Translatable description;
	private final KeyboardContext context;
	public int ntOrdinal = -1;
	public int ntKey;
	private InputAction[] mapping;

	KeyboardMappings(Translatable description, boolean nt, KeyboardContext context, boolean ignoreDuplicate, InputAction... init) {
		this.description = description;
		mapping = init;
		this.context = context;
		NT = nt;
		this.ignoreDuplicate = ignoreDuplicate;
	}

	KeyboardMappings(Translatable description, boolean nt, KeyboardContext context, InputAction... init) {
		this(description, nt, context, false, init);
	}

	private static boolean checkMappingsEmpty(InputAction[] mapping) {
		for(InputAction a : mapping) {
			if(a == null) {
				return false;
			}
		}
		return true;
	}

	public static void read() {
		BufferedInputStream in = null;
		try {
			List<String> names = new ObjectArrayList<>();
			List<String> values = new ObjectArrayList<>();
			File f = new FileExt("." + File.separator + "keyboard.cfg");
			DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = fac.newDocumentBuilder();
			in = new BufferedInputStream(new FileInputStream(f));
			Document doc = db.parse(in);

			Element root = doc.getDocumentElement();
			String versionStr = root.getAttribute("version");
			if(versionStr == null || versionStr.isBlank()) {
				throw new Exception("Could Not read input settings: version attribute missing");
			}
			int version = Integer.parseInt(versionStr);
			NodeList cn = root.getChildNodes();
			for(int i = 0; i < cn.getLength(); i++) {
				Node item = cn.item(i);
				if(item.getNodeType() == Node.ELEMENT_NODE && "mapping".equals(item.getNodeName().toLowerCase(Locale.ENGLISH))) {
					Node nameAtt = item.getAttributes().getNamedItem("name");

					if(nameAtt == null) {
						System.err.println("[WARNING] Node didnt have a name attribute: " + item.getNodeName());
						continue;
					} else {
						String nm = nameAtt.getNodeValue().toUpperCase(Locale.ENGLISH);
						KeyboardMappings toAssign = valueOf(nm);
						List<InputAction> maps = new ObjectArrayList<>();
						NodeList mapChi = item.getChildNodes();
						for(int c = 0; c < mapChi.getLength(); c++) {
							Node inputActionNode = mapChi.item(c);
							if(inputActionNode.getNodeType() == Node.ELEMENT_NODE && inputActionNode.getNodeName().toLowerCase(Locale.ENGLISH).equals(InputAction.nodeName.toLowerCase(Locale.ENGLISH))) {
								InputAction readAction = InputAction.readAction(inputActionNode);
								maps.add(readAction);
							}
						}
						if(maps.size() > 0) {
							toAssign.setMappings(maps.toArray(new InputAction[maps.size()]));
						}
					}
				}
			}

		} catch(Exception e) {
			e.printStackTrace();
			System.err.println("Could not read settings file: using defaults (" + e.getMessage() + ")");
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
			dirty = false;
			checkForDuplicates();
			indexActions();
		}

	}

	public static void writeDefault() {
		try {
			write("." + File.separator + "data" + File.separator + "config" + File.separator + "defaultSettings" + File.separator + "keyboard.cfg");
			//Copy the file to the current directory
			File defaultSettings = new FileExt("." + File.separator + "data" + File.separator + "config" + File.separator + "defaultSettings" + File.separator + "keyboard.cfg");
			File currentSettings = new FileExt("." + File.separator + "keyboard.cfg");
			FileUtil.copyFile(defaultSettings, currentSettings);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static void write() throws IOException {
		write("." + File.separator + "keyboard.cfg");
	}

	public static void write(String path) throws IOException {
		File f = new FileExt(path);
		f.delete();
		f.createNewFile();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document doc;
		try {
			doc = dbf.newDocumentBuilder().newDocument();
		} catch(ParserConfigurationException e) {
			throw new IOException(e);
		}

		Element root = doc.createElement("Mappings");
		doc.appendChild(root);
		root.setAttribute("version", String.valueOf(version));
		KeyboardMappings[] values = values();
		for(KeyboardMappings m : values) {
			Element inputNode = doc.createElement("Mapping");
			inputNode.setAttribute("name", m.name().toUpperCase(Locale.ENGLISH));

			for(InputAction a : m.getMappings()) {
				Node uid = a.getUID(doc);
				inputNode.appendChild(uid);
			}
			root.appendChild(inputNode);
		}
		try {
			XMLTools.writeDocument(f, doc);
		} catch(ParserConfigurationException | TransformerException e) {
			throw new RuntimeException(e);
		}

	}

	public static void main(String[] asfd) {
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter("./data/tutorial/KeyboardMappingVariables.txt", StandardCharsets.UTF_8));

			for(KeyboardMappings k : values()) {
				w.append("$" + k.name());
				int a = 50 - k.name().length();
				w.append(" ");
				for(int i = 0; i < a; i++) {
					w.append(" ");
				}

				w.append(" -> " + k.getDescription() + "; Context: " + k.context.name() + "\n");
			}
			w.close();
		} catch(IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void indexInputTypeActions(InputType type, Int2ObjectOpenHashMap<List<KeyboardMappings>> map) {
		KeyboardMappings[] values = values();
		for(KeyboardMappings v : values) {
			for(InputAction ia : v.getMappings()) {
				if(ia.type == type) {
					List<KeyboardMappings> list = map.get(ia.value);
					if(list == null) {
						list = new ObjectArrayList<>();
						map.put(ia.value, list);
					}
					if(!list.contains(v)) {
						list.add(v);
					}
				}
			}
		}
	}

	private static void indexActions() {
		mappingIndex.clear();
		for(InputType type : InputType.values()) {
			Int2ObjectOpenHashMap<List<KeyboardMappings>> map = new Int2ObjectOpenHashMap<>();
			mappingIndex.put(type, map);
			indexInputTypeActions(type, map);
		}
	}

	public static String formatText(String textContent) {
		for(KeyboardMappings k : valuesByLength()) {

			if(textContent.contains("$" + k.name())) {
				try {
					textContent = textContent.replaceAll("\\$" + k.name(), k.getKeyChar());
				} catch(Exception e) {
					System.err.println("ERROR WHEN REPLACING TEXT CONTENT:\n" + textContent + "\nfor\n" + "$" + k.name() + " -> " + k.getKeyChar());
					e.printStackTrace();

				}
			}
		}

		return textContent;
	}

	public static boolean checkForDuplicates() {
		duplicates.clear();
		for(KeyboardMappings mapping : values()) {
			for(KeyboardMappings m : values()) {
				if(m != mapping && m.hasDuplicateInputActions(mapping)) {

					//duplicate key
					if(!mapping.ignoreDuplicate && !m.ignoreDuplicate && checkRelated(mapping.context, m.context)) {
						//						System.err.println(String.format("DUPLICATE KEY for %-10s | %25s - %s",
						//								m.getKeyCharAbsolute(), mapping.name(), m.name())
						//						);
						duplicates.add(mapping);
						duplicates.add(m);
					}
				}
			}
		}

		return true;
	}

	private static boolean checkRelated(KeyboardContext a, KeyboardContext b) {
		return isRelated(a, b) || isRelated(b, a);
	}

	private static boolean isRelated(KeyboardContext a, KeyboardContext b) {
		if(a == b) {
			return true;
		}
		if(!a.isRoot()) {
			return isRelated(a.getParent(), b);
		} else {
			return false;
		}
	}

	private static KeyboardMappings[] valuesByLength() {
		if(byNameLength == null) {
			byNameLength = Arrays.copyOf(values(), values().length);
			Arrays.sort(byNameLength, (o1, o2) -> o2.name().length() - o1.name().length());
		}
		return byNameLength;
	}

	//INSERTED CODE
	public static String getKeyChar(int mapping) {
		/* Finished char mappings - TheDerpGamer */
		return switch(mapping) {
			case GLFW_KEY_BACKSPACE -> Lng.str("Backspace");
			case GLFW_KEY_BACKSLASH -> Lng.str("Backslash");
			case GLFW_KEY_LEFT_SHIFT -> Lng.str("Left Shift");
			case GLFW_KEY_RIGHT_SHIFT -> Lng.str("Right Shift");
			case GLFW_KEY_LEFT_CONTROL -> Lng.str("Left Ctrl");
			case GLFW_KEY_RIGHT_CONTROL -> Lng.str("Right Ctrl");
			case GLFW_KEY_SPACE -> Lng.str("Spacebar");
			case GLFW_KEY_LEFT_ALT -> Lng.str("L. Alt");
			case GLFW_KEY_RIGHT_ALT -> Lng.str("R. Alt");
			case GLFW_KEY_DELETE -> Lng.str("Del");
			case GLFW_KEY_INSERT -> Lng.str("Ins");
			case GLFW_KEY_HOME -> Lng.str("Home");
			case GLFW_KEY_PAGE_UP -> Lng.str("Pg. Up");
			case GLFW_KEY_PAGE_DOWN -> Lng.str("Pg. Down");
			case GLFW_KEY_END -> Lng.str("End");
			case GLFW_KEY_SEMICOLON -> Lng.str(";");
			case GLFW_KEY_COMMA -> Lng.str(",");
			case GLFW_KEY_0 -> Lng.str("0");
			case GLFW_KEY_1 -> Lng.str("1");
			case GLFW_KEY_2 -> Lng.str("2");
			case GLFW_KEY_3 -> Lng.str("3");
			case GLFW_KEY_4 -> Lng.str("4");
			case GLFW_KEY_5 -> Lng.str("5");
			case GLFW_KEY_6 -> Lng.str("6");
			case GLFW_KEY_7 -> Lng.str("7");
			case GLFW_KEY_8 -> Lng.str("8");
			case GLFW_KEY_9 -> Lng.str("9");
			case GLFW_KEY_KP_ADD -> Lng.str("+");
			case GLFW_KEY_APOSTROPHE -> Lng.str("`");
			case GLFW_KEY_CAPS_LOCK -> Lng.str("Caps Lock");
			case GLFW_KEY_DOWN -> Lng.str("Down");
			case GLFW_KEY_EQUAL -> Lng.str("=");
			case GLFW_KEY_ESCAPE -> Lng.str("Escape");
			case GLFW_KEY_LEFT_BRACKET -> Lng.str("[");
			case GLFW_KEY_LEFT -> Lng.str("Left");
			case GLFW_KEY_LEFT_SUPER -> Lng.str("Left Win/Mac");
			case GLFW_KEY_MINUS -> Lng.str("-");
			case GLFW_KEY_NUM_LOCK -> Lng.str("Num Lock");
			case GLFW_KEY_PAUSE -> Lng.str("Pause");
			case GLFW_KEY_PERIOD -> Lng.str(".");
			case GLFW_KEY_RIGHT_BRACKET -> Lng.str("]");
			case GLFW_KEY_ENTER -> Lng.str("Enter");
			case GLFW_KEY_RIGHT -> Lng.str("Right");
			case GLFW_KEY_RIGHT_SUPER -> Lng.str("Right Win/Mac");
			case GLFW_KEY_SCROLL_LOCK -> Lng.str("Scroll Lock");
			case GLFW_KEY_SLASH -> Lng.str("/");
			case GLFW_KEY_TAB -> Lng.str("Tab");
			case GLFW_KEY_UP -> Lng.str("Up");
			case GLFW_KEY_F1 -> Lng.str("F1");
			case GLFW_KEY_F2 -> Lng.str("F2");
			case GLFW_KEY_F3 -> Lng.str("F3");
			case GLFW_KEY_F4 -> Lng.str("F4");
			case GLFW_KEY_F5 -> Lng.str("F5");
			case GLFW_KEY_F6 -> Lng.str("F6");
			case GLFW_KEY_F7 -> Lng.str("F7");
			case GLFW_KEY_F8 -> Lng.str("F8");
			case GLFW_KEY_F9 -> Lng.str("F9");
			case GLFW_KEY_F10 -> Lng.str("F10");
			case GLFW_KEY_F11 -> Lng.str("F11");
			case GLFW_KEY_F12 -> Lng.str("F12");
			case GLFW_KEY_F13 -> Lng.str("F13");
			case GLFW_KEY_F14 -> Lng.str("F14");
			case GLFW_KEY_F15 -> Lng.str("F15");
			case GLFW_KEY_F16 -> Lng.str("F16");
			case GLFW_KEY_F17 -> Lng.str("F17");
			case GLFW_KEY_F18 -> Lng.str("F18");
			case GLFW_KEY_F19 -> Lng.str("F19");
			case GLFW_KEY_KP_0 -> Lng.str("0 (Keypad)");
			case GLFW_KEY_KP_1 -> Lng.str("1 (Keypad)");
			case GLFW_KEY_KP_2 -> Lng.str("2 (Keypad)");
			case GLFW_KEY_KP_3 -> Lng.str("3 (Keypad)");
			case GLFW_KEY_KP_4 -> Lng.str("4 (Keypad)");
			case GLFW_KEY_KP_5 -> Lng.str("5 (Keypad)");
			case GLFW_KEY_KP_6 -> Lng.str("6 (Keypad)");
			case GLFW_KEY_KP_7 -> Lng.str("7 (Keypad)");
			case GLFW_KEY_KP_8 -> Lng.str("8 (Keypad)");
			case GLFW_KEY_KP_9 -> Lng.str("9 (Keypad)");
			case GLFW_KEY_KP_DECIMAL -> Lng.str(". (Keypad)");
			case GLFW_KEY_KP_DIVIDE -> Lng.str("/  (Keypad)");
			case GLFW_KEY_KP_ENTER -> Lng.str("Enter (Keypad)");
			case GLFW_KEY_KP_EQUAL -> Lng.str("= (Keypad)");
			case GLFW_KEY_KP_MULTIPLY -> Lng.str("* (Keypad)");
			case GLFW_KEY_KP_SUBTRACT -> Lng.str("- (Keypad)");
			case GLFW_KEY_A -> Lng.str("A");
			case GLFW_KEY_B -> Lng.str("B");
			case GLFW_KEY_C -> Lng.str("C");
			case GLFW_KEY_D -> Lng.str("D");
			case GLFW_KEY_E -> Lng.str("E");
			case GLFW_KEY_F -> Lng.str("F");
			case GLFW_KEY_G -> Lng.str("G");
			case GLFW_KEY_H -> Lng.str("H");
			case GLFW_KEY_I -> Lng.str("I");
			case GLFW_KEY_J -> Lng.str("J");
			case GLFW_KEY_K -> Lng.str("K");
			case GLFW_KEY_L -> Lng.str("L");
			case GLFW_KEY_M -> Lng.str("M");
			case GLFW_KEY_N -> Lng.str("N");
			case GLFW_KEY_O -> Lng.str("O");
			case GLFW_KEY_P -> Lng.str("P");
			case GLFW_KEY_Q -> Lng.str("Q");
			case GLFW_KEY_R -> Lng.str("R");
			case GLFW_KEY_S -> Lng.str("S");
			case GLFW_KEY_T -> Lng.str("T");
			case GLFW_KEY_U -> Lng.str("U");
			case GLFW_KEY_V -> Lng.str("V");
			case GLFW_KEY_W -> Lng.str("W");
			case GLFW_KEY_X -> Lng.str("X");
			case GLFW_KEY_Y -> Lng.str("Y");
			case GLFW_KEY_Z -> Lng.str("Z");
			default -> Keyboard.getKeyNameUnique(mapping).toUpperCase(Locale.ENGLISH);
		};
	}

	public static boolean isControlDown() {
		return Keyboard.isKeyDown(GLFW_KEY_LEFT_CONTROL) || Keyboard.isKeyDown(GLFW_KEY_RIGHT_CONTROL);
	}

	public String getDescription() {
		return description.getName(this);
	}

	/**
	 * Removed an input action from this mapping
	 *
	 * @param action (must be the exact instance from the array)
	 * @return true if mapping was found and removed, false otherwise
	 */
	public boolean removeMapping(InputAction action) {
		int i = 0;
		boolean found = false;
		for(InputAction a : mapping) {
			if(a == action) {
				found = true;
				break;
			}
		}
		if(!found) {
			assert (false);
			return false;
		}
		InputAction[] mappingNew = new InputAction[mapping.length - 1];

		int c = 0;
		for(InputAction a : mapping) {
			if(a != action) {
				mappingNew[c] = a;
				c++;
			}
		}
		setMappings(mappingNew);
		return true;
	}

	public void addMapping(InputAction action) {
		assert (action != null);
		InputAction[] mappingNew = Arrays.copyOf(mapping, mapping.length + 1);
		mappingNew[mappingNew.length - 1] = action;
		assert (checkMappingsEmpty(mappingNew)) : Arrays.toString(mappingNew);
		setMappings(mappingNew);
		System.err.println("[KEYBOARDMAPPINGS] ADDED MAPPING: " + this + "; " + Arrays.toString(getMappings()));
	}

	public boolean isDuplicateInputAction(KeyEventInterface e) {
		InputAction action = e.generateInputAction();
		return isDuplicateInputAction(action);
	}

	public boolean hasDuplicateInputActions(KeyboardMappings m) {
		for(InputAction other : m.mapping) {
			assert (other != null) : m;
			if(isDuplicateInputAction(other)) {
				return true;
			}
		}

		return false;
	}

	//	public int get() {
	//		return value;
	//	}

	public boolean isDuplicateInputAction(InputAction other) {
		for(InputAction own : mapping) {
			assert (own != null) : this;
			assert (other != null) : other;
			if(own.equals(other)) {
				return true;
			}
		}
		return false;
	}

	public boolean equalsNtKey(int key) {
		return ntKey == key;
	}

	/**
	 * @return the context
	 */
	public KeyboardContext getContext() {
		return context;
	}

	//
	public String getKeyChar() {
		return getMappings().length == 0 ? Lng.str("<Unassigned>") : getMappings()[0].getName();
	}

	public InputAction[] getMappings() {
		return mapping;
	}

	public void setMappings(InputAction[] mapping) {
		this.mapping = mapping;
		dirty = true;
		checkForDuplicates();
		indexActions();
	}

	//	if(key == KeyboardMappings.FORWARD.getMapping()){return keyboardOfController.get()[0].get();}
	//	else if(key == KeyboardMappings.BACKWARDS.getMapping()){ return keyboardOfController.get()[1].get();}
	//	else if(key == KeyboardMappings.STRAFE_LEFT.getMapping()){ return keyboardOfController.get()[2].get();}
	//	else if(key == KeyboardMappings.STRAFE_RIGHT.getMapping()){ return keyboardOfController.get()[3].get();}
	//	else if(key == KeyboardMappings.DOWN.getMapping()){ return keyboardOfController.get()[4].get();}
	//	else if(key == KeyboardMappings.UP.getMapping()){ return keyboardOfController.get()[5].get();}
	//	else if(key == KeyboardMappings.BREAK.getMapping()){ return keyboardOfController.get()[6].get();}
	//	else if(key == KeyboardMappings.CHANGE_SHIP_MODE.getMapping()){ return keyboardOfController.get()[7].get();}
	public boolean isNTKeyDown(int ntkey) {
		assert (ntKey > 0);

		return (ntkey & ntKey) == ntKey;
	}

	public boolean isSticky(StateInterface state) {
		return this == FREE_CAM && Controller.FREE_CAM_STICKY;
	}

	public boolean isDownOrSticky(StateInterface state) {
		return isDown() || isSticky(state);
	}

	public boolean isDown() {
		for(InputAction a : mapping) {
			if(a.isDown()) {
				return true;
			}
		}
		return false;
	}

}
