package org.schema.game.common.data.world;

public class SectorNotFoundRuntimeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	

	public SectorNotFoundRuntimeException(int sectorId) {
		super("SECTOR: " + String.valueOf(sectorId));
	}

}
