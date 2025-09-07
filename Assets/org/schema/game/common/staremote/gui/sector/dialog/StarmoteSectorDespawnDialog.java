package org.schema.game.common.staremote.gui.sector.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.staremote.gui.StarmoteFrame;
import org.schema.game.common.staremote.gui.sector.StarmoteSectorSelectionPanel;
import org.schema.game.server.data.admin.AdminCommands;

public class StarmoteSectorDespawnDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private final JPanel contentPanel = new JPanel();
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JTextField textField;
	private StarmoteSectorSelectionPanel starmodeSectorSelectionPanel;
	private JCheckBox chckbxShipsOnly;
	private JCheckBox chckbxAllUniverse;
	private JRadioButton rdbtnUsed;
	private JRadioButton rdbtnUnused;
	private JRadioButton rdbtnAll;

	/**
	 * Create the dialog.
	 */
	public StarmoteSectorDespawnDialog(final GameClientState state) {
		super(StarmoteFrame.self, true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle("Change Sector");
		setBounds(100, 100, 541, 251);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0, 0};
		gbl_contentPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblSdadasdsa = new JLabel("WARNING: Changes made are PERMANENT! Use with care and backup beforehand!");
			lblSdadasdsa.setForeground(new Color(139, 0, 0));
			GridBagConstraints gbc_lblSdadasdsa = new GridBagConstraints();
			gbc_lblSdadasdsa.anchor = GridBagConstraints.WEST;
			gbc_lblSdadasdsa.gridwidth = 2;
			gbc_lblSdadasdsa.insets = new Insets(0, 0, 5, 5);
			gbc_lblSdadasdsa.gridx = 0;
			gbc_lblSdadasdsa.gridy = 0;
			contentPanel.add(lblSdadasdsa, gbc_lblSdadasdsa);
		}
		{
			JLabel lblEnterPlayerName = new JLabel("Enter First Letters");
			GridBagConstraints gbc_lblEnterPlayerName = new GridBagConstraints();
			gbc_lblEnterPlayerName.insets = new Insets(0, 0, 5, 5);
			gbc_lblEnterPlayerName.anchor = GridBagConstraints.EAST;
			gbc_lblEnterPlayerName.gridx = 0;
			gbc_lblEnterPlayerName.gridy = 1;
			contentPanel.add(lblEnterPlayerName, gbc_lblEnterPlayerName);
		}
		{
			textField = new JTextField();
			GridBagConstraints gbc_textField = new GridBagConstraints();
			gbc_textField.insets = new Insets(0, 0, 5, 0);
			gbc_textField.fill = GridBagConstraints.HORIZONTAL;
			gbc_textField.gridx = 1;
			gbc_textField.gridy = 1;
			contentPanel.add(textField, gbc_textField);
			textField.setColumns(10);
		}
		{
			JLabel lblOptions = new JLabel("Options");
			GridBagConstraints gbc_lblOptions = new GridBagConstraints();
			gbc_lblOptions.anchor = GridBagConstraints.WEST;
			gbc_lblOptions.insets = new Insets(0, 0, 5, 5);
			gbc_lblOptions.gridx = 0;
			gbc_lblOptions.gridy = 2;
			contentPanel.add(lblOptions, gbc_lblOptions);
		}
		{
			JPanel panel = new JPanel();
			GridBagConstraints gbc_panel = new GridBagConstraints();
			gbc_panel.insets = new Insets(0, 0, 5, 0);
			gbc_panel.fill = GridBagConstraints.BOTH;
			gbc_panel.gridx = 1;
			gbc_panel.gridy = 2;
			contentPanel.add(panel, gbc_panel);
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
			gbl_panel.rowHeights = new int[]{0, 0, 0};
			gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
			gbl_panel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
			panel.setLayout(gbl_panel);
			{
				chckbxShipsOnly = new JCheckBox("Ships only");
				chckbxShipsOnly.setSelected(true);
				GridBagConstraints gbc_chckbxShipsOnly = new GridBagConstraints();
				gbc_chckbxShipsOnly.insets = new Insets(0, 0, 5, 5);
				gbc_chckbxShipsOnly.gridx = 0;
				gbc_chckbxShipsOnly.gridy = 0;
				panel.add(chckbxShipsOnly, gbc_chckbxShipsOnly);
			}
			{
				chckbxAllUniverse = new JCheckBox("All Universe");
				chckbxAllUniverse.addActionListener(arg0 -> starmodeSectorSelectionPanel.setEnabled(!chckbxAllUniverse.isSelected()));
				GridBagConstraints gbc_chckbxAllUniverse = new GridBagConstraints();
				gbc_chckbxAllUniverse.insets = new Insets(0, 0, 5, 5);
				gbc_chckbxAllUniverse.gridx = 1;
				gbc_chckbxAllUniverse.gridy = 0;
				panel.add(chckbxAllUniverse, gbc_chckbxAllUniverse);
			}
			{
				rdbtnUsed = new JRadioButton("Edited Only");
				buttonGroup.add(rdbtnUsed);
				GridBagConstraints gbc_rdbtnUnused = new GridBagConstraints();
				gbc_rdbtnUnused.insets = new Insets(0, 0, 0, 5);
				gbc_rdbtnUnused.gridx = 0;
				gbc_rdbtnUnused.gridy = 1;
				panel.add(rdbtnUsed, gbc_rdbtnUnused);
				rdbtnUsed.setSelected(true);
			}
			{
				rdbtnUnused = new JRadioButton("Unedited Only");
				buttonGroup.add(rdbtnUnused);
				GridBagConstraints gbc_rdbtnUsed = new GridBagConstraints();
				gbc_rdbtnUsed.insets = new Insets(0, 0, 0, 5);
				gbc_rdbtnUsed.gridx = 1;
				gbc_rdbtnUsed.gridy = 1;
				panel.add(rdbtnUnused, gbc_rdbtnUsed);
			}
			{
				rdbtnAll = new JRadioButton("Edited & Unedited");
				buttonGroup.add(rdbtnAll);
				GridBagConstraints gbc_rdbtnAll = new GridBagConstraints();
				gbc_rdbtnAll.gridx = 2;
				gbc_rdbtnAll.gridy = 1;
				panel.add(rdbtnAll, gbc_rdbtnAll);
			}
		}

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Sector", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridwidth = 2;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 3;
		contentPanel.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		starmodeSectorSelectionPanel = new StarmoteSectorSelectionPanel();
		starmodeSectorSelectionPanel.setMinimumSize(new Dimension(400, 50));
		starmodeSectorSelectionPanel.setPreferredSize(new Dimension(400, 50));
		GridBagConstraints gbc_starmodeSectorSelectionPanel = new GridBagConstraints();
		gbc_starmodeSectorSelectionPanel.weightx = 1.0;
		gbc_starmodeSectorSelectionPanel.anchor = GridBagConstraints.NORTHWEST;
		gbc_starmodeSectorSelectionPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_starmodeSectorSelectionPanel.gridwidth = 2;
		gbc_starmodeSectorSelectionPanel.insets = new Insets(0, 0, 0, 5);
		gbc_starmodeSectorSelectionPanel.gridx = 0;
		gbc_starmodeSectorSelectionPanel.gridy = 0;
		panel.add(starmodeSectorSelectionPanel, gbc_starmodeSectorSelectionPanel);
		GridBagLayout gbl_starmodeSectorSelectionPanel = new GridBagLayout();
		gbl_starmodeSectorSelectionPanel.columnWidths = new int[]{0};
		gbl_starmodeSectorSelectionPanel.rowHeights = new int[]{0};
		gbl_starmodeSectorSelectionPanel.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_starmodeSectorSelectionPanel.rowWeights = new double[]{Double.MIN_VALUE};
		starmodeSectorSelectionPanel.setLayout(gbl_starmodeSectorSelectionPanel);

		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(e -> {
					boolean shipOnly = chckbxShipsOnly.isSelected();
					String mode = rdbtnAll.isSelected() ? "all" : (rdbtnUsed.isSelected() ? "used" : "unused");
					String name = textField.getText().trim();
					Vector3i coord = starmodeSectorSelectionPanel.getCoord();
					if (chckbxAllUniverse.isSelected()) {
						state.getController().sendAdminCommand(AdminCommands.DESPAWN_ALL, name, mode, shipOnly);
					} else {
						state.getController().sendAdminCommand(AdminCommands.DESPAWN_SECTOR, name, mode, shipOnly, coord.x, coord.y, coord.z);
					}
					dispose();
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(e -> dispose());
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

}
