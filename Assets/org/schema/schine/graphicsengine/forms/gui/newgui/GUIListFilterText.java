package org.schema.schine.graphicsengine.forms.gui.newgui;

public abstract class GUIListFilterText<E> implements GuiListFilter<String, E> {
	protected String string;

	/* (non-Javadoc)
	 * @see org.schema.schine.graphicsengine.forms.gui.newgui.GuiListFilter#getFilter()
	 */
	@Override
	public String getFilter() {
		return string;
	}

	public void setFilter(String string) {
		this.string = string;
	}

}
