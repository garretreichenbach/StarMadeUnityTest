package org.schema.game.common.staremote.gui.catalog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.schema.game.client.controller.CatalogChangeListener;
import org.schema.game.client.data.GameClientState;

public class StarmoteCatalogPanel extends JPanel implements CatalogChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private StarmoteCatalogTableModel model;
	private GameClientState state;
	private JTable table;

	/**
	 * Create the panel.
	 */
	public StarmoteCatalogPanel(GameClientState state) {
		this.state = state;

		state.getCatalogManager().listeners.add(this);
		state.getPlayer().getCatalog().listeners.add(this);

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		add(scrollPane, gbc_scrollPane);
		model = new StarmoteCatalogTableModel();
		table = new JTable(model);
		table.setDefaultRenderer(StarmoteCatalogEntry.class, new StarmoteCatalogTableCellRenderer());
		table.setDefaultRenderer(String.class, new StarmoteCatalogTableCellRenderer());

		table.setDefaultEditor(StarmoteCatalogEntry.class, new StarmoteCatalogTableCellEditor());
		table.setDefaultEditor(String.class, new StarmoteCatalogTableCellEditor());
		scrollPane.setViewportView(table);
		buildTable();

		model.setAutoWidth(table);
	}

	private void buildTable() {
		table.clearSelection();
		table.requestFocus();
		table.removeAll();

		model.rebuild(state);

		table.repaint();
		table.requestFocus();
		table.invalidate();
		table.validate();
		table.selectAll();
		table.clearSelection();
	}


	@Override
	public void onCatalogChanged() {
		buildTable();		
	}

	//	((GameClientState)getState()).getPlayer().getCatalog().getAllCatalog();

}
