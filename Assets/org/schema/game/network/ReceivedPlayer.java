package org.schema.game.network;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;

import org.schema.common.SerializationInterface;
import org.schema.game.common.data.player.PlayerInfoHistory;
import org.schema.game.common.data.player.PlayerState;

public class ReceivedPlayer implements SerializationInterface{

	public String name;
	public long lastLogin;
	public long lastLogout;
	public PlayerInfoHistory[] ips;


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}


	@Override
	public void serialize(DataOutput b, boolean isOnServer) throws IOException {
		b.writeUTF(name);
		b.writeLong(lastLogin);
		b.writeLong(lastLogout);
		b.writeInt(ips.length);
		for(int i = 0; i < ips.length; i++) {
			ips[i].serialize(b, isOnServer);
		}
	}


	@Override
	public void deserialize(DataInput b, int updateSenderStateId, boolean isOnServer) throws IOException {
		name = b.readUTF();
		lastLogin = b.readLong();
		lastLogout = b.readLong();
		ips = new PlayerInfoHistory[b.readInt()];
		for(int i = 0; i < ips.length; i++) {
			ips[i] = new PlayerInfoHistory();
			ips[i].deserialize(b, updateSenderStateId, isOnServer);
		}
	}


	public static ReceivedPlayer createFrom(PlayerState player) {
		ReceivedPlayer p = new ReceivedPlayer();
		p.name = player.getName();
		p.lastLogin = player.getLastLogin();
		p.lastLogout = player.getLastLogout();
		p.ips = player.getHosts().toArray(new PlayerInfoHistory[player.getHosts().size()]);

		
		ArrayList<PlayerInfoHistory> h = new ArrayList<PlayerInfoHistory>();
		h.addAll(player.getHosts());
		for (int j = 0; j < h.size(); j++) {
			h.get(j);
		}
		return null;
	}

}
