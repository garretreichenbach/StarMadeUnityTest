package org.schema.game.common.staremote.gui.settings;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

public class StarmoteSettingTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private final ArrayList<StarmoteSettingElement> list = new ArrayList<StarmoteSettingElement>();

	@Override
	public String getColumnName(int column) {
		return switch(column) {
			case 0 -> "Setting";
			case 1 -> "Value";
			default -> "-";
		};
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 0) {
			return String.class;
		} else {
			return StarmoteSettingElement.class;
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex > 0 && list.get(rowIndex).isEditable();
	}

	/**
	 * @return the list
	 */
	public ArrayList<StarmoteSettingElement> getList() {
		return list;
	}

	@Override
	public int getRowCount() {
		return list.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(int x, int y) {
		return list.get(x);
	}

	public void updateElements() {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).update()) {
				fireTableCellUpdated(i, 1);
			}
		}

	}

}
