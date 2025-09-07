package org.schema.game.common.staremote.gui.faction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import javax.swing.AbstractListModel;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.faction.Faction;

public class StarmoteFactionListModel extends AbstractListModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private final ArrayList<Faction> list = new ArrayList<Faction>();
	private GameClientState state;

	public StarmoteFactionListModel(GameClientState state) {
		this.state = state;
		fireContentsChanged(this, 0, state.getFactionManager().getFactionCollection().size());
	}

	@Override
	public int getSize() {
		return list.size();
	}

	@Override
	public Object getElementAt(int i) {
		return list.get(i);
	}

	public void recalc() {

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
            	list.clear();
            	synchronized(state){
            		list.addAll(state.getFactionManager().getFactionCollection());
		            	}
				Collections.sort(list, (a, b) -> a.getName().toLowerCase(Locale.ENGLISH).compareTo(b.getName().toLowerCase(Locale.ENGLISH)));
		
				fireContentsChanged(this, 0, state.getFactionManager().getFactionCollection().size());
		            }
				}
		);
	}

}
