package org.schema.game.common.controller.damage;

import org.schema.schine.common.language.Lng;

public enum DamageDealerType {
	PROJECTILE,
	MISSILE,
	PULSE,
	BEAM,
	EXPLOSIVE,
	GENERAL;

	public String getName() {
		switch(this) {
		case BEAM:
			return Lng.str("Beam");
		case EXPLOSIVE:
			return Lng.str("Explosive");
		case GENERAL:
			return Lng.str("General");
		case MISSILE:
			return Lng.str("Missile");
		case PROJECTILE:
			return Lng.str("Projectile");
		case PULSE:
			return Lng.str("Pulse");
		default:
			break;
		
		}
		return "DamageDealerType(N/A)";
	} 
}
