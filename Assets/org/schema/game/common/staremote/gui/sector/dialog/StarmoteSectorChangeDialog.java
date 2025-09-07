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

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.staremote.gui.StarmoteFrame;
import org.schema.game.common.staremote.gui.sector.StarmoteSectorSelectionPanel;
import org.schema.game.server.data.admin.AdminCommands;

public class StarmoteSectorChangeDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private final JPanel contentPanel = new JPanel();
	private JTextField textField;
	private StarmoteSectorSelectionPanel starmodeSectorSelectionPanel;

	/**
	 * Create the dialog.
	 */
	public StarmoteSectorChangeDialog(final GameClientState state) {
		super(StarmoteFrame.self, true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle("Change Sector");
		setBounds(100, 100, 421, 185);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0, 0};
		gbl_contentPanel.rowHeights = new int[]{0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblEnterPlayerName = new JLabel("Enter Player Name");
			GridBagConstraints gbc_lblEnterPlayerName = new GridBagConstraints();
			gbc_lblEnterPlayerName.insets = new Insets(0, 0, 5, 5);
			gbc_lblEnterPlayerName.anchor = GridBagConstraints.EAST;
			gbc_lblEnterPlayerName.gridx = 0;
			gbc_lblEnterPlayerName.gridy = 0;
			contentPanel.add(lblEnterPlayerName, gbc_lblEnterPlayerName);
		}
		{
			textField = new JTextField();
			GridBagConstraints gbc_textField = new GridBagConstraints();
			gbc_textField.insets = new Insets(0, 0, 5, 0);
			gbc_textField.fill = GridBagConstraints.HORIZONTAL;
			gbc_textField.gridx = 1;
			gbc_textField.gridy = 0;
			contentPanel.add(textField, gbc_textField);
			textField.setColumns(10);
		}

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Sector", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridwidth = 2;
		gbc_panel.insets = new Insets(0, 0, 0, 5);
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
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
					Vector3i coord = starmodeSectorSelectionPanel.getCoord();
					state.getController().sendAdminCommand(AdminCommands.CHANGE_SECTOR_FOR, textField.getText().trim(), coord.x, coord.y, coord.z);
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
