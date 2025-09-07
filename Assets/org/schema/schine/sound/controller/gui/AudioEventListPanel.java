package org.schema.schine.sound.controller.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.schema.schine.sound.controller.gui.AudioEventTableModel.AudioListFilter;

import com.bulletphysics.util.ObjectArrayList;

public class AudioEventListPanel extends JPanel{
	
	
	public JTable table;
	public AudioEventTableModel model;
	private final List<AudioListFilterCheckBox> checkBoxes = new ObjectArrayList<AudioEventListPanel.AudioListFilterCheckBox>();
	private JScrollPane scrollPane;
	private class AudioListFilterCheckBox extends JCheckBox implements ActionListener{
		private static final long serialVersionUID = 8343995668286082700L;
		private AudioListFilter filter;

		public AudioListFilterCheckBox(String s, AudioListFilter filter) {
			super(s);
			this.filter = filter;
			checkBoxes.add(this);
			
			this.addActionListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			setFilter(isSelected(), filter);
		}
		public void updateFromFilterStat() {
			setSelected(AudioEventListPanel.this.model.isFilter(filter));
		}
	}
	
	/**
	 * Create the panel.
	 */
	public AudioEventListPanel() {
		this.model = new AudioEventTableModel();
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{10};
		gridBagLayout.columnWeights = new double[]{0.0};
		
		gridBagLayout.rowHeights = new int[]{10, 10};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0};
		
		setLayout(gridBagLayout);
		
		
		JPanel filterPanel = new JPanel();
		GridBagConstraints gbc_filterPanel = new GridBagConstraints();
		gbc_filterPanel.weightx = 1.0;
		gbc_filterPanel.fill = GridBagConstraints.BOTH;
		gbc_filterPanel.anchor = GridBagConstraints.NORTH;
		gbc_filterPanel.gridx = 0;
		gbc_filterPanel.gridy = 0;
		add(filterPanel, gbc_filterPanel);
		
		AudioListFilterCheckBox chckbxAudio = new AudioListFilterCheckBox("Audio", AudioListFilter.SHOW_WITH_AUDIO);
		
		filterPanel.add(chckbxAudio);
		
		AudioListFilterCheckBox chckbxNoaudio = new AudioListFilterCheckBox("NoAudio", AudioListFilter.SHOW_WITHOUT_AUDIO);
		filterPanel.add(chckbxNoaudio);
		
		AudioListFilterCheckBox chckbxTag = new AudioListFilterCheckBox("TagAssigned", AudioListFilter.SHOW_TAG_AUDIO);
		filterPanel.add(chckbxTag);
		
		AudioListFilterCheckBox chckbxManual = new AudioListFilterCheckBox("Manual", AudioListFilter.SHOW_MANUAL_AUDIO);
		filterPanel.add(chckbxManual);
		
		AudioListFilterCheckBox chckbxRemote = new AudioListFilterCheckBox("Remote", AudioListFilter.SHOW_REMOTE_AUDIO);
		filterPanel.add(chckbxRemote);
		
		AudioListFilterCheckBox chckbxLocal = new AudioListFilterCheckBox("Local", AudioListFilter.SHOW_NON_REMOTE_AUDIO);
		filterPanel.add(chckbxLocal);
		
		updateCheckBoxes();
		
		
		
		this.scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.weighty = 1.0;
		gbc_scrollPane.weightx = 1.0;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.anchor = GridBagConstraints.NORTHWEST;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		add(scrollPane, gbc_scrollPane);
		
		table = new JTable(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		model.updateWidths(table);
		scrollPane.setViewportView(table);
		
		

	}
	private void updateCheckBoxes() {
		for(AudioListFilterCheckBox a : checkBoxes) {
			a.updateFromFilterStat();
		}
	}
	private void setFilter(boolean on, AudioListFilter filter) {
		if(on) {
			model.addFilter(filter);
		}else {
			model.removeFilter(filter);
		}
	}
	
}
