package org.schema.schine.sound.controller.gui;

import javax.swing.JButton;

import org.schema.schine.sound.controller.asset.AudioAsset;

public class AudioAssetDroppableButton extends JButton{



	private AudioEventDetailPanel pan;

	public AudioAssetDroppableButton(AudioEventDetailPanel pan, String text) {
		super(text);
		this.pan = pan;
	}

	public void onDrop(AudioAssetDroppableButton b, AudioAsset a) {
		pan.onDrop(b, a);
	}
	public boolean canDrop() {
		return pan.selected != null;
	}

}
