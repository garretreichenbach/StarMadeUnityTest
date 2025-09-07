package org.schema.game.common.facedit.craft;

import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

public class BlockTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public BlockTableModel(JFrame f) {
		super();
	}

	@Override
	public int getRowCount() {
		return ElementKeyMap.typeList() == null ? 0 : ElementKeyMap.typeList().length;
	}

	@Override
	public int getColumnCount() {
		return 5;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		ElementInformation info = ElementKeyMap.getInfo(ElementKeyMap.typeList()[rowIndex]);

		JButton graph = new JButton("Graph");
		graph.addActionListener(e -> {

		});
		return switch(columnIndex) {
			case (0) -> info.getId();
			case (1) -> info.getName();
			case (2) -> ElementInformation.rDesc[info.blockResourceType];
			case (3) -> info.getPrice(false);
			case (4) -> info.dynamicPrice;
			default -> "unknown cell";
		};
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int columnIndex) {
		return switch(columnIndex) {
			case (0) -> "ID";
			case (1) -> "NAME";
			case (2) -> "RESOURCE TYPE";
			case (3) -> "FIX PRICE";
			case (4) -> "DYN PRICE";
			default -> super.getColumnName(columnIndex);
		};
	}

}
