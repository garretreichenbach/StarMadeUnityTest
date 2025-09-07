package org.schema.game.common.staremote.gui.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.AbstractListModel;

import org.schema.game.client.data.GameClientState;
import org.schema.schine.network.objects.Sendable;

public class StarmoteEntityList extends AbstractListModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private ArrayList<Sendable> list = new ArrayList<Sendable>();
	private GameClientState state;

	public StarmoteEntityList(GameClientState state) {
		this.state = state;

		recalcList();
	}

	@Override
	public int getSize() {
		return list.size();
	}

	@Override
	public Object getElementAt(int index) {
		try {
			return list.get(index);
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void recalcList() {
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
            	list.clear();
            	synchronized(state){
					for (Sendable s : state.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
						list.add(s);
					}
            	}
					Collections.sort(list, (o1, o2) -> o1.getId() - o2.getId());
            	
					fireContentsChanged(this, 0, list.size());
            	
            }
		});
	}

}
