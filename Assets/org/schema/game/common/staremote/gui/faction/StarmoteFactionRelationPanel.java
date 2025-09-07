package org.schema.game.common.staremote.gui.faction;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionRelation.RType;
import org.schema.game.server.data.admin.AdminCommands;

public class StarmoteFactionRelationPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 *
	 */
	
	private Faction faction;
	private GameClientState state;

	public StarmoteFactionRelationPanel(GameClientState state, Faction faction) {
		this.state = state;
		this.faction = faction;

		Collection<Faction> facs = state.getFactionManager().getFactionCollection();

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0};
		gridBagLayout.rowHeights = new int[facs.size()];
		gridBagLayout.columnWeights = new double[]{0};
		gridBagLayout.rowWeights = new double[facs.size()];
		Arrays.fill(gridBagLayout.columnWeights, 1d);
		Arrays.fill(gridBagLayout.rowWeights, 1d);
		gridBagLayout.columnWeights[gridBagLayout.columnWeights.length - 1] = Double.MIN_VALUE;
		gridBagLayout.rowWeights[gridBagLayout.rowWeights.length - 1] = Double.MIN_VALUE;

		setLayout(gridBagLayout);
		int i = 0;
		for (Faction f : facs) {
			if (f != faction) {
				addRow(i, f);
				i++;
			}
		}

	}

	public JPanel addRow(int row, final Faction f) {
		JPanel panel = new JPanel();
		//		panel.setPreferredSize(new Dimension(500, 24));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel.weightx = 1.0;
		gbc_panel.anchor = GridBagConstraints.NORTHWEST;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = row;
		add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JLabel lblFaction = new JLabel(f.getName());
		lblFaction.setPreferredSize(new Dimension(200, 14));
		lblFaction.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblFaction = new GridBagConstraints();
		gbc_lblFaction.anchor = GridBagConstraints.WEST;
		gbc_lblFaction.weightx = 1.0;
		gbc_lblFaction.insets = new Insets(0, 5, 0, 5);
		gbc_lblFaction.gridx = 0;
		gbc_lblFaction.gridy = 0;
		panel.add(lblFaction, gbc_lblFaction);

		ButtonGroup g = new ButtonGroup();

		final JRadioButton rdbtnEnemy = new JRadioButton("Enemy");
		GridBagConstraints gbc_rdbtnEnemy = new GridBagConstraints();
		gbc_rdbtnEnemy.weightx = 1.0;
		gbc_rdbtnEnemy.anchor = GridBagConstraints.EAST;
		gbc_rdbtnEnemy.insets = new Insets(0, 0, 0, 5);
		gbc_rdbtnEnemy.gridx = 1;
		gbc_rdbtnEnemy.gridy = 0;

		rdbtnEnemy.setPreferredSize(new Dimension(66, 20));
		panel.add(rdbtnEnemy, gbc_rdbtnEnemy);
		g.add(rdbtnEnemy);

		final JRadioButton rdbtnNeutral = new JRadioButton("Neutral");
		GridBagConstraints gbc_rdbtnNeutral = new GridBagConstraints();
		gbc_rdbtnNeutral.weightx = 1.0;
		gbc_rdbtnNeutral.anchor = GridBagConstraints.EAST;
		gbc_rdbtnNeutral.insets = new Insets(0, 0, 0, 5);
		gbc_rdbtnNeutral.gridx = 2;
		gbc_rdbtnNeutral.gridy = 0;
		rdbtnNeutral.setPreferredSize(new Dimension(66, 20));
		panel.add(rdbtnNeutral, gbc_rdbtnNeutral);
		g.add(rdbtnNeutral);

		final JRadioButton rdbtnAlliance = new JRadioButton("Alliance");
		GridBagConstraints gbc_rdbtnAlliance = new GridBagConstraints();
		gbc_rdbtnAlliance.weightx = 1.0;
		gbc_rdbtnAlliance.anchor = GridBagConstraints.EAST;
		gbc_rdbtnAlliance.gridx = 3;
		gbc_rdbtnAlliance.gridy = 0;
		rdbtnAlliance.setPreferredSize(new Dimension(73, 20));
		panel.add(rdbtnAlliance, gbc_rdbtnAlliance);
		g.add(rdbtnAlliance);
		RType relation = state.getFactionManager().getRelation(faction.getIdFaction(), f.getIdFaction());
		if (relation == RType.ENEMY) {
			rdbtnEnemy.setSelected(true);
		} else if (relation == RType.NEUTRAL) {
			rdbtnNeutral.setSelected(true);
		} else if (relation == RType.FRIEND) {
			rdbtnAlliance.setSelected(true);
		} else {
			throw new IllegalArgumentException();
		}

		ActionListener al = arg0 -> {
			if (rdbtnEnemy.isSelected()) {
				state.getController().sendAdminCommand(AdminCommands.FACTION_MOD_RELATION, faction.getIdFaction(), f.getIdFaction(), "enemy");
			} else if (rdbtnNeutral.isSelected()) {
				state.getController().sendAdminCommand(AdminCommands.FACTION_MOD_RELATION, faction.getIdFaction(), f.getIdFaction(), "neutral");
			} else if (rdbtnAlliance.isSelected()) {
				state.getController().sendAdminCommand(AdminCommands.FACTION_MOD_RELATION, faction.getIdFaction(), f.getIdFaction(), "ally");
			}
		};
		rdbtnEnemy.addActionListener(al);
		rdbtnNeutral.addActionListener(al);
		rdbtnAlliance.addActionListener(al);

		return panel;
	}
}
