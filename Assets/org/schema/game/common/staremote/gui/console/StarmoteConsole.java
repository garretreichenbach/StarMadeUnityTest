package org.schema.game.common.staremote.gui.console;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

import org.schema.game.client.data.GameClientState;

public class StarmoteConsole extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Create the panel.
	 */
	public StarmoteConsole(GameClientState state) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{220, 10};
		gridBagLayout.rowHeights = new int[]{10, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		StarmoteChatbar staremoteChatbar = new StarmoteChatbar(state);
		GridBagConstraints gbc_staremoteChatbar = new GridBagConstraints();
		gbc_staremoteChatbar.insets = new Insets(0, 0, 5, 0);
		gbc_staremoteChatbar.weightx = 1.0;
		gbc_staremoteChatbar.fill = GridBagConstraints.HORIZONTAL;
		gbc_staremoteChatbar.anchor = GridBagConstraints.NORTHWEST;
		gbc_staremoteChatbar.gridx = 0;
		gbc_staremoteChatbar.gridy = 0;
		add(staremoteChatbar, gbc_staremoteChatbar);

		StarmoteConsoleOutput staremoteConsoleOutput = new StarmoteConsoleOutput(state);
		GridBagConstraints gbc_staremoteConsoleOutput = new GridBagConstraints();
		gbc_staremoteConsoleOutput.fill = GridBagConstraints.BOTH;
		gbc_staremoteConsoleOutput.gridx = 0;
		gbc_staremoteConsoleOutput.gridy = 1;
		add(staremoteConsoleOutput, gbc_staremoteConsoleOutput);
	}

}
