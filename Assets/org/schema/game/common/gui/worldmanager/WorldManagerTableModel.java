package org.schema.game.common.gui.worldmanager;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

public class WorldManagerTableModel extends AbstractTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	

	private Vector<WorldInfo> infos = new Vector<WorldInfo>();

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		if (column == 0) {
			return "Name";
		}
		if (column == 1) {
			return "Default";
		}
		if (column == 2) {
			return "Location";
		}

		return "unknown";
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int column) {
		if (column == 0) {
			return String.class;
		}
		if (column == 1) {
			return String.class;
		}
		if (column == 2) {
			return String.class;
		}

		return super.getColumnClass(column);
	}

	@Override
	public int getRowCount() {
		return infos.size();
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public Object getValueAt(int x, int y) {

		WorldInfo serverInfo = infos.get(x);

		if (y == 0) {
			return serverInfo.name;
		}
		if (y == 1) {
			return serverInfo.isDefault();
		}
		if (y == 2) {
			return serverInfo.path;
		}

		return "-";

	}

	public int getVersionColumn() {
		return 3;
	}

	public void clear() {
		infos.clear();
		fireTableDataChanged();
	}

	public void update(WorldInfo info) {

		infos.add(info);

		fireTableDataChanged();

	}

	public TableRowSorter<WorldManagerTableModel> getSorter() {
		TableRowSorter<WorldManagerTableModel> rowSorter = new TableRowSorter<WorldManagerTableModel>(this) {

		};
		rowSorter.setComparator(0, (Comparator<String>) String::compareTo);
		rowSorter.setComparator(1, (Comparator<String>) String::compareTo);
		rowSorter.setComparator(2, (Comparator<String>) String::compareTo);

		return rowSorter;
	}

	public void addAll(Collection<WorldInfo> a) {
		infos.addAll(a);
		fireTableDataChanged();
	}

	public void replaceAll(List<WorldInfo> worldInfos) {
		infos.clear();
		addAll(worldInfos);
	}

}
