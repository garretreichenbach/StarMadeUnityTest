package org.schema.schine.input;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SUPER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_ALT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SUPER;
import static org.lwjgl.glfw.GLFW.GLFW_MOD_ALT;
import static org.lwjgl.glfw.GLFW.GLFW_MOD_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_MOD_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOD_SUPER;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_REPEAT;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class KeyboardEvent implements KeyEventInterface{

	public boolean charEvent;
	public int mods;
	public int actionState;
	public int scancode;
	public int key;
	public String charac;
	public boolean leftShift;
	public boolean rightShift;
	public boolean leftAlt;
	public boolean rightAlt;
	public boolean leftControl;
	public boolean rightControl;
	public boolean leftSuper;
	public boolean rightSuper;
	public boolean debugKey;
	protected final List<KeyboardMappings> triggeredMappings = new ObjectArrayList<>();
	
	@Override
	public boolean isPressed() {
		return actionState == GLFW_PRESS;
	}
	@Override
	public boolean isReleased() {
		return actionState == GLFW_RELEASE;
	}
	@Override
	public boolean isRepeat() {
		return actionState == GLFW_REPEAT;
	}
	@Override
	public int getKey() {
		return key;
	}
	@Override
	public String getCharacter() {
		return charac;
	}
	@Override
	public String toString() {
		return "KeyboardEvent [key=" + key + ", charEvent=" + charEvent + ", state=" + actionState + ", charac=" + charac
				+ ", special="+getSpecialKeysDownString()+"]";
	}
	private String getSpecialKeysDownString() {
		StringBuffer v = new StringBuffer();
		if(leftShift) {
			v.append("leftShift");
			v.append(", ");
		}
		if(rightShift) {
			v.append("rightShift");
			v.append(", ");
		}
		if(leftAlt) {
			v.append("leftAlt");
			v.append(", ");
		}
		if(rightAlt) {
			v.append("rightAlt");
			v.append(", ");
		}
		if(leftControl) {
			v.append("leftControl");
			v.append(", ");
		}
		if(rightControl) {
			v.append("rightControl");
			v.append(", ");
		}
		if(leftSuper) {
			v.append("leftSuper");
			v.append(", ");
		}
		if(rightSuper) {
			v.append("rightSuper");
			v.append(", ");
		}
		if(debugKey) {
			v.append("debugKey");
		}
		return v.toString();
	}
	@Override
	public boolean isCharacterEvent() {
		return charEvent;
	}
	/**
	 * @return true if key was down during event
	 */
	@Override
	public boolean isLeftShift() {
		return leftShift;
	}
	/**
	 * @return true if key was down during event
	 */
	@Override
	public boolean isRightShift() {
		return rightShift;
	}
	/**
	 * @return true if key was down during event
	 */
	@Override
	public boolean isLeftAlt() {
		return leftAlt;
	}
	/**
	 * @return true if key was down during event
	 */
	@Override
	public boolean isRightAlt() {
		return rightAlt;
	}
	/**
	 * @return true if key was down during event
	 */
	@Override
	public boolean isLeftControl() {
		return leftControl;
	}
	/**
	 * @return true if key was down during event
	 */
	@Override
	public boolean isRightControl() {
		return rightControl;
	}
	/**
	 * @return true if key was down during event
	 */
	@Override
	public boolean isLeftSuper() {
		return leftSuper;
	}
	/**
	 * @return true if key was down during event
	 */
	@Override
	public boolean isRightSuper() {
		return rightSuper;
	}
	/**
	 * @return true if key was down during event
	 */
	@Override
	public boolean isDebugKey() {
		return debugKey;
	}
	public void checkSpecialKeysDownMod(int mods) {
		leftShift = (mods & GLFW_MOD_SHIFT) == GLFW_MOD_SHIFT; 
		rightShift = (mods & GLFW_MOD_SHIFT) == GLFW_MOD_SHIFT; 
		
		leftAlt = (mods & GLFW_MOD_ALT) == GLFW_MOD_ALT; 
		rightAlt = (mods & GLFW_MOD_ALT) == GLFW_MOD_ALT; 
		
		leftControl = (mods & GLFW_MOD_CONTROL) == GLFW_MOD_CONTROL; 
		rightControl = (mods & GLFW_MOD_CONTROL) == GLFW_MOD_CONTROL; 
		
		leftSuper = (mods & GLFW_MOD_SUPER) == GLFW_MOD_SUPER; 
		rightSuper = (mods & GLFW_MOD_SUPER) == GLFW_MOD_SUPER; 

		debugKey = KeyboardMappings.PLAYER_LIST.isDown();
	}
	/**
	 * creates state for special keys
	 * @param mods2 
	 */
	public void checkSpecialKeysDown() {
		leftShift = Keyboard.isKeyDown(GLFW_KEY_LEFT_SHIFT);
		rightShift = Keyboard.isKeyDown(GLFW_KEY_RIGHT_SHIFT);
		
		leftAlt = Keyboard.isKeyDown(GLFW_KEY_LEFT_ALT);
		rightAlt = Keyboard.isKeyDown(GLFW_KEY_RIGHT_ALT);
		
		leftControl = Keyboard.isKeyDown(GLFW_KEY_LEFT_CONTROL);
		rightControl = Keyboard.isKeyDown(GLFW_KEY_RIGHT_CONTROL);
		
		leftSuper = Keyboard.isKeyDown(GLFW_KEY_LEFT_SUPER);
		rightSuper = Keyboard.isKeyDown(GLFW_KEY_RIGHT_SUPER);

		debugKey = KeyboardMappings.PLAYER_LIST.isDown();
	}
	/**
	 * creates list with all triggered actions for this keyboard event (depending on how keyboard is mapped)
	 */
	public void checkTriggeredMappings(InputType type) {
		triggeredMappings.clear();
		List<KeyboardMappings> list = KeyboardMappings.mappingIndex.get(type).get(key);
		if(list != null) {
			for(KeyboardMappings m : list) {
				for(InputAction a : m.getMappings()) {
					if(a.value == key && a.isModifierDown()) {
						//add to triggered if the input action's modifier is down (or if it has none)
						triggeredMappings.add(m);
						break;
					}
				}
			}
		}
	}
	
	@Override
	public List<KeyboardMappings> getTriggeredMappings() {
		return triggeredMappings;
	}
	@Override
	public boolean isTriggered(KeyboardMappings action) {
		return isPressed() && triggeredMappings.contains(action);
	}
	@Override
	public boolean isTriggeredRelease(KeyboardMappings action) {
		return isTriggered(action);
	}
	@Override
	public boolean isInputType(InputType t) {
		return t == InputType.KEYBOARD;
	}
	@Override
	public boolean isSlotKey() {
		return Keyboard.isSlotKey(key);
	}
	@Override
	public int getSlotKey() {
		return Keyboard.getSlotKey(key);
	}
	@Override
	public InputAction generateInputAction() {
		assert(this.isInputType(InputType.KEYBOARD));
		InputAction a;
//		if(isSelfSpecialKey()) {
			a = new InputAction(InputType.KEYBOARD, key);
//		}else {
//			if(isAnyShift()) {
//				a = new InputAction(InputType.KEYBOARD_MOD, KeyboardControlKey.SHIFT);
//			}else if(isAnyAlt()) {
//				a = new InputAction(InputType.KEYBOARD_MOD, KeyboardControlKey.ALT);
//			}else if(isAnyControl()) {
//				a = new InputAction(InputType.KEYBOARD_MOD, KeyboardControlKey.CONTROL);
//			}else if(isAnySuper()) {
//				a = new InputAction(InputType.KEYBOARD_MOD, KeyboardControlKey.SUPER);
//			}else {
//				throw new RuntimeException("Unkwnown mod key");
//			}
//		}
		
		return a;
	}
	public boolean isSpecialKeyDown() {
		return isAnyAlt() || isAnyControl() || isAnyShift();
	}
	public boolean isSelfSpecialKey() {
		return 
				key == GLFW_KEY_LEFT_SHIFT ||
				key == GLFW_KEY_RIGHT_SHIFT ||
				key == GLFW_KEY_LEFT_CONTROL ||
				key == GLFW_KEY_RIGHT_CONTROL ||
				key == GLFW_KEY_LEFT_ALT ||
				key == GLFW_KEY_RIGHT_ALT;
				
	}
	@Override
	public boolean isAnyAlt() {
		return rightAlt || leftAlt;
	}
	@Override
	public boolean isAnyShift() {
		return rightShift || leftShift;
	}
	@Override
	public boolean isAnyControl() {
		return rightControl || leftControl;
	}
	@Override
	public boolean isAnySuper() {
		return rightSuper || leftSuper;
	}
	@Override
	public boolean isEscapeKeyRaw() {
		return getKeyboardKeyRaw() == GLFW_KEY_ESCAPE;
	}
	@Override
	public int getKeyboardKeyRaw() {
		return isInputType(InputType.KEYBOARD) ? key : Integer.MIN_VALUE;
	}
	@Override
	public InputType getType() {
		return InputType.KEYBOARD;
	}
	public boolean equalsEventBasic(KeyEventInterface e) {
		return getType() == e.getType() && key == e.getKey();
	}

}
