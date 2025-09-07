package org.schema.game.common.controller.ai;

import org.schema.schine.common.language.Lng;
import org.schema.schine.common.language.Translatable;

public enum Types {

	AIM_AT(en -> Lng.str("Aim at: ")),
	TYPE(en -> Lng.str("Type: ")),
	ACTIVE(en -> Lng.str("Active")),
	OWNER,
	AGGRESIVENESS,
	FEAR,

	ORIGIN_X,
	ORIGIN_Y,
	ORIGIN_Z,

	ROAM_X,
	ROAM_Y,
	ROAM_Z,
	ORDER,
	ATTACK_TARGET,
	FOLLOW_TARGET,

	TARGET_X,
	TARGET_Y,
	TARGET_Z,

	TARGET_AFFINITY,
	MANUAL(en -> Lng.str("Remote Control")),
	PRIORIZATION(en -> Lng.str("Target Prioritization")),
	FIRE_MODE(en -> Lng.str("Fire Mode (Canon/Missile)")),
	;

	private Translatable description;

	private Types(Translatable description) {
		this.description = description;
	}
	
	private Types(){
		this.description = Translatable.DEFAULT;
	}
	
	public String getDescription(){
		return description.getName(this);
	}
	
	
}
