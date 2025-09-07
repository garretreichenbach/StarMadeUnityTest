package org.schema.game.common.data.cubatoms;

public enum CubatomState {

	MASS(new int[]{0, 1, 2, 3}),
	SPINNING(new int[]{4, 5, 6, 7}),
	THERMAL(new int[]{8, 9, 10, 11}),
	CONDUCTIVITY(new int[]{12, 13, 14, 15}),;

	private final int[] flavors;

	private CubatomState(int[] flavors) {
		this.flavors = flavors;
	}

	/**
	 * @return the flavors
	 */
	public CubatomFlavor[] getFlavors() {
		CubatomFlavor[] c = new CubatomFlavor[]{
				CubatomFlavor.values()[flavors[0]],
				CubatomFlavor.values()[flavors[1]],
				CubatomFlavor.values()[flavors[2]],
				CubatomFlavor.values()[flavors[3]],
		};
		return c;
	}

}
