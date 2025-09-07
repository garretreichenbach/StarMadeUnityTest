package org.schema.schine.network.common.commands;

public interface Commandable {
	public Command getCommand();
	public String name();
	public int getId();
	public CommandFactory<?> getFac();
	public CommandHandler<?> getHandler();
	public CommandPackage getNewPackage();
	public void freePackage(CommandPackage commandPackage);
	
	public void addAllNetworkCommandables();
	
	
	
}
