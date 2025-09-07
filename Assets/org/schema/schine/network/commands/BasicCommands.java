package org.schema.schine.network.commands;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.schine.network.common.commands.*;

import java.util.List;

public enum BasicCommands implements Commandable {

	LOGIN_REQUEST(1, new CommandFactory<LoginRequestCommandPackage>() {
		@Override
		public LoginRequestCommandPackage getNewPackage() {
			return new LoginRequestCommandPackage();
		}

		@Override
		public boolean hasTarget() {
			return true;
		}

		@Override
		public LoginRequestCommandHandler instantiateHandler() {
			return new LoginRequestCommandHandler();
		}
	}),
	LOGIN_ANSWER(2, new CommandFactory<LoginAnswerCommandPackage>() {
		@Override
		public LoginAnswerCommandPackage getNewPackage() {
			return new LoginAnswerCommandPackage();
		}

		@Override
		public boolean hasTarget() {
			return true;
		}

		@Override
		public LoginAnswerCommandHandler instantiateHandler() {
			return new LoginAnswerCommandHandler();
		}
	}),
	REQUEST_SYNCH_ALL(3, new CommandFactory<RequestSynchronizeAllCommandPackage>() {
		@Override
		public RequestSynchronizeAllCommandPackage getNewPackage() {
			return new RequestSynchronizeAllCommandPackage();
		}

		@Override
		public boolean hasTarget() {
			return true;
		}

		@Override
		public RequestSynchronizeAllCommandHandler instantiateHandler() {
			return new RequestSynchronizeAllCommandHandler();
		}
	}),
	//	GAME_PACKAGE(4, new CommandFactory<LoginRequestCommandPackage>() {
//		@Override public LoginRequestCommandPackage getNewPackage() { return new LoginRequestCommandPackage(); }
//		@Override public boolean hasTarget() { return true; }
//		@Override public LoginRequestCommandHandler instantiateHandler() { return new LoginRequestCommandHandler(); }
//	}),
	GAME_REQUEST(5, new CommandFactory<GameRequestCommandPackage>() {
		@Override
		public GameRequestCommandPackage getNewPackage() {
			return new GameRequestCommandPackage();
		}

		@Override
		public boolean hasTarget() {
			return true;
		}

		@Override
		public GameRequestCommandHandler instantiateHandler() {
			return new GameRequestCommandHandler();
		}
	}),
	GAME_ANSWER(6, new CommandFactory<GameRequestAnswerCommandPackage>() {
		@Override
		public GameRequestAnswerCommandPackage getNewPackage() {
			return new GameRequestAnswerCommandPackage();
		}

		@Override
		public boolean hasTarget() {
			return true;
		}

		@Override
		public GameRequestAnswerCommandHandler instantiateHandler() {
			return new GameRequestAnswerCommandHandler();
		}
	}),
	MESSAGE(7, new CommandFactory<MessageCommandPackage>() {
		@Override
		public MessageCommandPackage getNewPackage() {
			return new MessageCommandPackage();
		}

		@Override
		public boolean hasTarget() {
			return true;
		}

		@Override
		public MessageCommandHandler instantiateHandler() {
			return new MessageCommandHandler();
		}
	}),
	SERVER_INFO_REQUEST(8, new CommandFactory<ServerInfoRequestCommandPackage>() {
		@Override
		public ServerInfoRequestCommandPackage getNewPackage() {
			return new ServerInfoRequestCommandPackage();
		}

		@Override
		public boolean hasTarget() {
			return true;
		}

		@Override
		public ServerInfoRequestCommandHandler instantiateHandler() {
			return new ServerInfoRequestCommandHandler();
		}
	}),
	SERVER_INFO_ANSWER(9, new CommandFactory<ServerInfoAnswerCommandPackage>() {
		@Override
		public ServerInfoAnswerCommandPackage getNewPackage() {
			return new ServerInfoAnswerCommandPackage();
		}

		@Override
		public boolean hasTarget() {
			return true;
		}

		@Override
		public ServerInfoAnswerCommandHandler instantiateHandler() {
			return new ServerInfoAnswerCommandHandler();
		}
	}),
	SYNCHRONIZE(10, new CommandFactory<SynchronizePublicCommandPackage>() {
		@Override
		public SynchronizePublicCommandPackage getNewPackage() {
			return new SynchronizePublicCommandPackage();
		}

		@Override
		public boolean hasTarget() {
			return true;
		}

		@Override
		public SynchronizePublicCommandHandler instantiateHandler() {
			return new SynchronizePublicCommandHandler();
		}
	}),
	SYNCHRONIZE_PRIVATE(11, new CommandFactory<SynchronizePrivateCommandPackage>() {
		@Override
		public SynchronizePrivateCommandPackage getNewPackage() {
			return new SynchronizePrivateCommandPackage();
		}

		@Override
		public boolean hasTarget() {
			return true;
		}

		@Override
		public SynchronizePrivateCommandHandler instantiateHandler() {
			return new SynchronizePrivateCommandHandler();
		}
	}),
	SYNCHRONIZE_ALL(12, new CommandFactory<SynchronizeAllCommandPackage>() {
		@Override
		public SynchronizeAllCommandPackage getNewPackage() {
			return new SynchronizeAllCommandPackage();
		}

		@Override
		public boolean hasTarget() {
			return true;
		}

		@Override
		public SynchronizeAllCommandHandler instantiateHandler() {
			return new SynchronizeAllCommandHandler();
		}
	}),

	LOGOUT(100, VoidCommandFactory.INSTANCE), //this command doesn't use packages
	PING(101, VoidCommandFactory.INSTANCE), //this command doesn't use packages
	PONG(102, VoidCommandFactory.INSTANCE),    //this command doesn't use packages
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

	private BasicCommands(int id, CommandFactory<?> fac) {
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
		assert (fac.hasTarget()) : this;
		return handler;
	}

	@Override
	public CommandPackage getNewPackage() {
		assert (fac.hasTarget()) : this;
		synchronized(packagePool) {
			if(!packagePool.isEmpty()) {
				return packagePool.removeLast();
			}
		}
		CommandPackage p = fac.getNewPackage();
		p.pooled = true;
		return p;
	}

	@Override
	public void freePackage(CommandPackage commandPackage) {
		if(commandPackage.pooled) {
			assert (fac.hasTarget()) : this;
			commandPackage.reset();
			synchronized(packagePool) {
				packagePool.add(commandPackage);
			}
		}
	}

	public static void addAllCommands() {
		if(!init) {
			Command.addCommands(values());
			init = true;
			;
		}
	}

	@Override
	public void addAllNetworkCommandables() {
		//set the available commands for the network engine
		addAllCommands();
	}
}
