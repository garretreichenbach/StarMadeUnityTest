package org.schema.schine.input;

import org.schema.schine.common.language.Lng;
import org.schema.schine.common.language.Translatable;

public enum KeyboardContext {

//	GENERAL(null, 0),
//	CHARACTER_IN_GRAVITY(GENERAL, 1),
//	FREE_IN_SPACE(GENERAL, 1),
//	SHIP_GENERAL(GENERAL, 1),
//	SHIP_FLIGHT_MODE(SHIP_GENERAL, 2),
//	SHIP_BUILD_MODE(SHIP_GENERAL, 2);
	
	GENERAL(null, 0, en -> Lng.str("General")),
	PLAYER(GENERAL, 1, en -> Lng.str("Character Controls")),
	SHIP(GENERAL, 1, en -> Lng.str("Ship Controls")),
	MAP(GENERAL, 1, en -> Lng.str("Map Controls")),
	BUILD(SHIP, 1, en -> Lng.str("Build Controls")),
	TUTORIAL(GENERAL, 1, en -> Lng.str("Ship Controls")),
	DIALOG(null, 0, en -> Lng.str("Ship Controls")),
	;
	private final Translatable description;
	private final KeyboardContext parent;
	private final int lvl;

	KeyboardContext(KeyboardContext parent, int lvl, Translatable description) {
		this.parent = parent;
		this.lvl = lvl;
		this.description = description;
	}
	
	/**
	 * @return the desc
	 */
	public String getDesc() {
		return description.getName(this);
	}

	/**
	 * @return the lvl
	 */
	public int getLvl() {
		return lvl;
	}
	
	public boolean isRoot(){
		return parent == null;
	}
	
	public KeyboardContext getParent(){
		return parent;
	}


}
