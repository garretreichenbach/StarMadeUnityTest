package org.schema.game.common.staremote.gui.player;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import org.schema.game.common.data.player.PlayerState;

public class StarmotePlayerListCellRenderer extends JLabel implements ListCellRenderer {

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
		if (value instanceof PlayerState) {
			PlayerState player = (PlayerState) value;

			this.setText(player.getName());
		} else {
			this.setText("StarmoteSynchException");
		}

		this.setOpaque(true);

		if (isSelected) {
			this.setForeground(UIManager.getColor("List.selectionForeground"));
			this.setBackground(UIManager.getColor("List.selectionBackground"));
		} else {
			//		      this.setForeground(person.getSchriftfarbe());
			this.setBackground(UIManager.getColor("List.background"));
			this.setForeground(UIManager.getColor("List.foreground"));
		}

		return this;
	}

}
