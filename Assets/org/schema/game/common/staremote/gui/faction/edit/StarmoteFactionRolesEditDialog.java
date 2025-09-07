package org.schema.game.common.staremote.gui.faction.edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionPermission;
import org.schema.game.common.data.player.faction.FactionRoles;
import org.schema.game.common.staremote.gui.StarmoteFrame;

public class StarmoteFactionRolesEditDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private final JPanel contentPanel = new JPanel();
	private Faction faction;
	private GameClientState gState;
	private FactionRoles roles;
	private JTextField[] textField;

	/**
	 * Create the dialog.
	 */
	public StarmoteFactionRolesEditDialog(GameClientState state, Faction faction) {
		super(StarmoteFrame.self, true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		roles = new FactionRoles();
		roles.factionId = faction.getIdFaction();

		textField = new JTextField[FactionRoles.ROLE_COUNT];

		this.faction = faction;
		this.gState = state;
		setBounds(100, 100, 706, 262);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0};
		gbl_contentPanel.rowHeights = new int[]{0, 0};
		gbl_contentPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JPanel panel = new JPanel();
			GridBagConstraints gbc_panel = new GridBagConstraints();
			gbc_panel.anchor = GridBagConstraints.NORTHWEST;
			gbc_panel.fill = GridBagConstraints.HORIZONTAL;
			gbc_panel.gridx = 0;
			gbc_panel.gridy = 0;
			contentPanel.add(panel, gbc_panel);
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};

			gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};

			gbl_panel.rowHeights = new int[FactionRoles.ROLE_COUNT + 1];
			gbl_panel.rowWeights = new double[FactionRoles.ROLE_COUNT + 1];
			gbl_panel.rowWeights[FactionRoles.ROLE_COUNT] = Double.MIN_VALUE;

			panel.setLayout(gbl_panel);
			{
				JLabel lblRole_1 = new JLabel("Role");
				GridBagConstraints gbc_lblRole_1 = new GridBagConstraints();
				gbc_lblRole_1.insets = new Insets(5, 5, 5, 5);
				gbc_lblRole_1.gridx = 0;
				gbc_lblRole_1.gridy = 0;
				panel.add(lblRole_1, gbc_lblRole_1);
			}
			{
				JLabel lblPermissionEdit = new JLabel("Permission Edit");
				lblPermissionEdit.setHorizontalAlignment(SwingConstants.LEFT);
				lblPermissionEdit.setHorizontalTextPosition(SwingConstants.LEFT);
				lblPermissionEdit.setBackground(Color.LIGHT_GRAY);
				GridBagConstraints gbc_lblPermissionEdit = new GridBagConstraints();
				gbc_lblPermissionEdit.anchor = GridBagConstraints.WEST;
				gbc_lblPermissionEdit.insets = new Insets(0, 5, 5, 5);
				gbc_lblPermissionEdit.gridx = 2;
				gbc_lblPermissionEdit.gridy = 0;
				panel.add(lblPermissionEdit, gbc_lblPermissionEdit);
			}
			{
				JLabel lblKickPermission = new JLabel("Kick Permission");
				lblKickPermission.setBackground(Color.LIGHT_GRAY);
				GridBagConstraints gbc_lblKickPermission = new GridBagConstraints();
				gbc_lblKickPermission.anchor = GridBagConstraints.WEST;
				gbc_lblKickPermission.insets = new Insets(0, 5, 5, 5);
				gbc_lblKickPermission.gridx = 3;
				gbc_lblKickPermission.gridy = 0;
				panel.add(lblKickPermission, gbc_lblKickPermission);
			}
			{
				JLabel lblInvitePermission = new JLabel("Invite Permission");
				lblInvitePermission.setBackground(Color.LIGHT_GRAY);
				GridBagConstraints gbc_lblInvitePermission = new GridBagConstraints();
				gbc_lblInvitePermission.anchor = GridBagConstraints.WEST;
				gbc_lblInvitePermission.insets = new Insets(0, 5, 5, 5);
				gbc_lblInvitePermission.gridx = 4;
				gbc_lblInvitePermission.gridy = 0;
				panel.add(lblInvitePermission, gbc_lblInvitePermission);
			}
			{
				JLabel lblFactionEditPermission = new JLabel("Edit Permission");
				lblFactionEditPermission.setBackground(Color.LIGHT_GRAY);
				GridBagConstraints gbc_lblFactionEditPermission = new GridBagConstraints();
				gbc_lblFactionEditPermission.anchor = GridBagConstraints.WEST;
				gbc_lblFactionEditPermission.insets = new Insets(0, 5, 5, 0);
				gbc_lblFactionEditPermission.gridx = 5;
				gbc_lblFactionEditPermission.gridy = 0;
				panel.add(lblFactionEditPermission, gbc_lblFactionEditPermission);
			}

			for (int i = 0; i < FactionRoles.ROLE_COUNT; i++) {
				addBox(i, panel);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(e -> {
					apply();

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

	public void addBox(final int column, JPanel panel) {
		{
			JLabel lblRole = new JLabel("#" + (column + 1) + ":");
			GridBagConstraints gbc_lblRole = new GridBagConstraints();
			gbc_lblRole.insets = new Insets(0, 0, 0, 5);
			gbc_lblRole.anchor = GridBagConstraints.EAST;
			gbc_lblRole.gridx = 0;
			gbc_lblRole.gridy = 1 + column;
			panel.add(lblRole, gbc_lblRole);
		}

		textField[column] = new JTextField();
		textField[column].setPreferredSize(new Dimension(90, 20));
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.weightx = 1.0;
		gbc_textField.insets = new Insets(0, 0, 0, 5);
		gbc_textField.anchor = GridBagConstraints.WEST;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 1 + column;
		panel.add(textField[column], gbc_textField);
		textField[column].setColumns(20);

		textField[column].setText(faction.getRoles().getRoles()[column].name);

		final JCheckBox chckbxPermissionEdit = new JCheckBox("");
		chckbxPermissionEdit.setBackground(Color.LIGHT_GRAY);
		chckbxPermissionEdit.setActionCommand("");
		GridBagConstraints gbc_chckbxPermissionEdit = new GridBagConstraints();
		gbc_chckbxPermissionEdit.fill = GridBagConstraints.BOTH;
		gbc_chckbxPermissionEdit.anchor = GridBagConstraints.WEST;
		gbc_chckbxPermissionEdit.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxPermissionEdit.gridx = 2;
		gbc_chckbxPermissionEdit.gridy = 1 + column;
		panel.add(chckbxPermissionEdit, gbc_chckbxPermissionEdit);
		chckbxPermissionEdit.setSelected(faction.getRoles().hasPermissionEditPermission(column));

		final JCheckBox chckbxKick = new JCheckBox("");
		chckbxKick.setBackground(SystemColor.controlHighlight);
		GridBagConstraints gbc_chckbxKick = new GridBagConstraints();
		gbc_chckbxKick.fill = GridBagConstraints.BOTH;
		gbc_chckbxKick.anchor = GridBagConstraints.WEST;
		gbc_chckbxKick.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxKick.gridx = 3;
		gbc_chckbxKick.gridy = 1 + column;
		panel.add(chckbxKick, gbc_chckbxKick);
		chckbxKick.setSelected(faction.getRoles().hasKickPermission(column));

		final JCheckBox chckbxInvite = new JCheckBox("");
		chckbxInvite.setBackground(Color.LIGHT_GRAY);
		GridBagConstraints gbc_chckbxInvite = new GridBagConstraints();
		gbc_chckbxInvite.fill = GridBagConstraints.BOTH;
		gbc_chckbxInvite.anchor = GridBagConstraints.WEST;
		gbc_chckbxInvite.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxInvite.gridx = 4;
		gbc_chckbxInvite.gridy = 1 + column;
		panel.add(chckbxInvite, gbc_chckbxInvite);
		chckbxInvite.setSelected(faction.getRoles().hasInvitePermission(column));

		final JCheckBox chckbxEdit = new JCheckBox("");
		chckbxEdit.setBackground(SystemColor.controlHighlight);
		GridBagConstraints gbc_chckbxEdit = new GridBagConstraints();
		gbc_chckbxEdit.fill = GridBagConstraints.BOTH;
		gbc_chckbxEdit.anchor = GridBagConstraints.WEST;
		gbc_chckbxEdit.gridx = 5;
		gbc_chckbxEdit.gridy = 1 + column;
		panel.add(chckbxEdit, gbc_chckbxEdit);
		chckbxEdit.setSelected(faction.getRoles().hasRelationshipPermission(column));

		ActionListener l = arg0 -> {

			if (chckbxEdit.isSelected()) {
				roles.getRoles()[column].role = roles.getRoles()[column].role | FactionPermission.PermType.RELATIONSHIPS_EDIT.value;
			}
			if (chckbxInvite.isSelected()) {
				roles.getRoles()[column].role = roles.getRoles()[column].role | FactionPermission.PermType.INVITE_PERMISSION.value;
			}
			if (chckbxKick.isSelected()) {
				roles.getRoles()[column].role = roles.getRoles()[column].role | FactionPermission.PermType.KICK_PERMISSION.value;
			}
			if (chckbxPermissionEdit.isSelected()) {
				roles.getRoles()[column].role = roles.getRoles()[column].role | FactionPermission.PermType.FACTION_EDIT_PERMISSION.value;
			}

		};
		chckbxEdit.addActionListener(l);
		chckbxInvite.addActionListener(l);
		chckbxKick.addActionListener(l);
		chckbxPermissionEdit.addActionListener(l);
	}

	private void apply() {

		for (int i = 0; i < FactionRoles.ROLE_COUNT; i++) {
			roles.getRoles()[i].name = textField[i].getText().trim();
		}
		gState.getFactionManager().sendFactionRoles(roles);
	}
}
