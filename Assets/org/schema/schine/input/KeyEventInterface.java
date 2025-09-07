package org.schema.schine.input;

import java.util.List;

public interface KeyEventInterface {

	public boolean isPressed();
	public boolean isReleased();
	public boolean isRepeat();

	public int getKey();

	public String getCharacter();

	public boolean isCharacterEvent();
	
	public boolean isLeftShift();
	public boolean isRightShift();
	public boolean isLeftAlt();
	public boolean isRightAlt();
	public boolean isLeftControl();
	public boolean isRightControl();
	public boolean isLeftSuper();
	public boolean isRightSuper();

	public boolean isDebugKey();

	/**
	 * @param action
	 * @return events that have not been triggered but are now active (button released -> button pressed)
	 */
	public List<KeyboardMappings> getTriggeredMappings();

	public boolean isTriggered(KeyboardMappings action);
	
	public void checkTriggeredMappings(InputType type);

	public boolean isInputType(InputType t);
	
	public boolean isSlotKey();
	public int getSlotKey();

	public InputAction generateInputAction();

	public boolean isAnySuper();
	public boolean isAnyControl();
	public boolean isAnyAlt();
	public boolean isAnyShift();

	/**
	 * @param action
	 * @return events that have been triggered but are no longer (button pressed -> button released)
	 */
	public boolean isTriggeredRelease(KeyboardMappings action);
	public boolean isEscapeKeyRaw();
	public int getKeyboardKeyRaw();
	public InputType getType();
	public boolean equalsEventBasic(KeyEventInterface e);
}
