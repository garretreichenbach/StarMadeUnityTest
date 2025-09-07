package org.schema.schine.sound.controller.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

public class AudioEventManagerPanel extends JPanel {

	public AudioEventListPanel audioEventListPanel;
	public AudioEventDetailPanel audioEventDetailPanel;
	AudioAssetListPanel audioAssetListPanel;

	/**
	 * Create the panel.
	 */
	public AudioEventManagerPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 0.0};
		gridBagLayout.rowWeights = new double[]{0.0};
		setLayout(gridBagLayout);
		
		this.audioEventListPanel = new AudioEventListPanel();
		GridBagConstraints gbc_audioEventListPanel = new GridBagConstraints();
		gbc_audioEventListPanel.insets = new Insets(0, 0, 5, 5);
		gbc_audioEventListPanel.anchor = GridBagConstraints.NORTHWEST;
		gbc_audioEventListPanel.fill = GridBagConstraints.BOTH;
		gbc_audioEventListPanel.weighty = 0;
		gbc_audioEventListPanel.weightx = 1;
		gbc_audioEventListPanel.gridx = 0;
		gbc_audioEventListPanel.gridy = 0;
		add(audioEventListPanel, gbc_audioEventListPanel);
		
		this.audioEventDetailPanel = new AudioEventDetailPanel();
		GridBagConstraints gbc_audioEventDetailPanel = new GridBagConstraints();
		gbc_audioEventDetailPanel.insets = new Insets(0, 0, 5, 0);
		gbc_audioEventDetailPanel.weighty = 1.0;
		gbc_audioEventDetailPanel.weightx = 0.0;
		gbc_audioEventDetailPanel.anchor = GridBagConstraints.NORTHWEST;
		gbc_audioEventDetailPanel.fill = GridBagConstraints.BOTH;
		gbc_audioEventDetailPanel.gridx = 1;
		gbc_audioEventDetailPanel.gridy = 0;
		add(audioEventDetailPanel, gbc_audioEventDetailPanel);
		
		this.audioAssetListPanel = new AudioAssetListPanel();
		GridBagConstraints gbc_audioAssetListPanel = new GridBagConstraints();
		gbc_audioAssetListPanel.weightx = 1.0;
		gbc_audioAssetListPanel.insets = new Insets(0, 0, 0, 5);
		gbc_audioAssetListPanel.fill = GridBagConstraints.BOTH;
		gbc_audioAssetListPanel.gridx = 2;
		gbc_audioAssetListPanel.gridy = 0;
		add(audioAssetListPanel, gbc_audioAssetListPanel);
		
		audioAssetListPanel.onButtonDraggable = true;
		
		AudioAssetTreeTransferHandler.draggableToButtonTree = audioAssetListPanel.tree;
		
	}

}
