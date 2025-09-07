package org.schema.game.common.staremote.gui.settings;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class StarmoteSettingsTableCellRenderer extends JLabel implements TableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
	                                               boolean isSelected, boolean hasFocus, int row, int column) {
		StarmoteSettingElement s = (StarmoteSettingElement) value;
		if (column == 0) {
			this.setText(s.getValueName());
			return this;
		} else {
			return s.getComponent();
		}
	}

}
