package org.schema.game.network;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import org.schema.common.SerializationInterface;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.resource.FileExt;
import org.schema.schine.resource.tag.Tag;

public class StarMadePlayerStats implements SerializationInterface{


	public ReceivedPlayer[] receivedPlayers;

	
	
	public static class PlayerStat {
		
	}
	public static StarMadePlayerStats create(GameServerState state) {
		StarMadePlayerStats p = new StarMadePlayerStats();
		File f = new FileExt(GameServerState.ENTITY_DATABASE_PATH);

		File[] listFiles = f.listFiles((arg0, name) -> name.startsWith("ENTITY_PLAYERSTATE"));
		ArrayList<PlayerState> players = new ArrayList<PlayerState>();
		for (int i = 0; i < listFiles.length; i++) {
			try {
				Tag tag = Tag.readFrom(new BufferedInputStream(new FileInputStream(listFiles[i])), true, false);
				PlayerState player = new PlayerState(state);
				player.initialize();
				player.fromTagStructure(tag);
				String fName = listFiles[i].getName();
				player.setName(fName.substring("ENTITY_PLAYERSTATE_".length(), fName.lastIndexOf(".")));
				players.add(player);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		p.receivedPlayers = new ReceivedPlayer[players.size()];
		for (int i = 0; i < players.size(); i++) {

			PlayerState player = players.get(i);

			StringBuilder sb = new StringBuilder();
			
			p.receivedPlayers[i] = ReceivedPlayer.createFrom(player);
			

			

		}
		return p;
	}
	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeInt(receivedPlayers.length);
		for(int i = 0; i < receivedPlayers.length; i++) {
			receivedPlayers[i].serialize(b, isOnServer);
		}
		
	}
	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		receivedPlayers = new ReceivedPlayer[b.readInt()];
		for(int i = 0; i < receivedPlayers.length; i++) {
			receivedPlayers[i] = new ReceivedPlayer();
			receivedPlayers[i].deserialize(b, updateSenderStateId, isOnServer);
		}		
	}
	


}
