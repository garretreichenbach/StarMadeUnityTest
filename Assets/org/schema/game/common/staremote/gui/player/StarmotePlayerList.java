package org.schema.game.common.staremote.gui.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.AbstractListModel;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.network.objects.Sendable;

public class StarmotePlayerList extends AbstractListModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private ArrayList<PlayerState> players = new ArrayList<PlayerState>();
	private GameClientState state;

	public StarmotePlayerList(GameClientState state) {
		this.state = state;

		recalcList();
	}

	@Override
	public int getSize() {
		return players.size();
	}

	@Override
	public Object getElementAt(int index) {
		try {
			return players.get(index);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Exception";
	}

	public void recalcList() {
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
            	players.clear();
            	synchronized(state){
					for (Sendable s : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
						if (s instanceof PlayerState) {
							players.add((PlayerState) s);
						}
					}
            	}
					Collections.sort(players, (o1, o2) -> o1.getName().compareTo(o2.getName()));
					fireContentsChanged(this, 0, players.size());
            	
            }
		}
		);
	}

}
