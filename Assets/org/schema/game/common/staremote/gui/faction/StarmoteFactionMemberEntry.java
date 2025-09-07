package org.schema.game.common.staremote.gui.faction;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionPermission;
import org.schema.game.server.data.admin.AdminCommands;

public class StarmoteFactionMemberEntry implements Comparable<StarmoteFactionMemberEntry> {

	private final FactionPermission p;
	private final GameClientState state;
	private Faction faction;

	public StarmoteFactionMemberEntry(FactionPermission p, Faction faction, GameClientState state, StarmoteFactionTableModel model) {
		this.p = p;
		this.state = state;
		this.faction = faction;
	}

	@Override
	public int compareTo(StarmoteFactionMemberEntry o) {
		return this.p.playerUID.toLowerCase(Locale.ENGLISH).compareTo(o.p.playerUID.toLowerCase(Locale.ENGLISH));
	}

	public Color getColor(int column) {
		//		switch(column){
		//		case 0: return null;
		//		case 1: return null;
		//		case 2: return null;
		//		case 3: return null;
		//		case 4: return null;
		//		case 5: return null;
		//		case 6: return null;
		//		case 7: return p.enemyUsable() ? Color.GREEN : Color.RED;
		//		case 8: return p.faction() ? Color.GREEN : Color.RED;
		//		case 9: return p.homeOnly() ? Color.GREEN : Color.RED;
		//		case 10: return p.others() ? Color.GREEN : Color.RED;
		//		case 11: return null;
		//		}
		return null;
	}

	public Component getComponent(int column, final JTable table) {
		switch (column) {

			case 0:
				return new JLabel(p.playerUID);
			case 1:
				final JComboBox box = new JComboBox();
				for (int i = 0; i < faction.getRoles().getRoles().length; i++) {
					box.addItem(faction.getRoles().getRoles()[i].name);
				}
				box.setSelectedItem(faction.getRoles().getRoles()[p.role].name);

				box.addActionListener(arg0 -> {

					state.getController().sendAdminCommand(AdminCommands.FACTION_MOD_MEMBER, p.playerUID, box.getSelectedIndex() + 1);
					System.err.println("MODIFYING TO " + box.getSelectedIndex());
				});

				return box;
			case 2:
				JButton b = new JButton("remove");
				b.setPreferredSize(new Dimension(40, 20));
				b.setHorizontalTextPosition(SwingConstants.LEFT);
				b.addActionListener(e -> state.getController().sendAdminCommand(AdminCommands.FACTION_DEL_MEMBER, p.playerUID));

				return b;
		}
		return new JLabel("-");
	}

	//	@Override
	//	public int compare(StarmoteFactionEntry o1, StarmoteFactionEntry o2) {
	//		return o1.p.catUID.compareTo(o2.p.catUID);
	//	}

	public boolean update() {
				return false;
	}

}
