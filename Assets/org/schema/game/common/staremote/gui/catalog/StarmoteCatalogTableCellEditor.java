package org.schema.game.common.staremote.gui.catalog;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

public class StarmoteCatalogTableCellEditor extends AbstractCellEditor implements TableCellEditor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	

	@Override
	public Object getCellEditorValue() {
		return null;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
	                                             boolean isSelected, int row, int column) {
		StarmoteCatalogEntry s = (StarmoteCatalogEntry) value;
		Component c = s.getComponent(column, table);
		assert (c != null);
		return c;

	}

	//
	//	@Override
	//    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	//        renderer.setComp((Comp) value);
	//        return renderer;
	//    }
	//
	//    @Override
	//    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
	//        editor.setComp((Comp) value);
	//        return editor;
	//    }
	//
	//    @Override
	//    public Object getCellEditorValue() {
	//        return editor.getComp();
	//    }
	//
	@Override
	public boolean isCellEditable(EventObject anEvent) {
		return true;
	}

	@Override
	public boolean shouldSelectCell(EventObject anEvent) {
		return false;
	}

}
