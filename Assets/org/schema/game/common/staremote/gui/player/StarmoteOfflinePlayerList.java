package org.schema.game.common.staremote.gui.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.AbstractListModel;

import org.schema.game.client.data.GameClientState;
import org.schema.game.network.ReceivedPlayer;
import org.schema.game.network.StarMadePlayerStats;

public class StarmoteOfflinePlayerList extends AbstractListModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private ArrayList<ReceivedPlayer> players = new ArrayList<ReceivedPlayer>();
	public StarmoteOfflinePlayerList(GameClientState state) {
	}

	@Override
	public int getSize() {
		return players.size();
	}

	@Override
	public Object getElementAt(int index) {
		return players.get(index);
	}

	public void update(StarMadePlayerStats requestPlayerStats) {
		players.clear();
		for (int i = 0; i < requestPlayerStats.receivedPlayers.length; i++) {
			players.add(requestPlayerStats.receivedPlayers[i]);
		}
		System.err.println("[Starmote] All-Players Request has been awnsered " + players);
		Collections.sort(players, (o1, o2) -> o1.name.compareTo(o2.name));
		fireContentsChanged(this, 0, players.size());
	}

}
