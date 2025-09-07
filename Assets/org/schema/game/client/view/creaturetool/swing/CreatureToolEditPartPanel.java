package org.schema.game.client.view.creaturetool.swing;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.creaturetool.CreatureTool;
import org.schema.schine.resource.CreatureStructure.PartType;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Locale;
import java.util.Objects;

public class CreatureToolEditPartPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private CreatureToolMainPanel creatureToolMainPanel;
	private CreatureToolPartDataset data;
	private JComboBox comboBox;
	private CreatureToolEditPartSettingsPanel creatureToolEditPartSettingsPanel;

	/**
	 * Create the panel.
	 *
	 * @param creatureTool
	 * @param state
	 * @param creatureToolMainPanel
	 */
	public CreatureToolEditPartPanel(PartType type, GameClientState state, CreatureTool creatureTool, final CreatureToolMainPanel creatureToolMainPanel) {
		this.creatureToolMainPanel = creatureToolMainPanel;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[]{0.0, 1.0};
		gridBagLayout.columnWeights = new double[]{1.0};
		setLayout(gridBagLayout);

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Select", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.weightx = 1.0;
		gbc_panel.anchor = GridBagConstraints.NORTHWEST;
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		comboBox = new JComboBox(new CreaturePartJComboboxModel(type));
		comboBox.addActionListener(e -> updateDataSet());
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.weightx = 1.0;
		gbc_comboBox.anchor = GridBagConstraints.SOUTH;
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 0;
		gbc_comboBox.gridy = 0;
		panel.add(comboBox, gbc_comboBox);

		creatureToolEditPartSettingsPanel = new CreatureToolEditPartSettingsPanel(type, state, creatureTool, creatureToolMainPanel);
		creatureToolEditPartSettingsPanel.setBorder(new TitledBorder(null, "Edit", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_creatureToolEditPartSettingsPanel = new GridBagConstraints();
		gbc_creatureToolEditPartSettingsPanel.weightx = 1.0;
		gbc_creatureToolEditPartSettingsPanel.fill = GridBagConstraints.BOTH;
		gbc_creatureToolEditPartSettingsPanel.gridx = 0;
		gbc_creatureToolEditPartSettingsPanel.gridy = 1;
		add(creatureToolEditPartSettingsPanel, gbc_creatureToolEditPartSettingsPanel);
		GridBagLayout gbl_creatureToolEditPartSettingsPanel = new GridBagLayout();
		gbl_creatureToolEditPartSettingsPanel.columnWidths = new int[]{0};
		gbl_creatureToolEditPartSettingsPanel.rowHeights = new int[]{0};
		gbl_creatureToolEditPartSettingsPanel.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_creatureToolEditPartSettingsPanel.rowWeights = new double[]{Double.MIN_VALUE};
		creatureToolEditPartSettingsPanel.setLayout(gbl_creatureToolEditPartSettingsPanel);
	}

	private void updateDataSet() {
		if ("none".equals(Objects.requireNonNull(comboBox.getSelectedItem()).toString().toLowerCase(Locale.ENGLISH)) || "-1".equals(comboBox.getSelectedItem().toString().toLowerCase(Locale.ENGLISH))) {
			data = null;
		} else {

			data = new CreatureToolPartDataset();
			data.mesh = comboBox.getSelectedItem().toString();
		}
		creatureToolMainPanel.update();
	}

	public CreatureToolPartDataset getData() {
		return data;
	}

	public void updateGUI() {
		creatureToolEditPartSettingsPanel.updateGUI();
	}

}
