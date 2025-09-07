package org.schema.game.common.staremote.gui.faction;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.schema.game.client.controller.FactionChangeListener;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.schine.graphicsengine.forms.gui.GUIChangeListener;

public class StarmoteFactionMembersPanel extends JPanel implements GUIChangeListener, FactionChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private StarmoteFactionTableModel model;
	private GameClientState state;
	private JTable table;

	/**
	 * Create the panel.
	 */
	public StarmoteFactionMembersPanel(GameClientState state, Faction faction) {
		this.state = state;

		state.getFactionManager().listeners.add(this);
		state.getPlayer().getFactionController().addObserver(this);

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
		model = new StarmoteFactionTableModel(state, faction);
		table = new JTable(model);
		table.setDefaultRenderer(StarmoteFactionMemberEntry.class, new StarmoteFactionTableCellRenderer());
		table.setDefaultRenderer(String.class, new StarmoteFactionTableCellRenderer());

		table.setDefaultEditor(StarmoteFactionMemberEntry.class, new StarmoteFactionTableCellEditor());
		table.setDefaultEditor(String.class, new StarmoteFactionTableCellEditor());
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
	public void onChange(boolean updateListDim) {
		buildTable();
	}
	//	((GameClientState)getState()).getPlayer().getFaction().getAllFaction();

	@Override
	public void onFactionChanged() {
		onChange(false);		
	}

	@Override
	public void onRelationShipOfferChanged() {
		onChange(false);		
	}

	@Override
	public void onFactionNewsDeleted() {
		onChange(false);		
	}

	@Override
	public void onInvitationsChanged() {
		onChange(false);		
	}

}
