package org.schema.game.common.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.schema.game.common.crashreporter.CrashReporter;
import org.schema.schine.network.ServerClientChangeListener;
import org.schema.schine.network.server.ServerController;
import org.schema.schine.network.server.ServerState;

public class ServerMainPanel extends JPanel implements ServerClientChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private ServerClientsInfoPanel serverClientsInfoPanel;
	private ServerController serverController;

	/**
	 * Create the panel.
	 *
	 * @param serverController
	 */
	public ServerMainPanel(ServerController serverController) {
		this.serverController = serverController;
		serverController.clientChangeListeners.add(this);
		setBorder(new TitledBorder(null, "Main Server Control", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(200, 200));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.anchor = GridBagConstraints.NORTHWEST;
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.weighty = 1.0;
		gbc_panel.weightx = 1.0;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Running Servers", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.anchor = GridBagConstraints.NORTHWEST;
		gbc_panel_1.weighty = 1.0;
		gbc_panel_1.weightx = 1.0;
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
		gbc_scrollPane.anchor = GridBagConstraints.NORTHWEST;
		gbc_scrollPane.weighty = 1.0;
		gbc_scrollPane.weightx = 1.0;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		panel_1.add(scrollPane, gbc_scrollPane);

		serverClientsInfoPanel = new ServerClientsInfoPanel(serverController);
		scrollPane.setViewportView(serverClientsInfoPanel);

		JPanel mainButtonsPanel = new JPanel();
		GridBagConstraints gbc_mainButtonsPanel = new GridBagConstraints();
		gbc_mainButtonsPanel.anchor = GridBagConstraints.SOUTHEAST;
		gbc_mainButtonsPanel.gridwidth = 0;
		gbc_mainButtonsPanel.gridheight = 0;
		gbc_mainButtonsPanel.gridx = 0;
		gbc_mainButtonsPanel.gridy = 1;
		add(mainButtonsPanel, gbc_mainButtonsPanel);
		mainButtonsPanel.setLayout(new GridLayout(0, 1, 0, 0));

		JButton btnShutDownServer = new JButton("Shut Down Server");
		btnShutDownServer.addActionListener(e -> {
			try {
				CrashReporter.createThreadDump();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			ServerState.setFlagShutdown(true);
		});
		btnShutDownServer.setHorizontalAlignment(SwingConstants.LEFT);
		mainButtonsPanel.add(btnShutDownServer);

	}



	@Override
	public void onClientsChanged() {
		serverClientsInfoPanel.update(serverController);		
	}

}
