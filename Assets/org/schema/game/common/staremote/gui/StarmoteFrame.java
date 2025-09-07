package org.schema.game.common.staremote.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.staremote.Staremote;
import org.schema.game.common.staremote.gui.console.StarmoteConsole;
import org.schema.game.network.StarMadeServerStats;

public class StarmoteFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	public static JFrame self;
	private JPanel contentPane;
	private JLabel lblServerstatus;
	private JLabel lblMemory;
	private Staremote starmote;

	/**
	 * Create the frame.
	 */
	public StarmoteFrame(GameClientState state, Staremote mote) {
		self = this;
		this.starmote = mote;
		setTitle("StarMote (StarMade Remote Admin Tool)");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 1024, 768);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{424, 0};
		gbl_contentPane.rowHeights = new int[]{252, 30};
		gbl_contentPane.columnWeights = new double[]{1.0};
		gbl_contentPane.rowWeights = new double[]{5.0, 1.0};
		contentPane.setLayout(gbl_contentPane);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setDividerSize(3);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		GridBagConstraints gbc_splitPane = new GridBagConstraints();
		gbc_splitPane.weighty = 30.0;
		gbc_splitPane.insets = new Insets(0, 0, 5, 0);
		gbc_splitPane.fill = GridBagConstraints.BOTH;
		gbc_splitPane.gridx = 0;
		gbc_splitPane.gridy = 0;
		contentPane.add(splitPane, gbc_splitPane);

		StarmoteMainTabsPanel staremoteMainTabsPanel = new StarmoteMainTabsPanel(state, mote);
		staremoteMainTabsPanel.setPreferredSize(new Dimension(104, 300));
		splitPane.setLeftComponent(staremoteMainTabsPanel);

		StarmoteConsole staremoteConsole = new StarmoteConsole(state);
		splitPane.setRightComponent(staremoteConsole);
		splitPane.setDividerLocation(500);

		JPanel panel = new JPanel();
		panel.setMaximumSize(new Dimension(60, 20));
		panel.setMinimumSize(new Dimension(10, 20));
		panel.setPreferredSize(new Dimension(10, 20));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.weighty = 1.0;
		gbc_panel.anchor = GridBagConstraints.NORTH;
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		contentPane.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		lblServerstatus = new JLabel("ServerStatus: ");
		lblServerstatus.setPreferredSize(new Dimension(180, 14));
		lblServerstatus.setMinimumSize(new Dimension(180, 40));
		lblServerstatus.setMaximumSize(new Dimension(180, 40));
		GridBagConstraints gbc_lblServerstatus = new GridBagConstraints();
		gbc_lblServerstatus.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblServerstatus.weighty = 1.0;
		gbc_lblServerstatus.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblServerstatus.insets = new Insets(0, 5, 3, 0);
		gbc_lblServerstatus.gridx = 0;
		gbc_lblServerstatus.gridy = 0;
		panel.add(lblServerstatus, gbc_lblServerstatus);

		lblMemory = new JLabel("Memory");
		lblMemory.setMinimumSize(new Dimension(38, 180));
		lblMemory.setMaximumSize(new Dimension(38, 180));
		GridBagConstraints gbc_lblMemory = new GridBagConstraints();
		gbc_lblMemory.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblMemory.insets = new Insets(0, 5, 0, 0);
		gbc_lblMemory.weighty = 1.0;
		gbc_lblMemory.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblMemory.gridx = 0;
		gbc_lblMemory.gridy = 1;
		panel.add(lblMemory, gbc_lblMemory);

	}

	/* (non-Javadoc)
	 * @see java.awt.Window#dispose()
	 */
	@Override
	public void dispose() {
		starmote.exit();
		super.dispose();
	}

	public void updateStats(StarMadeServerStats stats) {
		lblServerstatus.setText("SERVER PING: " + stats.ping + " ms");
		lblMemory.setText("SERVER MEMORY: " + (stats.takenMemory / 1024) / 1024 + " / " + (stats.totalMemory / 1024) / 1024 + " MB");
	}

}
