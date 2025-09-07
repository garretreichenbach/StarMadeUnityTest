package org.schema.schine.network.commands;

import org.schema.schine.network.common.commands.Commandable;




public class SynchronizeAllCommandPackage extends SynchronizeCommandPackage{

	@Override
	public Commandable getType() {
		return BasicCommands.SYNCHRONIZE_ALL;
	}
}
