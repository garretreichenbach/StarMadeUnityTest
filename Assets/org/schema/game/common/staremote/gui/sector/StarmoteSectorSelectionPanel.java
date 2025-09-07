package org.schema.game.common.staremote.gui.sector;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import org.schema.common.util.linAlg.Vector3i;

public class StarmoteSectorSelectionPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private JSpinner zSpinner;
	private JSpinner ySpinner;
	private JSpinner xSpinner;
	private JPanel panel;

	public StarmoteSectorSelectionPanel() {
		setPreferredSize(new Dimension(267, 44));
		setMaximumSize(new Dimension(700, 80));
		setMinimumSize(new Dimension(180, 50));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		panel = new JPanel();
		panel.setPreferredSize(new Dimension(300, 50));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.weightx = 1.0;
		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel.anchor = GridBagConstraints.NORTHWEST;
		gbc_panel.insets = new Insets(0, 0, 0, 5);
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 1.0};
		panel.setLayout(gbl_panel);

		JLabel lblX = new JLabel("X");
		GridBagConstraints gbc_lblX = new GridBagConstraints();
		gbc_lblX.weighty = 1.0;
		gbc_lblX.insets = new Insets(0, 0, 5, 5);
		gbc_lblX.gridx = 0;
		gbc_lblX.gridy = 0;
		panel.add(lblX, gbc_lblX);

		JLabel lblY = new JLabel("Y");
		GridBagConstraints gbc_lblY = new GridBagConstraints();
		gbc_lblY.weighty = 1.0;
		gbc_lblY.insets = new Insets(0, 0, 5, 5);
		gbc_lblY.gridx = 1;
		gbc_lblY.gridy = 0;
		panel.add(lblY, gbc_lblY);

		JLabel lblZ = new JLabel("Z");
		GridBagConstraints gbc_lblZ = new GridBagConstraints();
		gbc_lblZ.weighty = 1.0;
		gbc_lblZ.insets = new Insets(0, 0, 5, 0);
		gbc_lblZ.gridx = 2;
		gbc_lblZ.gridy = 0;
		panel.add(lblZ, gbc_lblZ);

		xSpinner = new JSpinner();
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.weighty = 1.0;
		gbc_spinner.weightx = 1.0;
		gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner.insets = new Insets(0, 0, 5, 5);
		gbc_spinner.gridx = 0;
		gbc_spinner.gridy = 1;
		panel.add(xSpinner, gbc_spinner);

		ySpinner = new JSpinner();
		GridBagConstraints gbc_spinner_1 = new GridBagConstraints();
		gbc_spinner_1.weighty = 1.0;
		gbc_spinner_1.weightx = 1.0;
		gbc_spinner_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner_1.insets = new Insets(0, 0, 5, 5);
		gbc_spinner_1.gridx = 1;
		gbc_spinner_1.gridy = 1;
		panel.add(ySpinner, gbc_spinner_1);

		zSpinner = new JSpinner();
		GridBagConstraints gbc_spinner_2 = new GridBagConstraints();
		gbc_spinner_2.insets = new Insets(0, 0, 5, 0);
		gbc_spinner_2.weighty = 1.0;
		gbc_spinner_2.weightx = 1.0;
		gbc_spinner_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner_2.gridx = 2;
		gbc_spinner_2.gridy = 1;
		panel.add(zSpinner, gbc_spinner_2);

	}

	public Vector3i getCoord() {
		Vector3i r = new Vector3i();
		r.x = (Integer) xSpinner.getValue();
		r.y = (Integer) ySpinner.getValue();
		r.z = (Integer) zSpinner.getValue();

		return r;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		xSpinner.setEnabled(enabled);
		ySpinner.setEnabled(enabled);
		zSpinner.setEnabled(enabled);
	}
}
