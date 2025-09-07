package org.schema.schine.sound.controller.gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.schema.schine.sound.controller.FiredAudioEvent;
import org.schema.schine.sound.controller.asset.AudioAsset;

public class AudioManagerFrame extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8898771447418158332L;
	private AudioManagerMainPanel audioManagerMainPanel;

	public AudioManagerFrame() {
		setTitle("AudioManager");
		this.setType(javax.swing.JFrame.Type.NORMAL);
		this.setSize(1600, 900);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.audioManagerMainPanel = new AudioManagerMainPanel();
		getContentPane().add(audioManagerMainPanel, BorderLayout.CENTER);
		
		
		this.audioManagerMainPanel.audioEventManagerPanel.audioEventListPanel.table.getSelectionModel().addListSelectionListener(lse -> {
		    if (!lse.getValueIsAdjusting()) {
			    int row = AudioManagerFrame.this.audioManagerMainPanel.audioEventManagerPanel.audioEventListPanel.table.getSelectedRow();
					if(row < 0) return;
			    FiredAudioEvent a = AudioManagerFrame.this.audioManagerMainPanel.audioEventManagerPanel.audioEventListPanel.model.getEventAtRow(row);
			    AudioManagerFrame.this.audioManagerMainPanel.audioEventManagerPanel.audioEventDetailPanel.setSelected(a);
		    }
		});
		
	}

	
	public void setupAndShow() {
		this.setVisible(true);
	}

	public void onFiredEvent(FiredAudioEvent e) {
		assert(e.entry != null);
		this.audioManagerMainPanel.audioEventManagerPanel.audioEventListPanel.model.onFiredEvent(e);
	}


	public void onSelectedAsset(AudioAsset a) {
		this.audioManagerMainPanel.audioAssetMainPanel.audioAssetDetailPanel.onSelectedAsset(a);
	}
}
