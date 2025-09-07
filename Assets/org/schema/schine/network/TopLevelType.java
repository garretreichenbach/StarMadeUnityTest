package org.schema.schine.network;

import org.schema.schine.common.language.Lng;

public enum TopLevelType {
	GENERAL(false),
	SEGMENT_CONTROLLER(true),
	PLAYER(true),
	ASTRONAUT(false), 
	SEG_PROVIDER(false), 
	OTHER_SPACE(false), 
	SECTOR(true), 
	SPACE_CREATURE(false), 
	FACTION(true);

	private final boolean hasRules;

	private TopLevelType(boolean hasRules) {
		this.hasRules = hasRules;
	}
	public String getName() {
		return switch(this) {
			case ASTRONAUT -> Lng.str("Astronaut");
			case GENERAL -> Lng.str("General");
			case OTHER_SPACE -> Lng.str("Other Space Object");
			case PLAYER -> Lng.str("Player");
			case SECTOR -> Lng.str("Sector");
			case SEGMENT_CONTROLLER -> Lng.str("Entity");
			case SEG_PROVIDER -> Lng.str("SegProvider");
			case SPACE_CREATURE -> Lng.str("Space Creature");
			case FACTION -> Lng.str("Faction");
			default -> "UNKNOWN_TOPLEVEL_TYPE";
		};
		
	}

	public boolean hasRules() {
		return hasRules;
	}
}
