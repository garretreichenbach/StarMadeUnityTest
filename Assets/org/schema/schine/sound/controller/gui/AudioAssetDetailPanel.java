package org.schema.schine.sound.controller.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import org.schema.schine.sound.controller.MusicTags;
import org.schema.schine.sound.controller.asset.AudioAsset;
import org.schema.schine.sound.controller.asset.AudioAsset.AudioGeneralTag;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class AudioAssetDetailPanel extends JPanel implements Runnable{

	private AudioAsset selectedAsset;
	private JTextField textFieldVolume;
	private JLabel lblNameval;
	private JCheckBox chckbxStreaming;
	private JCheckBox chckbxCachedStreaming;
	private JCheckBox chckbxBasicResourcedloaded;
	private JPanel panelMusicTags;

	/**
	 * Create the panel.
	 */
	public AudioAssetDetailPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblName = new JLabel("Name");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		add(lblName, gbc_lblName);
		
		this.lblNameval = new JLabel("NameVal");
		GridBagConstraints gbc_lblNameval = new GridBagConstraints();
		gbc_lblNameval.insets = new Insets(0, 0, 5, 0);
		gbc_lblNameval.gridx = 1;
		gbc_lblNameval.gridy = 0;
		add(lblNameval, gbc_lblNameval);
		
		JLabel lblGeneralvolume = new JLabel("GeneralVolume");
		GridBagConstraints gbc_lblGeneralvolume = new GridBagConstraints();
		gbc_lblGeneralvolume.insets = new Insets(0, 0, 5, 5);
		gbc_lblGeneralvolume.anchor = GridBagConstraints.EAST;
		gbc_lblGeneralvolume.gridx = 0;
		gbc_lblGeneralvolume.gridy = 1;
		add(lblGeneralvolume, gbc_lblGeneralvolume);
		
		textFieldVolume = new JTextField();
		textFieldVolume.addActionListener(e -> {
			try {
			float t = Float.parseFloat(textFieldVolume.getText());
			textFieldVolume.setBackground(Color.WHITE.brighter());
			selectedAsset.setVolume(t);
			}catch(Exception ex) {
				ex.printStackTrace();
				textFieldVolume.setBackground(Color.RED.brighter());
			}
		});
		GridBagConstraints gbc_textFieldVolume = new GridBagConstraints();
		gbc_textFieldVolume.insets = new Insets(0, 0, 5, 0);
		gbc_textFieldVolume.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldVolume.gridx = 1;
		gbc_textFieldVolume.gridy = 1;
		add(textFieldVolume, gbc_textFieldVolume);
		textFieldVolume.setColumns(10);
		
		this.chckbxStreaming = new JCheckBox("Streaming");
		chckbxStreaming.addActionListener(e -> selectedAsset.getId().setStream(chckbxStreaming.isSelected()));
		GridBagConstraints gbc_chckbxStreaming = new GridBagConstraints();
		gbc_chckbxStreaming.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxStreaming.gridx = 0;
		gbc_chckbxStreaming.gridy = 2;
		add(chckbxStreaming, gbc_chckbxStreaming);
		
		this.chckbxCachedStreaming = new JCheckBox("Cached Streaming");
		chckbxCachedStreaming.addActionListener(e -> selectedAsset.getId().setStreamCache(chckbxCachedStreaming.isSelected()));
		GridBagConstraints gbc_chckbxCachedStreaming = new GridBagConstraints();
		gbc_chckbxCachedStreaming.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxCachedStreaming.anchor = GridBagConstraints.NORTHWEST;
		gbc_chckbxCachedStreaming.gridx = 1;
		gbc_chckbxCachedStreaming.gridy = 2;
		add(chckbxCachedStreaming, gbc_chckbxCachedStreaming);
		
		this.chckbxBasicResourcedloaded = new JCheckBox("Basic Resourced (loaded on startup)");
		chckbxBasicResourcedloaded.addActionListener(e -> {
			if(chckbxBasicResourcedloaded.isSelected()) {
				selectedAsset.generalTag = AudioGeneralTag.BASIC;
			}else {
				selectedAsset.generalTag = AudioGeneralTag.GAME;
			}
		});
		GridBagConstraints gbc_chckbxBasicResourcedloaded = new GridBagConstraints();
		gbc_chckbxBasicResourcedloaded.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxBasicResourcedloaded.anchor = GridBagConstraints.NORTHWEST;
		gbc_chckbxBasicResourcedloaded.gridwidth = 2;
		gbc_chckbxBasicResourcedloaded.gridx = 0;
		gbc_chckbxBasicResourcedloaded.gridy = 3;
		add(chckbxBasicResourcedloaded, gbc_chckbxBasicResourcedloaded);
		
		panelMusicTags = new JPanel();
		panelMusicTags.setBorder(new TitledBorder(null, "Music Tags", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridwidth = 2;
		gbc_panel.insets = new Insets(0, 0, 0, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 4;
		add(panelMusicTags, gbc_panel);
		
		
		for(MusicTags t : MusicTags.values()) {
			final MusicTagCheckbox chckbxMusic = new MusicTagCheckbox(t);
			chckbxMusic.addActionListener(e -> {
				if(chckbxMusic.isSelected()) {
					selectedAsset.addMusicTag(t);
				}else {
					selectedAsset.removeMusicTag(t);
				}
			});
			panelMusicTags.add(chckbxMusic);
			chTag.put(t, chckbxMusic);
		}

		run();
	}
	private Object2ObjectOpenHashMap<MusicTags, MusicTagCheckbox> chTag = new Object2ObjectOpenHashMap<MusicTags, AudioAssetDetailPanel.MusicTagCheckbox>();
	private class MusicTagCheckbox extends JCheckBox{
		private static final long serialVersionUID = -4991620917436861286L;
		MusicTags tag;
		public MusicTagCheckbox(MusicTags t) {
			super(t.getTagName());
			this.tag = t;
		}
	}
	public void onSelectedAsset(AudioAsset a) {
		this.selectedAsset = a;
		SwingUtilities.invokeLater(this);
	}

	
	private void resetLabels() {
		lblNameval.setText("n/a");
		textFieldVolume.setText("");
		chckbxStreaming.setSelected(false);
		chckbxCachedStreaming.setSelected(false);
		chckbxBasicResourcedloaded.setSelected(false);
		
		textFieldVolume.setEnabled(false);
		chckbxStreaming.setEnabled(false);
		chckbxCachedStreaming.setEnabled(false);
		chckbxBasicResourcedloaded.setEnabled(false);
	}
	
	@Override
	public void run() {
		resetLabels();
		if(selectedAsset == null) {
			return;
		}
		textFieldVolume.setEnabled(true);
		chckbxStreaming.setEnabled(true);
		chckbxCachedStreaming.setEnabled(true);
		chckbxBasicResourcedloaded.setEnabled(true);
		
		lblNameval.setText(selectedAsset.toString());
		textFieldVolume.setText(String.valueOf(selectedAsset.getVolume()));
		chckbxStreaming.setSelected(selectedAsset.getId().isStream());
		chckbxCachedStreaming.setSelected(selectedAsset.getId().isUseStreamCache());
		chckbxBasicResourcedloaded.setSelected(selectedAsset.generalTag == AudioGeneralTag.BASIC);
		
		for(MusicTags t : MusicTags.values()) {
			chTag.get(t).setSelected(selectedAsset != null && selectedAsset.musicTags.contains(t));
		}
	}

}
