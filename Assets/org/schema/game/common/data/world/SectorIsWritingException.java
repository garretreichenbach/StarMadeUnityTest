package org.schema.game.common.data.world;

public class SectorIsWritingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	public final Sector sector;

	public SectorIsWritingException(Sector sector) {
		super(sector.toString());
		this.sector = sector;
	}

}
