package org.schema.game.common.starcalc;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.schema.game.common.controller.elements.combination.modifier.tagMod.BasicModifier;
import org.schema.game.common.controller.elements.combination.modifier.tagMod.formula.FloatBuffFormula;
import org.schema.game.common.controller.elements.combination.modifier.tagMod.formula.FloatNervFomula;
import org.schema.game.common.util.GuiErrorHandler;

public class WeaponFormulaCalculator extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private JTextField textFieldPBlock;
	private JTextField textField_master;
	private JTextField textField_slave;
	private JEditorPane editorPane;
	private JComboBox comboBox;
	private JCheckBox chckbxInverse;
	private JCheckBox chckbxLinear;
	private JTextField textField;
	private JTextField textField_1;

	/**
	 * Create the panel.
	 */
	public WeaponFormulaCalculator() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[]{1.0};
		gridBagLayout.columnWeights = new double[]{1.0};
		setLayout(gridBagLayout);

		JScrollPane scrollPane = new JScrollPane();
		//		scrollPane.setPreferredSize(new Dimension(800, 800));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.weighty = 1.0;
		gbc_scrollPane.weightx = 0.0;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		add(scrollPane, gbc_scrollPane);

		JPanel panel = new JPanel();
		scrollPane.setViewportView(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.weightx = 1.0;
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 0;
		panel.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 1.0, 1.0, 1.0, 0.0};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);

		JLabel lblPowerblock = new JLabel("BasePerBlk/ModValue");
		GridBagConstraints gbc_lblPowerblock = new GridBagConstraints();
		gbc_lblPowerblock.insets = new Insets(0, 0, 5, 5);
		gbc_lblPowerblock.anchor = GridBagConstraints.EAST;
		gbc_lblPowerblock.gridx = 0;
		gbc_lblPowerblock.gridy = 0;
		panel_1.add(lblPowerblock, gbc_lblPowerblock);

		textFieldPBlock = new JTextField();
		textFieldPBlock.setText("10");
		GridBagConstraints gbc_textFieldPBlock = new GridBagConstraints();
		gbc_textFieldPBlock.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldPBlock.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldPBlock.gridx = 1;
		gbc_textFieldPBlock.gridy = 0;
		panel_1.add(textFieldPBlock, gbc_textFieldPBlock);
		textFieldPBlock.setColumns(10);

		textField = new JTextField();
		textField.setText("30");
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 2;
		gbc_textField.gridy = 0;
		panel_1.add(textField, gbc_textField);
		textField.setColumns(10);

		chckbxInverse = new JCheckBox("inverse");
		GridBagConstraints gbc_chckbxInverse = new GridBagConstraints();
		gbc_chckbxInverse.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxInverse.gridx = 3;
		gbc_chckbxInverse.gridy = 0;
		panel_1.add(chckbxInverse, gbc_chckbxInverse);

		chckbxLinear = new JCheckBox("linear");
		GridBagConstraints gbc_chckbxLinear = new GridBagConstraints();
		gbc_chckbxLinear.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxLinear.gridx = 4;
		gbc_chckbxLinear.gridy = 0;
		chckbxLinear.setSelected(true);
		panel_1.add(chckbxLinear, gbc_chckbxLinear);

		comboBox = new JComboBox(new String[]{"buff", "nerf", "skip"});
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.weightx = 0.0;
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 5;
		gbc_comboBox.gridy = 0;
		panel_1.add(comboBox, gbc_comboBox);

		JLabel lblMasterslave = new JLabel("Master/Slave/Effect");
		GridBagConstraints gbc_lblMasterslave = new GridBagConstraints();
		gbc_lblMasterslave.insets = new Insets(0, 0, 0, 5);
		gbc_lblMasterslave.anchor = GridBagConstraints.EAST;
		gbc_lblMasterslave.gridx = 0;
		gbc_lblMasterslave.gridy = 1;
		panel_1.add(lblMasterslave, gbc_lblMasterslave);

		textField_master = new JTextField();
		textField_master.setText("100");
		GridBagConstraints gbc_textField_master = new GridBagConstraints();
		gbc_textField_master.insets = new Insets(0, 0, 0, 5);
		gbc_textField_master.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_master.gridx = 1;
		gbc_textField_master.gridy = 1;
		panel_1.add(textField_master, gbc_textField_master);
		textField_master.setColumns(10);

		JButton btnCalculateSingle = new JButton("Calculate Single");
		btnCalculateSingle.addActionListener(e -> calculate(false));

		textField_slave = new JTextField();
		textField_slave.setText("1");
		GridBagConstraints gbc_textField_slave = new GridBagConstraints();
		gbc_textField_slave.insets = new Insets(0, 0, 0, 5);
		gbc_textField_slave.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_slave.gridx = 2;
		gbc_textField_slave.gridy = 1;
		panel_1.add(textField_slave, gbc_textField_slave);
		textField_slave.setColumns(10);

		textField_1 = new JTextField();
		textField_1.setText("0");
		textField_1.setColumns(10);
		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
		gbc_textField_1.insets = new Insets(0, 0, 0, 5);
		gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_1.gridx = 3;
		gbc_textField_1.gridy = 1;
		panel_1.add(textField_1, gbc_textField_1);
		GridBagConstraints gbc_btnCalculateSingle = new GridBagConstraints();
		gbc_btnCalculateSingle.insets = new Insets(0, 0, 0, 5);
		gbc_btnCalculateSingle.gridx = 4;
		gbc_btnCalculateSingle.gridy = 1;
		panel_1.add(btnCalculateSingle, gbc_btnCalculateSingle);

		JButton btnCalculateStatistics = new JButton("Calculate Statistics");
		btnCalculateStatistics.addActionListener(e -> calculate(true));
		GridBagConstraints gbc_btnCalculateStatistics = new GridBagConstraints();
		gbc_btnCalculateStatistics.gridx = 5;
		gbc_btnCalculateStatistics.gridy = 1;
		panel_1.add(btnCalculateStatistics, gbc_btnCalculateStatistics);

		JPanel panel_2 = new JPanel();
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.weighty = 10.0;
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 1;
		panel.add(panel_2, gbc_panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[]{0, 0};
		gbl_panel_2.rowHeights = new int[]{0, 0};
		gbl_panel_2.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel_2.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panel_2.setLayout(gbl_panel_2);

		editorPane = new JEditorPane();
		editorPane.setEditable(false);
		GridBagConstraints gbc_editorPane = new GridBagConstraints();
		gbc_editorPane.gridwidth = 2;
		gbc_editorPane.weighty = 1.0;
		gbc_editorPane.weightx = 1.0;
		gbc_editorPane.fill = GridBagConstraints.BOTH;
		gbc_editorPane.gridx = 0;
		gbc_editorPane.gridy = 0;
		panel_2.add(editorPane, gbc_editorPane);
	}

	protected void calculate(boolean stats) {
		try {
			String title = "master" + ":" + "slave" + "\t\t\t" + "inputBef" + " -> " + "input" + "\t\t\t" + "ratio" + "\t\t\t" + "output";
			int master = Integer.parseInt(textField_master.getText());
			int slave = Integer.parseInt(textField_slave.getText());
			int effect = Integer.parseInt(textField_1.getText());
			float value = Float.parseFloat(textFieldPBlock.getText());

			BasicModifier formula = getFormula();
			boolean linear = chckbxLinear.isSelected();
			if (!stats) {
				String s = getStat(master, slave, effect, value, linear, formula);
				editorPane.setText(title + "\n" + s);
			} else {
				StringBuffer sb = new StringBuffer();
				sb.append(title + "\n");
				for (int i = 1; i < 100000; i *= 10) {
					for (int g = 0; g < 100000 && g <= i; g += 10) {

						String s = getStat(i, g, effect, value, linear, formula);
						sb.append(s + "\n");
					}
				}
				editorPane.setText(sb.toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
			GuiErrorHandler.processNormalErrorDialogException(e, true);
		}
	}

	private String getStat(int master, int slave, int effect, float input, boolean linear, BasicModifier formula) {

		float ratio;
		float inputBef = input;
//		if(linear){
//			input *= master;
//		}

		if (master <= 0 || slave <= 0) {
			ratio = 0;
		} else {
			ratio = (float) slave / (float) master;
		}

		float output = formula.getOutput(input, master, slave, effect, ratio);
		return master + ":" + slave + "\t\t\t" + inputBef + " -> " + input + "\t\t\t" + ratio + "\t\t\t" + output;
	}

	private BasicModifier getFormula() {

		float maxBonus = Float.parseFloat(textField.getText());

		boolean inverse = chckbxInverse.isSelected();
		boolean linear = chckbxLinear.isSelected();

		if (comboBox.getSelectedItem().equals("skip")) {
			return new BasicModifier(inverse, maxBonus, linear, null);
		} else if (comboBox.getSelectedItem().equals("buff")) {
			FloatBuffFormula floatBuffFormula = new FloatBuffFormula();
			return new BasicModifier(inverse, maxBonus, linear, floatBuffFormula);
		} else if (comboBox.getSelectedItem().equals("nerf")) {
			FloatNervFomula floatNerfFormula = new FloatNervFomula();
			return new BasicModifier(inverse, maxBonus, linear, floatNerfFormula);
		} else {
			throw new IllegalArgumentException("must be skip/buff/nerf");
		}

	}
}
