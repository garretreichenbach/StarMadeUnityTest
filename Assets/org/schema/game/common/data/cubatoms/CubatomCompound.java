package org.schema.game.common.data.cubatoms;

public class CubatomCompound {
	private final CubatomFlavor[] compound;

	public CubatomCompound(CubatomFlavor[] compound) {
		super();
		assert (compound != null);
		if (compound.length == 0 || compound.length > 4) {
			throw new IllegalArgumentException("Cubatom compound must have at least 1 and max 4 states");
		}
		for (int x = 0; x < compound.length; x++) {
			for (int y = 0; y < compound.length; y++) {
				if (x != y && compound[x].getState() == compound[y].getState()) {
					throw new IllegalArgumentException("Cubatom compound may not have 2 flavors of the same state");
				}
			}
		}
		this.compound = compound;
	}

	/**
	 * @return the compound
	 */
	public CubatomFlavor[] getCompound() {
		return compound;
	}

}
