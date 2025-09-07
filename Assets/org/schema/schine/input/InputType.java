package org.schema.schine.input;

import java.util.Locale;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public enum InputType {
	MOUSE("MOUSE_BUTTON"),
	MOUSE_WHEEL("MOUSE_WHEEL"),
	KEYBOARD("KEYBOARD"),
	KEYBOARD_MOD("KEYBOARD_MOD"),
	JOYSTICK("CONTROLLER_BUTTON"),
	BLOCK("BLOCK"),
	JOYSTICK_HAT("CONTROLLER_HAT"),
	;
	
	public final String prefix;
	private InputType(String prefix) {
		this.prefix = prefix;
	}
	
	public static final Object2ObjectOpenHashMap<String, InputType> strMap;
	static {
		strMap = new Object2ObjectOpenHashMap<>();
		for(InputType r : InputType.values()) {
			strMap.put(r.prefix.toUpperCase(Locale.ENGLISH), r);
		}
		strMap.trim();
	}
	
}
