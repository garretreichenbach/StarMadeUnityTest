package org.schema.game.common.facedit;

import javax.swing.event.TreeSelectionListener;

import org.schema.game.common.data.element.ElementInformation;

public interface ElementTreeInterface extends TreeSelectionListener {

	public void removeEntry(ElementInformation info);

	public boolean hasPopupMenu();

	public void reinitializeElements();

}
