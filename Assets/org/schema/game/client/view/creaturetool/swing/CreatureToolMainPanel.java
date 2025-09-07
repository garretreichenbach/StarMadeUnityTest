package org.schema.game.client.view.creaturetool.swing;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.client.view.character.AnimationNotSetException;
import org.schema.game.client.view.creaturetool.CreatureTool;
import org.schema.game.common.data.creature.AIRandomCompositeCreature;
import org.schema.game.common.data.creature.CreaturePartNode;
import org.schema.game.common.data.creature.CreaturePartNode.AttachmentType;
import org.schema.schine.resource.CreatureStructure.PartType;
import org.schema.schine.resource.ResourceLoadEntry;
import org.schema.schine.resource.ResourceLoader;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class CreatureToolMainPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private CreatureTool creatureTool;
	private GameClientState state;
	private CreatureToolEditPartPanel creatureToolEditPartPanel2;
	private CreatureToolEditPartPanel creatureToolEditPartPanel1;
	private CreatureToolEditPartPanel creatureToolEditPartPanel0;
	private JSpinner spinner;
	private JComboBox comboBox;

	/**
	 * Create the panel.
	 *
	 * @param creatureTool
	 * @param state
	 */
	public CreatureToolMainPanel(GameClientState state, final CreatureTool creatureTool) {
		this.state = state;
		this.creatureTool = creatureTool;

		//This is a hacky fix because the models don't seem to load correctly in SP without it.
		ObjectArrayList<ResourceLoadEntry> l = new ObjectArrayList<>();
		ResourceLoader.loadModelConfig(l);
		state.getResourceMap().initForServer(l);
		//

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[]{1.0, 1.0};
		gridBagLayout.columnWeights = new double[]{1.0};
		setLayout(gridBagLayout);

		JPanel panelTop = new JPanel();
		GridBagConstraints gbc_panelTop = new GridBagConstraints();
		gbc_panelTop.insets = new Insets(0, 0, 5, 0);
		gbc_panelTop.fill = GridBagConstraints.BOTH;
		gbc_panelTop.gridx = 0;
		gbc_panelTop.gridy = 0;
		add(panelTop, gbc_panelTop);
		GridBagLayout gbl_panelTop = new GridBagLayout();
		gbl_panelTop.columnWidths = new int[]{0, 0};
		gbl_panelTop.rowHeights = new int[]{0, 0};
		gbl_panelTop.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panelTop.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panelTop.setLayout(gbl_panelTop);

		JScrollPane scrollPaneSettings = new JScrollPane();
		GridBagConstraints gbc_scrollPaneSettings = new GridBagConstraints();
		gbc_scrollPaneSettings.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneSettings.gridx = 0;
		gbc_scrollPaneSettings.gridy = 0;
		panelTop.add(scrollPaneSettings, gbc_scrollPaneSettings);

		JPanel panel = new JPanel();
		scrollPaneSettings.setViewportView(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Parent Animation", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.insets = new Insets(0, 0, 5, 0);
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 0;
		panel.add(panel_2, gbc_panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[]{0, 0};
		gbl_panel_2.rowHeights = new int[]{0, 0};
		gbl_panel_2.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel_2.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_2.setLayout(gbl_panel_2);

		comboBox = new JComboBox(new CreaturePartAnimationComboboxModel());
		comboBox.addActionListener(e -> updateGUI());
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 0;
		gbc_comboBox.gridy = 0;
		panel_2.add(comboBox, gbc_comboBox);

		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel_1.anchor = GridBagConstraints.NORTHWEST;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 1;
		panel.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);

		JLabel lblSpeed = new JLabel("Speed");
		GridBagConstraints gbc_lblSpeed = new GridBagConstraints();
		gbc_lblSpeed.insets = new Insets(0, 0, 5, 5);
		gbc_lblSpeed.gridx = 0;
		gbc_lblSpeed.gridy = 0;
		panel_1.add(lblSpeed, gbc_lblSpeed);

		spinner = new JSpinner(new CreatureToolSpeedSpinnerModel(creatureTool, PartType.BOTTOM, PartType.MIDDLE, PartType.TOP));
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.insets = new Insets(0, 0, 5, 0);
		gbc_spinner.gridx = 1;
		gbc_spinner.gridy = 0;
		panel_1.add(spinner, gbc_spinner);

		JButton btnSpawnThisAbomination = new JButton("Spawn this abomination!");
		btnSpawnThisAbomination.addActionListener(e -> creatureTool.spawn());
		GridBagConstraints gbc_btnSpawnThisAbomination = new GridBagConstraints();
		gbc_btnSpawnThisAbomination.weightx = 1.0;
		gbc_btnSpawnThisAbomination.anchor = GridBagConstraints.EAST;
		gbc_btnSpawnThisAbomination.insets = new Insets(0, 0, 0, 5);
		gbc_btnSpawnThisAbomination.gridx = 2;
		gbc_btnSpawnThisAbomination.gridy = 0;
		panel_1.add(btnSpawnThisAbomination, gbc_btnSpawnThisAbomination);

		JButton despawnButton = new JButton("Destroy this abomination!");
		despawnButton.addActionListener(e -> creatureTool.despawn());
		GridBagConstraints gbc_despawnButton = new GridBagConstraints();
		gbc_despawnButton.anchor = GridBagConstraints.EAST;
		gbc_despawnButton.gridx = 3;
		gbc_despawnButton.gridy = 0;
		panel_1.add(despawnButton, gbc_despawnButton);

		JPanel panelParts = new JPanel();
		GridBagConstraints gbc_panelParts = new GridBagConstraints();
		gbc_panelParts.weighty = 3.0;
		gbc_panelParts.fill = GridBagConstraints.BOTH;
		gbc_panelParts.gridx = 0;
		gbc_panelParts.gridy = 1;
		add(panelParts, gbc_panelParts);
		GridBagLayout gbl_panelParts = new GridBagLayout();
		gbl_panelParts.columnWidths = new int[]{0, 0};
		gbl_panelParts.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panelParts.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panelParts.rowWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
		panelParts.setLayout(gbl_panelParts);

		JPanel panelPart0 = new JPanel();
		GridBagConstraints gbc_panelPart0 = new GridBagConstraints();
		gbc_panelPart0.insets = new Insets(0, 0, 5, 0);
		gbc_panelPart0.fill = GridBagConstraints.BOTH;
		gbc_panelPart0.gridx = 0;
		gbc_panelPart0.gridy = 0;
		panelParts.add(panelPart0, gbc_panelPart0);
		GridBagLayout gbl_panelPart0 = new GridBagLayout();
		gbl_panelPart0.columnWidths = new int[]{0, 0};
		gbl_panelPart0.rowHeights = new int[]{0, 0};
		gbl_panelPart0.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panelPart0.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panelPart0.setLayout(gbl_panelPart0);

		JScrollPane scrollPanePart0 = new JScrollPane();
		GridBagConstraints gbc_scrollPanePart0 = new GridBagConstraints();
		gbc_scrollPanePart0.weighty = 1.0;
		gbc_scrollPanePart0.weightx = 1.0;
		gbc_scrollPanePart0.anchor = GridBagConstraints.NORTHWEST;
		gbc_scrollPanePart0.fill = GridBagConstraints.BOTH;
		gbc_scrollPanePart0.gridx = 0;
		gbc_scrollPanePart0.gridy = 0;
		panelPart0.add(scrollPanePart0, gbc_scrollPanePart0);

		creatureToolEditPartPanel0 = new CreatureToolEditPartPanel(PartType.BOTTOM, state, creatureTool, this);
		//		creatureToolEditPartPanel0.setPreferredSize(new Dimension(500, 500));
		//		creatureToolEditPartPanel0.setMinimumSize(new Dimension(400, 400));
		creatureToolEditPartPanel0.setBorder(new TitledBorder(null, "Part-Basis", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		scrollPanePart0.setViewportView(creatureToolEditPartPanel0);
		GridBagLayout gbl_creatureToolEditPartPanel0 = new GridBagLayout();
		gbl_creatureToolEditPartPanel0.columnWidths = new int[]{0};
		gbl_creatureToolEditPartPanel0.rowHeights = new int[]{0};
		gbl_creatureToolEditPartPanel0.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_creatureToolEditPartPanel0.rowWeights = new double[]{Double.MIN_VALUE};
		//		creatureToolEditPartPanel0.setLayout(gbl_creatureToolEditPartPanel0);

		JPanel panelPart1 = new JPanel();
		GridBagConstraints gbc_panelPart1 = new GridBagConstraints();
		gbc_panelPart1.insets = new Insets(0, 0, 5, 0);
		gbc_panelPart1.fill = GridBagConstraints.BOTH;
		gbc_panelPart1.gridx = 0;
		gbc_panelPart1.gridy = 1;
		panelParts.add(panelPart1, gbc_panelPart1);
		GridBagLayout gbl_panelPart1 = new GridBagLayout();
		gbl_panelPart1.columnWidths = new int[]{0, 0};
		gbl_panelPart1.rowHeights = new int[]{0, 0};
		gbl_panelPart1.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panelPart1.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panelPart1.setLayout(gbl_panelPart1);

		JScrollPane scrollPanePart1 = new JScrollPane();
		GridBagConstraints gbc_scrollPanePart1 = new GridBagConstraints();
		gbc_scrollPanePart1.anchor = GridBagConstraints.NORTHWEST;
		gbc_scrollPanePart1.weightx = 1.0;
		gbc_scrollPanePart1.weighty = 1.0;
		gbc_scrollPanePart1.fill = GridBagConstraints.BOTH;
		gbc_scrollPanePart1.gridx = 0;
		gbc_scrollPanePart1.gridy = 0;
		panelPart1.add(scrollPanePart1, gbc_scrollPanePart1);

		creatureToolEditPartPanel1 = new CreatureToolEditPartPanel(PartType.MIDDLE, state, creatureTool, this);
		creatureToolEditPartPanel1.setBorder(new TitledBorder(null, "Second Part", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		scrollPanePart1.setViewportView(creatureToolEditPartPanel1);
		GridBagLayout gbl_creatureToolEditPartPanel1 = new GridBagLayout();
		gbl_creatureToolEditPartPanel1.columnWidths = new int[]{0};
		gbl_creatureToolEditPartPanel1.rowHeights = new int[]{0};
		gbl_creatureToolEditPartPanel1.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_creatureToolEditPartPanel1.rowWeights = new double[]{Double.MIN_VALUE};
		//		creatureToolEditPartPanel1.setLayout(gbl_creatureToolEditPartPanel1);

		JPanel panelPart2 = new JPanel();
		GridBagConstraints gbc_panelPart2 = new GridBagConstraints();
		gbc_panelPart2.fill = GridBagConstraints.BOTH;
		gbc_panelPart2.gridx = 0;
		gbc_panelPart2.gridy = 2;
		panelParts.add(panelPart2, gbc_panelPart2);
		GridBagLayout gbl_panelPart2 = new GridBagLayout();
		gbl_panelPart2.columnWidths = new int[]{0, 0};
		gbl_panelPart2.rowHeights = new int[]{0, 0};
		gbl_panelPart2.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panelPart2.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panelPart2.setLayout(gbl_panelPart2);

		JScrollPane scrollPanePart2 = new JScrollPane();
		GridBagConstraints gbc_scrollPanePart2 = new GridBagConstraints();
		gbc_scrollPanePart2.weighty = 1.0;
		gbc_scrollPanePart2.weightx = 1.0;
		gbc_scrollPanePart2.fill = GridBagConstraints.BOTH;
		gbc_scrollPanePart2.gridx = 0;
		gbc_scrollPanePart2.gridy = 0;
		panelPart2.add(scrollPanePart2, gbc_scrollPanePart2);

		creatureToolEditPartPanel2 = new CreatureToolEditPartPanel(PartType.TOP, state, creatureTool, this);
		creatureToolEditPartPanel2.setBorder(new TitledBorder(null, "Third Part", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		scrollPanePart2.setViewportView(creatureToolEditPartPanel2);
		GridBagLayout gbl_creatureToolEditPartPanel2 = new GridBagLayout();
		gbl_creatureToolEditPartPanel2.columnWidths = new int[]{0};
		gbl_creatureToolEditPartPanel2.rowHeights = new int[]{0};
		gbl_creatureToolEditPartPanel2.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_creatureToolEditPartPanel2.rowWeights = new double[]{Double.MIN_VALUE};
		//		creatureToolEditPartPanel2.setLayout(gbl_creatureToolEditPartPanel2);
	}

	public void updateGUI() {
		((CreatureToolSpeedSpinnerModel) spinner.getModel()).fireChangeEvent();

		try {
			creatureTool.updateForcedAnimation(PartType.BOTTOM, comboBox.getSelectedItem());
		} catch (AnimationNotSetException e1) {
			System.err.println("[0] BOTTOM COULD NOT SET ANIMATION! " + comboBox.getSelectedItem());
		}
		try {
			creatureTool.updateForcedAnimation(PartType.MIDDLE, comboBox.getSelectedItem());
		} catch (AnimationNotSetException e1) {
			System.err.println("[1] MIDDLE COULD NOT SET ANIMATION! " + comboBox.getSelectedItem());
		}
		try {
			creatureTool.updateForcedAnimation(PartType.TOP, comboBox.getSelectedItem());
		} catch (AnimationNotSetException e1) {
			System.err.println("[2] TOP COULD NOT SET ANIMATION! " + comboBox.getSelectedItem());
		}

		creatureToolEditPartPanel0.updateGUI();
		creatureToolEditPartPanel1.updateGUI();
		creatureToolEditPartPanel2.updateGUI();
	}

	public void update() {
		Float lastSpeed = null;
		if (creatureTool.getCreature() != null) {
			lastSpeed = creatureTool.getCreature().getSpeed();
		}
		AIRandomCompositeCreature c;
		if (creatureToolEditPartPanel0.getData() != null) {

			CreaturePartNode pBot = new CreaturePartNode(PartType.BOTTOM, state, creatureToolEditPartPanel0.getData().mesh, null);
			if (creatureToolEditPartPanel1.getData() != null) {
				CreaturePartNode pMid = new CreaturePartNode(PartType.MIDDLE, state, creatureToolEditPartPanel1.getData().mesh, null);
				pBot.attach(state, pMid, AttachmentType.MAIN);

				if (creatureToolEditPartPanel2.getData() != null) {
					CreaturePartNode pTop = new CreaturePartNode(PartType.TOP, state, creatureToolEditPartPanel2.getData().mesh, null);
					pMid.attach(state, pTop, AttachmentType.MAIN);
				}
			}

			c = AIRandomCompositeCreature.instantiate(state, 4, 1, 0.5f, 0.25f, new Vector3i(1, 1, 1), pBot);
		} else {
			c = null;
		}
		creatureTool.updateFromGUI(c);

		if (lastSpeed != null) {
			if (creatureTool.getCreature() != null) {
				creatureTool.getCreature().setSpeed(lastSpeed);
			}
			System.err.println("SET LAST SPEED: " + lastSpeed);
			spinner.getModel().setValue(lastSpeed);
		}

		updateGUI();
	}

	public void updateParenAnimation(PartType type) {

	}

}
