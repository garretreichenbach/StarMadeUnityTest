package org.schema.schine.input;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.bulletphysics.util.ObjectArrayList;

public class JoystickEvent implements KeyEventInterface{

	public int button;
	public int actionState;
	public boolean mouseEvent;
	private boolean debugKey;
	private List<KeyboardMappings> triggeredMappings = new ObjectArrayList<>();

	@Override
	public boolean isPressed() {
		return actionState == GLFW.GLFW_PRESS;
	}

	@Override
	public int getKey() {
		return button;
	}

	@Override
	public String getCharacter() {
		return "";
	}

	@Override
	public boolean isCharacterEvent() {
		return false;
	}

	@Override
	public boolean isLeftShift() {
		return false;
	}

	@Override
	public boolean isRightShift() {
		return false;
	}

	@Override
	public boolean isLeftAlt() {
		return false;
	}

	@Override
	public boolean isRightAlt() {
		return false;
	}

	@Override
	public boolean isLeftControl() {
		return false;
	}

	@Override
	public boolean isRightControl() {
		return false;
	}

	@Override
	public boolean isDebugKey() {
		return debugKey;
	}

	@Override
	public List<KeyboardMappings> getTriggeredMappings() {
		return null;
	}

	@Override
	public boolean isTriggered(KeyboardMappings action) {
		return false;
	}
	/**
	 * creates state for special keys
	 */
	public void checkSpecialKeysDown() {
		debugKey = KeyboardMappings.PLAYER_LIST.isDown();
	}
	/**
	 * creates list with all triggered actions for this keyboard event (depending on how keyboard is mapped)
	 */
	public void checkTriggeredMappings() {
		triggeredMappings.clear();
		List<KeyboardMappings> list = KeyboardMappings.mappingIndex.get(InputType.JOYSTICK).get(button);
		if(list != null) {
			for(KeyboardMappings m : list) {
				for(InputAction a : m.getMappings()) {
					if(a.value == button && a.isModifierDown()) {
						//add to triggered if the input action's modifier is down (or if it has none)
						triggeredMappings.add(m);
						break;
					}
				}
			}
		}
	}

	@Override
	public boolean isInputType(InputType t) {
				return false;
	}

	@Override
	public boolean isSlotKey() {
				return false;
	}

	@Override
	public int getSlotKey() {
				return 0;
	}

	@Override
	public InputAction generateInputAction() {
				return null;
	}

	@Override
	public boolean isAnyControl() {
				return false;
	}

	@Override
	public boolean isAnyAlt() {
				return false;
	}

	@Override
	public boolean isAnyShift() {
				return false;
	}

	@Override
	public boolean isTriggeredRelease(KeyboardMappings action) {
		return isReleased() && isTriggered(action);
	}

	@Override
	public boolean isReleased() {
		return actionState == GLFW.GLFW_RELEASE;
	}

	@Override
	public boolean isRepeat() {
		return actionState == GLFW.GLFW_REPEAT;
	}

	@Override
	public void checkTriggeredMappings(InputType type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isEscapeKeyRaw() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getKeyboardKeyRaw() {
		return Integer.MIN_VALUE;
	}

	@Override
	public boolean isLeftSuper() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRightSuper() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAnySuper() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public InputType getType() {
		return InputType.JOYSTICK;
	}

	@Override
	public boolean equalsEventBasic(KeyEventInterface e) {
		return false;
	}



}
