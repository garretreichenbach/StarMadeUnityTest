package org.schema.game.common.data.player;

public class CrewFleetRequest {

	public static final byte MODE_ADD = 0;
	public static final byte MODE_REMOVE = 1;
	public static final byte TYPE_CREW = 0;
	public static final byte TYPE_FLEET = 1;
	public String ai;
	public byte mode;
	public byte type;

	public CrewFleetRequest() {
	}
	public CrewFleetRequest(String ai, byte mode, byte type) {
		this.mode = mode;
		this.ai = ai;
		this.type = type;
	}
}
