package org.schema.game.common.data.cubatoms;

public enum CubatomFlavor {

	NEGATIVE(CubatomState.MASS, false),
	LIGHT(CubatomState.MASS, false),
	MEDIUM(CubatomState.MASS, false),
	HEAVY(CubatomState.MASS, true),

	ANTI_SPIN(CubatomState.SPINNING, false),
	NULL_SPIN(CubatomState.SPINNING, false),
	BASE_SPIN(CubatomState.SPINNING, false),
	SUPER_SPIN(CubatomState.SPINNING, true),

	SOLID(CubatomState.THERMAL, false),
	LIQUID(CubatomState.THERMAL, false),
	GAS(CubatomState.THERMAL, false),
	PLASMA(CubatomState.THERMAL, true),

	DRAINING(CubatomState.CONDUCTIVITY, false),
	INSULATOR(CubatomState.CONDUCTIVITY, false),
	CONDUCTIVE(CubatomState.CONDUCTIVITY, false),
	SUPER_CONDUCTIVE(CubatomState.CONDUCTIVITY, true),;

	private final CubatomState state;
	private final boolean special;

	private CubatomFlavor(CubatomState state, boolean special) {
		this.state = state;
		this.special = special;
	}

	/**
	 * @return the state
	 */
	public CubatomState getState() {
		return state;
	}

	public boolean isSpecial() {
		return special;
	}

}
