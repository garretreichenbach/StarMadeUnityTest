package org.schema.schine.sound.controller.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

public class AudioAssetMainPanel extends JPanel {

	public AudioAssetListPanel audioAssetListPanel;
	public AudioAssetDetailPanel audioAssetDetailPanel;

	/**
	 * Create the panel.
	 */
	public AudioAssetMainPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0};
		gridBagLayout.columnWeights = new double[]{0, 1};
		gridBagLayout.rowWeights = new double[]{0};
		setLayout(gridBagLayout);
		
		this.audioAssetListPanel = new AudioAssetListPanel();
		GridBagConstraints gbc_audioAssetListPanel = new GridBagConstraints();
		gbc_audioAssetListPanel.weighty = 1.0;
		gbc_audioAssetListPanel.insets = new Insets(0, 0, 5, 0);
		gbc_audioAssetListPanel.fill = GridBagConstraints.BOTH;
		gbc_audioAssetListPanel.gridx = 0;
		gbc_audioAssetListPanel.gridy = 0;
		add(audioAssetListPanel, gbc_audioAssetListPanel);
		
		this.audioAssetDetailPanel = new AudioAssetDetailPanel();
		GridBagConstraints gbc_audioAssetDetailPanel = new GridBagConstraints();
		gbc_audioAssetDetailPanel.weighty = 1.0;
		gbc_audioAssetDetailPanel.weightx = 1.0;
		gbc_audioAssetDetailPanel.fill = GridBagConstraints.BOTH;
		gbc_audioAssetDetailPanel.gridx = 1;
		gbc_audioAssetDetailPanel.gridy = 0;
		add(audioAssetDetailPanel, gbc_audioAssetDetailPanel);

	}

}
