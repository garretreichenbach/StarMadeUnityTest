package org.schema.game.common.staremote.gui.entity;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.schema.game.client.controller.SendableAddedRemovedListener;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.staremote.Staremote;
import org.schema.schine.network.objects.Sendable;

public class StarmoteEntityPanel extends JPanel implements SendableAddedRemovedListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private StarmoteEntityList model;
	private JList list;
	/**
	 * Create the panel.
	 */
	public StarmoteEntityPanel(GameClientState state) {
		state.getController().addSendableAddedRemovedListener(this);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{133, 0};
		gridBagLayout.rowHeights = new int[]{25, 0};
		gridBagLayout.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		final JSplitPane splitPane = new JSplitPane();
		splitPane.setDividerSize(3);
		GridBagConstraints gbc_splitPane = new GridBagConstraints();
		gbc_splitPane.weighty = 1.0;
		gbc_splitPane.weightx = 1.0;
		gbc_splitPane.fill = GridBagConstraints.BOTH;
		gbc_splitPane.anchor = GridBagConstraints.NORTHWEST;
		gbc_splitPane.gridx = 0;
		gbc_splitPane.gridy = 0;
		add(splitPane, gbc_splitPane);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setMinimumSize(new Dimension(250, 23));
		splitPane.setLeftComponent(scrollPane);
		model = new StarmoteEntityList(state);
		list = new JList(model);
		splitPane.setRightComponent(new JPanel());
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (list.getSelectedIndex() >= 0) {
					Sendable elementAt = (Sendable) list.getModel().getElementAt(list.getSelectedIndex());

					StarmoteEntitySettingsPanel p = new StarmoteEntitySettingsPanel(elementAt);

					System.err.println("VALUE CHANGED: " + elementAt);

					splitPane.setRightComponent(new JScrollPane(p));
					Staremote.currentlyVisiblePanel = p;

				}
			}
		});
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		list.addListSelectionListener(arg0 -> {

		});
		list.setCellRenderer(new StarmoteEntityListCellRenderer());
		scrollPane.setViewportView(list);

		JLabel lblPlayers = new JLabel("Players");
		scrollPane.setColumnHeaderView(lblPlayers);
		splitPane.setDividerLocation(250);
	}


	@Override
	public void onAddedSendable(Sendable s) {
		if (model != null) {
			model.recalcList();
		}		
	}

	@Override
	public void onRemovedSendable(Sendable s) {
		if (model != null) {
			model.recalcList();
		}		
	}

}
