package org.schema.game.common.staremote.gui.catalog;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class StarmoteCatalogTableCellRenderer implements TableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
	                                               boolean isSelected, boolean hasFocus, int row, int column) {
		StarmoteCatalogEntry s = (StarmoteCatalogEntry) value;
		Component c = s.getComponent(column, table);
		assert (c != null);
		Color color = s.getColor(column);
		if (color != null) {
			c.setForeground(color.darker());
		} else {
			//			c.setBackground(UIManager.getColor("Table.alternateRowColor"));
			//			c.setBackground(Color.BLUE);
			//			c.setForeground(Color.BLUE);
		}

		return c;
	}

}
