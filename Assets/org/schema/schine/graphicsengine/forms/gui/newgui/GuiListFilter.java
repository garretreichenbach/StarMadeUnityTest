package org.schema.schine.graphicsengine.forms.gui.newgui;

public interface GuiListFilter<E, T> {
	public boolean isOk(E input, T listElement);

	public E getFilter();
}
