package org.schema.game.common.staremote.gui.entity;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import org.schema.schine.network.objects.Sendable;

public class StarmoteEntityListCellRenderer extends JLabel implements ListCellRenderer {

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

		Sendable player = (Sendable) value;

		this.setText(player != null ? player.toString() : "NULL");

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
