package org.schema.game.common.staremote.gui.player;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.staremote.Staremote;
import org.schema.game.network.ReceivedPlayer;
import org.schema.schine.common.language.Lng;

public class StarmotePlayerPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private StarmotePlayerList model;
	private JList list;
	private StarmoteOfflinePlayerList offlineModel;
	/**
	 * Create the panel.
	 */
	public StarmotePlayerPanel(GameClientState state, final Staremote starmote) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{120, 0};
		gridBagLayout.rowHeights = new int[]{25, 0};
		gridBagLayout.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.weighty = 1.0;
		gbc_tabbedPane.weightx = 1.0;
		gbc_tabbedPane.anchor = GridBagConstraints.NORTHWEST;
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 0;
		add(tabbedPane, gbc_tabbedPane);

		final JSplitPane splitPane = new JSplitPane();
		tabbedPane.addTab(Lng.str("Online"), null, splitPane, null);
		splitPane.setDividerSize(3);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setMinimumSize(new Dimension(100, 23));
		splitPane.setLeftComponent(scrollPane);
		list = new JList(model = new StarmotePlayerList(state));
		splitPane.setRightComponent(new JPanel());
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (list.getSelectedIndex() >= 0) {
					PlayerState elementAt = (PlayerState) list.getModel().getElementAt(list.getSelectedIndex());

					StarmotePlayerSettingPanel p = new StarmotePlayerSettingPanel(elementAt);

					System.err.println("VALUE CHANGED: " + elementAt);

					splitPane.setRightComponent(new JScrollPane(p));
					Staremote.currentlyVisiblePanel = p;

				}
			}
		});
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		list.addListSelectionListener(arg0 -> {

		});
		list.setCellRenderer(new StarmotePlayerListCellRenderer());
		scrollPane.setViewportView(list);

		JLabel lblPlayers = new JLabel("Players");
		scrollPane.setColumnHeaderView(lblPlayers);
		splitPane.setDividerLocation(130);

		final JSplitPane splitPane_allplayers = new JSplitPane();
		splitPane_allplayers.setPreferredSize(new Dimension(130, 25));
		splitPane_allplayers.setMinimumSize(new Dimension(100, 25));
		tabbedPane.addTab(Lng.str("All"), null, splitPane_allplayers, null);

		JPanel panel = new JPanel();
		splitPane_allplayers.setLeftComponent(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		offlineModel = new StarmoteOfflinePlayerList(state);

		JButton btnRequest = new JButton("Request");
		btnRequest.addActionListener(arg0 -> starmote.requestAllPlayers(offlineModel));
		GridBagConstraints gbc_btnRequest = new GridBagConstraints();
		gbc_btnRequest.insets = new Insets(0, 0, 5, 0);
		gbc_btnRequest.gridx = 0;
		gbc_btnRequest.gridy = 0;
		panel.add(btnRequest, gbc_btnRequest);

		JScrollPane scrollPane_1 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 1;
		panel.add(scrollPane_1, gbc_scrollPane_1);

		final JList list_1 = new JList(offlineModel);
		list_1.addListSelectionListener(arg0 -> {
			Object selectedValue = list_1.getSelectedValue();
			if (selectedValue != null) {
				splitPane_allplayers.setRightComponent(new JScrollPane(new StarmoteOfflinePlayerDetailsPanel((ReceivedPlayer) selectedValue)));
			}
		});
		scrollPane_1.setViewportView(list_1);

		JPanel panel_1 = new JPanel();
		splitPane_allplayers.setRightComponent(panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0};
		gbl_panel_1.rowHeights = new int[]{0};
		gbl_panel_1.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);

	}

//	@Override
//	public void update(Observable o, Object arg) {
//		if (model != null) {
//			model.recalcList();
//		}
//	}

}
