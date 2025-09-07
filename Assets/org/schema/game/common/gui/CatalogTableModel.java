package org.schema.game.common.gui;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.schema.game.client.view.GameResourceLoader;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.data.blueprintnw.BlueprintEntry;

public class CatalogTableModel extends AbstractTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	

	private List<BlueprintEntry> readBluePrints;

	public CatalogTableModel() {
		refreshBluePrints();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		if (column == 0) {
			return "#";
		}
		if (column == 1) {
			return "Name";
		}
		if (column == 2) {
			return "Mass";
		}
		if (column == 3) {
			return "Type";
		}
		if (column == 4) {
			return "Size";
		}

		return "unknown";
	}

	@Override
	public Class<? extends Object> getColumnClass(int column) {
		if (column == 0) {
			return Integer.class;
		}
		if (column == 1) {
			return String.class;
		}
		if (column == 2) {
			return Float.class;
		}
		if (column == 3) {
			return String.class;
		}
		if (column == 4) {
			return String.class;
		}

		return getValueAt(0, column).getClass();
	}

	/**
	 * @return the readBluePrints
	 */
	public List<BlueprintEntry> getReadBluePrints() {
		return readBluePrints;
	}

	/**
	 * @param readBluePrints the readBluePrints to set
	 */
	public void setReadBluePrints(List<BlueprintEntry> readBluePrints) {
		this.readBluePrints = readBluePrints;
	}

	@Override
	public int getRowCount() {
		try {
			return readBluePrints.size();
		} catch (Exception e) {
		}
		return 0;
	}

	@Override
	public int getColumnCount() {
		return 5;
	}

	@Override
	public Object getValueAt(int x, int y) {

		try {
			BlueprintEntry s = readBluePrints.get(x);
			if (y == 0) {
				return x;
			}
			if (y == 1) {
				return String.valueOf(s.getName());
			}
			if (y == 2) {
				return s.getMass();
			}
			if (y == 3) {
				return s.getType().name();
			}
			if (y == 4) {
				return s.getBb().min + " - " + s.getBb().max;
			}
			
			if (y == -1) {
				return readBluePrints.get(x);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "-";

	}

	public void refreshBluePrints() {
		ElementKeyMap.initializeData(GameResourceLoader.getConfigInputFile());
		readBluePrints = BluePrintController.active.readBluePrints();
		fireTableDataChanged();
	}

	public void update() {

		refreshBluePrints();

	}

}
