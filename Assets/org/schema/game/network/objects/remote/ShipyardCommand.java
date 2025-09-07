package org.schema.game.network.objects.remote;

import org.schema.game.common.controller.elements.shipyard.ShipyardCollectionManager.ShipyardCommandType;

public class ShipyardCommand extends SimpleCommand<ShipyardCommandType>{

	public int factionId;

	public ShipyardCommand(int factionId, ShipyardCommandType command, Object... args) {
		super(command, args);
		this.factionId =  factionId;
	}

	public ShipyardCommand() {
	}

	@Override
	protected void checkMatches(ShipyardCommandType command, Object[] args) {
		command.checkMatches(args);		
	}
}
