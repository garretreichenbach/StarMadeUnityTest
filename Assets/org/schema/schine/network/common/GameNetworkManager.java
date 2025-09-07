package org.schema.schine.network.common;

import java.io.IOException;

import org.schema.schine.network.StateInterface;
import org.schema.schine.network.commands.BasicCommands;
import org.schema.schine.network.common.commands.Commandable;


public class GameNetworkManager extends NetworkManager {

	public GameNetworkManager(StateInterface state) {
		super(state);
	}

	@Override
	public void sendLogout(NetworkProcessor proc) throws IOException {
		BasicCommands.LOGOUT.getCommand().send(proc);
	}

	@Override
	public void sendPing(NetworkProcessor proc) throws IOException {
		BasicCommands.PING.getCommand().send(proc);
	}

	@Override
	public void sendPong(NetworkProcessor proc) throws IOException {
		BasicCommands.PONG.getCommand().send(proc);
	}


	@Override
	public void processReceivedCommandWithoutTarget(NetworkProcessor proc, StateInterface state,
			Commandable commandable) throws IOException {
		if(commandable instanceof BasicCommands) {
			BasicCommands com = (BasicCommands)commandable;
			switch(com) {
				case LOGOUT -> disconnectClient(proc);
				case PING -> receivedPing(proc);
				case PONG -> receivedPong(proc);
				default -> throw new IOException("Unhandled void command");
			}
		}
	}

	

	public void disconnectClient(NetworkProcessor proc) {
		if(!isOnServer()) {
			System.err.println("[CLIENT] Server sent logout. disconnecting socket");
			proc.disconnect();
		}else {
//			Log.warning("Illegal logout command sent to server from (not executed)"+proc);
		}		
	}


	

}
