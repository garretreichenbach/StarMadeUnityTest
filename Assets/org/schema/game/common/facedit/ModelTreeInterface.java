package org.schema.game.common.facedit;

import javax.swing.event.TreeSelectionListener;

import org.schema.game.common.facedit.model.Model;

public interface ModelTreeInterface extends TreeSelectionListener {


	public boolean hasPopupMenu();


	public void removeEntry(Model model);


	public void addEntry(Model info);


	public void refresh();

}
