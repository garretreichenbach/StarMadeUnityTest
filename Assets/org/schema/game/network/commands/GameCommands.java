package org.schema.game.network.commands;

import java.util.List;

import org.schema.schine.network.common.commands.Command;
import org.schema.schine.network.common.commands.CommandFactory;
import org.schema.schine.network.common.commands.CommandHandler;
import org.schema.schine.network.common.commands.CommandPackage;
import org.schema.schine.network.common.commands.Commandable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;


public enum GameCommands implements Commandable {
	
	ADMIN(30, new CommandFactory<AdminCommandCommandPackage>() {
		@Override public AdminCommandCommandPackage getNewPackage() { return new AdminCommandCommandPackage(); }
		@Override public boolean hasTarget() { return true; }
		@Override public AdminCommandCommandHandler instantiateHandler() { return new AdminCommandCommandHandler(); }
	}),
	;
	static {
		values()[0].addAllNetworkCommandables();
	}
	private static boolean init;
	private final int id;
	private final CommandFactory<?> fac;
	private final Command command;
	private final CommandHandler<?> handler;
	private final List<CommandPackage> packagePool = new ObjectArrayList<CommandPackage>();
	
	private GameCommands(int id, CommandFactory<?> fac) {
		this.id = id;
		this.fac = fac;
		this.command = new Command(this);
		this.handler = fac.hasTarget() ? fac.instantiateHandler() : null;
	}

	@Override
	public Command getCommand() {
		return command;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public CommandFactory<?> getFac() {
		return fac;
	}

	@Override
	public CommandHandler<?> getHandler() {
		assert(fac.hasTarget()):this;
		return handler;
	}

	
	
	@Override
	public CommandPackage getNewPackage() {
		assert(fac.hasTarget()):this;
		synchronized(packagePool) {
			if(!packagePool.isEmpty()) {
				return packagePool.remove(packagePool.size()-1); 
			}
		}
		CommandPackage p = fac.getNewPackage();
		p.pooled = true;
		return p;
	}

	@Override
	public void freePackage(CommandPackage commandPackage) {
		if(commandPackage.pooled) {
			assert(fac.hasTarget()):this;
			commandPackage.reset();
			synchronized(packagePool) {
				packagePool.add(commandPackage);
			}
		}
	}
	public static void addAllCommands() {
		if(!init) {
			Command.addCommands(values());
			init = true;;
		}
	}
	@Override
	public void addAllNetworkCommandables() {
		//set the available commands for the network engine
		addAllCommands();		
	}
}
