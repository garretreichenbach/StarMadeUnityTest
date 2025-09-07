package org.schema.game.common.facedit;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.schema.game.common.data.element.ElementInformation;

public class ElementTreeRenderer extends DefaultTreeCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	

	public ElementTreeRenderer() {
	}

	@Override
	public Component getTreeCellRendererComponent(
			JTree tree,
			Object value,
			boolean sel,
			boolean expanded,
			boolean leaf,
			int row,
			boolean hasFocus) {

		super.getTreeCellRendererComponent(
				tree, value, sel,
				expanded, leaf, row,
				hasFocus);
		if (leaf && isInfo(value)) {
			ElementInformation info = ((ElementInformation) ((DefaultMutableTreeNode) value).getUserObject());
			setIcon(EditorTextureManager.getImage(info.getTextureId(0)));
			setToolTipText("This book is in the Tutorial series.");
		} else {
			setToolTipText(null); //no tool tip
		}

		return this;
	}

	protected boolean isInfo(Object value) {
		DefaultMutableTreeNode node =
				(DefaultMutableTreeNode) value;
		return node.getUserObject() instanceof ElementInformation;
	}

}
