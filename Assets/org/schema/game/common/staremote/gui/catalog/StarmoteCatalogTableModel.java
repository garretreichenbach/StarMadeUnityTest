package org.schema.game.common.staremote.gui.catalog;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.catalog.CatalogPermission;

public class StarmoteCatalogTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private final ArrayList<StarmoteCatalogEntry> list = new ArrayList<StarmoteCatalogEntry>();

	//	/* (non-Javadoc)
	//	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	//	 */
	@Override
	public String getColumnName(int column) {
		return switch(column) {
			case 0 -> "Name";
			case 1 -> "Owner";
			case 2 -> "Price";
			case 3 -> "Rating";
			case 4 -> "Description";
			case 5 -> "Date";
			case 6 -> "# Spawned";
			case 7 -> "Enemy ACL";
			case 8 -> "Faction ACL";
			case 9 -> "Homebase ACL";
			case 10 -> "Others ACL";
			case 11 -> "Options";
			default -> "-";
		};
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		//		if (columnIndex == 0) {
		//			return String.class;
		//		} else {
		return StarmoteCatalogEntry.class;
		//		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex >= getColumnCount() - 5;
	}

	/**
	 * @return the list
	 */
	public ArrayList<StarmoteCatalogEntry> getList() {
		return list;
	}

	@Override
	public int getRowCount() {
		return list.size();
	}

	@Override
	public int getColumnCount() {
		return 12;
	}

	@Override
	public Object getValueAt(int x, int y) {
		StarmoteCatalogEntry starmoteCatalogEntry = list.get(x);
		assert (starmoteCatalogEntry != null);
		return starmoteCatalogEntry;
	}

	public void rebuild(final GameClientState state) {
		

		EventQueue.invokeLater(() -> {
			list.clear();
			synchronized(state){
				final List<CatalogPermission> allCatalog = state.getPlayer().getCatalog().getAllCatalog();
				final ArrayList<CatalogPermission> c = new ArrayList<CatalogPermission>();
				c.addAll(allCatalog);
				for (int i = 0; i < c.size(); i++) {
					StarmoteCatalogEntry e = new StarmoteCatalogEntry(c.get(i), state, StarmoteCatalogTableModel.this);
					list.add(e);
				}
			}
			Collections.sort(list);
			fireTableDataChanged();

		});

	}

	public void setAutoWidth(JTable table) {

		{
			TableColumn column = table.getColumn("Rating");
			column.setPreferredWidth(20);
		}
		{
			TableColumn column = table.getColumn("# Spawned");
			column.setPreferredWidth(20);
		}
		{
			TableColumn column = table.getColumn("Enemy ACL");
			column.setPreferredWidth(20);
		}
		{
			TableColumn column = table.getColumn("Faction ACL");
			column.setPreferredWidth(20);
		}
		{
			TableColumn column = table.getColumn("Homebase ACL");
			column.setPreferredWidth(20);
		}
		{
			TableColumn column = table.getColumn("Others ACL");
			column.setPreferredWidth(20);
		}
		{
			TableColumn column = table.getColumn("Options");
			column.setPreferredWidth(50);
		}

	}

	public void updateElements() {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).update()) {
				fireTableCellUpdated(i, 1);
			}
		}

	}

}
