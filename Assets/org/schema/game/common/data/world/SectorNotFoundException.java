package org.schema.game.common.data.world;

public class SectorNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	

	public SectorNotFoundException(int sectorId) {
		super("SECTOR: " + String.valueOf(sectorId));
	}

}
