package org.schema.game.common.data.player.playermessage;

import java.sql.SQLException;

import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.PlayerNotFountException;

public class ServerPlayerMessager {

	private final GameServerState state;

	public ServerPlayerMessager(GameServerState state) {
		super();
		this.state = state;
	}

	public void send(String from, String to, String topic, String message) {
		if(to != null && to.trim().length() > 0){
			try {
				PlayerState p = state.getPlayerFromName(to);
				if(p.getClientChannel() != null){
					p.getClientChannel().getPlayerMessageController().serverSend(from, to, topic, message);
				}
	
			} catch (PlayerNotFountException e) {
				try {
					System.err.println("[SERVER] Message to " + to + " couldnt be delivered and will be stored in the database");
					state.getDatabaseIndex().getTableManager().getPlayerMessagesTable().updateOrInsertMessage(PlayerMessageController.getNew(from, to, topic, message));
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
}
