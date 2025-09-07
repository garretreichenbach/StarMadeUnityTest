package org.schema.game.common.facedit;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementReactorChange;

public class ElementReactorChangePanel extends JPanel implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel lblGeneral;
	private JLabel lblParent;
	private JCheckBox chckbxUpgrade;
	private final ElementReactorChange c;

	/**
	 * Create the panel.
	 */
	public ElementReactorChangePanel(final JDialog d, final ElementReactorChange c) {
		setLayout(new GridBagLayout());
		this.c = c;
		c.addObserver(this);
		JButton btnGeneralChamber = new JButton("General Chamber");
		btnGeneralChamber.addActionListener(e -> {
			ElementChoserDialog diag = new ElementChoserDialog((JFrame)d.getParent(), c::setRoot);
			diag.setVisible(true);
		});
		GridBagConstraints gbc_btnGeneralChamber = new GridBagConstraints();
		gbc_btnGeneralChamber.weighty = 1.0;
		gbc_btnGeneralChamber.weightx = 1.0;
		gbc_btnGeneralChamber.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnGeneralChamber.insets = new Insets(0, 0, 5, 5);
		gbc_btnGeneralChamber.gridx = 0;
		gbc_btnGeneralChamber.gridy = 0;
		add(btnGeneralChamber, gbc_btnGeneralChamber);
		
		JButton btnParent = new JButton("Parent");
		btnParent.addActionListener(e -> {
			ElementChoserDialog diag = new ElementChoserDialog((JFrame)d.getParent(), c::setParent);
			diag.setVisible(true);
		});
		GridBagConstraints gbc_btnParent = new GridBagConstraints();
		gbc_btnParent.weighty = 1.0;
		gbc_btnParent.weightx = 1.0;
		gbc_btnParent.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnParent.insets = new Insets(0, 0, 5, 5);
		gbc_btnParent.gridx = 1;
		gbc_btnParent.gridy = 0;
		add(btnParent, gbc_btnParent);
		
		chckbxUpgrade = new JCheckBox("upgrade");
		chckbxUpgrade.addActionListener(e -> c.setUpgrade(chckbxUpgrade.isSelected()));
		GridBagConstraints gbc_chckbxUpgrade = new GridBagConstraints();
		gbc_chckbxUpgrade.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxUpgrade.weightx = 1.0;
		gbc_chckbxUpgrade.anchor = GridBagConstraints.NORTHWEST;
		gbc_chckbxUpgrade.gridx = 2;
		gbc_chckbxUpgrade.gridy = 0;
		add(chckbxUpgrade, gbc_chckbxUpgrade);
		
		lblGeneral = new JLabel("general");
		GridBagConstraints gbc_lblGeneral = new GridBagConstraints();
		gbc_lblGeneral.weighty = 1.0;
		gbc_lblGeneral.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblGeneral.insets = new Insets(0, 0, 5, 5);
		gbc_lblGeneral.gridx = 0;
		gbc_lblGeneral.gridy = 1;
		add(lblGeneral, gbc_lblGeneral);
		
		lblParent = new JLabel("parent");
		GridBagConstraints gbc_lblParent = new GridBagConstraints();
		gbc_lblParent.weighty = 1.0;
		gbc_lblParent.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblParent.insets = new Insets(0, 0, 5, 5);
		gbc_lblParent.gridx = 1;
		gbc_lblParent.gridy = 1;
		add(lblParent, gbc_lblParent);
		
		JButton btnOk = new JButton("Ok");
		btnOk.addActionListener(e -> {
			c.apply();
			d.dispose();
		});
		
		JButton btnClearParent = new JButton("Directly from General (no parent)");
		btnClearParent.addActionListener(e -> {
			c.parent = null;
			updateValues();
		});
		GridBagConstraints gbc_btnClearParent = new GridBagConstraints();
		gbc_btnClearParent.anchor = GridBagConstraints.WEST;
		gbc_btnClearParent.insets = new Insets(0, 0, 5, 5);
		gbc_btnClearParent.gridx = 1;
		gbc_btnClearParent.gridy = 2;
		add(btnClearParent, gbc_btnClearParent);
		btnOk.setVerticalAlignment(SwingConstants.BOTTOM);
		GridBagConstraints gbc_btnOk = new GridBagConstraints();
		gbc_btnOk.weighty = 1.0;
		gbc_btnOk.anchor = GridBagConstraints.SOUTHEAST;
		gbc_btnOk.insets = new Insets(0, 0, 0, 5);
		gbc_btnOk.gridx = 1;
		gbc_btnOk.gridy = 3;
		add(btnOk, gbc_btnOk);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(e -> d.dispose());
		btnCancel.setVerticalAlignment(SwingConstants.BOTTOM);
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.anchor = GridBagConstraints.SOUTHEAST;
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 3;
		add(btnCancel, gbc_btnCancel);
		updateValues();
	}

	@Override
	public void update(Observable o, Object arg) {
		updateValues();
	}

	private void updateValues() {
		lblGeneral.setText(c.root != null ? c.root.toString() : "not set");
		lblParent.setText(c.parent != null ? c.parent.toString() : "directly from general (no parent)");
		chckbxUpgrade.setSelected(c.upgrade);
	}

}
