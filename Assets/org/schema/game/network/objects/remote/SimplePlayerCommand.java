package org.schema.game.network.objects.remote;

import org.schema.game.common.data.player.SimplePlayerCommands;

public class SimplePlayerCommand extends SimpleCommand<SimplePlayerCommands>{

	public SimplePlayerCommand(SimplePlayerCommands command, Object... args) {
		super(command, args);
	}

	public SimplePlayerCommand() {
	}

	@Override
	protected void checkMatches(SimplePlayerCommands command, Object[] args) {
		command.checkMatches(args);		
	}
}
