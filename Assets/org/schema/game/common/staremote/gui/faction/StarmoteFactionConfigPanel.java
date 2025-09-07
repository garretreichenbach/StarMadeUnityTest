package org.schema.game.common.staremote.gui.faction;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.staremote.gui.faction.edit.StarmoteFactionAddMemberDialog;
import org.schema.game.common.staremote.gui.faction.edit.StarmoteFactionRolesEditDialog;
import org.schema.game.server.data.admin.AdminCommands;

public class StarmoteFactionConfigPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private JTextField textField;

	public StarmoteFactionConfigPanel(final GameClientState state, final Faction faction) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		textField = new JTextField();
		textField.setText(faction.getName());
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 5, 0);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 0;
		gbc_textField.gridy = 0;
		add(textField, gbc_textField);
		textField.setColumns(10);

		final JTextArea textArea = new JTextArea();
		textArea.setText(faction.getDescription());
		GridBagConstraints gbc_textArea = new GridBagConstraints();
		gbc_textArea.insets = new Insets(0, 0, 5, 0);
		gbc_textArea.fill = GridBagConstraints.BOTH;
		gbc_textArea.gridx = 0;
		gbc_textArea.gridy = 1;
		add(textArea, gbc_textArea);

		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel.anchor = GridBagConstraints.NORTH;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 2;
		add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JButton btnApplyChanges = new JButton("Apply Name/Description");
		btnApplyChanges.addActionListener(e -> state.getController().sendAdminCommand(AdminCommands.FACTION_EDIT, faction.getIdFaction(), textField.getText(), textArea.getText()));
		GridBagConstraints gbc_btnApplyChanges = new GridBagConstraints();
		gbc_btnApplyChanges.insets = new Insets(0, 0, 0, 5);
		gbc_btnApplyChanges.gridx = 0;
		gbc_btnApplyChanges.gridy = 0;
		panel.add(btnApplyChanges, gbc_btnApplyChanges);

		JButton btnAd = new JButton("Add Member");
		btnAd.addActionListener(e -> (new StarmoteFactionAddMemberDialog(state, faction)).setVisible(true));
		GridBagConstraints gbc_btnAd = new GridBagConstraints();
		gbc_btnAd.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnAd.insets = new Insets(0, 0, 0, 5);
		gbc_btnAd.gridx = 2;
		gbc_btnAd.gridy = 0;
		panel.add(btnAd, gbc_btnAd);

		JButton btnEditRoles = new JButton("Edit Roles");
		btnEditRoles.addActionListener(e -> {
			StarmoteFactionRolesEditDialog d = new StarmoteFactionRolesEditDialog(state, faction);
			d.setVisible(true);
		});
		GridBagConstraints gbc_btnEditRoles = new GridBagConstraints();
		gbc_btnEditRoles.insets = new Insets(0, 0, 0, 5);
		gbc_btnEditRoles.gridx = 3;
		gbc_btnEditRoles.gridy = 0;
		panel.add(btnEditRoles, gbc_btnEditRoles);

		JButton btnDeleteFaction = new JButton("Delete Faction");
		btnDeleteFaction.addActionListener(e -> state.getController().sendAdminCommand(AdminCommands.FACTION_DELETE, faction.getIdFaction()));
		GridBagConstraints gbc_btnDeleteFaction = new GridBagConstraints();
		gbc_btnDeleteFaction.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnDeleteFaction.gridx = 5;
		gbc_btnDeleteFaction.gridy = 0;
		panel.add(btnDeleteFaction, gbc_btnDeleteFaction);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.weighty = 1.0;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 3;
		add(scrollPane, gbc_scrollPane);

		StarmoteFactionRelationPanel starmoteFactionRelationPanel = new StarmoteFactionRelationPanel(state, faction);
		scrollPane.setViewportView(starmoteFactionRelationPanel);
	}

}
