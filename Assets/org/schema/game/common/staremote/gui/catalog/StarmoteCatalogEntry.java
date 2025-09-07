package org.schema.game.common.staremote.gui.catalog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Locale;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.staremote.gui.StarmoteFrame;
import org.schema.game.common.staremote.gui.catalog.edit.StarmoteCatalogEditDialog;

public class StarmoteCatalogEntry implements Comparable<StarmoteCatalogEntry> {

	private final CatalogPermission p;
	private final GameClientState state;
	private StarmoteCatalogTableModel model;

	public StarmoteCatalogEntry(CatalogPermission p, GameClientState state, StarmoteCatalogTableModel model) {
		this.p = p;
		this.state = state;
		this.model = model;
	}

	@Override
	public int compareTo(StarmoteCatalogEntry o) {
				return this.p.getUid().toLowerCase(Locale.ENGLISH).compareTo(o.p.getUid().toLowerCase(Locale.ENGLISH));
	}

	public Color getColor(int column) {
		return switch(column) {
			case 0 -> null;
			case 1 -> null;
			case 2 -> null;
			case 3 -> null;
			case 4 -> null;
			case 5 -> null;
			case 6 -> null;
			case 7 -> p.enemyUsable() ? Color.GREEN : Color.RED;
			case 8 -> p.faction() ? Color.GREEN : Color.RED;
			case 9 -> p.homeOnly() ? Color.GREEN : Color.RED;
			case 10 -> p.others() ? Color.GREEN : Color.RED;
			case 11 -> null;
			default -> null;
		};
	}

	public Component getComponent(int column, final JTable table) {
		switch (column) {
			case 0:
				return new JLabel(p.getUid());
			case 1:
				return new JLabel(p.ownerUID);
			case 2:
				return new JLabel(String.valueOf(p.price));
			case 3:
				return new JLabel(String.valueOf(p.rating));
			case 4:
				return new JLabel(String.valueOf(p.description));
			case 5:
				return new JLabel((new Date(p.date)).toString());
			case 6:
				return new JLabel(String.valueOf(p.timesSpawned));
			case 7:
				return getPermissionBox(CatalogPermission.P_ENEMY_USABLE);
			case 8:
				return getPermissionBox(CatalogPermission.P_BUY_FACTION);
			case 9:
				return getPermissionBox(CatalogPermission.P_BUY_HOME_ONLY);
			case 10:
				return getPermissionBox(CatalogPermission.P_BUY_OTHERS);
			case 11:
				JButton b = new JButton("Edit");
				b.setPreferredSize(new Dimension(40, 20));
				b.setHorizontalTextPosition(SwingConstants.LEFT);
				b.addActionListener(e -> {

					StarmoteCatalogEditDialog d = new StarmoteCatalogEditDialog(StarmoteFrame.self, p, state, model, table);
					d.setVisible(true);

				});

				return b;
		}
		return new JLabel("-");
	}

	private JCheckBox getPermissionBox(final int permission) {
		final JCheckBox l = new JCheckBox();
		l.setSelected((p.permission & permission) == permission);

		ActionListener actionListener = actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent
					.getSource();
			boolean selected = abstractButton.getModel().isSelected();
			CatalogPermission cp = new CatalogPermission(p);
			cp.setPermission(selected, permission);
			System.err.println("SET TO " + selected);
			state.getCatalogManager().clientRequestCatalogEdit(cp);
		};

		l.addActionListener(actionListener);
		return l;
	}

	//	@Override
	//	public int compare(StarmoteCatalogEntry o1, StarmoteCatalogEntry o2) {
	//		return o1.p.catUID.compareTo(o2.p.catUID);
	//	}

	public boolean update() {
				return false;
	}

}
