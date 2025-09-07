package org.schema.schine.sound.controller.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.schema.schine.sound.controller.AudioController;

public class AudioManagerMainPanel extends JPanel {

	public AudioEventManagerPanel audioEventManagerPanel;
	public AudioAssetMainPanel audioAssetMainPanel;
	private JTabbedPane tabbedPane;
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenuItem mntmSave;

	/**
	 * Create the panel.
	 */
	public AudioManagerMainPanel() {
		setLayout(new BorderLayout(0, 0));
		
		this.tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane);
		
		this.audioEventManagerPanel = new AudioEventManagerPanel();
		tabbedPane.addTab("Events", null, audioEventManagerPanel, null);
		
		this.audioAssetMainPanel = new AudioAssetMainPanel();
		tabbedPane.addTab("Assets", null, audioAssetMainPanel, null);
		
		menuBar = new JMenuBar();
		add(menuBar, BorderLayout.NORTH);
		
		mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		mntmSave = new JMenuItem("Save");
		mnFile.add(mntmSave);
		
		mntmSave.addActionListener(e -> AudioController.instance.save());
		
		tabbedPane.addChangeListener(e -> {
			audioEventManagerPanel.audioAssetListPanel.onBecomingActive(tabbedPane.getSelectedIndex() == 0);
			audioAssetMainPanel.audioAssetListPanel.onBecomingActive(tabbedPane.getSelectedIndex() == 1);
		});

	}

}
