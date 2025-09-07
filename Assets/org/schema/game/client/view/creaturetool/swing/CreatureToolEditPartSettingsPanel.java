package org.schema.game.client.view.creaturetool.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.character.AnimationNotSetException;
import org.schema.game.client.view.creaturetool.CreatureTool;
import org.schema.schine.resource.CreatureStructure.PartType;

public class CreatureToolEditPartSettingsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private PartType type;
	private CreatureTool creatureTool;
	private CreatureToolMainPanel creatureToolMainPanel;
	private JLabel lblLoaded;
	private JComboBox comboBox;
	private JCheckBox chckbxOverrideParent;
	private JPanel panel_1;

	/**
	 * Create the panel.
	 */
	public CreatureToolEditPartSettingsPanel(final PartType type, GameClientState state, final CreatureTool creatureTool, final CreatureToolMainPanel creatureToolMainPanel) {
		this.type = type;
		this.creatureTool = creatureTool;
		this.creatureToolMainPanel = creatureToolMainPanel;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{1.0};
		setLayout(gridBagLayout);

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Animation", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.weighty = 1.0;
		gbc_panel.weightx = 1.0;
		gbc_panel.anchor = GridBagConstraints.NORTHWEST;
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		chckbxOverrideParent = new JCheckBox("Override Parent");
		chckbxOverrideParent.addActionListener(e -> update());
		GridBagConstraints gbc_chckbxOverrideParent = new GridBagConstraints();
		gbc_chckbxOverrideParent.anchor = GridBagConstraints.WEST;
		gbc_chckbxOverrideParent.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxOverrideParent.gridx = 0;
		gbc_chckbxOverrideParent.gridy = 0;
		panel.add(chckbxOverrideParent, gbc_chckbxOverrideParent);

		panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.anchor = GridBagConstraints.NORTHWEST;
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 2;
		panel.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);

		lblLoaded = new JLabel("");
		GridBagConstraints gbc_lblLoaded = new GridBagConstraints();
		gbc_lblLoaded.gridx = 0;
		gbc_lblLoaded.gridy = 3;
		panel.add(lblLoaded, gbc_lblLoaded);

		comboBox = new JComboBox(new CreaturePartAnimationComboboxModel());
		comboBox.addActionListener(e -> update());
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 0;
		gbc_comboBox.gridy = 1;
		panel.add(comboBox, gbc_comboBox);

	}

	public void update() {
		if (chckbxOverrideParent.isSelected()) {
			try {
				creatureTool.updateForcedAnimation(type, comboBox.getSelectedItem());
				lblLoaded.setText("Loaded: " + comboBox.getSelectedItem());
			} catch (AnimationNotSetException e1) {
				System.err.println("COULD NOT SET ANIMATION! " + comboBox.getSelectedItem());
				lblLoaded.setText("NO ANIMATION SET FOR: " + comboBox.getSelectedItem());
			}
		} else {
			lblLoaded.setText("");
			creatureToolMainPanel.updateParenAnimation(type);
		}
	}

	public void updateGUI() {
		update();
	}
}
