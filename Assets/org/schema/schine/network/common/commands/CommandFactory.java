package org.schema.schine.network.common.commands;

public interface CommandFactory<E extends CommandPackage> {
	public E getNewPackage();

	public boolean hasTarget();

	public CommandHandler<E> instantiateHandler();
}
