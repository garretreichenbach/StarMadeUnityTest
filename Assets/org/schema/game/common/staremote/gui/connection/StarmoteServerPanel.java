package org.schema.game.common.staremote.gui.connection;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.schema.common.ListAction;
import org.schema.game.common.gui.LoginDialog;
import org.schema.game.common.staremote.Staremote;

public class StarmoteServerPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Boolean started = false;
	/**
	 *
	 */
	private Object o = new Object();
	private JButton btnRemoveConnection;
	private JButton btnEditConnection;

	public StarmoteServerPanel(final JFrame frame, final Staremote starmote) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.weighty = 5.0;
		gbc_panel.weightx = 1.0;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.weightx = 1.0;
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 0;
		panel.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0};
		gbl_panel_1.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		panel_1.add(scrollPane, gbc_scrollPane);

		final StarmodeConnectionListModel model = new StarmodeConnectionListModel();
		final JList list = new JList(model);

		scrollPane.setViewportView(list);

		JPanel panel_2 = new JPanel();
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.weightx = 0.0010;
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 1;
		gbc_panel_2.gridy = 0;
		panel.add(panel_2, gbc_panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[]{0};
		gbl_panel_2.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel_2.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_panel_2.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_2.setLayout(gbl_panel_2);

		final JButton btnConnect = new JButton("Connect");
		btnConnect.setEnabled(false);
		btnConnect.addActionListener(e -> {
			Object selectedValue = list.getSelectedValue();
			if (selectedValue != null) {
				StarmoteConnection c = (StarmoteConnection) selectedValue;
				frame.dispose();
				starmote.connect(c);
			}
		});
		GridBagConstraints gbc_btnConnect = new GridBagConstraints();
		gbc_btnConnect.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnConnect.anchor = GridBagConstraints.EAST;
		gbc_btnConnect.insets = new Insets(0, 0, 5, 0);
		gbc_btnConnect.gridx = 0;
		gbc_btnConnect.gridy = 0;
		panel_2.add(btnConnect, gbc_btnConnect);

		JButton btnAddConnection = new JButton("Add Connection");
		btnAddConnection.addActionListener(e -> (new StarmoteAddConnectionDialog(frame, model, null)).setVisible(true));
		GridBagConstraints gbc_btnAddConnection = new GridBagConstraints();
		gbc_btnAddConnection.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnAddConnection.anchor = GridBagConstraints.EAST;
		gbc_btnAddConnection.insets = new Insets(0, 0, 5, 0);
		gbc_btnAddConnection.gridx = 0;
		gbc_btnAddConnection.gridy = 2;
		panel_2.add(btnAddConnection, gbc_btnAddConnection);

		btnEditConnection = new JButton("Edit Connection ");
		btnEditConnection.addActionListener(e -> {
			Object selectedValue = list.getSelectedValue();
			if (selectedValue != null) {
				StarmoteConnection c = (StarmoteConnection) selectedValue;
				btnConnect.setEnabled(false);
				btnRemoveConnection.setEnabled(false);
				btnEditConnection.setEnabled(false);
				(new StarmoteAddConnectionDialog(frame, model, c)).setVisible(true);
			}
		});
		btnEditConnection.setEnabled(false);
		GridBagConstraints gbc_btnEditConnection = new GridBagConstraints();
		gbc_btnEditConnection.insets = new Insets(0, 0, 5, 0);
		gbc_btnEditConnection.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnEditConnection.anchor = GridBagConstraints.EAST;
		gbc_btnEditConnection.gridx = 0;
		gbc_btnEditConnection.gridy = 3;
		panel_2.add(btnEditConnection, gbc_btnEditConnection);

		btnRemoveConnection = new JButton("Remove Connection");
		btnRemoveConnection.setEnabled(false);
		btnRemoveConnection.addActionListener(e -> {
			Object selectedValue = list.getSelectedValue();
			if (selectedValue != null) {
				btnConnect.setEnabled(false);
				btnRemoveConnection.setEnabled(false);
				btnEditConnection.setEnabled(false);

				StarmoteConnection c = (StarmoteConnection) selectedValue;
				model.remove(c);
			}
		});

		JButton btnUplinkSettings = new JButton("Uplink Settings");
		btnUplinkSettings.addActionListener(e -> {
			LoginDialog d = new LoginDialog(frame);
			d.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			d.setVisible(true);
		});
		GridBagConstraints gbc_btnUplinkSettings = new GridBagConstraints();
		gbc_btnUplinkSettings.anchor = GridBagConstraints.EAST;
		gbc_btnUplinkSettings.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnUplinkSettings.insets = new Insets(0, 0, 5, 0);
		gbc_btnUplinkSettings.gridx = 0;
		gbc_btnUplinkSettings.gridy = 5;
		panel_2.add(btnUplinkSettings, gbc_btnUplinkSettings);
		GridBagConstraints gbc_btnRemoveConnection = new GridBagConstraints();
		gbc_btnRemoveConnection.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnRemoveConnection.anchor = GridBagConstraints.EAST;
		gbc_btnRemoveConnection.gridx = 0;
		gbc_btnRemoveConnection.gridy = 7;
		panel_2.add(btnRemoveConnection, gbc_btnRemoveConnection);

		JPanel panel_3 = new JPanel();
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.weighty = 1.0;
		gbc_panel_3.weightx = 0.2;
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.gridx = 0;
		gbc_panel_3.gridy = 1;
		add(panel_3, gbc_panel_3);
		GridBagLayout gbl_panel_3 = new GridBagLayout();
		gbl_panel_3.columnWidths = new int[]{0, 0};
		gbl_panel_3.rowHeights = new int[]{0, 0};
		gbl_panel_3.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_panel_3.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_3.setLayout(gbl_panel_3);

		JButton btnExit = new JButton("   Exit   ");
		btnExit.addActionListener(arg0 -> frame.dispose());
		GridBagConstraints gbc_btnExit = new GridBagConstraints();
		gbc_btnExit.insets = new Insets(5, 0, 5, 5);
		gbc_btnExit.weightx = 1.0;
		gbc_btnExit.anchor = GridBagConstraints.EAST;
		gbc_btnExit.gridx = 0;
		gbc_btnExit.gridy = 0;
		panel_3.add(btnExit, gbc_btnExit);

		list.addListSelectionListener(arg0 -> {
			btnConnect.setEnabled(list.getSelectedIndex() >= 0);
			btnRemoveConnection.setEnabled(list.getSelectedIndex() >= 0);
			btnEditConnection.setEnabled(list.getSelectedIndex() >= 0);
		});
		list.addMouseListener(new ListAction(list, new Action() {

			@Override
			public Object getValue(String key) {
				return null;
			}			@Override
			public void actionPerformed(ActionEvent e) {
				synchronized (o) {
					if (!started) {
						started = true;
					} else {
						return;
					}
				}
				StarmoteConnection c = (StarmoteConnection) list.getSelectedValue();
				frame.dispose();
				starmote.connect(c);
			}

			@Override
			public void putValue(String key, Object value) {
			}			@Override
			public void addPropertyChangeListener(PropertyChangeListener listener) {

			}



			@Override
			public boolean isEnabled() {
				return false;
			}



			@Override
			public void removePropertyChangeListener(PropertyChangeListener listener) {
			}

			@Override
			public void setEnabled(boolean b) {
			}
		}));

	}
}
