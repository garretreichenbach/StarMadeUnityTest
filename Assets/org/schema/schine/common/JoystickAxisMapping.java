package org.schema.schine.common;

public enum JoystickAxisMapping {
	PITCH("Ship Pitch (look up/down)"),
	YAW("Ship Yaw (look left/right)"),
	ROLL("Ship Roll"),
	FORWARD_BACK("Ship forward/back"),
	RIGHT_LEFT("Ship right/left"),
	UP_DOWN("Ship up/down"),

	FORWARD_THRUST_AXIS("thrust forward"),
	HOTBAR_AXIS("hotbar"),;

	public final String desc;

	private JoystickAxisMapping(String desc) {
		this.desc = desc;
	}

}
