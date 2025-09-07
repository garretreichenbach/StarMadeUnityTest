package org.schema.game.common.staremote.gui.faction;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionPermission;

public class StarmoteFactionTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private final ArrayList<StarmoteFactionMemberEntry> list = new ArrayList<StarmoteFactionMemberEntry>();
	private GameClientState gState;
	private Faction faction;

	public StarmoteFactionTableModel(GameClientState state, Faction faction) {
		this.gState = state;
		this.faction = faction;

		rebuild(state);
	}

	//	/* (non-Javadoc)
	//	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	//	 */
	@Override
	public String getColumnName(int column) {
		return switch(column) {
			case 0 -> "Name";
			case 1 -> "Role";
			case 2 -> "Option";
			default -> "-";
		};
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		//		if (columnIndex == 0) {
		//			return String.class;
		//		} else {
		return StarmoteFactionMemberEntry.class;
		//		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex >= getColumnCount() - 5;
	}

	/**
	 * @return the list
	 */
	public ArrayList<StarmoteFactionMemberEntry> getList() {
		return list;
	}

	@Override
	public int getRowCount() {
		return list.size();
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public Object getValueAt(int x, int y) {
		StarmoteFactionMemberEntry starmoteFactionEntry = list.get(x);
		assert (starmoteFactionEntry != null);
		return starmoteFactionEntry;
	}

	public void rebuild(final GameClientState state) {
		

		EventQueue.invokeLater(() -> {
			synchronized(gState){
				final Collection<FactionPermission> allFaction = faction.getMembersUID().values();
				final ArrayList<FactionPermission> c = new ArrayList<FactionPermission>();
				c.addAll(allFaction);

				list.clear();
				for (int i = 0; i < c.size(); i++) {
					StarmoteFactionMemberEntry e = new StarmoteFactionMemberEntry(c.get(i), faction, state, StarmoteFactionTableModel.this);
					list.add(e);
				}
				Collections.sort(list);
			}
			fireTableDataChanged();

		});

	}

	public void setAutoWidth(JTable table) {

		//		{
		//			TableColumn column = table.getColumn("Rating");
		//			column.setPreferredWidth(20);
		//		}
		//		{
		//			TableColumn column = table.getColumn("# Spawned");
		//			column.setPreferredWidth(20);
		//		}
		//		{
		//			TableColumn column = table.getColumn("Enemy ACL");
		//			column.setPreferredWidth(20);
		//		}
		//		{
		//			TableColumn column = table.getColumn("Faction ACL");
		//			column.setPreferredWidth(20);
		//		}
		//		{
		//			TableColumn column = table.getColumn("Homebase ACL");
		//			column.setPreferredWidth(20);
		//		}
		//		{
		//			TableColumn column = table.getColumn("Others ACL");
		//			column.setPreferredWidth(20);
		//		}
		//		{
		//			TableColumn column = table.getColumn("Options");
		//			column.setPreferredWidth(50);
		//		}

	}

	public void updateElements() {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).update()) {
				fireTableCellUpdated(i, 1);
			}
		}

	}

}
