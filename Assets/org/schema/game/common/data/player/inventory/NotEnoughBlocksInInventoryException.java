package org.schema.game.common.data.player.inventory;

import org.schema.game.common.data.element.ElementKeyMap;

public class NotEnoughBlocksInInventoryException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	public final short type;
	public final int requested;
	public final int available;

	public NotEnoughBlocksInInventoryException(short arg0, int requested,
	                                           int available) {

		super(ElementKeyMap.exists(arg0) ? ElementKeyMap.getInfo(arg0)
				.getName() : "unknown(" + arg0 + ")" + " wanted " + requested
				+ " but had: " + available);
		this.type = arg0;
		this.requested = requested;
		this.available = available;

	}

}
