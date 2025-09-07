package org.schema.game.server.data;

public interface ServerExecutionJob {
	public boolean execute(GameServerState state);
}
