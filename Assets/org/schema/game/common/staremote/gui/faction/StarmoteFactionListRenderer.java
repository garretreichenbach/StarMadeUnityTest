package org.schema.game.common.staremote.gui.faction;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import org.schema.game.common.data.player.faction.Faction;

public class StarmoteFactionListRenderer extends JLabel implements ListCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
	                                              int index, boolean isSelected, boolean cellHasFocus) {

		Faction faction = (Faction) value;
		assert (faction != null);
		this.setText(faction.getName());

		this.setOpaque(true);

		if (isSelected) {
			this.setForeground(UIManager.getColor("List.selectionForeground"));
			this.setBackground(UIManager.getColor("List.selectionBackground"));
		} else {
			//		      this.setForeground(person.getSchriftfarbe());
			this.setBackground(UIManager.getColor("List.background"));
		}

		return this;
	}

}
