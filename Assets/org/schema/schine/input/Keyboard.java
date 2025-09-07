package org.schema.schine.input;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.GraphicsContext;

import java.util.Locale;

import static org.lwjgl.glfw.GLFW.*;

public class Keyboard {

	public enum KeyboardControlKey {
		CONTROL("CONTROL", GLFW_KEY_LEFT_CONTROL, GLFW_KEY_RIGHT_CONTROL),
		ALT("ALT", GLFW_KEY_LEFT_ALT, GLFW_KEY_RIGHT_ALT),
		SHIFT("SHIFT", GLFW_KEY_LEFT_SHIFT, GLFW_KEY_RIGHT_SHIFT),
		SUPER("SUPER", GLFW_KEY_LEFT_SUPER, GLFW_KEY_RIGHT_SUPER);

		public final int[] keys;
		public final String id;

		KeyboardControlKey(String id, int... keys) {
			this.id = id;
			this.keys = keys;
		}

		public String getName() {
			return switch(this) {
				case ALT -> Lng.str("Alt");
				case CONTROL -> Lng.str("Control");
				case SHIFT -> Lng.str("Shift");
				case SUPER -> Lng.str("Super");
			};
		}

		public static KeyboardControlKey getById(String idAnyCase) throws InputTypeParseException {
			for(KeyboardControlKey k : values()) {
				if(k.id.toLowerCase(Locale.ENGLISH).equals(idAnyCase.toLowerCase(Locale.ENGLISH))) {
					return k;
				}
			}
			throw new InputTypeParseException("Control key not found: " + idAnyCase);
		}
	}

	public static final int[] slotKeys = {GLFW_KEY_1, GLFW_KEY_2, GLFW_KEY_3, GLFW_KEY_4, GLFW_KEY_5, GLFW_KEY_6, GLFW_KEY_7, GLFW_KEY_8, GLFW_KEY_9, GLFW_KEY_0};

	private static Int2IntOpenHashMap initializeKeyMap() {
		Int2IntOpenHashMap slotKeyMap = new Int2IntOpenHashMap();
		slotKeyMap.defaultReturnValue(-1);
		for(int i = 0; i < slotKeys.length; i++) {
			slotKeyMap.put(slotKeys[i], i);
		}

		return slotKeyMap;
	}

	private static final Int2IntOpenHashMap slotKeyMap = initializeKeyMap();

	public static boolean isSlotKey(int key) {
		return slotKeyMap.containsKey(key);
	}

	public static int getSlotKey(int key) {
		return slotKeyMap.get(key);
	}

	private static final Int2ObjectOpenHashMap<String> KEY_NAMES = new Int2ObjectOpenHashMap<>();

	static {
		KEY_NAMES.defaultReturnValue("Unknown");

		KEY_NAMES.put(GLFW_KEY_UNKNOWN, "Unknown");
		KEY_NAMES.put(GLFW_KEY_0, "0");
		KEY_NAMES.put(GLFW_KEY_1, "1");
		KEY_NAMES.put(GLFW_KEY_2, "2");
		KEY_NAMES.put(GLFW_KEY_3, "3");
		KEY_NAMES.put(GLFW_KEY_4, "4");
		KEY_NAMES.put(GLFW_KEY_5, "5");
		KEY_NAMES.put(GLFW_KEY_6, "6");
		KEY_NAMES.put(GLFW_KEY_7, "7");
		KEY_NAMES.put(GLFW_KEY_8, "8");
		KEY_NAMES.put(GLFW_KEY_9, "9");

		KEY_NAMES.put(GLFW_KEY_Q, "Q");
		KEY_NAMES.put(GLFW_KEY_W, "W");
		KEY_NAMES.put(GLFW_KEY_E, "E");
		KEY_NAMES.put(GLFW_KEY_R, "R");
		KEY_NAMES.put(GLFW_KEY_T, "T");
		KEY_NAMES.put(GLFW_KEY_Y, "Y");
		KEY_NAMES.put(GLFW_KEY_U, "U");
		KEY_NAMES.put(GLFW_KEY_I, "I");
		KEY_NAMES.put(GLFW_KEY_O, "O");
		KEY_NAMES.put(GLFW_KEY_P, "P");
		KEY_NAMES.put(GLFW_KEY_A, "A");
		KEY_NAMES.put(GLFW_KEY_S, "S");
		KEY_NAMES.put(GLFW_KEY_D, "D");
		KEY_NAMES.put(GLFW_KEY_F, "F");
		KEY_NAMES.put(GLFW_KEY_G, "G");
		KEY_NAMES.put(GLFW_KEY_H, "H");
		KEY_NAMES.put(GLFW_KEY_J, "J");
		KEY_NAMES.put(GLFW_KEY_K, "K");
		KEY_NAMES.put(GLFW_KEY_L, "L");
		KEY_NAMES.put(GLFW_KEY_Z, "Z");
		KEY_NAMES.put(GLFW_KEY_X, "X");
		KEY_NAMES.put(GLFW_KEY_C, "C");
		KEY_NAMES.put(GLFW_KEY_V, "V");
		KEY_NAMES.put(GLFW_KEY_B, "B");
		KEY_NAMES.put(GLFW_KEY_N, "N");
		KEY_NAMES.put(GLFW_KEY_M, "M");

		KEY_NAMES.put(GLFW_KEY_F1, "F1");
		KEY_NAMES.put(GLFW_KEY_F2, "F2");
		KEY_NAMES.put(GLFW_KEY_F3, "F3");
		KEY_NAMES.put(GLFW_KEY_F4, "F4");
		KEY_NAMES.put(GLFW_KEY_F5, "F5");
		KEY_NAMES.put(GLFW_KEY_F6, "F6");
		KEY_NAMES.put(GLFW_KEY_F7, "F7");
		KEY_NAMES.put(GLFW_KEY_F8, "F8");
		KEY_NAMES.put(GLFW_KEY_F9, "F9");
		KEY_NAMES.put(GLFW_KEY_F10, "F10");
		KEY_NAMES.put(GLFW_KEY_F11, "F11");
		KEY_NAMES.put(GLFW_KEY_F12, "F12");
		KEY_NAMES.put(GLFW_KEY_F13, "F13");
		KEY_NAMES.put(GLFW_KEY_F14, "F14");
		KEY_NAMES.put(GLFW_KEY_F15, "F15");

		KEY_NAMES.put(GLFW_KEY_KP_0, "Numpad0");
		KEY_NAMES.put(GLFW_KEY_KP_1, "Numpad1");
		KEY_NAMES.put(GLFW_KEY_KP_2, "Numpad2");
		KEY_NAMES.put(GLFW_KEY_KP_3, "Numpad3");
		KEY_NAMES.put(GLFW_KEY_KP_4, "Numpad4");
		KEY_NAMES.put(GLFW_KEY_KP_5, "Numpad5");
		KEY_NAMES.put(GLFW_KEY_KP_6, "Numpad6");
		KEY_NAMES.put(GLFW_KEY_KP_7, "Numpad7");
		KEY_NAMES.put(GLFW_KEY_KP_8, "Numpad8");
		KEY_NAMES.put(GLFW_KEY_KP_9, "Numpad9");

		KEY_NAMES.put(GLFW_KEY_KP_EQUAL, "NumpadEquals");
		KEY_NAMES.put(GLFW_KEY_KP_ENTER, "NumpadEnter");
		KEY_NAMES.put(GLFW_KEY_KP_DECIMAL, "NumpadComma");
		KEY_NAMES.put(GLFW_KEY_KP_DIVIDE, "NumpadDivide");
		KEY_NAMES.put(GLFW_KEY_KP_SUBTRACT, "NumpadSubtract");
		KEY_NAMES.put(GLFW_KEY_KP_MULTIPLY, "NumpadMultiply");

		KEY_NAMES.put(GLFW_KEY_LEFT_ALT, "LeftAlt");
		KEY_NAMES.put(GLFW_KEY_RIGHT_ALT, "RightAlt");

		KEY_NAMES.put(GLFW_KEY_LEFT_CONTROL, "LeftCtrl");
		KEY_NAMES.put(GLFW_KEY_RIGHT_CONTROL, "RightCtrl");

		KEY_NAMES.put(GLFW_KEY_LEFT_SHIFT, "LeftShift");
		KEY_NAMES.put(GLFW_KEY_RIGHT_SHIFT, "RightShift");

		KEY_NAMES.put(GLFW_KEY_LEFT_SUPER, "LeftOption");
		KEY_NAMES.put(GLFW_KEY_RIGHT_SUPER, "RightOption");

		KEY_NAMES.put(GLFW_KEY_MINUS, "Minus");
		KEY_NAMES.put(GLFW_KEY_EQUAL, "Equals");
		KEY_NAMES.put(GLFW_KEY_LEFT_BRACKET, "LeftBracket");
		KEY_NAMES.put(GLFW_KEY_RIGHT_BRACKET, "RigthBracket");
		KEY_NAMES.put(GLFW_KEY_SEMICOLON, "Semicolon");
		KEY_NAMES.put(GLFW_KEY_APOSTROPHE, "Apostrophe");
		KEY_NAMES.put(GLFW_KEY_GRAVE_ACCENT, "GraveAccent");
		KEY_NAMES.put(GLFW_KEY_BACKSLASH, "Backslash");
		KEY_NAMES.put(GLFW_KEY_COMMA, "Comma");
		KEY_NAMES.put(GLFW_KEY_PERIOD, "Period");
		KEY_NAMES.put(GLFW_KEY_SLASH, "Slash");

		KEY_NAMES.put(GLFW_KEY_WORLD_1, "w1");
		KEY_NAMES.put(GLFW_KEY_WORLD_2, "w2");
		KEY_NAMES.put(GLFW_KEY_ESCAPE, "Esc");
		KEY_NAMES.put(GLFW_KEY_ENTER, "Enter");
		KEY_NAMES.put(GLFW_KEY_SPACE, "Space");
		KEY_NAMES.put(GLFW_KEY_BACKSPACE, "Backspace");
		KEY_NAMES.put(GLFW_KEY_TAB, "Tab");

		KEY_NAMES.put(GLFW_KEY_PRINT_SCREEN, "PrintScreen");
		KEY_NAMES.put(GLFW_KEY_PAUSE, "Pause");

		KEY_NAMES.put(GLFW_KEY_HOME, "Home");
		KEY_NAMES.put(GLFW_KEY_PAGE_UP, "Page Up");
		KEY_NAMES.put(GLFW_KEY_PAGE_DOWN, "Page Down");
		KEY_NAMES.put(GLFW_KEY_END, "End");
		KEY_NAMES.put(GLFW_KEY_INSERT, "Insert");
		KEY_NAMES.put(GLFW_KEY_DELETE, "Delete");

		KEY_NAMES.put(GLFW_KEY_UP, "Up");
		KEY_NAMES.put(GLFW_KEY_LEFT, "Left");
		KEY_NAMES.put(GLFW_KEY_RIGHT, "Right");
		KEY_NAMES.put(GLFW_KEY_DOWN, "Down");

		KEY_NAMES.put(GLFW_KEY_NUM_LOCK, "NumLock");
		KEY_NAMES.put(GLFW_KEY_CAPS_LOCK, "CapsLock");
		KEY_NAMES.put(GLFW_KEY_SCROLL_LOCK, "ScrollLock");

		//	        KEY_NAMES[GLFW_KEY_KANA] = "Kana";
		//	        KEY_NAMES[GLFW_KEY_CONVERT] = "Convert";
		//	        KEY_NAMES[GLFW_KEY_NOCONVERT] = "No Convert";
		//	        KEY_NAMES[GLFW_KEY_YEN] = "Yen";
		//	        KEY_NAMES[GLFW_KEY_CIRCUMFLEX] = "Circumflex";
		//	        KEY_NAMES[GLFW_KEY_KANJI] = "Kanji";
		//	        KEY_NAMES[GLFW_KEY_AX] = "Ax";
		//	        KEY_NAMES[GLFW_KEY_UNLABELED] = "Unlabeled";
	}

	public static String getKeyTranslated(int mapping) {
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
			default -> getKeyNameUnique(mapping).toUpperCase(Locale.ENGLISH);
		};
	}

	private static boolean repeatEvents;

	public static boolean isKeyDown(int mapping) {
		return mapping > 0 && (glfwGetKey(GraphicsContext.getWindowId(), mapping) == GLFW_PRESS || glfwGetKey(GraphicsContext.getWindowId(), mapping) == GLFW_REPEAT);
	}

	public static String getKeyNameUnique(int mapping) {
		return !KEY_NAMES.containsKey(mapping) ? "Key-N/A(" + mapping + ")" : KEY_NAMES.get(mapping);
	}

	private static Object2IntOpenHashMap<String> keymapping;
	private static Object2IntOpenHashMap<String> keymappingLowerCase;

	public static void createKeymapping() {
		keymapping = new Object2IntOpenHashMap<>();
		keymappingLowerCase = new Object2IntOpenHashMap<>();
		keymapping.defaultReturnValue(-2);
		keymappingLowerCase.defaultReturnValue(-2);
		for(it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<String> k : KEY_NAMES.int2ObjectEntrySet()) {

			keymapping.put(k.getValue(), k.getIntKey());
			keymappingLowerCase.put(k.getValue().toLowerCase(Locale.ENGLISH), k.getIntKey());
		}
	}

	public static int getKeyFromName(String uniqueKeyString) throws InputTypeParseException {

		int keyIndex = keymappingLowerCase.getInt(uniqueKeyString.toLowerCase(Locale.ENGLISH));
		if(keyIndex == -2) {
			throw new InputTypeParseException(uniqueKeyString + " key not known");
		}
		return keyIndex;
	}

	public static void enableRepeatEvents(boolean b) {
		repeatEvents = b;
	}

	public static boolean isRepeatEvent() {
		return repeatEvents;
	}

	public static boolean isCreated() {
		return GraphicsContext.isInitialized();
	}

	public static int getNumberPressed() {
		if(isKeyDown(GLFW_KEY_0)) {
			return 0;
		} else if(isKeyDown(GLFW_KEY_1)) {
			return 1;
		} else if(isKeyDown(GLFW_KEY_2)) {
			return 2;
		} else if(isKeyDown(GLFW_KEY_3)) {
			return 3;
		} else if(isKeyDown(GLFW_KEY_4)) {
			return 4;
		} else if(isKeyDown(GLFW_KEY_5)) {
			return 5;
		} else if(isKeyDown(GLFW_KEY_6)) {
			return 6;
		} else if(isKeyDown(GLFW_KEY_7)) {
			return 7;
		} else if(isKeyDown(GLFW_KEY_8)) {
			return 8;
		} else if(isKeyDown(GLFW_KEY_9)) {
			return 9;
		}
		return -1;
	}

	public static boolean isModDown(KeyboardControlKey controlKey) {
		for(int k : controlKey.keys) {
			if(isKeyDown(k)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isDebugKeyDown() {
		return KeyboardMappings.PLAYER_LIST.isDown();
	}

}
