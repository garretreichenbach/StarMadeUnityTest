package org.schema.schine.network.common.commands;


public class VoidCommandFactory {
	public final static CommandFactory<VoidCommandPackage> INSTANCE = new CommandFactory<VoidCommandPackage>() {
		@Override public VoidCommandPackage getNewPackage() { throw new RuntimeException("no target"); }
		@Override public boolean hasTarget() { return false; }
		@Override public VoidCommandHandler instantiateHandler() { throw new RuntimeException("no target"); }
	};
}
