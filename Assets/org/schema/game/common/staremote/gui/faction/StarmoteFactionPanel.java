package org.schema.game.common.staremote.gui.faction;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import org.schema.game.client.controller.FactionChangeListener;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.staremote.gui.faction.edit.StarmoteFactionAddDialog;
import org.schema.schine.graphicsengine.forms.gui.GUIChangeListener;

public class StarmoteFactionPanel extends JPanel implements GUIChangeListener, FactionChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private StarmoteFactionListModel model;
	private JList list;
	private StarmoteFactionMembersPanel currentMembersPanel;
	/**
	 * Create the panel.
	 */
	public StarmoteFactionPanel(final GameClientState state) {
		state.getFactionManager().listeners.add(this);
		state.getPlayer().getFactionController().addObserver(this);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		final JSplitPane splitPane = new JSplitPane();
		splitPane.setDividerSize(3);

		GridBagConstraints gbc_splitPane = new GridBagConstraints();
		gbc_splitPane.insets = new Insets(0, 0, 5, 0);
		gbc_splitPane.fill = GridBagConstraints.BOTH;
		gbc_splitPane.weighty = 1.0;
		gbc_splitPane.weightx = 1.0;
		gbc_splitPane.fill = GridBagConstraints.BOTH;
		gbc_splitPane.anchor = GridBagConstraints.NORTHWEST;
		gbc_splitPane.gridx = 0;
		gbc_splitPane.gridy = 0;
		add(splitPane, gbc_splitPane);

		model = new StarmoteFactionListModel(state);
		list = new JList(model);
		list.setCellRenderer(new StarmoteFactionListRenderer());
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setMinimumSize(new Dimension(250, 23));
		splitPane.setLeftComponent(scrollPane);
		splitPane.setRightComponent(new JPanel());

		list.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (list.getSelectedIndex() >= 0) {
					Faction faction = (Faction) list.getModel().getElementAt(list.getSelectedIndex());

					currentMembersPanel = new StarmoteFactionMembersPanel(state, faction);

					JSplitPane rightSplit = new JSplitPane();
					rightSplit.setRightComponent(currentMembersPanel);
					rightSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
					splitPane.setRightComponent(rightSplit);
					JScrollPane scroll = new JScrollPane(new StarmoteFactionConfigPanel(state, faction));
					rightSplit.setDividerLocation(280);
					scroll.setPreferredSize(new Dimension(32, 230));
					rightSplit.setLeftComponent(scroll);
					currentMembersPanel.onChange(false);
				}
			}
		});
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JButton btnAddFaction = new JButton("Add Faction");
		btnAddFaction.addActionListener(arg0 -> (new StarmoteFactionAddDialog(state)).setVisible(true));
		btnAddFaction.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_btnAddFaction = new GridBagConstraints();
		gbc_btnAddFaction.anchor = GridBagConstraints.WEST;
		gbc_btnAddFaction.gridx = 0;
		gbc_btnAddFaction.gridy = 1;
		add(btnAddFaction, gbc_btnAddFaction);
	}

	@Override
	public void onChange(boolean updateListDim) {
		model.recalc();
		if (currentMembersPanel != null) {
			currentMembersPanel.onChange(updateListDim);
		}
	}

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
