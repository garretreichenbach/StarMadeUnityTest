package org.schema.schine.graphicsengine.forms.gui.newgui;

import org.lwjgl.glfw.GLFW;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontInterface;
import org.schema.schine.graphicsengine.forms.font.FontLibrary.FontSize;
import org.schema.schine.input.InputState;
import org.schema.schine.input.InputType;
import org.schema.schine.input.Keyboard;

public class GUIHelperIconManager {
	
	private static final FontInterface onSize = FontSize.MEDIUM_15;
	private static final FontInterface afterSize = FontSize.MEDIUM_15;
	
	public static GUIHelperIcon get(InputState state, InputType type, int key){
		return get(state, type, key, onSize, afterSize);
	}
	public static GUIHelperIcon get(InputState state, InputType type, int key, FontInterface sizeOn, FontInterface sizeAfter){
		GUIHelperTextureType t = GUIHelperTextureType.SINGLE;
		GUIHelperIcon h;
		switch(type) {
			case BLOCK -> {
				t = GUIHelperTextureType.NONE;
				h = new GUIHelperIcon(state, t, sizeOn, sizeAfter);
				return h;
			}
			case JOYSTICK -> throw new RuntimeException("TODO");
			case KEYBOARD -> {
				switch(key) {
					case (GLFW.GLFW_KEY_BACKSPACE) -> {
						t = GUIHelperTextureType.ONEANDHALF;
						sizeOn = sizeOn.smaller();
					}
					case (GLFW.GLFW_KEY_TAB) -> t = GUIHelperTextureType.ONEANDHALF;
					case (GLFW.GLFW_KEY_LEFT_SHIFT) -> {
						t = GUIHelperTextureType.TWO;
						sizeOn = sizeOn.smaller();
					}
					case (GLFW.GLFW_KEY_RIGHT_SHIFT) -> {
						t = GUIHelperTextureType.TWO;
						sizeOn = sizeOn.smaller();
					}
					case (GLFW.GLFW_KEY_LEFT_ALT) -> {
						t = GUIHelperTextureType.TWO;
						sizeOn = sizeOn.smaller();
					}
					case (GLFW.GLFW_KEY_RIGHT_ALT) -> {
						t = GUIHelperTextureType.TWO;
						sizeOn = sizeOn.smaller();
					}
					case (GLFW.GLFW_KEY_LEFT_CONTROL) -> {
						t = GUIHelperTextureType.TWO;
						sizeOn = sizeOn.smaller();
					}
					case (GLFW.GLFW_KEY_RIGHT_CONTROL) -> {
						t = GUIHelperTextureType.TWO;
						sizeOn = sizeOn.smaller();
					}
					case (GLFW.GLFW_KEY_SPACE) -> {
						t = GUIHelperTextureType.TWO;
						sizeOn = sizeOn.smaller();
					}
					case (GLFW.GLFW_KEY_ENTER) -> {
						t = GUIHelperTextureType.ONEANDHALF;
						sizeOn = sizeOn.smaller();
					}
				}
				h = new GUIHelperIcon(state, t, sizeOn, sizeAfter);
				h.setTextOn(Keyboard.getKeyTranslated(key));
				return h;
			}
			case MOUSE -> {
				t = GUIHelperTextureType.MOUSE_LEFT;
				if(key == 1) {
					t = GUIHelperTextureType.MOUSE_RIGHT;
				} else if(key == 2) {
					t = GUIHelperTextureType.MOUSE_MID;
				} else if(key == 3) {
					t = GUIHelperTextureType.MOUSE_WUP;
				} else if(key == 4) {
					t = GUIHelperTextureType.MOUSE_WDOWN;
				}
				h = new GUIHelperIcon(state, t, sizeOn, sizeAfter);
				return h;
			}
			default -> throw new RuntimeException("TODO");
		}
	}
}
