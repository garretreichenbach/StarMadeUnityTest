package org.schema.schine.graphicsengine.forms.gui.newgui;

import java.util.Comparator;
import java.util.List;

public interface GuiListSorter<E> {

	public Comparator<E> getSorter();

	public void sort(List<GUITileParam<E>> tiles);
}
